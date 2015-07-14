package search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import misc.Logger;

import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import parsers.xml.XMLParser;
import query.QueryMatcherVisitor;
import services.Service;
import services.resources.Resource;
import services.resources.ResourceLoader;
import services.resources.ResourceSerializer;
import data.Article;
import data.ArticleList;

public abstract class SearchEngine {

	protected Logger logger;
	
	// NOTE: name must have unix-names format
	protected String name = "";
	
	protected String inputBasePath = "";
	protected String outputBasePath = "";
	protected String outputServicePrefix = "output";
	
	protected ResourceSerializer resourceSerializer;
	protected ResourceLoader resourceLoader;
	
	protected QueryMatcherVisitor queryMatcherVisitor;
	
	protected Integer loginDuration = 30*60*1000; // minutes
	protected Integer numberOfResultsPerPage;
	
	protected String queryText;
	protected ParseTree originalQueryTree; // needed for the filtering
	protected Integer searchIndex; // used in the saving
	protected Integer totalSearches; // needed for statistics
	protected Integer startYear = null; // optional
	protected Integer endYear = null; // optional
	protected Boolean fastOutput = false;

	protected Integer outputCount;
	protected ArticleList outputArticleList; // final articles from all pages
	
	public SearchEngine(String name) {
		this.logger = new Logger("SearchEngine");
		
		this.name = name;
		
		this.inputBasePath = "data/engines/" + this.name + "/";
		
		this.resourceSerializer = new ResourceSerializer();
		this.resourceLoader = new ResourceLoader();
		
		this.queryMatcherVisitor = new QueryMatcherVisitor();
	}

	public void execute() {
		this.outputBasePath = "data/output/" + this.name + "/" + this.searchIndex + "/";
		
		this.logger.log("\n"+this.name.toUpperCase()+": search started");
		
		this.login();
		// re-login every time the login duration expires
		final SearchEngine me = this;
		final Timer loginTimer = new Timer();
		loginTimer.schedule(new TimerTask() {
		    @Override
		    public void run() {
		    	me.login();
		    }
		}, this.loginDuration);
		
		if (this.fastOutput) this.fastSearch();
		else this.fullSearch();
		
		// stop timer before returning
		loginTimer.cancel(); // Terminates this timer, discarding any currently scheduled tasks.
		loginTimer.purge(); // Removes all cancelled tasks from this timer's task queue.
	}
	
	protected void login() {
		// default: no login needed
		return;
	}
	
	// Only extract the unfiltered number of results (count)
	protected void fastSearch() {
		Resource searchResult = this.extractSearchResult(1);
		Integer count = this.getCount(searchResult);
		
		this.setOutputCount(count);
	}
	
	// Search in all pages, filter and merge the results.
	protected void fullSearch() {
		List<Resource> outputResourceList = new ArrayList<Resource>();
		
		// get first page
		Resource firstOutputResource = this.searchPage(1);
		outputResourceList.add(firstOutputResource);
		
		// get remaining pages
		Integer count = this.getCount(firstOutputResource);
		Integer numberOfPages = this.calculateNumberOfPages(count, this.numberOfResultsPerPage);
		for (int i=2; i<=numberOfPages; i++) {
			Resource pageOutputResource = this.searchPage(i);
			outputResourceList.add(pageOutputResource);
		}
		
		// convert output resources from all pages into a single list of articles
		ArticleList allPagesArticleList = new ArticleList();
		for (Resource outputResource : outputResourceList) {
			ArticleList outputArticleList = this.createArticlesFromOutput(outputResource);
			allPagesArticleList.addAll(outputArticleList);
		}
		
		this.setOutputCount(count);
		this.setOutputArticleList(allPagesArticleList);
	}
	
	protected Resource searchPage(Integer pageNumber) {
		
		Resource searchResult = this.extractSearchResult(pageNumber);
		List<String> searchResultArticleIdList = this.getArticleIdsFromSearchResult(searchResult);
		
		// first filtering with the information we have right now
		List<String> validArticleIdList = this.filterArticleIds(searchResultArticleIdList, searchResult);
		
		// get needed valid article details (and related article ids)
		List<Resource> validArticleDetailsList = this.extractNeededArticleDetails(validArticleIdList);
		
		if (!validArticleDetailsList.isEmpty()) { // (the check avoids useless re-filtering if there aren't details)
			// filter again with the new information
			validArticleIdList = this.filterArticleIds(validArticleIdList, searchResult, validArticleDetailsList);

			// update valid article details
			for (int i=0; i<validArticleDetailsList.size(); i++) {
				Resource validArticleDetails = validArticleDetailsList.get(i);
				String validArticleDetailsId = this.getArticlePropertyFromDetails(validArticleDetails, "id");
				if (!validArticleIdList.contains(validArticleDetailsId)) {
					// not valid => remove
					validArticleDetailsList.remove(i);
					i--;
				}
			}
		}
		
		Resource outputResource = this.extractOutput(searchResult, validArticleDetailsList, validArticleIdList);

		// print some statistics on current progress
		Integer count = this.getCount(searchResult);
		Integer processed = this.calculateStartResult(pageNumber+1, this.numberOfResultsPerPage) - 1;
		if (processed > count) { // last page
			processed = count;
		}
		Integer percent = (count == 0) ? 100 : (int) Math.floor( ((float)processed / count) * 100 );
		this.logger.log("\n"+this.name.toUpperCase()+": search "+this.searchIndex+"/"+this.totalSearches+", "
						+ "processed "+processed+"/"+count+" ("+percent+"%)");
		
		return outputResource;
	}
	
	protected abstract Resource extractSearchResult(Integer pageNumber);
	
	protected List<Resource> extractNeededArticleDetails(List<String> articleIdList) {
		List<Resource> articleDetailsList = new ArrayList<Resource>();
		
		// default: get all article details
		for (String articleID : articleIdList) {
			Resource validArticleDetails = this.extractArticleDetails(articleID);
			articleDetailsList.add(validArticleDetails);
		}
		
		return articleDetailsList;
	}
	
	protected abstract Resource extractArticleDetails(String articleId);
	
	// Note: articleDetailList is optional, pass it as null to filter by only using the searchResult (if possible).
	// Note: whenever filtering an article is not possible since there are missing information, the article is kept.
	protected List<String> filterArticleIds(List<String> articleIdList, Resource searchResult, List<Resource> articleDetailList) {
		List<String> filteredArticleIdList = new ArrayList<String>();

		for (String articleId : articleIdList) {
			
			Element searchResultArticleEl = this.getArticleElementFromSearchResult(searchResult, articleId);
			
			// the articleDetail could either exist or not, try to find it
			Resource articleDetail = null;
			if (articleDetailList != null) {
				for (Resource currentArticleDetail : articleDetailList) {
					String currentArticleDetailId = this.getArticlePropertyFromDetails(currentArticleDetail, "id");
					if (currentArticleDetailId.equals(articleId)) {
						articleDetail = currentArticleDetail;
					}
				}
			}
			
			String title = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "title");
			if (title == null && articleDetail != null) {
				title = this.getArticlePropertyFromDetails(articleDetail, "title");
			}
			
			String abstractProp = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "abstract");
			if (abstractProp == null && articleDetail != null) {
				abstractProp = this.getArticlePropertyFromDetails(articleDetail, "abstract");
			}				
			
			String keywords = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "keywords");
			if (keywords == null && articleDetail != null) {
				keywords = this.getArticlePropertyFromDetails(articleDetail, "keywords");
			}
			
			String year = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "year");
			if (year == null && articleDetail != null) {
				year = this.getArticlePropertyFromDetails(articleDetail, "year");
			}
			
			// convert year to number (if possible)
			Integer yearNumber = null;
			if (year != null) {
				try {
					yearNumber = Integer.parseInt(year);
				} catch (NumberFormatException e) {
					// keep number null
					yearNumber = null;
				}
			}
			
			// check against the query only if we have all the required information
			// (e.g. skip if the keywords aren't in the search result service, but does the check anyway
			// if the details contains empty keywords for a specific article).
			// concatenate to consider all fields at once
			// (otherwise for example the terms in an AND expression will have to be matched ALL by one single field)
			String queryTarget = null;
			Boolean canBuildTarget = title != null && abstractProp != null && keywords != null;
			if (canBuildTarget) {
				queryTarget = title + " " + abstractProp + " " + keywords;
			}
			
			Boolean passes = (this.startYear == null || yearNumber == null || yearNumber >= this.startYear) &&
							 (this.endYear == null || yearNumber == null || yearNumber <= this.endYear) &&
							 (queryTarget == null || this.doMatchQuery(queryTarget));
			if (passes) {
				filteredArticleIdList.add(articleId);
			}
			
		}
		
		return filteredArticleIdList;
	}
	
	protected List<String> filterArticleIds(List<String> articleIdList, Resource searchResult) {
		return this.filterArticleIds(articleIdList, searchResult, null);
	}
	
	protected Resource extractOutput(Resource searchResult, List<Resource> articleDetailsList, List<String> validArticleIdList) {

		// retrieve page number
		String pageNumber = searchResult.getName().split("_")[1]; // name format is "searchResult_1"

		Resource outputResource;
		String resourceFilename = this.outputBasePath + "resources/" + this.outputServicePrefix + "_" + pageNumber + ".xml";
		
		try {
			outputResource = this.resourceLoader.load(new File(resourceFilename));
			this.logger.log("\nResumed: " + resourceFilename);
			return outputResource;
		} catch (SAXException | IOException e1) {
			// proceed
			this.logger.log("\nFetching new " + resourceFilename);
		}
		
		Service outputService = new Service();
		String serviceFilename = this.inputBasePath + "services/" + this.outputServicePrefix + ".xml";
		outputService.loadFromFile(new File(serviceFilename));
		
		// set the base engine scope
		outputService.getEngineBaseScope().put("validArticleIdList", validArticleIdList);
		
		// set any needed data
		outputService.addData("pageNumber", pageNumber);
		outputService.addData("searchResult", searchResult.getName());

		// add any needed resource
		outputService.getResourceList().add(searchResult);
		for (Resource articleDetails : articleDetailsList) {
			outputService.getResourceList().add(articleDetails);
		}
		
		outputResource = outputService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(outputResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return outputResource;
	}
	
	protected ArticleList createArticlesFromOutput(Resource outputResource) {
		ArticleList articleList = new ArticleList();
		
		Document outputContent = (Document) outputResource.getContent();
		
		try {
			List<Element> articleElList = XMLParser.select("articles/item", outputContent.getDocumentElement());
			for (Element articleEl : articleElList) {
				String currentId = XMLParser.select("id", articleEl).get(0).getTextContent().trim();
				String currentTitle = XMLParser.select("title", articleEl).get(0).getTextContent().trim();
				String currentAbstract = XMLParser.select("abstract", articleEl).get(0).getTextContent().trim();
				String currentKeywords = XMLParser.select("keywords", articleEl).get(0).getTextContent().trim();
				Integer currentYear = Integer.parseInt( XMLParser.select("year", articleEl).get(0).getTextContent().trim() );
				String currentAuthors = XMLParser.select("authors", articleEl).get(0).getTextContent().trim();
				String currentPublication = XMLParser.select("publication", articleEl).get(0).getTextContent().trim();
				
				Article article = new Article();
				article.setSource(this.name);
				article.setId(currentId);
				article.setTitle(currentTitle);
				article.setAbstract(currentAbstract);
				article.setKeywords(currentKeywords);
				article.setYear(currentYear);
				article.setAuthors(currentAuthors);
				article.setPublication(currentPublication);
				
				articleList.add(article);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articleList;
	}
	

	protected List<String> getArticlePropertiesFromSearchResult(Resource searchResult, String propertyName) {
		List<String> articlePropertyList = new ArrayList<String>();
		
		Document searchResultContent = (Document) searchResult.getContent();
		
		try {
			List<Element> articleList = XMLParser.select("articles/item", searchResultContent.getDocumentElement());
			for (Element articleEl : articleList) {
				String property = ( (String) XMLParser.getXpath().evaluate(propertyName, articleEl, XPathConstants.STRING) ).trim();
				articlePropertyList.add(property);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articlePropertyList;
	}
	
	// Note: returns null if the property doesn't exist
	protected String getArticlePropertyFromDetails(Resource articleDetails, String propertyName) {
		String property;
		
		Document articleDetailsContent = (Document) articleDetails.getContent();

		try {
			property = XMLParser.select("article/"+propertyName, articleDetailsContent.getDocumentElement()).get(0).getTextContent().trim();
		} catch (Exception e) {
			property = null;
		}
		
		return property;
	}
	
	// Note: returns null if the property doesn't exist
	protected String getArticlePropertyFromSearchResultArticleEl(Element searchResultArticleEl, String propertyName) {
		String property;
		
		try {
			property = XMLParser.select(propertyName, searchResultArticleEl).get(0).getTextContent().trim();
		} catch (Exception e) {
			property = null;
		}	
		
		return property;
	}
	
	protected List<String> getArticleIdsFromSearchResult(Resource searchResult) {
		return this.getArticlePropertiesFromSearchResult(searchResult, "id");
	}
	
	protected List<String> getArticleIdsFromDetails(List<Resource> articleDetailsList) {
		List<String> articleIdList = new ArrayList<String>();
		
		for (Resource articleDetails : articleDetailsList) {
			String id = this.getArticlePropertyFromDetails(articleDetails, "id");
			articleIdList.add(id);
		}
		
		return articleIdList;
	}
	
	protected Boolean doMatchQuery(String target) {
		if (target == null || target.isEmpty()) {
			// nothing can be said, in doubt keep it
			return true;
		}

		this.queryMatcherVisitor.setTarget(target);
		Boolean passes = this.queryMatcherVisitor.visit(this.originalQueryTree);
		return passes;
	}
	
	protected Element getArticleElementFromSearchResult(Resource searchResult, String articleId) {
		Document searchResultContent = (Document) searchResult.getContent();
		
		try {
			List<Element> articleList = XMLParser.select("articles/item", searchResultContent.getDocumentElement());
			for (Element articleEl : articleList) {
				String currentId = XMLParser.select("id", articleEl).get(0).getTextContent().trim();
				if (currentId.equals(articleId)) {
					return articleEl;
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected Integer getCount(Resource output) {
		Integer count;
		
		Document searchResultContent = (Document) output.getContent();
		
		try {
			XPathExpression expr = XMLParser.getXpath().compile("/response/meta/count");
			Double countDouble = (Double) expr.evaluate(searchResultContent, XPathConstants.NUMBER); // (returns NaN if count is not a number (e.g. if it's empty))
			count = countDouble.intValue(); // (an eventual NaN value is converted to 0)
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			count = null;
		}
		
		return count;
	}
	
	protected Integer calculateNumberOfPages(Integer count, Integer numberOfResultsPerPage) {
		return (int) Math.ceil( ((float)count) / numberOfResultsPerPage);
	}
	
	protected Integer calculateStartResult(Integer pageNumber, Integer numberOfResultsPerPage) {
		return ((pageNumber-1) * numberOfResultsPerPage) + 1;
	}
	
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getLoginDuration() {
		return this.loginDuration;
	}

	public void setLoginDuration(Integer loginDuration) {
		this.loginDuration = loginDuration;
	}

	public Integer getNumberOfResultsPerPage() {
		return this.numberOfResultsPerPage;
	}

	public void setNumberOfResultsPerPage(Integer numberOfResultsPerPage) {
		this.numberOfResultsPerPage = numberOfResultsPerPage;
	}

	public String getQueryText() {
		return this.queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	public ParseTree getOriginalQueryTree() {
		return this.originalQueryTree;
	}

	public void setOriginalQueryTree(ParseTree originalQueryTree) {
		this.originalQueryTree = originalQueryTree;
	}

	public Integer getSearchIndex() {
		return this.searchIndex;
	}

	public void setSearchIndex(Integer searchIndex) {
		this.searchIndex = searchIndex;
	}

	public Integer getTotalSearches() {
		return this.totalSearches;
	}

	public void setTotalSearches(Integer totalSearches) {
		this.totalSearches = totalSearches;
	}

	public Integer getStartYear() {
		return this.startYear;
	}

	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}

	public Integer getEndYear() {
		return this.endYear;
	}

	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}

	public Boolean isFastOutput() {
		return this.fastOutput;
	}

	public void setFastOutput(Boolean fastOutput) {
		this.fastOutput = fastOutput;
	}

	public Integer getOutputCount() {
		return this.outputCount;
	}

	public void setOutputCount(Integer outputCount) {
		this.outputCount = outputCount;
	}	
	
	public ArticleList getOutputArticleList() {
		return this.outputArticleList;
	}

	public void setOutputArticleList(ArticleList outputArticleList) {
		this.outputArticleList = outputArticleList;
	}
	
}

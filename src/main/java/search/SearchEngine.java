package search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.DOMException;
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

	// NOTE: name must have unix-names format
	protected String name = "";
	
	protected String inputBasePath = "";
	protected String outputBasePath = "";
	
	protected ResourceSerializer resourceSerializer;
	protected ResourceLoader resourceLoader;
	
	protected Integer numberOfResultsPerPage;
	protected String queryText;
	protected ParseTree originalQueryTree; // needed for the filtering
	protected Integer searchIndex; // used in the saving
	
	public SearchEngine(String name) {
		
		this.name = name;
		
		this.inputBasePath = "data/engines/" + this.name + "/";
		
		this.resourceSerializer = new ResourceSerializer();
		this.resourceLoader = new ResourceLoader();
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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

	// TODO: add search input parameters
	// Returns a list of output resources
	public abstract ArticleList search();
	
	protected ArticleList searchAllPages() {
		List<Resource> outputResourceList = new ArrayList<Resource>();
		
		// get first page
		Resource firstOutputResource = this.searchPage(1);
		outputResourceList.add(firstOutputResource);
		
		// get remaining pages
		Integer count = this.getCount(firstOutputResource);
		Integer numberOfPages = this.calculateNumberOfPages(count, this.numberOfResultsPerPage);
		for (int i=1; i<numberOfPages; i++) {
			Resource pageOutputResource = this.searchPage(i);
			outputResourceList.add(pageOutputResource);
		}
		
		// convert output resources from all pages into a single list of articles
		ArticleList allPagesArticleList = new ArticleList();
		for (Resource outputResource : outputResourceList) {
			ArticleList outputArticleList = this.createArticlesFromOutput(outputResource);
			allPagesArticleList.addAll(outputArticleList);
		}
		
		return allPagesArticleList;
	}
	
	public Resource searchPage(Integer pageNumber) {
		Resource searchResult = this.searchFromDefault(pageNumber);
		//Resource searchResult = this.searchFromXML();
		List<String> articleIdList = this.getArticleIdsFromSearchResult(searchResult);
		
		// first filtering with the information we have right now
		List<String> validArticleIdList = this.filterArticleIdsBySearchResult(searchResult, articleIdList);
		
		// get all valid article details
		List<Resource> validArticleDetailsList = new ArrayList<Resource>();
		for (String validArticleId : validArticleIdList) {
			Resource validArticleDetails = this.getArticleDetailsFromDefault(validArticleId);
			validArticleDetailsList.add(validArticleDetails);
		}
		
		// TODO: filter again with the new information
		validArticleDetailsList = this.filterArticleDetails(validArticleDetailsList, searchResult);
		// update the valid ids
		validArticleIdList = this.getArticleIdsFromDetails(validArticleDetailsList);
		
		return this.output(searchResult, validArticleDetailsList, validArticleIdList);
	}
	
	public abstract Resource searchFromDefault(Integer pageNumber);
	
	public abstract List<String> filterArticleIdsBySearchResult(Resource searchResult, List<String> articleIdList);
	
	public abstract Resource getArticleDetailsFromDefault(String articleId);
	
	public List<Resource> filterArticleDetails(List<Resource> articleDetailList, Resource searchResult) {
		List<Resource> filteredArticleDetailList = new ArrayList<Resource>();

		for (Resource articleDetail : articleDetailList) {			
			String id = this.getArticlePropertyFromDetails(articleDetail, "id");
			Element searchResultArticleEl = this.getArticleElementFromSearchResult(searchResult, id);

			String title = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "title");
			if (title == null || title.isEmpty()) {
				title = this.getArticlePropertyFromDetails(articleDetail, "title");
			}
			
			String abstractProp = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "abstract");
			if (abstractProp == null || abstractProp.isEmpty()) {
				abstractProp = this.getArticlePropertyFromDetails(articleDetail, "abstract");
			}				
			
			String keywords = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "keywords");
			if (keywords == null || keywords.isEmpty()) {
				keywords = this.getArticlePropertyFromDetails(articleDetail, "keywords");
			}
			
			// concatenate to consider all fields at once
			// (otherwise for example the terms in an AND expression will have to be matched ALL by one single field)
			String target = title + " " + abstractProp + " " + keywords;
			
			if (this.doMatchQuery(target)) {
				filteredArticleDetailList.add(articleDetail);
			}

		}
		
		return filteredArticleDetailList;
	}
	
	public List<String> getArticlePropertiesFromSearchResult(Resource searchResult, String propertyName) {
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
	
	public String getArticlePropertyFromDetails(Resource articleDetails, String propertyName) {
		String property;
		
		Document articleDetailsContent = (Document) articleDetails.getContent();

		try {
			Element articleEl = XMLParser.select("article", articleDetailsContent.getDocumentElement()).get(0);
			property = ( (String) XMLParser.getXpath().evaluate(propertyName, articleEl, XPathConstants.STRING) ).trim();
		} catch (XPathExpressionException e) {		
			property = null;
		}
		
		return property;
	}
	
	public String getArticlePropertyFromSearchResultArticleEl(Element searchResultArticleEl, String propertyName) {
		String property;
		
		try {
			property = XMLParser.select(propertyName, searchResultArticleEl).get(0).getTextContent().trim();
		} catch (Exception e) {
			property = null;
		}	
		
		return property;
	}
	
	public List<String> getArticleIdsFromSearchResult(Resource searchResult) {
		return this.getArticlePropertiesFromSearchResult(searchResult, "id");
	}
	
	public List<String> getArticleIdsFromDetails(List<Resource> articleDetailsList) {
		List<String> articleIdList = new ArrayList<String>();
		
		for (Resource articleDetails : articleDetailsList) {
			String id = this.getArticlePropertyFromDetails(articleDetails, "id");
			articleIdList.add(id);
		}
		
		return articleIdList;
	}
	
	public Boolean doMatchQuery(String target) {
		if (target == null || target.isEmpty()) {
			// nothing can be said, in doubt keep it
			return true;
		}

		QueryMatcherVisitor visitor = new QueryMatcherVisitor();
		visitor.setTarget(target);
		Boolean passes = visitor.visit(this.originalQueryTree);
		return passes;
	}
	
	public Element getArticleElementFromSearchResult(Resource searchResult, String articleId) {
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
	
	public Resource output(Resource searchResult, List<Resource> articleDetailsList, List<String> validArticleIdList) {

		// retrieve page number
		String pageNumber = searchResult.getName().split("_")[1]; // name format is "searchResult_1"

		Resource outputResource;
		String resourceFilename = this.outputBasePath + "resources/output_" + pageNumber + ".xml";
		
		try {
			outputResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return outputResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
		}
		
		Service outputService = new Service();
		String serviceFilename = this.inputBasePath + "services/output.xml";
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
	
	public Integer getCount(Resource output) {
		Integer count;
		
		Document searchResultContent = (Document) output.getContent();
		
		try {
			XPathExpression expr = XMLParser.getXpath().compile("/response/meta/count");
			Double countDouble = (Double) expr.evaluate(searchResultContent, XPathConstants.NUMBER);
			count = countDouble.intValue();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			count = null;
		}
		
		return count;
	}
	
	public Integer calculateNumberOfPages(Integer count, Integer numberOfResultsPerPage) {
		return (int) Math.ceil( ((float)count) / numberOfResultsPerPage);
	}
	
	public Integer calculateStartResult(Integer pageNumber, Integer numberOfResultsPerPage) {
		return ((pageNumber-1) * numberOfResultsPerPage) + 1;
	}
	
	public ArticleList createArticlesFromOutput(Resource outputResource) {
		ArticleList articleList = new ArticleList();
		
		Document outputContent = (Document) outputResource.getContent();
		
		try {
			List<Element> articleElList = XMLParser.select("articles/item", outputContent.getDocumentElement());
			for (Element articleEl : articleElList) {
				String currentTitle = XMLParser.select("title", articleEl).get(0).getTextContent().trim();
				String currentAbstract = XMLParser.select("abstract", articleEl).get(0).getTextContent().trim();
				String currentKeywords = XMLParser.select("keywords", articleEl).get(0).getTextContent().trim();
				Integer currentYear = Integer.parseInt( XMLParser.select("year", articleEl).get(0).getTextContent().trim() );
				
				Article article = new Article();
				article.setSource(this.name);
				article.setTitle(currentTitle);
				article.setAbstract(currentAbstract);
				article.setKeywords(currentKeywords);
				article.setYear(currentYear);
				
				articleList.add(article);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articleList;
	}
	
}

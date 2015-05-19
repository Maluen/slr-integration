package engines.acm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import parsers.xml.XMLParser;
import query.QueryMatcherVisitor;
import services.Service;
import services.resources.Resource;
import engines.Engine;

public class ACMEngine extends Engine {

	public ACMEngine() {
		super("acm");
	}

	@Override
	public void search(ParseTree queryTree) {
		this.setQueryTree(queryTree);
		
		// TODO: convert generic search input into engine-specific search input
		String queryText = queryTree.getText();
		
		Resource homeResource = this.home();
		Map<String, String> userData = this.getUserData(homeResource);
		
		Resource searchResult  = this.searchFromHTML(queryText, userData);
		List<String> articleIdList = this.getArticleIdsFromSearchResult(searchResult);
		
		// first filtering with the information we have right now
		List<String> validArticleIdList = this.filterArticleIdsBySearchResult(searchResult, articleIdList);
		
		// DEBUG
		//List<Resource> articleDetailsList = new ArrayList<Resource>();
		//Resource articleDetails = this.getArticleDetails("2400267.2400302", userData);
		//articleDetailsList.add(articleDetails);
		
		// get all valid article details
		List<Resource> validArticleDetailsList = new ArrayList<Resource>();
		for (String validArticleId : validArticleIdList) {
			Resource validArticleDetails = this.getArticleDetailsFromHTML(validArticleId, userData);
			validArticleDetailsList.add(validArticleDetails);
		}
		
		// TODO: filter again with the new information
		validArticleDetailsList = this.filterArticleDetails(validArticleDetailsList, searchResult);
		// update the valid ids
		validArticleIdList = this.getArticleIdsFromDetails(validArticleDetailsList);
		
		this.output(searchResult, validArticleDetailsList, validArticleIdList);
	}
	
	/**
	 * @return user data that can be passed to the service
	 */
	public Resource home() {
		Resource homeResource;
		String resourceFilename = this.outputBasePath + "resources/home-html.xml";

		try {
			homeResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return homeResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
		}
		
		Service homeService = new Service();
		String serviceFilename = this.inputBasePath + "services/home-html.xml";
		homeService.loadFromFile(new File(serviceFilename));
		homeResource = homeService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(homeResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return homeResource;
	}
	
	public Map<String, String> getUserData(Resource homeResource) {
		Document homeContent = (Document) homeResource.getContent();
		
		String cfid = null;
		String cftoken = null;
		String atuvc = null;
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("/response/session/cfid");
			cfid = (String) expr.evaluate(homeContent, XPathConstants.STRING);
			
			XPathExpression expr2 = xpath.compile("/response/session/cftoken");
			cftoken = (String) expr2.evaluate(homeContent, XPathConstants.STRING);
			
			XPathExpression expr3 = xpath.compile("/response/session/atuvc");
			atuvc = (String) expr3.evaluate(homeContent, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, String> userData = new HashMap<String, String>();
		userData.put("cfid", cfid);
		userData.put("cftoken", cftoken);
		userData.put("atuvc", atuvc);
		return userData;
	}
	
	public Resource searchFromHTML(String queryText, Map<String, String> userData) {
		Resource searchResultResource;
		String resourceFilename = this.outputBasePath + "resources/searchresult-html.xml";
		
		try {
			searchResultResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return searchResultResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
		}
		
		Service searchService = new Service();
		String serviceFilename = this.inputBasePath + "services/searchresult-html.xml";
		searchService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		for (Map.Entry<String, String> aData : userData.entrySet()) {
			searchService.addData( aData.getKey(), aData.getValue() );
		}
		searchService.addData("query", queryText);
		
		searchResultResource = searchService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(searchResultResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return searchResultResource;
	}
	
	public List<String> filterArticleIdsBySearchResult(Resource searchResult, List<String> articleIdList) {		
		List<String> filteredArticleIdList = new ArrayList<String>();
		
		List<String> articleTitleList = this.getArticlePropertiesFromSearchResult(searchResult, "title");
		List<String> articleKeywordsList = this.getArticlePropertiesFromSearchResult(searchResult, "keywords");
		
		QueryMatcherVisitor visitor = new QueryMatcherVisitor();
		for (int i=0; i<articleIdList.size(); i++) {
			String id = articleIdList.get(i);
			String title = articleTitleList.get(i);
			String keywords = articleKeywordsList.get(i);
			
			// General conditions are in OR, thus here we can only filter any specific-field requirement, such as
			// 'Abstract': 'term' and so on
			filteredArticleIdList.add(id);

			/*
			if (this.doMatchQuery(title) || this.doMatchQuery(keywords) {
				filteredArticleIdList.add(id);
			}
			*/
		}
	
		return filteredArticleIdList;
	}
	
	/*
	public List<Resource> filterArticleDetails(List<Resource> articleDetailList) {
		List<Resource> filteredArticleDetailList = new ArrayList<Resource>();
		
		QueryMatcherVisitor visitor = new QueryMatcherVisitor();
		for (Resource articleDetail : articleDetailList) {
			String title = this.getArticlePropertyFromDetails(articleDetail, "title");
			String abstractProp = this.getArticlePropertyFromDetails(articleDetail, "abstract");
						
			if (this.doMatchQuery(title) || this.doMatchQuery(abstractProp)) {
				filteredArticleDetailList.add(articleDetail);
			}
		}
		
		return filteredArticleDetailList;
	}
	*/
	
	public List<Resource> filterArticleDetails(List<Resource> articleDetailList, Resource searchResult) {
		List<Resource> filteredArticleDetailList = new ArrayList<Resource>();

		for (Resource articleDetail : articleDetailList) {
			Document articleDetailContent = (Document) articleDetail.getContent();
			
			String id = this.getArticlePropertyFromDetails(articleDetail, "id");
			
			Element searchResultArticleEl = this.getArticleElementFromSearchResult(searchResult, id);
			Element articleDetailEl;
			try {
				articleDetailEl = XMLParser.select("article", articleDetailContent.getDocumentElement()).get(0);
			} catch (XPathExpressionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				
				// wrong schema
				return null;
			}
			
			try {
				String title = XMLParser.select("title", searchResultArticleEl).get(0).getTextContent().trim();
				String abstractProp = XMLParser.select("abstract", articleDetailEl).get(0).getTextContent().trim();
				String keywords = XMLParser.select("keywords", searchResultArticleEl).get(0).getTextContent().trim();
				
				if (this.doMatchQuery(title) || this.doMatchQuery(abstractProp) || this.doMatchQuery(keywords)) {
					filteredArticleDetailList.add(articleDetail);
				}
				
			} catch (DOMException | XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				// in doubt insert the element anyway
				filteredArticleDetailList.add(articleDetail);
			}
		}
		
		return filteredArticleDetailList;
	}
	
	public Resource getArticleDetailsFromHTML(String articleId, Map<String, String> userData) {
		Resource articleDetailsResource;
		String resourceFilename = this.outputBasePath + "resources/articledetails-html_"+articleId+".xml";
		
		try {
			articleDetailsResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return articleDetailsResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
		}
		
		Service articleDetailsService = new Service();
		String serviceFilename = this.inputBasePath + "services/articledetails-html.xml";
		articleDetailsService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		for (Map.Entry<String, String> aData : userData.entrySet()) {
			articleDetailsService.addData( aData.getKey(), aData.getValue() );
		}
		articleDetailsService.addData("id", articleId);
		
		articleDetailsResource = articleDetailsService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(articleDetailsResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return articleDetailsResource;
	}

}

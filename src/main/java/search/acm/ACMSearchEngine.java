package search.acm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import parsers.xml.XMLParser;
import query.QueryMatcherVisitor;
import search.SearchEngine;
import services.Service;
import services.resources.Resource;
import data.ArticleList;

public class ACMSearchEngine extends SearchEngine {

	protected Map<String, String> userData;
	
	public ACMSearchEngine() {
		super("acm");
		
		this.numberOfResultsPerPage = 20;
		
		this.userData = new HashMap<String, String>();
	}

	@Override
	public ArticleList search() {
		Resource homeResource = this.home();
		this.userData = this.getUserData(homeResource);
		
		return this.searchAllPages();
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
		
		try {
			XPathExpression expr = XMLParser.getXpath().compile("/response/session/cfid");
			cfid = ( (String) expr.evaluate(homeContent, XPathConstants.STRING) ).trim();
			
			XPathExpression expr2 = XMLParser.getXpath().compile("/response/session/cftoken");
			cftoken = ( (String) expr2.evaluate(homeContent, XPathConstants.STRING) ).trim();
			
			XPathExpression expr3 = XMLParser.getXpath().compile("/response/session/atuvc");
			atuvc = ( (String) expr3.evaluate(homeContent, XPathConstants.STRING) ).trim();
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

	public Resource searchFromDefault(Integer pageNumber) {	
		return this.searchFromHTML(pageNumber);
	}
	
	public Resource searchFromHTML(Integer pageNumber) {	
		Resource searchResultResource;
		String resourceFilename = this.outputBasePath + "resources/searchresult_" + pageNumber + "-html.xml";
		
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
		
		for (Map.Entry<String, String> aData : this.userData.entrySet()) {
			searchService.addData( aData.getKey(), aData.getValue() );
		}
		searchService.addData("query", this.queryText);
		searchService.addData("pageNumber", pageNumber.toString());
		
		Integer startResult = this.calculateStartResult(pageNumber, this.numberOfResultsPerPage);
		searchService.addData("startResult", startResult.toString());
		
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
		}
	
		return filteredArticleIdList;
	}
	
	public Resource getArticleDetailsFromDefault(String articleId) {
		return this.getArticleDetailsFromHTML(articleId);
	}
	
	public Resource getArticleDetailsFromHTML(String articleId) {
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
		for (Map.Entry<String, String> aData : this.userData.entrySet()) {
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

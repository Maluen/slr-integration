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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import parsers.xml.XMLParser;
import search.SearchEngine;
import services.Service;
import services.resources.Resource;

public class ACMSearchEngine extends SearchEngine {

	protected Map<String, String> userData;
	
	public ACMSearchEngine() {
		super("acm");
		
		this.numberOfResultsPerPage = 20;
		
		this.userData = new HashMap<String, String>();
	}
	
	@Override
	protected void login() {
		this.logger.log("\n"+this.name.toUpperCase()+": login start");
		
		// Home resource is not resumed since we want to always make a request
		// to create the user session
		Resource homeResource = this.extractHome(false);
		
		// extend existing user data with the new one
		// Note: empty data are skipped if we already have a previous value, since the server doesn't return
		// cookies that we already have (applies if we are already/still logged in).
		Map<String, String> newUserData = this.getUserData(homeResource);
		for (Map.Entry<String, String> entry : newUserData.entrySet()) {
			String newUserDataKey = entry.getKey();
			String newUserDataValue = entry.getValue();
			
			if (!this.userData.containsKey(newUserDataKey) || (newUserDataValue != null && !newUserDataValue.isEmpty())) {
				this.userData.put(newUserDataKey, newUserDataValue);
			}
		}
		
		this.logger.log("\n"+this.name.toUpperCase()+": login done");
	}
	
	protected Resource extractHome(Boolean tryResume) {
		Resource homeResource;
		String resourceFilename = this.outputBasePath + "resources/home-html.xml";

		if (tryResume) {
			try {
				homeResource = this.resourceLoader.load(new File(resourceFilename));
				this.logger.log("\nResumed: " + resourceFilename);
				return homeResource;
			} catch (SAXException | IOException e1) {
				// proceed
				this.logger.log("\nFetching new " + resourceFilename);
			}
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
	
	protected Map<String, String> getUserData(Resource homeResource) {
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

	@Override
	protected Resource extractSearchResult(Integer pageNumber) {	
		Resource searchResultResource;
		String resourceFilename = this.outputBasePath + "resources/searchresult_" + pageNumber + "-html.xml";
		
		try {
			searchResultResource = this.resourceLoader.load(new File(resourceFilename));
			this.logger.log("\nResumed: " + resourceFilename);
			return searchResultResource;
		} catch (SAXException | IOException e1) {
			// proceed
			this.logger.log("\nFetching new " + resourceFilename);
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
		
		if (this.startYear != null) searchService.addData("startYear", this.startYear.toString());
		if (this.endYear != null) searchService.addData("endYear", this.endYear.toString());
		
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
	
	@Override
	protected Resource extractArticleDetails(String articleId) {
		Resource articleDetailsResource;
		String resourceFilename = this.outputBasePath + "resources/articledetails-html_"+articleId+".xml";
		
		try {
			articleDetailsResource = this.resourceLoader.load(new File(resourceFilename));
			this.logger.log("\nResumed: " + resourceFilename);
			return articleDetailsResource;
		} catch (SAXException | IOException e1) {
			// proceed
			this.logger.log("\nFetching new " + resourceFilename);
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

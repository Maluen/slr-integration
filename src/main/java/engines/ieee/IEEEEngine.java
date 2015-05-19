package engines.ieee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class IEEEEngine extends Engine {

	public IEEEEngine() {
		super("ieee");
	}

	@Override
	public void search(ParseTree queryTree) {
		this.setQueryTree(queryTree);
		
		// TODO: convert generic search input into engine-specific search input
		String queryText = queryTree.getText();
	
		Resource searchResult = this.searchFromHTML(queryText);
		//Resource searchResult = this.searchFromXML(queryText);
		List<String> articleIdList = this.getArticleIdsFromSearchResult(searchResult);
		
		// first filtering with the information we have right now
		List<String> validArticleIdList = this.filterArticleIdsBySearchResult(searchResult, articleIdList);
		
		// get all valid article details
		List<Resource> validArticleDetailsList = new ArrayList<Resource>();
		for (String validArticleId : validArticleIdList) {
			Resource validArticleDetails = this.getArticleDetailsFromHTML(validArticleId);
			validArticleDetailsList.add(validArticleDetails);
		}
		
		// TODO: filter again with the new information
		validArticleDetailsList = this.filterArticleDetails(validArticleDetailsList, searchResult);
		// update the valid ids
		validArticleIdList = this.getArticleIdsFromDetails(validArticleDetailsList);
		
		this.output(searchResult, validArticleDetailsList, validArticleIdList);
	}
	
	public Resource searchFromHTML(String queryText) {
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
		searchService.addData("queryText", queryText);
		searchService.addData("pageNumber", "1");
		
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
	
	public Integer getCount(Resource searchResultHtmlResource) {
		Integer count;
		
		Document searchResultContent = (Document) searchResultHtmlResource.getContent();
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("/response/meta/count");
			Double countDouble = (Double) expr.evaluate(searchResultContent, XPathConstants.NUMBER);
			count = countDouble.intValue();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			count = null;
		}
		
		return count;
	}
	
	public Resource searchFromXML(String queryText) {
		Resource searchResultResource;
		String resourceFilename = this.outputBasePath + "resources/searchresult-xml.xml";
		
		try {
			searchResultResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return searchResultResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
		}
		
		Service searchService = new Service();
		String serviceFilename = this.inputBasePath + "services/searchresult-xml.xml";
		searchService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		searchService.addData("queryText", queryText);
		searchService.addData("startNumber", "1");
		searchService.addData("numberOfResults", "25"); // max 1000
		
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
		List<String> articleAbstractList = this.getArticlePropertiesFromSearchResult(searchResult, "abstract");
		
		QueryMatcherVisitor visitor = new QueryMatcherVisitor();
		for (int i=0; i<articleIdList.size(); i++) {
			String id = articleIdList.get(i);
			String title = articleTitleList.get(i);
			String abstractProp = articleAbstractList.get(i);
			
			// General conditions are in OR, thus here we can only filter any specific-field requirement, such as
			// 'Abstract': 'term' and so on
			filteredArticleIdList.add(id);
			
			/*
			if (this.doMatchQuery(title) || this.doMatchQuery(abstractProp) {
				filteredArticleIdList.add(id);
			}
			*/
		}
	
		return filteredArticleIdList;
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
				String abstractProp = XMLParser.select("abstract", searchResultArticleEl).get(0).getTextContent().trim();
				String keywords = XMLParser.select("keywords", articleDetailEl).get(0).getTextContent().trim();
				
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
	
}

package engines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

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

public abstract class Engine {

	// NOTE: name must have unix-names format
	protected String name = "";
	
	protected String inputBasePath = "";
	protected String outputBasePath = "";
	
	protected ResourceSerializer resourceSerializer;
	protected ResourceLoader resourceLoader;
	
	protected ParseTree queryTree;
	
	public Engine(String name) {
		
		this.name = name;
		
		this.inputBasePath = "data/engines/" + this.name + "/";
		this.outputBasePath = "data/output/engines/" + this.name + "/";
		
		this.resourceSerializer = new ResourceSerializer();
		this.resourceLoader = new ResourceLoader();
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ParseTree getQueryTree() {
		return this.queryTree;
	}

	public void setQueryTree(ParseTree queryTree) {
		this.queryTree = queryTree;
	}

	// TODO: add search input parameters
	public abstract void search(ParseTree queryTree);
	
	public List<String> getArticlePropertiesFromSearchResult(Resource searchResult, String propertyName) {
		List<String> articlePropertyList = new ArrayList<String>();
		
		Document searchResultContent = (Document) searchResult.getContent();
		
		try {
			List<Element> articleList = XMLParser.select("articles/item", searchResultContent.getDocumentElement());
			for (Element articleEl : articleList) {
				String property = (String) XMLParser.getXpath().evaluate(propertyName, articleEl, XPathConstants.STRING);
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
			property = (String) XMLParser.getXpath().evaluate(propertyName, articleEl, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
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
		Boolean passes = visitor.visit(this.queryTree);
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
}

package engines.ieee;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import misc.Utils;

import org.w3c.dom.Document;

import services.Resource;
import services.Service;
import engines.Engine;

public class IEEEEngine extends Engine {

	public IEEEEngine() {
		super("ieee");
	}

	public void search(String queryText) {
		
		// TODO: convert generic search input into engine-specific search input
	
		this.searchFromHTML(queryText);
		this.searchFromXML(queryText);
	}
	
	public void searchFromHTML(String queryText) {
		// Fetch, parse and extract service content
		
		String fileName = "services/searchresult-html.xml"; // relative to base paths
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File(this.inputBasePath + fileName);
		searchService.loadFromFile(serviceFile);
		// set any needed data
		searchService.addData("queryText", queryText);
		searchService.addData("pageNumber", "1");
		
		Resource searchResultResource = searchService.execute(); // TODO: make this async?
		Document searchResultContent = (Document) searchResultResource.getContent();
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// explore result content tree!
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("/response/meta/count");
			Double count = (Double) expr.evaluate(searchResultContent, XPathConstants.NUMBER);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void searchFromXML(String queryText) {
		
		String fileName = "services/searchresult-xml.xml"; // relative to base paths
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File(this.inputBasePath + fileName);
		searchService.loadFromFile(serviceFile);
		// set any needed data
		searchService.addData("queryText", queryText);
		searchService.addData("startNumber", "1");
		searchService.addData("numberOfResults", "25"); // max 1000
		
		Resource searchResultResource = searchService.execute(); // TODO: make this async?
		Document searchResultContent = (Document) searchResultResource.getContent();
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
}

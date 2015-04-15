package engines.ieee;

import java.io.File;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import misc.Utils;

import org.w3c.dom.Document;

import services.Service;
import engines.Engine;

public class IEEEEngine implements Engine {

	public void search() {
		
		// convert generic search input into engine-specific search input
		// ...

		// Fetch, parse and extract service content
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File("data/engines/ieee/services/searchresult-html.xml");
		searchService.loadFromFile(serviceFile);
		// set any needed data
		searchService.addData("queryText", "mde OR dsl");
		searchService.addData("pageNumber", "1");
		
		Document searchResultContent = searchService.request(); // TODO: make this async?
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, "data/index.xml");
		} catch (TransformerFactoryConfigurationError | TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// explore result content tree!
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("/content/meta/count");
			Double count = (Double) expr.evaluate(searchResultContent, XPathConstants.NUMBER);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
}

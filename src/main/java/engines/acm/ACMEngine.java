package engines.acm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class ACMEngine extends Engine {

	public ACMEngine() {
		super("acm");
	}

	@Override
	public void search(String queryText) {
		Map<String, String> userData = this.login();
		this.searchFromHtml(queryText, userData);
	}
	
	/**
	 * @return user data that can be passed to the service
	 */
	public Map<String, String> login() {
		String fileName = "services/home-html.xml"; // relative to base paths
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File(this.inputBasePath + fileName);
		searchService.loadFromFile(serviceFile);
		
		Document searchResultContent = searchService.request(); // TODO: make this async?
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// explore result content tree!
		String cfid = null;
		String cftoken = null;
		String atuvc = null;
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("/response/session/cfid");
			cfid = (String) expr.evaluate(searchResultContent, XPathConstants.STRING);
			
			XPathExpression expr2 = xpath.compile("/response/session/cftoken");
			cftoken = (String) expr2.evaluate(searchResultContent, XPathConstants.STRING);
			
			XPathExpression expr3 = xpath.compile("/response/session/atuvc");
			atuvc = (String) expr3.evaluate(searchResultContent, XPathConstants.STRING);
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
	
	public void searchFromHtml(String queryText, Map<String, String> userData) {
		String fileName = "services/searchresult-html.xml"; // relative to base paths
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File(this.inputBasePath + fileName);
		searchService.loadFromFile(serviceFile);
		// set any needed data
		for (Map.Entry<String, String> aData : userData.entrySet()) {
			searchService.addData( aData.getKey(), aData.getValue() );
		}
		searchService.addData("query", queryText);
		
		Document searchResultContent = searchService.request(); // TODO: make this async?
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}

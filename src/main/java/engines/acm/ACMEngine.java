package engines.acm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import services.Service;
import engines.Engine;

public class ACMEngine extends Engine {

	public ACMEngine() {
		super("acm");
	}

	@Override
	public void search(String queryText) {
		Map<String, String> userData = this.login();
		//List<String> articleIdList = this.searchFromHtml(queryText, userData);
		
		//this.getArticleDetails(articleIdList.get(0), userData);
		
		// DEBUG
		this.getArticleDetails("2400267.2400302", userData);
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
		
		Document searchResultContent = searchService.execute(); // TODO: make this async?
		
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
	
	public List<String> searchFromHtml(String queryText, Map<String, String> userData) {
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
		
		Document searchResultContent = searchService.execute(); // TODO: make this async?
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<String> articleIdList = new ArrayList<String>();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XMLParser xmlParser = new XMLParser();
		try {
			List<Element> articleList = xmlParser.select("articles/item", searchResultContent.getDocumentElement());
			for (Element article : articleList) {
				String id = (String) xpath.evaluate("id", article, XPathConstants.STRING);
				articleIdList.add(id);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articleIdList;
	}
	
	public void getArticleDetails(String articleId, Map<String, String> userData) {
		String fileName = "services/articledetails-html.xml"; // relative to base paths
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File(this.inputBasePath + fileName);
		searchService.loadFromFile(serviceFile);
		// set any needed data
		for (Map.Entry<String, String> aData : userData.entrySet()) {
			searchService.addData( aData.getKey(), aData.getValue() );
		}
		searchService.addData("id", articleId);
		
		Document searchResultContent = searchService.execute(); // TODO: make this async?
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}

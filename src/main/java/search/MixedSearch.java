package search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import misc.Utils;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.query.QueryParser;
import parsers.xml.DocumentFactory;
import parsers.xml.XMLParser;
import data.ArticleList;

public class MixedSearch {

	protected String outputBasePath = "data/output/searches/";
	protected String configurationFilename = "search.xml";
	protected XMLParser xmlParser;
	
	protected String[] sites = new String[]{
			"acm", "ieee"
	};
	
	protected String queryText; // MANDATORY
	protected ParseTree queryTree; // calculated from queryText
	
	public MixedSearch() {
		this.xmlParser = new XMLParser();
	}

	public Boolean isResumable() {
		// true if configuration file exists
		return Utils.doFileExists(this.outputBasePath+this.configurationFilename);
	}
	
	public ArticleList resume() {
		
		try {
			this.loadConfiguration();
			
		} catch (IOException e) {
			// configuration file does not exist or has errors
			e.printStackTrace();
			return null;
		}
		
		return this.execute();
	};
	
	public ArticleList newSearch() {
		// TODO: replace any previous search data with the new one
		
		try {
			// remove directory content
			FileUtils.cleanDirectory(new File(this.outputBasePath));
			// create new configuration file
			this.saveConfiguration();
			
		} catch (IOException | TransformerFactoryConfigurationError | TransformerException e) {
			// Delete or save errors
			e.printStackTrace();
			return null;
		}
		
		return this.execute();
	}
	
	public ArticleList execute() {		
		// search from all sites
		List<ArticleList> allSitesArticleList = new ArrayList<ArticleList>();
		for (int i=0; i<this.sites.length; i++) {
			SearchManager searchManager = SearchManagerFactory.create(this.sites[i]);
			searchManager.setQueryTree(this.queryTree);
			ArticleList siteArticles = searchManager.execute();
			
			allSitesArticleList.add(siteArticles);
		}
		
		// merge all
		MixedSearchMerger mixedSearchMerger = new MixedSearchMerger();
		mixedSearchMerger.setAllArticleList(allSitesArticleList);
		ArticleList searchResult = mixedSearchMerger.execute();
	
		return searchResult;
	}
	
	// Load configuration from XML file
	public void loadConfiguration() throws IOException {
		
		String fileContent = Utils.getFileContent(new File(this.outputBasePath+this.configurationFilename));
		Document document = this.xmlParser.parse(fileContent);
		Element rootEl = document.getDocumentElement();
		
		// read query
		String queryText = XMLParser.getChildElementByTagName(rootEl, "query").getTextContent().trim();
		
		// read sites
		List<String> siteList = new ArrayList<String>();
		Element sitesEl = XMLParser.getChildElementByTagName(rootEl, "sites");
		List<Element> siteElList = XMLParser.getChildElements(sitesEl);
		for (Element siteEl : siteElList) {
			String siteName = siteEl.getTextContent().trim();
			siteList.add(siteName);
		}
		String[] siteArray = siteList.toArray(new String[siteList.size()]);

		// set configuration
		this.setQueryText(queryText);
		this.setSites(siteArray);		
	}
	
	// Save current configuration to XML file
	public void saveConfiguration() throws TransformerFactoryConfigurationError, TransformerException, IOException {
		
		Document document = DocumentFactory.getDocBuilder().newDocument();

		// root element
		Element documentRootEl = document.createElement("search");
		document.appendChild(documentRootEl);
		
		// queryText
		Element queryEl = document.createElement("query");
		queryEl.setTextContent(this.queryText);
		documentRootEl.appendChild(queryEl);
		
		// sites
		Element sitesEl = document.createElement("sites");
		for (String site : this.sites) {
			Element siteEl = document.createElement("item");
			siteEl.setTextContent(site);
			sitesEl.appendChild(siteEl);	
		}
		documentRootEl.appendChild(sitesEl);
		
		Utils.saveDocument(document, this.outputBasePath+this.configurationFilename);
	}
	
	public String[] getSites() {
		return this.sites;
	}

	public void setSites(String[] sites) {
		this.sites = sites;
	}

	public ParseTree getQueryText() {
		return this.queryTree;
	}
	
	public void setQueryText(String queryText) {
		this.queryText = queryText;
		
		// parse query
		QueryParser languageParser = new QueryParser();
		ParseTree queryTree = languageParser.parse(this.queryText);
		this.setQueryTree(queryTree);
	}

	public void setQueryTree(ParseTree queryTree) {
		this.queryTree = queryTree;
	}
	
}

package search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import misc.Logger;
import misc.Utils;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.query.QueryParser;
import parsers.xml.DocumentFactory;
import parsers.xml.XMLParser;
import data.ArticleList;

public class MixedSearch {

	protected Logger logger;
	
	protected String outputBasePath = "data/output/";
	protected String configurationFilename = "search.xml";
	protected XMLParser xmlParser;
	
	protected String[] sites = new String[]{
			"acm", "ieee"
	};
	protected String queryText; // MANDATORY
	protected ParseTree queryTree; // calculated from queryText
	protected Integer startYear = null; // optional
	protected Integer endYear = null; // optional
	protected Boolean fastOutput = false;
	protected String outputCSVFilename; // MANDATORY
	
	public MixedSearch() {
		this.logger = new Logger("MixedSearch");
		
		this.xmlParser = new XMLParser();
	}

	public Boolean isResumable() {
		// true if configuration file exists
		return Utils.doFileExists(this.outputBasePath+this.configurationFilename);
	}
	
	public void resume() {
		this.logger.log("Resuming search");
		
		try {
			// load resume configuration file
			this.loadConfiguration();
			
		} catch (IOException e) {
			// configuration file does not exist or has errors
			e.printStackTrace();
			return;
		}
		
		this.execute();
	};
	
	public void newSearch() {
		this.logger.log("Starting new search");
		
		try {
			// remove directory content
			File outputDirectory = new File(this.outputBasePath);
			if (outputDirectory.exists()) { // (exists check avoids exceptions if directory is missing)
				FileUtils.cleanDirectory(new File(this.outputBasePath));
			}
			// create resume configuration file
			this.saveConfiguration();
			
		} catch (IOException | TransformerFactoryConfigurationError | TransformerException e) {
			// Delete or save errors
			e.printStackTrace();
			return;
		}
		
		this.execute();
	}
	
	public void execute() {
		if (this.fastOutput) this.fastSearch();
		else this.fullSearch();
	}
	
	public void fastSearch() {
		// search from all sites
		List<List<Integer>> allSitesOutputCountList = new ArrayList<List<Integer>>();
		for (int i=0; i<this.sites.length; i++) {
			
			SearchManager searchManager = SearchManagerFactory.create(this.sites[i]);
			this.configureSearchManager(searchManager);
			searchManager.execute();
			
			List<Integer> siteCountList = searchManager.getOutputCountList();
			allSitesOutputCountList.add(siteCountList);
		}
		
		// print fast search output to stdout
		System.out.println("\n#######################################################################");
		System.out.println("Total number of results:\n");
		for (int i=0; i<this.sites.length; i++) {
			
			Integer siteTotalCount = 0;
			List<Integer> siteCountList = allSitesOutputCountList.get(i);
			for (Integer siteCount : siteCountList) {
				siteTotalCount += siteCount;
			}
			
			String numOfSearchesDescription = siteCountList.size() + " search";
			if (siteCountList.size() != 1) numOfSearchesDescription += "es"; // plural
			
			System.out.println(this.sites[i].toUpperCase()+": " + siteTotalCount + " ("+numOfSearchesDescription+")");
		}
		System.out.println("\n(Warning: the number of results may be overestimated \n"
						 + "since no local filtering has been performed yet, \n"
						 + "this is true especially in case of multiple searches due to splitting)");
		System.out.println("#######################################################################");
	}
	
	public void fullSearch() {
		// search from all sites
		List<ArticleList> allSitesArticleList = new ArrayList<ArticleList>();
		for (int i=0; i<this.sites.length; i++) {
			
			SearchManager searchManager = SearchManagerFactory.create(this.sites[i]);
			this.configureSearchManager(searchManager);
			searchManager.execute();
			
			ArticleList siteArticles = searchManager.getOutputArticleList();
			allSitesArticleList.add(siteArticles);
		}
		
		// merge all
		MixedSearchMerger mixedSearchMerger = new MixedSearchMerger();
		mixedSearchMerger.setAllArticleList(allSitesArticleList);
		ArticleList searchResult = mixedSearchMerger.execute();
		
		// save results to file
		this.logger.log("\nFinal number of fetched articles: " + searchResult.size());
		this.logger.log("Saving output csv to \"" + this.outputCSVFilename + "\".");
		try {
			searchResult.saveAsCSV(this.outputCSVFilename);
			this.logger.log("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void configureSearchManager(SearchManager searchManager) {
		searchManager.setQueryTree(this.queryTree);
		searchManager.setStartYear(this.startYear);
		searchManager.setEndYear(this.endYear);
		searchManager.setFastOutput(this.fastOutput);
	}
	
	// Load configuration from XML file
	public void loadConfiguration(String path) throws IOException {
		
		String fileContent = Utils.getFileContent(new File(path));
		Document document = this.xmlParser.parse(fileContent);
		Element rootEl = document.getDocumentElement();
		
		// read query
		String queryText = XMLParser.getChildElementByTagName(rootEl, "query").getTextContent().trim();
		
		// read sites
		Element sitesEl = XMLParser.getChildElementByTagName(rootEl, "sites");
		String[] sites = sitesEl.getTextContent().trim().split("\\s*,\\s*");
		
		// start year (optional)
		Integer startYear = null;
		Element startyearEl = XMLParser.getChildElementByTagName(rootEl, "startyear");
		if (startyearEl != null) {
			String startYearString = startyearEl.getTextContent().trim();
			if (!startYearString.isEmpty()) startYear = Integer.parseInt(startYearString); // (ignore empty element)
		}
		
		// end year (optional)
		Integer endYear = null;
		Element endyearEl = XMLParser.getChildElementByTagName(rootEl, "endyear");
		if (endyearEl != null) {
			String endYearString = endyearEl.getTextContent().trim();
			if (!endYearString.isEmpty()) endYear = Integer.parseInt(endYearString); // (ignore empty element)
		}
		
		// read fast output (optional)
		Boolean fastOutput = false;
		Element fastoutputEl = XMLParser.getChildElementByTagName(rootEl, "fastoutput");
		if (fastoutputEl != null) {
			fastOutput = Boolean.parseBoolean( fastoutputEl.getTextContent().trim() );
		}

		// output CSV filename
		String outputCSVFilename = XMLParser.getChildElementByTagName(rootEl, "outputpath").getTextContent().trim();
		
		// set configuration
		this.setQueryText(queryText);
		this.setSites(sites);		
		this.setStartYear(startYear);
		this.setEndYear(endYear);
		this.setFastOutput(fastOutput);
		this.setOutputCSVFilename(outputCSVFilename);
	}
	
	public void loadConfiguration() throws IOException {
		this.loadConfiguration(this.outputBasePath+this.configurationFilename);
	}
	
	// Save current configuration to XML file
	public void saveConfiguration(String path) throws TransformerFactoryConfigurationError, TransformerException, IOException {
		
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
		sitesEl.setTextContent(StringUtils.join(this.sites, ", "));
		documentRootEl.appendChild(sitesEl);
		
		// start year (optional)
		if (this.startYear != null) {
			Element startyearEl = document.createElement("startyear");
			startyearEl.setTextContent(this.startYear.toString());
			documentRootEl.appendChild(startyearEl);
		}
		
		// end year (optional)
		if (this.endYear != null) {
			Element endyearEl = document.createElement("endyear");
			endyearEl.setTextContent(this.endYear.toString());
			documentRootEl.appendChild(endyearEl);
		}
		
		// fast output
		Element fastoutputEl = document.createElement("fastoutput");
		fastoutputEl.setTextContent(this.fastOutput.toString());
		documentRootEl.appendChild(fastoutputEl);
		
		// output CSV filename
		Element outputpathEl = document.createElement("outputpath");
		outputpathEl.setTextContent(this.outputCSVFilename);
		documentRootEl.appendChild(outputpathEl);
		
		Utils.saveDocument(document, path);
	}
	
	public void saveConfiguration() throws TransformerFactoryConfigurationError, TransformerException, IOException {
		this.saveConfiguration(this.outputBasePath+this.configurationFilename);
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
	
	public Integer getStartYear() {
		return this.startYear;
	}

	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}

	public Integer getEndYear() {
		return this.endYear;
	}

	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}

	public Boolean isFastOutput() {
		return this.fastOutput;
	}

	public void setFastOutput(Boolean fastOutput) {
		this.fastOutput = fastOutput;
	}

	public String getOutputCSVFilename() {
		return this.outputCSVFilename;
	}

	public void setOutputCSVFilename(String outputCSVFilename) {
		this.outputCSVFilename = outputCSVFilename;
	}
	
}

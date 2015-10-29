package search.springer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import misc.Settings;

import org.xml.sax.SAXException;

import search.SearchEngine;
import services.Service;
import services.resources.Resource;

public class SpringerSearchEngine extends SearchEngine {

	Settings settings;
	
	public SpringerSearchEngine() {
		super("springer");
		
		this.numberOfResultsPerPage = 20;
		
		this.settings = Settings.getInstance();
	}

	@Override
	protected Resource extractSearchResult(Integer pageNumber) {
		Resource searchResultResource;
		String resourceFilename = this.outputBasePath + "resources/searchresult_" + pageNumber + "-xml.xml";
		
		try {
			searchResultResource = this.resourceLoader.load(new File(resourceFilename));
			this.logger.log("\nResumed: " + resourceFilename);
			return searchResultResource;
		} catch (SAXException | IOException e1) {
			// proceed
			this.logger.log("\nFetching new " + resourceFilename);
		}
		
		Service searchService = new Service();
		String serviceFilename = this.inputBasePath + "services/searchresult-xml.xml";
		searchService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		
		searchService.addData("apiKey", this.settings.get("springerApiKey"));
		
		searchService.addData("query", this.queryText);
		searchService.addData("pageNumber", pageNumber.toString());
		searchService.addData("numberOfResults", this.numberOfResultsPerPage.toString()); // max 1000
		
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
		// normalize id for using it in a filename
		String normalizedArticleId = articleId.replaceAll("[^a-zA-Z0-9-]", "_");
		
		Resource articleDetailsResource;
		String resourceFilename = this.outputBasePath + "resources/articledetails-xml_"+normalizedArticleId+".xml";
		
		try {
			articleDetailsResource = this.resourceLoader.load(new File(resourceFilename));
			this.logger.log("\nResumed: " + resourceFilename);
			return articleDetailsResource;
		} catch (SAXException | IOException e1) {
			// proceed
			this.logger.log("\nFetching new " + resourceFilename);
		}
		
		Service articleDetailsService = new Service();
		String serviceFilename = this.inputBasePath + "services/articledetails-xml.xml";
		articleDetailsService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		articleDetailsService.addData("apiKey", this.settings.get("springerApiKey"));
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

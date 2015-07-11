package search.ieee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import services.Service;
import services.resources.Resource;

// TODO: not working anymore since IEEE switched to AngularJS with XHR calls returning JSON
public class IEEESearchEngineScrape extends IEEESearchEngine {

	public IEEESearchEngineScrape() {
		
		this.outputServicePrefix = "output-scrape";
		this.numberOfResultsPerPage = 100;
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
		searchService.addData("query", this.queryText);
		searchService.addData("pageNumber", pageNumber.toString());
		searchService.addData("resultsPerPage", this.numberOfResultsPerPage.toString());
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

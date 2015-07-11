package search.ieee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import services.Service;
import services.resources.Resource;

public class IEEESearchEngineAPI extends IEEESearchEngine {

	public IEEESearchEngineAPI() {
		
		this.outputServicePrefix = "output-api";
		this.numberOfResultsPerPage = 100;
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
	protected List<Resource> extractNeededArticleDetails(List<String> articleIdList) {
		// no details are needed
		return new ArrayList<Resource>();
	}
	
	@Override
	protected Resource extractArticleDetails(String articleId) {
		// details are neither used or needed in the API version
		throw new UnsupportedOperationException();
	}
	
}

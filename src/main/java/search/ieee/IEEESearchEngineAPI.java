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
		this.numberOfResultsPerPage = 1000;
	}
	
	@Override
	protected Resource extractSearchResult(Integer pageNumber) {	
		Resource searchResultResource;
		String resourceFilename = this.outputBasePath + "resources/searchresult_" + pageNumber + "-xml.xml";
		
		try {
			searchResultResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return searchResultResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
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
	protected List<String> filterArticleIdsBySearchResult(List<String> articleIdList, Resource searchResult) {		
		List<String> filteredArticleIdList = new ArrayList<String>();

		// The search result contains all the required information, the whole filtering can be done right now.
		for (String articleId : articleIdList) {
			Element searchResultArticleEl = this.getArticleElementFromSearchResult(searchResult, articleId);

			String title = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "title");
			
			String abstractProp = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "abstract");			
			
			String keywords = this.getArticlePropertyFromSearchResultArticleEl(searchResultArticleEl, "keywords");
			
			// concatenate to consider all fields at once
			// (otherwise for example the terms in an AND expression will have to be matched ALL by one single field)
			String target = title + " " + abstractProp + " " + keywords;
			
			if (this.doMatchQuery(target)) {
				filteredArticleIdList.add(articleId);
			}
		}
		
		return filteredArticleIdList;
	}
	
	@Override
	protected List<Resource> extractNeededArticleDetails(List<String> articleIdList) {
		// no details are needed
		return new ArrayList<Resource>();
	}
	
	@Override
	protected Resource extractArticleDetails(String articleId) {
		// details are neither used or needed in the API
		return null;
	}
	
	@Override
	protected List<String> filterArticleIdsByArticleDetailsAndSearchResult(List<String> articleIdList, List<Resource> articleDetailList, Resource searchResult) {
		// everything has already been filtered, avoid another useless filtering
		// to speed-up computation
		List<String> filteredArticleIdList = new ArrayList<String>();
		filteredArticleIdList.addAll(articleIdList); // clone
		return filteredArticleIdList;
	}
}

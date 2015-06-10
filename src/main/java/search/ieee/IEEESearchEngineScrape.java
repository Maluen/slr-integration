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
			System.out.println("Resumed: " + resourceFilename);
			return searchResultResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
		}
		
		Service searchService = new Service();
		String serviceFilename = this.inputBasePath + "services/searchresult-html.xml";
		searchService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		searchService.addData("query", this.queryText);
		searchService.addData("pageNumber", pageNumber.toString());
		searchService.addData("resultsPerPage", this.numberOfResultsPerPage.toString());
		
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
		
		// General conditions are in OR, thus here we can only filter any specific-field requirement, such as
		// 'Abstract': 'term' and so on, which we don't support right now
		filteredArticleIdList.addAll(articleIdList); // clone
	
		return filteredArticleIdList;	
	}
	
	@Override
	protected Resource extractArticleDetails(String articleId) {
		Resource articleDetailsResource;
		String resourceFilename = this.outputBasePath + "resources/articledetails-html_"+articleId+".xml";
		
		try {
			articleDetailsResource = this.resourceLoader.load(new File(resourceFilename));
			System.out.println("Resumed: " + resourceFilename);
			return articleDetailsResource;
		} catch (SAXException | IOException e1) {
			// proceed
			System.out.println("Unable to resume " + resourceFilename);
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

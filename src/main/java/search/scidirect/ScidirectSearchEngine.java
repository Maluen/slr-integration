package search.scidirect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import parsers.xml.XMLParser;
import search.SearchEngine;
import services.Service;
import services.resources.Resource;

public class ScidirectSearchEngine extends SearchEngine {
	
	public ScidirectSearchEngine() {
		super("scidirect");
		
		this.numberOfResultsPerPage = 25;
	}
	
	@Override
	protected void login() {
		this.logger.log("\n"+this.name.toUpperCase()+": login start");
		
		// Home resource is not resumed since we want to always make a request
		// to create the user session
		Resource homeResource = this.extractHome(false);
		
		this.logger.log("\n"+this.name.toUpperCase()+": login done");
	}
	
	protected Resource extractHome(Boolean tryResume) {
		Resource homeResource;
		String resourceFilename = this.outputBasePath + "resources/home-html.xml";

		if (tryResume) {
			try {
				homeResource = this.resourceLoader.load(new File(resourceFilename));
				this.logger.log("\nResumed: " + resourceFilename);
				return homeResource;
			} catch (SAXException | IOException e1) {
				// proceed
				this.logger.log("\nFetching new " + resourceFilename);
			}
		}
		
		Service homeService = new Service();
		String serviceFilename = this.inputBasePath + "services/home-html.xml";
		homeService.loadFromFile(new File(serviceFilename));
		homeResource = homeService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(homeResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return homeResource;
	}
	
	protected Resource extractSearchForm(Boolean tryResume) {
		Resource searchFormResource;
		String resourceFilename = this.outputBasePath + "resources/searchForm-html.xml";
		
		if (tryResume) {
			try {
				searchFormResource = this.resourceLoader.load(new File(resourceFilename));
				this.logger.log("\nResumed: " + resourceFilename);
				return searchFormResource;
			} catch (SAXException | IOException e1) {
				// proceed
				this.logger.log("\nFetching new " + resourceFilename);
			}
		}
		
		Service searchFormService = new Service();
		String serviceFilename = this.inputBasePath + "services/searchForm-html.xml";
		searchFormService.loadFromFile(new File(serviceFilename));
		searchFormResource = searchFormService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(searchFormResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return searchFormResource;
	}

	protected Resource extractSearchResultMeta(Integer pageNumber) {
		Resource searchResultMetaResource;
		String resourceFilename = this.outputBasePath + "resources/searchresultmeta_" + pageNumber + "-html.xml";
		
		try {
			searchResultMetaResource = this.resourceLoader.load(new File(resourceFilename));
			this.logger.log("\nResumed: " + resourceFilename);
			return searchResultMetaResource;
		} catch (SAXException | IOException e1) {
			// proceed
			this.logger.log("\nFetching new " + resourceFilename);
		}
		
		Service searchService = new Service();
		String serviceFilename = this.inputBasePath + "services/searchresultmeta-html.xml";
		searchService.loadFromFile(new File(serviceFilename));
		
		// set any needed data
		
		if (pageNumber == 1) {
			searchService.addData("isFirstPage", "true");
			
			// we want to get the (maybe updated) first page md5, thus resuming is disabled
			Resource searchFormResource = this.extractSearchForm(false);
			String firstPageMD5 = this.getResponseString(searchFormResource, "firstPage/md5");
			searchService.addData("currentPageMD5", firstPageMD5);
			
		} else {
			
			// extract needed data from the previous page
			Resource previousSearchResultMeta = this.extractSearchResultMeta(pageNumber-1);
			
			searchService.addData("currentPageMD5", this.getResponseString(previousSearchResultMeta, "nextPage/md5"));
			
			searchService.addData("ArticleListID", this.getResponseString(previousSearchResultMeta, "nextPage/ArticleListID"));
			searchService.addData("st", this.getResponseString(previousSearchResultMeta, "nextPage/st"));
			searchService.addData("count", this.getResponseString(previousSearchResultMeta, "nextPage/count"));
			searchService.addData("chunk", this.getResponseString(previousSearchResultMeta, "nextPage/chunk"));
			searchService.addData("hitCount", this.getResponseString(previousSearchResultMeta, "nextPage/hitCount"));
			searchService.addData("PREV_LIST", this.getResponseString(previousSearchResultMeta, "nextPage/PREV_LIST"));
			searchService.addData("NEXT_LIST", this.getResponseString(previousSearchResultMeta, "nextPage/NEXT_LIST"));
			searchService.addData("TOTAL_PAGES", this.getResponseString(previousSearchResultMeta, "nextPage/TOTAL_PAGES"));
		}
		
		searchService.addData("query", this.queryText);
		searchService.addData("pageNumber", pageNumber.toString());
		if (this.startYear != null) searchService.addData("startYear", this.startYear.toString());
		if (this.endYear != null) searchService.addData("endYear", this.endYear.toString());
		
		searchResultMetaResource = searchService.execute(); // TODO: make this async?
		
		// save resource
		try {
			this.resourceSerializer.serialize(searchResultMetaResource, resourceFilename);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return searchResultMetaResource;
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
		
		// load resource and set any needed data
		
		Resource searchResultMetaResource = this.extractSearchResultMeta(pageNumber);
		
		Service searchService = new Service();
		String serviceFilename;
		
		Boolean areFakeResults = this.getResponseString(searchResultMetaResource, "areFakeResults").equals("true");
		if (areFakeResults) {
			// no results
			serviceFilename = this.inputBasePath + "services/searchresult-html-empty.xml";
			
		} else {
			serviceFilename = this.inputBasePath + "services/searchresult-html.xml";
			
			searchService.addData("nextPageMD5", this.getResponseString(searchResultMetaResource, "nextPage/md5"));
			searchService.addData("ArticleListID", this.getResponseString(searchResultMetaResource, "nextPage/ArticleListID"));
			searchService.addData("st", this.getResponseString(searchResultMetaResource, "nextPage/st"));
			searchService.addData("count", this.getResponseString(searchResultMetaResource, "nextPage/count"));
			searchService.addData("chunk", this.getResponseString(searchResultMetaResource, "nextPage/chunk"));
			searchService.addData("hitCount", this.getResponseString(searchResultMetaResource, "nextPage/hitCount"));
			searchService.addData("PREV_LIST", this.getResponseString(searchResultMetaResource, "nextPage/PREV_LIST"));
			searchService.addData("NEXT_LIST", this.getResponseString(searchResultMetaResource, "nextPage/NEXT_LIST"));
			searchService.addData("TOTAL_PAGES", this.getResponseString(searchResultMetaResource, "nextPage/TOTAL_PAGES"));
			
			// list of articles to export
			String articleIdsQueryFragment = "";
			List<String> articleIdList = this.getArticlePropertiesFromSearchResult(searchResultMetaResource, "id");
			for (String articleId : articleIdList) {
				articleIdsQueryFragment += "&art=" + articleId;
			}
			searchService.addData("articleIdsQueryFragment", articleIdsQueryFragment);
		}
		searchService.loadFromFile(new File(serviceFilename));
		
		searchService.addData("realCount", this.getResponseString(searchResultMetaResource, "meta/count"));

		searchService.addData("query", this.queryText);
		searchService.addData("pageNumber", pageNumber.toString());
		if (this.startYear != null) searchService.addData("startYear", this.startYear.toString());
		if (this.endYear != null) searchService.addData("endYear", this.endYear.toString());
		
		searchService.addData("searchResultMeta", searchResultMetaResource.getName());
		// add any needed resource
		searchService.getResourceList().add(searchResultMetaResource);
		
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
		// details are neither used or needed
		throw new UnsupportedOperationException();
	}

}

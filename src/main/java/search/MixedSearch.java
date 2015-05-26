package search;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import data.ArticleList;

public class MixedSearch {

	protected String[] sites = new String[]{
			"acm", "ieee"
	};
	protected ParseTree queryTree;

	public ArticleList execute() {
		
		// search from all sites
		List<ArticleList> allSitesArticleList = new ArrayList<ArticleList>();
		for (int i=0; i<this.sites.length; i++) {
			SearchManager searchManager = SearchManagerFactory.create(this.sites[i]);
			searchManager.setQueryTree(queryTree);
			ArticleList siteArticles = searchManager.execute();
			
			allSitesArticleList.add(siteArticles);
		}
		
		// merge all
		MixedSearchMerger mixedSearchMerger = new MixedSearchMerger();
		mixedSearchMerger.setAllArticleList(allSitesArticleList);
		ArticleList searchResult = mixedSearchMerger.execute();
	
		return searchResult;
	}
	
	public String[] getSites() {
		return this.sites;
	}

	public void setSites(String[] sites) {
		this.sites = sites;
	}

	public ParseTree getQueryTree() {
		return queryTree;
	}

	public void setQueryTree(ParseTree queryTree) {
		this.queryTree = queryTree;
	}
	
}

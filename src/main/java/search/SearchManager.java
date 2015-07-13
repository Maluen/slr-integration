package search;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import data.ArticleList;

public abstract class SearchManager {

	protected ParseTree queryTree;
	
	protected Integer startYear = null; // optional
	protected Integer endYear = null; // optional

	public ArticleList execute() {
		// adapt
		SearchAdapter searchAdapter = this.createAdapter();
		List<String> queryTextList = searchAdapter.execute(this.queryTree);
		
		List<ArticleList> allSearchesArticleList = new ArrayList<ArticleList>();
		for (int i=0; i<queryTextList.size(); i++) {
			String currentQueryText = queryTextList.get(i);
			
			// search
			SearchEngine currentSearchEngine = this.createEngine();
			currentSearchEngine.setQueryText(currentQueryText);
			currentSearchEngine.setOriginalQueryTree(this.queryTree);
			currentSearchEngine.setSearchIndex(1+i); // start at 1
			currentSearchEngine.setTotalSearches(queryTextList.size());
			currentSearchEngine.setStartYear(this.startYear);
			currentSearchEngine.setEndYear(this.endYear);
			ArticleList currentSearchArticleList = currentSearchEngine.execute();
			
			allSearchesArticleList.add(currentSearchArticleList);
		}
		
		// merge
		SearchMerger searchMerger = this.createMerger();
		searchMerger.setAllArticleList(allSearchesArticleList);
		ArticleList siteArticleList = searchMerger.execute();
		
		return siteArticleList;
	}
	
	public abstract SearchAdapter createAdapter();
	public abstract SearchEngine createEngine();
	public abstract SearchMerger createMerger();
	
	public ParseTree getQueryTree() {
		return queryTree;
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
	
}

package search;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import data.ArticleList;

public abstract class SearchManager {

	protected ParseTree queryTree;

	public ArticleList execute() {
		// split
		SearchSplitter searchSplitter = this.createSplitter();
		searchSplitter.setQueryTree(this.queryTree);
		List<String> queryTextList = searchSplitter.execute();
	
		List<ArticleList> allSearchesArticleList = new ArrayList<ArticleList>();
		for (String currentQueryText : queryTextList) {
			// search
			SearchEngine currentSearchEngine = this.createEngine();
			currentSearchEngine.setQueryText(currentQueryText);
			currentSearchEngine.setOriginalQueryTree(this.queryTree);
			ArticleList currentSearchArticleList = currentSearchEngine.search();
			
			allSearchesArticleList.add(currentSearchArticleList);
		}
		
		// merge
		SearchMerger searchMerger = this.createMerger();
		searchMerger.setAllArticleList(allSearchesArticleList);
		ArticleList siteArticleList = searchMerger.execute();
		
		return siteArticleList;
	}
	
	public abstract SearchSplitter createSplitter();
	public abstract SearchEngine createEngine();
	public abstract SearchMerger createMerger();
	
	public ParseTree getQueryTree() {
		return queryTree;
	}

	public void setQueryTree(ParseTree queryTree) {
		this.queryTree = queryTree;
	}
	
}
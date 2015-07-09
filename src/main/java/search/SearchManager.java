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
		List<String> queryTextList = searchSplitter.execute(this.queryTree);
		
		List<ArticleList> allSearchesArticleList = new ArrayList<ArticleList>();
		for (int i=0; i<queryTextList.size(); i++) {
			String currentQueryText = queryTextList.get(i);
			
			// search
			SearchEngine currentSearchEngine = this.createEngine();
			currentSearchEngine.setQueryText(currentQueryText);
			currentSearchEngine.setOriginalQueryTree(this.queryTree);
			currentSearchEngine.setSearchIndex(1+i); // start at 1
			currentSearchEngine.setTotalSearches(queryTextList.size());
			ArticleList currentSearchArticleList = currentSearchEngine.execute();
			
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

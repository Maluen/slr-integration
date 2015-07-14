package search;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import data.ArticleList;

public abstract class SearchManager {

	protected ParseTree queryTree;
	
	protected Integer startYear = null; // optional
	protected Integer endYear = null; // optional
	
	protected Boolean fastOutput = false;

	protected List<Integer> outputCountList; // unfiltered counts from all searches on this specific site
	protected ArticleList outputArticleList; // final articles from all searches on this specific site
	
	public void execute() {
		if (this.fastOutput) this.fastSearch();
		else this.fullSearch();
	}
	
	public void fastSearch() {
		// adapt
		SearchAdapter searchAdapter = this.createAdapter();
		List<String> queryTextList = searchAdapter.execute(this.queryTree);
		
		List<Integer> allSearchesCount = new ArrayList<Integer>();
		for (int i=0; i<queryTextList.size(); i++) {
			String currentQueryText = queryTextList.get(i);
			
			// search
			SearchEngine currentSearchEngine = this.createEngine();
			this.configureSearchEngine(currentSearchEngine, currentQueryText, i, queryTextList.size());
			currentSearchEngine.execute();
			
			Integer currentSearchCount = currentSearchEngine.getOutputCount();
			allSearchesCount.add(currentSearchCount);
		}
		
		this.setOutputCountList(allSearchesCount);
	}
	
	public void fullSearch() {
		// adapt
		SearchAdapter searchAdapter = this.createAdapter();
		List<String> queryTextList = searchAdapter.execute(this.queryTree);
		
		// search
		List<ArticleList> allSearchesArticleList = new ArrayList<ArticleList>();
		List<Integer> allSearchesCount = new ArrayList<Integer>();
		for (int i=0; i<queryTextList.size(); i++) {
			String currentQueryText = queryTextList.get(i);
			
			SearchEngine currentSearchEngine = this.createEngine();
			this.configureSearchEngine(currentSearchEngine, currentQueryText, i, queryTextList.size());
			currentSearchEngine.execute();
			
			ArticleList currentSearchArticleList = currentSearchEngine.getOutputArticleList();
			allSearchesArticleList.add(currentSearchArticleList);
			
			Integer currentSearchCount = currentSearchEngine.getOutputCount();
			allSearchesCount.add(currentSearchCount);
		}
		
		// merge
		SearchMerger searchMerger = this.createMerger();
		searchMerger.setAllArticleList(allSearchesArticleList);
		ArticleList siteArticleList = searchMerger.execute();
		
		this.setOutputArticleList(siteArticleList);
		this.setOutputCountList(allSearchesCount);
	}
	
	public abstract SearchAdapter createAdapter();
	public abstract SearchEngine createEngine();
	public abstract SearchMerger createMerger();
	
	protected void configureSearchEngine(SearchEngine engine, String queryText, Integer searchIndex, Integer totalSearches) {
		engine.setQueryText(queryText);
		engine.setOriginalQueryTree(this.queryTree);
		engine.setSearchIndex(1+searchIndex); // start at 1
		engine.setTotalSearches(totalSearches);
		engine.setStartYear(this.startYear);
		engine.setEndYear(this.endYear);
		engine.setFastOutput(this.fastOutput);
	}
	
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

	public Boolean isFastOutput() {
		return this.fastOutput;
	}

	public void setFastOutput(Boolean fastOutput) {
		this.fastOutput = fastOutput;
	}

	public List<Integer> getOutputCountList() {
		return this.outputCountList;
	}

	public void setOutputCountList(List<Integer> outputCountList) {
		this.outputCountList = outputCountList;
	}	
	
	public ArticleList getOutputArticleList() {
		return this.outputArticleList;
	}

	public void setOutputArticleList(ArticleList outputArticleList) {
		this.outputArticleList = outputArticleList;
	}
	
}

package search.ieee;

import search.SearchEngine;
import search.SearchManager;
import search.SearchMerger;
import search.SearchSplitter;

public class IEEESearchManager extends SearchManager {

	@Override
	public SearchSplitter createSplitter() {
		return new IEEESearchSplitter();
	}

	@Override
	public SearchEngine createEngine() {
		return new IEEESearchEngineAPI();
	}

	@Override
	public SearchMerger createMerger() {
		return new IEEESearchMerger();
	}
	
}

package search.ieee;

import search.SearchEngine;
import search.SearchManager;
import search.SearchMerger;
import search.SearchAdapter;

public class IEEESearchManager extends SearchManager {

	@Override
	public SearchAdapter createAdapter() {
		return new IEEESearchAdapter();
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

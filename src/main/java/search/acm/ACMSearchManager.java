package search.acm;

import search.SearchEngine;
import search.SearchManager;
import search.SearchMerger;
import search.SearchAdapter;

public class ACMSearchManager extends SearchManager {

	@Override
	public SearchAdapter createAdapter() {
		return new ACMSearchAdapter();
	}

	@Override
	public SearchEngine createEngine() {
		return new ACMSearchEngine();
	}

	@Override
	public SearchMerger createMerger() {
		return new ACMSearchMerger();
	}

}

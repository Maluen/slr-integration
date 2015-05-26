package search.acm;

import search.SearchEngine;
import search.SearchManager;
import search.SearchMerger;
import search.SearchSplitter;

public class ACMSearchManager extends SearchManager {

	@Override
	public SearchSplitter createSplitter() {
		return new ACMSearchSplitter();
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

package search.scidirect;

import search.SearchAdapter;
import search.SearchEngine;
import search.SearchManager;
import search.SearchMerger;

public class ScidirectSearchManager extends SearchManager {

	@Override
	public SearchAdapter createAdapter() {
		return new ScidirectSearchAdapter();
	}

	@Override
	public SearchEngine createEngine() {
		return new ScidirectSearchEngine();
	}

	@Override
	public SearchMerger createMerger() {
		return new ScidirectSearchMerger();
	}
	
}

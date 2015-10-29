package search.springer;

import search.SearchAdapter;
import search.SearchEngine;
import search.SearchManager;
import search.SearchMerger;

public class SpringerSearchManager  extends SearchManager {

	@Override
	public SearchAdapter createAdapter() {
		return new SpringerSearchAdapter();
	}

	@Override
	public SearchEngine createEngine() {
		return new SpringerSearchEngine();
	}

	@Override
	public SearchMerger createMerger() {
		return new SpringerSearchMerger();
	}

}

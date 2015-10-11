package search.scidirect;

import java.util.ArrayList;
import java.util.List;

import search.SearchEngine;
import services.resources.Resource;

public class ScidirectSearchEngine extends SearchEngine {

	public ScidirectSearchEngine() {
		super("scidirect");
		
		this.numberOfResultsPerPage = 25;
	}

	@Override
	protected Resource extractSearchResult(Integer pageNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<Resource> extractNeededArticleDetails(List<String> articleIdList) {
		// no details are needed
		return new ArrayList<Resource>();
	}
	
	@Override
	protected Resource extractArticleDetails(String articleId) {
		// details are neither used or needed
		throw new UnsupportedOperationException();
	}

}

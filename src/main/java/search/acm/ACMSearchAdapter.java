package search.acm;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import search.SearchAdapter;

public class ACMSearchAdapter extends SearchAdapter {
	
	public List<String> execute(ParseTree queryTree) {
		
		// optimization: only search in the fields we are interested in
		String queryText = queryTree.getText();
		queryText = "Title:("+queryText+") OR Abstract:("+queryText+") OR Keywords:("+queryText+")";
		
		// splitting isn't required
		List<String> queryTextList = new ArrayList<String>();
		queryTextList.add(queryText);
		return queryTextList;
	}
	
}

package search.acm;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import query.ACMQueryOptimizerVisitor;
import search.SearchAdapter;

public class ACMSearchAdapter extends SearchAdapter {
	
	@Override
	public List<String> execute(ParseTree queryTree, Integer startYear, Integer endYear) {
		
		// optimization: only search in the fields we are interested in		
		ACMQueryOptimizerVisitor optimizerVisitor = new ACMQueryOptimizerVisitor();
		String optimizedQueryText = optimizerVisitor.visit(queryTree);
		
		// splitting isn't required
		List<String> queryTextList = new ArrayList<String>();
		queryTextList.add(optimizedQueryText);
		return queryTextList;
	}
	
}

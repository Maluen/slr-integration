package search.scidirect;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import query.ScidirectQueryOptimizerVisitor;
import search.SearchAdapter;

public class ScidirectSearchAdapter extends SearchAdapter {

	@Override
	public List<String> execute(ParseTree queryTree) {
		
		// optimization: only search in the fields we are interested in		
		ScidirectQueryOptimizerVisitor optimizerVisitor = new ScidirectQueryOptimizerVisitor();
		String optimizedQueryText = optimizerVisitor.visit(queryTree);
		
		System.out.println(optimizedQueryText);
		
		// splitting isn't required
		List<String> queryTextList = new ArrayList<String>();
		queryTextList.add(optimizedQueryText);
		
		// adapt operators
		List<String> newQueryTextList = new ArrayList<String>();
		for (String queryText : queryTextList) {
			String newQueryText = queryText;
			
			// NOT => AND NOT
			newQueryText = newQueryText.replaceAll("NOT", "AND NOT");
			// "phrase" => {phrase}
			newQueryText = newQueryText.replaceAll("\"([\\s\\S]*?)\"", "{$1}");
			
			newQueryTextList.add(newQueryText);
		}
		return newQueryTextList;
	}
	
}

package search.scidirect;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import search.SearchAdapter;

public class ScidirectSearchAdapter extends SearchAdapter {

	@Override
	public List<String> execute(ParseTree queryTree) {
		List<String> queryTextList = super.execute(queryTree);
		
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

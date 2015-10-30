package search.ieee;

import java.util.List;

import misc.Utils;

import org.antlr.v4.runtime.tree.ParseTree;

import query.QuerySplitterVisitor;
import search.SearchAdapter;

public class IEEESearchAdapter extends SearchAdapter {

	protected Integer MAX_WILDCARD = 2; // API (Scrape has 5 limit instead)	
	protected Integer MAX_WORDS = 13;
	
	@Override
	public List<String> execute(ParseTree queryTree, Integer startYear, Integer endYear) {
		
		List<String> wildcardQueryTextList = this.splitForTarget(
				Utils.createList(queryTree.getText()),
				QuerySplitterVisitor.TargetType.WILDCARD, 
				this.MAX_WILDCARD
		);
		
		List<String> wordQueryPartTextList = this.splitForTarget(
				wildcardQueryTextList,
				QuerySplitterVisitor.TargetType.WORD,
				this.MAX_WORDS
		);
				
		return wordQueryPartTextList;
	}

}

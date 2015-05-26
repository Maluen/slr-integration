package search;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public abstract class SearchSplitter {

	protected ParseTree queryTree;

	// TODO: convert generic search input into engine-specific search input(s)
	public List<String> execute() {
		// default implementation splits nothing
		List<String> queryTextList = new ArrayList<String>();
		queryTextList.add(this.queryTree.getText());
		return queryTextList;
	}
	
	public ParseTree getQueryTree() {
		return queryTree;
	}

	public void setQueryTree(ParseTree queryTree) {
		this.queryTree = queryTree;
	}
	
}

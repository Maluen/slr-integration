package search;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import parsers.query.QueryParser;
import query.QuerySplittedPart;
import query.QuerySplittedPartList;
import query.QuerySplitterVisitor;

public abstract class SearchAdapter {

	protected QueryParser queryParser;
	
	public SearchAdapter() {
		this.queryParser = new QueryParser();
	}
	
	// TODO: convert generic search input into engine-specific search input(s)
	public List<String> execute(ParseTree queryTree, Integer startYear, Integer endYear) {
		// default implementation splits nothing
		List<String> queryTextList = new ArrayList<String>();
		queryTextList.add(queryTree.getText());
		return queryTextList;
	}
	
	public List<String> splitForTarget(List<String> queryTextList, QuerySplitterVisitor.TargetType target, Integer targetMaxCount) {
		List<String> result = new ArrayList<String>();
		
		for (String queryText : queryTextList) {	
			ParseTree queryTree = this.queryParser.parse(queryText);
			
			QuerySplitterVisitor splitterVisitor = new QuerySplitterVisitor();
			splitterVisitor.setTarget(target);
			splitterVisitor.setTargetMaxCount(targetMaxCount);
			QuerySplittedPartList splittedQuery = splitterVisitor.visit(queryTree);

			List<String> queryTextPartList = new ArrayList<String>();
			for (QuerySplittedPart splittedQueryPart : splittedQuery) {
				queryTextPartList.add( splittedQueryPart.getQueryText() );
			}
			
			result.addAll(queryTextPartList);
		}

		return result;
	}
	
}

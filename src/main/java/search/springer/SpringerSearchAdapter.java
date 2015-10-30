package search.springer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import search.SearchAdapter;

public class SpringerSearchAdapter extends SearchAdapter {

	Integer yearLowerBound = 1900;
	Integer maxYearConstraintLength = 1200;
	
	@Override
	public List<String> execute(ParseTree queryTree, Integer startYear, Integer endYear) {
		
		String queryText = queryTree.getText();
		
		if (startYear != null || endYear != null) {
			// add year range constraint (if possible)
			
			if (startYear == null) startYear = this.yearLowerBound;
			if (endYear == null) endYear = Calendar.getInstance().get(Calendar.YEAR); // current year
		
			String yearRangeConstraint = "";
			for (int i=startYear; i<=endYear; i++) {
				yearRangeConstraint += "year:"+i;
				if (i != endYear) yearRangeConstraint += " OR ";
			}

			String newQueryText;
			if (queryText.isEmpty()) {
				newQueryText = yearRangeConstraint;
			} else {
				newQueryText = "("+queryText+") AND ("+yearRangeConstraint+")";
			}
			if (newQueryText.length() <= this.maxYearConstraintLength) {
				// (we are quite sure that the final url won't be too long => use the year constraint)
				queryText = newQueryText;
			}
		}
		
		// splitting isn't required
		List<String> queryTextList = new ArrayList<String>();
		queryTextList.add(queryText);
		return queryTextList;
	}
	
}

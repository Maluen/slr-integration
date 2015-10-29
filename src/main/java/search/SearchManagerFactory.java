package search;

import search.acm.ACMSearchManager;
import search.ieee.IEEESearchManager;
import search.scidirect.ScidirectSearchManager;
import search.springer.SpringerSearchManager;

public class SearchManagerFactory {
	
	public static SearchManager create(String name) {
		if (name.equals("acm")) {
			return new ACMSearchManager();
		} else if (name.equals("ieee")) {
			return new IEEESearchManager();
		} else if (name.equals("scidirect")) {
			return new ScidirectSearchManager();
		} else if (name.equals("springer")) {
			return new SpringerSearchManager();
		}
		
		return null;
	}
	
}

package search;

import search.acm.ACMSearchManager;
import search.ieee.IEEESearchManager;

public class SearchManagerFactory {
	
	public static SearchManager create(String name) {
		if (name.equals("acm")) {
			return new ACMSearchManager();
		} else if (name.equals("ieee")) {
			return new IEEESearchManager();
		}
		
		return null;
	}
	
}

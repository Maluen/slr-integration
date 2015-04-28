package misc;

public class Logger {
	
	protected static boolean isEnabled;
	protected static String[] logCategories;
	
	protected String category;
	
	public static boolean isEnabled() {
		return isEnabled;
	}

	public static void setEnabled(boolean isEnabled) {
		Logger.isEnabled = isEnabled;
	}

	public static String[] getLogCategories() {
		return logCategories;
	}

	public static void setLogCategories(String[] logCategories) {
		Logger.logCategories = logCategories;
	}
	
	public static boolean isLogCategory(String category) {
		for (int i=0; i<Logger.logCategories.length; i++) {
			if (Logger.logCategories[i].equals(category)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Logger() {
		
	}
	
	public Logger(String category) {
		this.category = category;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void log(String message, String category, int tabAmount) {
		if (Logger.isEnabled && Logger.isLogCategory(category)) {
			System.out.println(message);
		}
	}
	
}

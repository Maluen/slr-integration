package misc;

import java.io.PrintStream;

public class Logger {
	
	public static enum Level {
		NORMAL,
		ERROR
	};
	
	protected static boolean isEnabled = true;
	protected static String[] logCategories;
	
	protected String category = "";
	protected PrintStream printStream;
	
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
		// default print stream
		this.setPrintStream(System.out);
	}
	
	public Logger(String category) {
		this();
		this.category = category;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public PrintStream getPrintStream() {
		return this.printStream;
	}

	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	public void log(String message, Logger.Level level) {
		if (Logger.isEnabled && Logger.isLogCategory(this.category)) {
			
			this.printStream.println(message);
			if (level == Logger.Level.ERROR && System.err != this.printStream && System.out != this.printStream) {
				// also log to stderr (if isn't duplicate logging)
				System.err.println(message);
			}
			
		}
	}
	
	public void log(String message) {
		this.log(message, Logger.Level.NORMAL);
	}
	
}

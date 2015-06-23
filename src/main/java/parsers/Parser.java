package parsers;

import misc.Logger;

public abstract class Parser {

	protected Logger logger;
	
	public Parser() {
		this.logger = new Logger("Parser");
	}
	
	public abstract Object parse(String content);
	
}

package converters;

import org.w3c.dom.Document;

import services.Data;

public abstract class DocumentToText extends Converter {

	public static String getToContentType() {
		// DUMMY (static methods can't be abstract)
		return null;
	}
	
	public abstract String convert(Document template, Data<String> data);
	
}

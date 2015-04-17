package converters;

import org.w3c.dom.Document;

import services.Data;


public class DocumentToJSON extends DocumentToText {

	public static String getToContentType() {
		return "text/json";
	}
	
	@Override
	public String convert(Document template, Data<String> data) {
		// TODO Auto-generated method stub
		return null;
	}


}

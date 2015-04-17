package converters;

import org.w3c.dom.Document;

import parsers.json.JSONParser;

public class JSONtoDocument extends TextToDocument {

	protected JSONParser jsonParser;
	
	public JSONtoDocument() {
		this.jsonParser = new JSONParser();
	}
	
	public static String getFromContentType() {
		return "text/json";
	}

	@Override
	public Document convert(String content, Document template) {
		// TODO Auto-generated method stub
		return null;
	}

}

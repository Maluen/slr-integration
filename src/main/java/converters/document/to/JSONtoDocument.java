package converters.document.to;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.json.JSONParser;

public class JSONtoDocument extends ToDocument {

	protected JSONParser jsonParser;
	
	public JSONtoDocument() {		
		this.jsonParser = new JSONParser();
	}
	
	public String getFromContentType() {
		return "text/json";
	}

	@Override
	public Document convert() {
		// STUB
		Document document = this.docBuilder.newDocument();
		Element element = document.createElement("stub");
		document.appendChild(element);
		return document;
	}

}

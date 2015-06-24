package converters.document.to;

import javax.script.ScriptEngine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.json.JSONParser;
import services.Data;

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
		this.logger.log("\n");
		return document;
	}
	
	@Override
	public Element process(Element templateElement, ScriptEngine engine,
			Data<String> data) throws UnsupportedOperationException, Exception {
		throw new UnsupportedOperationException();
	}

}

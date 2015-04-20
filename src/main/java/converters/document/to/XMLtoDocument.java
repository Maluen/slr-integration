package converters.document.to;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;

public class XMLtoDocument extends TextToDocument {

	protected XMLParser xmlParser;
	
	public XMLtoDocument() {		
		this.xmlParser = new XMLParser();
	}
	
	public String getFromContentType() {
		return "text/xml";
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

package converters;

import org.w3c.dom.Document;

import parsers.xml.XMLParser;

public class XMLtoDocument extends TextToDocument {

	protected XMLParser xmlParser;
	
	public XMLtoDocument() {
		this.xmlParser = new XMLParser();
	}
	
	public static String getFromContentType() {
		return "text/xml";
	}
	
	@Override
	public Document convert(String content, Document template) {
		// TODO Auto-generated method stub
		return null;
	}

}

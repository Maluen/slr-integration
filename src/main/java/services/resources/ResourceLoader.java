package services.resources;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import parsers.xml.DocumentFactory;
import parsers.xml.XMLParser;

public class ResourceLoader {

	public Object loadXMLContent(Element contentEl) {
		Element contentRootEl = XMLParser.getChildElements(contentEl).get(0); // first child element		
		Document contentDocument = XMLParser.createDocumentFromElement(contentRootEl);
		return contentDocument;
	}
	
	public Object loadDefaultContent(Element contentEl) {
		// get text content by default
		return contentEl.getTextContent().trim();
	}
	
	public Object loadContent(String contentType, Element contentEl) {
		if (contentType.equals("text/xml")) {
			return this.loadXMLContent(contentEl);
		}
		
		return this.loadDefaultContent(contentEl);
	}
	
	public Resource load(File resourceFile) throws SAXException, IOException {
		Document document = DocumentFactory.getDocBuilder().parse(resourceFile);
		
		Element documentRootEl = document.getDocumentElement();
		
		Element nameEl = XMLParser.getChildElementByTagName(documentRootEl, "name");
		Element contentTypeEl = XMLParser.getChildElementByTagName(documentRootEl, "contentType");
		Element contentEl = XMLParser.getChildElementByTagName(documentRootEl, "content");
		
		String name = nameEl.getTextContent().trim();
		String contentType = contentTypeEl.getTextContent().trim();
		Object content = this.loadContent(contentType, contentEl);
		
		Resource resource = new Resource();
		resource.setName(name);
		resource.setContentType(contentType);
		resource.setContent(content);
		return resource;
	}
	
}

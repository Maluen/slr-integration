package services.resources;

import misc.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.DocumentFactory;

public class ResourceSerializer {

	protected Element serializeXMLContent(Document content, Document saveDocument, Element saveDocumentContentEl) {		
		Element contentDocumentRootEl = (Element) content.getDocumentElement();
		contentDocumentRootEl = (Element) saveDocument.importNode(contentDocumentRootEl, true);

		saveDocumentContentEl.appendChild(contentDocumentRootEl);
		return saveDocumentContentEl;
	}
	
	protected Element serializeDefaultContent(Object content, Document saveDocument, Element saveDocumentContentEl) {		
		// append content to string by default
		saveDocumentContentEl.setTextContent(content.toString());
		return saveDocumentContentEl;
	}
	
	protected Element serializeContent(Object content, Document saveDocument, Element saveDocumentContentEl) {
		if (content instanceof Document) {
			return this.serializeXMLContent( (Document) content, saveDocument, saveDocumentContentEl);
		}

		return this.serializeDefaultContent(content, saveDocument, saveDocumentContentEl);
	}
	
	public void serialize(Resource resource, String filename) throws Exception {

		String name = resource.getName();
		String contentType = resource.getContentType();
		Object content = resource.getContent();
		
		Document document = DocumentFactory.getDocBuilder().newDocument();

		// root element
		Element documentRootEl = document.createElement("resource");
		document.appendChild(documentRootEl);
		
		Element nameEl = document.createElement("name");
		nameEl.setTextContent(name);
		documentRootEl.appendChild(nameEl);
		
		Element contentTypeEl = document.createElement("contentType");
		contentTypeEl.setTextContent(contentType);
		documentRootEl.appendChild(contentTypeEl);
		
		Element contentEl = document.createElement("content");
		contentEl = this.serializeContent(content, document, contentEl);
		documentRootEl.appendChild(contentEl);
		
		Utils.saveDocument(document, filename);
	}
	
}

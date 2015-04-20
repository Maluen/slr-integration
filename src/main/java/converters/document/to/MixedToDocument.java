package converters.document.to;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import converters.document.DocumentConverterFactory;

public class MixedToDocument extends ToDocument {

	protected XMLParser xmlParser;
	protected Map<String, Object> contentMap; // <content type, content>
	protected Object defaultContent;
	
	public MixedToDocument() {
		this.xmlParser = new XMLParser();

		this.contentMap = new HashMap<String, Object>();
	}
	
	@Override
	public String getFromContentType() {
		return "multipart/mixed";
	}
	
	public Map<String, Object> getContentMap() {
		return this.contentMap;
	}
	
	public Map<String, Object> getContent() {
		return this.getContentMap();
	}
	
	public void setContent(Object content) {
		this.setContentMap( (Map<String, Object>) contentMap);
	}

	public void setContentMap(Map<String, Object> contentMap) {
		this.contentMap = contentMap;
	}

	public void addContent(String contentType, Object content) {
		this.contentMap.put(contentType, content);
	}
	
	public Object getDefaultContent() {
		return this.defaultContent;
	}

	public void setDefaultContent(Object defaultContent) {
		this.defaultContent = defaultContent;
	}

	@Override
	public Document convert() throws Exception {
		
		// starting point
		Element templateRootElement = this.template.getDocumentElement();
		
		// create output document
		Document doc = this.docBuilder.newDocument();
		this.setDocument(doc);
		
		// process!
		Element docRootElement = this.process(templateRootElement);
		doc.appendChild(docRootElement);
		
		return doc;
	}
	
	public Element process(Element templateElement) throws Exception {

		String contentType = templateElement.getAttribute("contentType");
		if (contentType.isEmpty()) {
			// just proceed further with children
			
			List<Element> childElementList = XMLParser.getChildElements(templateElement);
			if (childElementList.isEmpty()) {
				// just return the element (to keep element content like text etc.)
				Element docElement = (Element) this.document.importNode(templateElement, true);
				return docElement;
			}
			Element docElement = this.document.createElement(templateElement.getTagName());			
			for (Element childElement : childElementList) {
				Element docElementChild = this.process(childElement);
				docElement.appendChild(docElementChild);
			}
			return docElement;
		}
		
		// a converter wasn't able to handle this templateElement,
		// find the suitable converter
		
		Object content = this.contentMap.get(contentType);
		if (content == null) {
			content = this.defaultContent;
		}

		Document template = this.xmlParser.createDocumentFromElement(templateElement);
		
		ToDocument converter = DocumentConverterFactory.createToDocument(contentType);
		converter.setParent(this);
		converter.setContent(content);
		converter.setTemplate(template);
		Document converterDocument = converter.convert();
		
		Element docElement = (Element) converterDocument.getDocumentElement();
		docElement = (Element) this.document.importNode(docElement, true);
		
		return docElement;
	}
	
	/*
	public TextToDocument findConverterByContentType(String contentType) {		
		for (TextToDocument converterCandidate : this.converterContents.keySet()) {
			if (converterCandidate.getFromContentType().equals(contentType)) {
				return converterCandidate;
			}
		}
		
		return null;
	}*/
	

}

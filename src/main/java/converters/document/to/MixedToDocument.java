package converters.document.to;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import services.Resource;
import services.Template;
import converters.document.DocumentConverterFactory;

public class MixedToDocument extends ToDocument {

	protected XMLParser xmlParser;
	protected Map<String, Resource> resourceMap; // key: resource name
	protected String defaultResourceName;
	
	public MixedToDocument() {
		this.xmlParser = new XMLParser();

		this.resourceMap = new HashMap<String, Resource>();
	}
	
	@Override
	public String getFromContentType() {
		return "multipart/mixed";
	}
	
	public Map<String, Resource> getResourceMap() {
		return this.resourceMap;
	}

	public void setResourceMap(Map<String, Resource> resourceMap) {
		this.resourceMap = resourceMap;
	}

	public void addResource(String resourceName, Resource resource) {
		this.resourceMap.put(resourceName, resource);
	}

	public String getDefaultResourceName() {
		return this.defaultResourceName;
	}

	public void setDefaultResourceName(String defaultResourceName) {
		this.defaultResourceName = defaultResourceName;
	}

	@Override
	public Document convert() throws Exception {
		
		// create output document
		Document document = this.docBuilder.newDocument();
		this.setDocument(document);
		
		// starting point
		Element templateRootEl = this.template.getDocumentElement();
		
		// process!
		Element documentRootEl = this.process(templateRootEl);
		document.appendChild(documentRootEl);
		
		return document;
	}
	
	public Element process(Element templateEl) throws Exception {
		
        // create a JavaScript engine
        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
        engine = this.configureScriptEngine(engine, null);
		
		String resourceName = (String) Template.getProperty(templateEl, "from", engine);
		if (resourceName == null) resourceName = "";
		
		if (resourceName.isEmpty()) {
			// use default name
			resourceName = this.getDefaultResourceName();
		}
		
		// a converter wasn't able to handle this templateElement,
		// find the suitable one
		
		Resource resource = this.resourceMap.get(resourceName);
		String contentType = resource.getContentType();
		
		Document template = this.xmlParser.createDocumentFromElement(templateEl);
		
		ToDocument converter = DocumentConverterFactory.createToDocument(contentType);
		converter.setParent(this);
		converter.setResource(resource);
		converter.setTemplate(template);
		Document converterDocument = converter.convert();
		
		Element documentEl = (Element) converterDocument.getDocumentElement();
		documentEl = (Element) this.document.importNode(documentEl, true);
		
		return documentEl;
	}	

}

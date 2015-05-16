package converters.document.to;

import javax.script.ScriptEngine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import services.Data;
import services.Template;
import services.resources.Resource;
import services.resources.ResourceList;
import converters.document.DocumentConverterFactory;

public class MixedToDocument extends ToDocument {

	protected XMLParser xmlParser;
	private ResourceList resourceList;
	protected String defaultResourceName = "";
	
	public MixedToDocument() {
		this.xmlParser = new XMLParser();

		this.resourceList = new ResourceList();
	}
	
	@Override
	public String getFromContentType() {
		return "multipart/mixed";
	}
	
	public ResourceList getResourceList() {
		return this.resourceList;
	}
	
	public void setResourceList(ResourceList resourceList) {
		this.resourceList = resourceList;
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
		
        // create a JavaScript engine
        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
        engine = this.configureScriptEngine(engine, null);
		
		// process!
		Element documentRootEl = this.process(templateRootEl, engine, this.data);
		document.appendChild(documentRootEl);
		
		return document;
	}
	
	@Override
	public Element process(Element templateEl, ScriptEngine engine, Data<String> data) throws Exception {

        // update data
		// Note: if the converter was called from another converter that failed,
		// then its engine is used so to get the same data
        data = Template.getData(templateEl, engine, data);
		
		String resourceName = (String) Template.getProperty(templateEl, "from", engine, data);
		if (resourceName == null) resourceName = "";
		
		if (resourceName.isEmpty()) {
			// use default name
			resourceName = this.getDefaultResourceName();
		}
		
		// a converter wasn't able to handle this templateElement,
		// find the suitable one
		
		Resource resource = this.resourceList.getByName(resourceName);
		if (resource == null) {
			// error: unknown resource
			throw new Exception();
		}
		
		String contentType = resource.getContentType();
		
		Document template = XMLParser.createDocumentFromElement(templateEl);
		
		ToDocument converter = DocumentConverterFactory.createToDocument(contentType);
		converter.setParent(this);
		converter.setEngineBaseScope(this.engineBaseScope);
		converter.setResource(resource);
		converter.setTemplate(template);
		converter.setData(data);
		Document converterDocument = converter.convert();
		
		Element documentEl = (Element) converterDocument.getDocumentElement();
		documentEl = (Element) this.document.importNode(documentEl, true);
		
		return documentEl;
	}	

}

package converters.document.to;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import services.Data;
import services.Template;
import services.resources.Resource;
import converters.document.DocumentConverter;

public abstract class ToDocument extends DocumentConverter {

	protected ScriptEngineManager scriptFactory;
	protected DocumentBuilderFactory docFactory;
	protected DocumentBuilder docBuilder;
		
	// optional
	protected ToDocument parent;
	
	protected Map<String, Object> engineBaseScope;
	
	protected Resource resource;
	protected Document template;
	protected Data<String> data;
	
	public ToDocument() {
	    // create a script engine manager
        this.scriptFactory = new ScriptEngineManager();
		
		this.docFactory = DocumentBuilderFactory.newInstance();
		try {
			this.docBuilder = this.docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.engineBaseScope = new HashMap<String, Object>();
		
		this.data = new Data<String>();
	}
	
	public abstract String getFromContentType();
	
	public ToDocument getParent() {
		return this.parent;
	}
	
	public void setParent(ToDocument parent) {
		this.parent = parent;
	}
	
	public Map<String, Object> getEngineBaseScope() {
		return this.engineBaseScope;
	}

	public void setEngineBaseScope(Map<String, Object> engineBaseScope) {
		this.engineBaseScope = engineBaseScope;
	}

	public Resource getResource() {
		return this.resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public Document getTemplate() {
		return this.template;
	}

	public void setTemplate(Document template) {
		this.template = template;
	}
	
	public Data<String> getData() {
		return this.data;
	}

	public void setData(Data<String> data) {
		this.data = data;
	}

	public abstract Document convert() throws Exception;
	
	public abstract Element process(Element templateElement, ScriptEngine engine, Data<String> data) throws Exception;
	
	protected Element processIfUnsupported(Element templateEl, ScriptEngine engine, Data<String> data) throws Exception {
		
		String resourceName = (String) Template.getProperty(templateEl, "from", engine, data);
		if (resourceName == null) resourceName = "";
		
		if (!resourceName.isEmpty() && !resourceName.equals(this.resource.getName())) {
			// unsupported element
			Element documentEl = this.processUnsupported(templateEl, engine, data);
			documentEl = (Element) this.document.importNode(documentEl, true);
			return documentEl;
		}
		
		return null;
	}
	
	protected Element processUnsupported(Element templateElement, ScriptEngine engine, Data<String> data) throws Exception {
		if (this.parent != null) {
			// try with parent
			return this.parent.process(templateElement, engine, data);
		}
		
		// TODO: add better exception
		throw new Exception("Unsupported template element");
	}

	protected ScriptEngine configureScriptEngine(ScriptEngine engine, Object contentEl) {
		
		// put all base scope
		for (Map.Entry<String, Object> entry : this.engineBaseScope.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			engine.put(key, value);
		}
		
		// extend with parameter
		engine.put("el", contentEl);
		return engine;
	}
	
	protected List<Object> filterContentElements(List<Object> contentElList, Element templateEl, Data<String> data) throws Exception {
		
		List<Object> filteredContentElList = new ArrayList<Object>();
		
		if (!Template.hasProperty(templateEl, "condition")) {
			// nothing to do
			return contentElList;
		}
		
		for (Object contentEl : contentElList) {
			// create a JavaScript engine with the current element
			ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
			engine = this.configureScriptEngine(engine, contentEl);

			Object property = Template.getProperty(templateEl, "condition", engine, data);
			Boolean passes;
			
			if (property == null) {
				// error: skip element
				passes = false;
			} else if (property instanceof String) {
				
				String passesString = (String) property;
				if (passesString.isEmpty()) {
					// no condition
					passes = true;
				} else {
					passes = Boolean.parseBoolean( (String) property);
				}
				
			} else if (property instanceof Boolean) {
				passes = (Boolean) property;
			} else {
				// unsupported value
				return null;
			}
			
			if (passes) {
				filteredContentElList.add(contentEl);
			}
		}
		
		return filteredContentElList;
	}

}

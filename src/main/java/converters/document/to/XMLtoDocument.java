package converters.document.to;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import services.Data;
import services.Template;

public class XMLtoDocument extends ToDocument {

	protected XMLParser xmlParser;
	
	public XMLtoDocument() {		
		this.xmlParser = new XMLParser();
	}
	
	public String getFromContentType() {
		return "text/xml";
	}
	
	protected Document parseContent(Object content) {
		if (content instanceof String) {
			// parse content
			return this.xmlParser.parse( (String) content );
		} else if (content instanceof Document) {
			return (Document) content;
		}
		
		return null;
	}
	
	@Override
	public Document convert() throws Exception {
		
		// create output document
		Document document = this.docBuilder.newDocument();
		this.setDocument(document);
		
		// parse content
		Object content = this.resource.getContent();
		Document parsedContent = this.parseContent(content);
		
		// starting point
		Element contentRootEl = parsedContent.getDocumentElement();
		Element templateRootEl = this.template.getDocumentElement();
		
		// process!
		Element documentRootEl = this.process(templateRootEl, contentRootEl, this.data);
		document.appendChild(documentRootEl);
		
		return document;
	}
	
	// retrieve base elements pool
	protected List<Object> findContentElements(Element templateEl, Element fromContentEl, ScriptEngine engine, Data<String> data) {
		List<Element> contentElList;
		
		Object el = Template.getProperty(templateEl, "el", engine, data);
		
		if (!Template.hasProperty(templateEl, "el")) {
			// defaults to parent element
			contentElList = new ArrayList<Element>();
			contentElList.add(fromContentEl);
			
		} else if (el == null) {
			// error: no element
			contentElList = new ArrayList<Element>();
			
		} else if (el instanceof Element) {
			// element returned directly
			contentElList = new ArrayList<Element>();
			contentElList.add( (Element) el);
			
		} else if (el instanceof List) {
			// assumes is List<Element>
			// element collection returned directly
			contentElList = (List<Element>) el;
			
		} else if (el instanceof String) {
			// el is the xpath selector string
			String selector = (String) el;
			
			if (selector.isEmpty()) {
				// defaults to parent element
				contentElList = new ArrayList<Element>();
				contentElList.add(fromContentEl);
			} else {
				try {
					contentElList = this.xmlParser.select(selector, fromContentEl);
				} catch (XPathExpressionException e) {
					// selector error
					e.printStackTrace();
					contentElList = null;
				}
			}

		} else {
			// unsupported value
			return null;
		}
		
		return (List<Object>)(List<?>) contentElList;
	}

	public Element process(Element templateEl, Element fromContentEl, Data<String> data) throws Exception {
		
        // create a JavaScript engine
        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
        engine = this.configureScriptEngine(engine, fromContentEl);
		
        // update data
        data = Template.getData(templateEl, engine, data);
		
		Element documentEl;
		documentEl = this.processIfUnsupported(templateEl, engine, data);
		if (documentEl != null) return documentEl;
		documentEl = this.document.createElement(templateEl.getTagName());

		// retrieve base elements pool
		List<Object> contentElList = this.findContentElements(templateEl, fromContentEl, engine, data);
		
		// filter base elements pool if needed		
		try {
			contentElList = this.filterContentElements(contentElList, templateEl, data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// This exception (class to refactor) means that the template has incorrect schema
			// => return
			e.printStackTrace();
			return null;
		}

		if (contentElList.isEmpty()) {
			System.out.println("Empty el for " + templateEl.getTagName());
		}
		
		// get first content element
		Element firstContentEl = null;
		if (contentElList.size() > 0) { // check to prevent exception
			firstContentEl = (Element) contentElList.get(0);
		}
		
        // update JavaScript engine to pass first element
		// Note: might be null
        engine = this.scriptFactory.getEngineByName("JavaScript");
        engine = this.configureScriptEngine(engine, firstContentEl);

		// get mode
		String mode = (String) Template.getProperty(templateEl, "mode", engine, data);
		if (mode == null) mode = "";
		
		if (mode.equals("list")) {

			System.out.println("Processing list " + templateEl.getTagName());
			
			// retrieve list item descriptor
			Element valueListItemEl = Template.getListItemDescriptor(templateEl);
			
			// create one list item for each content element
			for (Object contentEl : contentElList) {
				Element documentListItemEl = this.process(valueListItemEl, (Element) contentEl, data);
				documentEl.appendChild(documentListItemEl);
			}
			
		}  else {

			if (mode.equals("script")) {
				String script = Template.getScriptContent(templateEl, data);

		        // evaluate JavaScript code from String
		        try {
		        	String documentElText = (String) engine.eval(script);
		        	documentEl.setTextContent(documentElText);
					System.out.println(templateEl.getTagName() + ": " + documentElText + " (script)");
				} catch (ScriptException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(templateEl.getTagName() + ": script failed");
				}
				
			} else if (mode.equals("text")) {
				
				String documentElText;
				
				if (firstContentEl == null) {
					// el is strictly required here, leave content blank
					documentElText = "";
				} else {
					documentElText = firstContentEl.getTextContent().trim();
				}
				
				documentEl.setTextContent(documentElText);
				System.out.println(templateEl.getTagName() + ": " + documentElText + " (text)");
				
			} else {
				// nothing, just proceed further with children
				
				System.out.println(templateEl.getTagName());
				
				List<Element> templateElNextLevel = Template.getNextLevel(templateEl);
				for (Element templateElNextLevelChild : templateElNextLevel) {
					Element documentElChild = this.process(templateElNextLevelChild, firstContentEl, data);
					documentEl.appendChild(documentElChild);
				}
			}
		}
		
		return documentEl;
	}
	
	protected ScriptEngine configureScriptEngine(ScriptEngine engine, Object contentEl) {
		engine.put("el", contentEl);
		engine.put("parser", this.xmlParser);
		return engine;
	}
	
	@Override
	public Element process(Element templateElement, ScriptEngine engine,
			Data<String> data) throws UnsupportedOperationException, Exception {
		throw new UnsupportedOperationException();
	}
	
}

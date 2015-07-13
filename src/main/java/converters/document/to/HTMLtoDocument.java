package converters.document.to;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import misc.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.html.HTMLParser;
import services.Data;
import services.Template;

public class HTMLtoDocument extends ToDocument {

	protected HTMLParser htmlParser;

	public HTMLtoDocument() {
		this.htmlParser = new HTMLParser();
	}
	
	public String getFromContentType() {
		return "text/html";
	}
	
	protected org.jsoup.nodes.Document parseContent(Object content) {
		if (content instanceof String) {
			// parse content
			return this.htmlParser.parse( (String) content );
		} else if (content instanceof org.jsoup.nodes.Document) {
			return (org.jsoup.nodes.Document) content;
		}
		
		return null;
	}

	@Override
	public Document convert() throws Exception {
		this.logger.log("Converting " + this.resource.getName());

		// create output document
		Document document = this.docBuilder.newDocument();
		this.setDocument(document);
		
		// parse response
		Object content = this.resource.getContent();
		org.jsoup.nodes.Document parsedContent = this.parseContent(content);
		
		// starting point
		org.jsoup.nodes.Element contentRootEl = parsedContent.getElementsByTag("html").first();
		Element templateRootEl = this.template.getDocumentElement();
		
		// process!
		Element documentRootEl = this.process(templateRootEl, contentRootEl, this.data);
		document.appendChild(documentRootEl);
		
		this.logger.log("\n");
		
		return document;
	}
	
	protected List<Object> toList(org.jsoup.select.Elements elements) {
		Object[] elementArray = elements.toArray();
		List<Object> elementList = new ArrayList<Object>( Arrays.asList(elementArray) );
		return elementList;
	}

	// retrieve base elements pool
	protected List<Object> findContentElements(Element templateEl, org.jsoup.nodes.Element fromContentEl, ScriptEngine engine, Data<String> data) {
		org.jsoup.select.Elements contentElements;
		
		Object el = Template.getProperty(templateEl, "el", engine, data);
		
		if (!Template.hasProperty(templateEl, "el")) {
			// defaults to parent element
			contentElements = new org.jsoup.select.Elements(fromContentEl);
			
		} else if (el == null) {
			// error: no element
			contentElements = new org.jsoup.select.Elements();
			
		} else if (el instanceof org.jsoup.nodes.Element) {
			// element returned directly
			contentElements = new org.jsoup.select.Elements( (org.jsoup.nodes.Element) el );
			
		} else if (el instanceof org.jsoup.select.Elements) {
			// element collection returned directly
			contentElements = (org.jsoup.select.Elements) el;
			
		} else if (el instanceof String) {
			// el is a jsoup selector string
			String selector = (String) el;
			
			if (selector.isEmpty()) {
				// defaults to parent element
				contentElements = new org.jsoup.select.Elements(fromContentEl);
			} else {
				contentElements = fromContentEl.select(selector);
			}
			
		} else {
			// unsupported value
			return null;
		}
		
		return this.toList(contentElements);
	}
	
	public Element process(Element templateEl, org.jsoup.nodes.Element fromContentEl, Data<String> data) throws Exception {
		
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
			this.logger.log("No matched elements for " + templateEl.getTagName());
		}
		
		// use first content element
		org.jsoup.nodes.Element firstContentEl = null;
		if (contentElList.size() > 0) { // check to prevent exception
			firstContentEl = (org.jsoup.nodes.Element) contentElList.get(0);
		}

        // update JavaScript engine to pass first element
		// Note: might be null
        engine = this.scriptFactory.getEngineByName("JavaScript");
        engine = this.configureScriptEngine(engine, firstContentEl);
		
		// get mode
		String mode = (String) Template.getProperty(templateEl, "mode", engine, data);
		if (mode == null) mode = "";
		
		if (mode.equals("list")) {

			this.logger.log("Processing list " + templateEl.getTagName());
			
			// retrieve list item descriptor
			Element valueListItemEl = Template.getListItemDescriptor(templateEl);
			
			// create one list item for each content element
			for (Object contentEl : contentElList) {
				Element documentListItemEl = this.process(valueListItemEl, (org.jsoup.nodes.Element) contentEl, data);
				documentEl.appendChild(documentListItemEl);
			}
			
		} else {

			if (mode.equals("script")) {
				String script = Template.getScriptContent(templateEl, data);

		        // evaluate JavaScript code from String
		        try {
		        	String documentElText = (String) engine.eval(script);
		        	documentEl.setTextContent(documentElText);
		        	this.logger.log(templateEl.getTagName() + ": " + documentElText + " (script)");
				} catch (ScriptException e) {
					this.logger.log(ExceptionUtils.getStackTrace(e));
					this.logger.log(templateEl.getTagName() + ": script failed", Logger.Level.ERROR);
				}
				
			} else if (mode.equals("text")) {
				
				String documentElText;
				
				if (firstContentEl == null) {
					// el is strictly required here, leave content blank
					documentElText = "";
				} else {
					
					String val = firstContentEl.val();
					String content = firstContentEl.text();
					
					if (!val.isEmpty()) {
						// element is a form element (input, textarea, etc.)
						documentElText = val;
					} else {
						// normal element
						documentElText = content.trim();
					}					
					
				}
				
				documentEl.setTextContent(documentElText);
				this.logger.log(templateEl.getTagName() + ": " + documentElText + " (text)");
				
			} else {
				// defaults
				
				this.logger.log(templateEl.getTagName());
				
				List<Element> templateElNextLevel = Template.getNextLevel(templateEl);	
				if (templateElNextLevel.size() > 0) {
					// proceed further with children
					for (Element templateElNextLevelChild : templateElNextLevel) {
						Element documentElChild = this.process(templateElNextLevelChild, firstContentEl, data);
						documentEl.appendChild(documentElChild);
					}
					this.logger.log(templateEl.getTagName() + ": next level");
					
				} else {
					// evaluate value (expected string)
					String documentElText = (String) Template.evaluateValue(templateEl, engine, data);
					documentEl.setTextContent(documentElText);
					this.logger.log(templateEl.getTagName() + ": " + documentElText + " (evaluate value)");
				}
			}
		}
		
		return documentEl;
	}
	
	@Override
	public Element process(Element templateElement, ScriptEngine engine,
			Data<String> data) throws UnsupportedOperationException, Exception {
		throw new UnsupportedOperationException();
	}
	
	protected ScriptEngine configureScriptEngine(ScriptEngine engine, Object contentEl) {
		engine = super.configureScriptEngine(engine, contentEl);
		
		engine.put("parser", this.htmlParser);
		return engine;
	}
	
}

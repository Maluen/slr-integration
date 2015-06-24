package converters.document.to;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import network.http.HTTPCookie;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.cookie.CookieParser;
import services.Data;
import services.Template;

public class SetCookiesToDocument extends ToDocument {

	protected CookieParser cookieParser;
	
	public SetCookiesToDocument() {
		this.cookieParser = new CookieParser();
	}
	
	@Override
	public String getFromContentType() {
		return "application/x-www-response-cookies";
	}
	
	protected List<HTTPCookie> parseContent(Object content) {
		if (content instanceof List<?>) {
			// assumes is List<String> (can't do precise instanceof check)
			// (list of "Set-Cookie" header values)
			// parse content
			List<HTTPCookie> parsedContent = new ArrayList<HTTPCookie>();
			for (String cookieString : (List<String>) content) {
				HTTPCookie httpCookie = this.cookieParser.parse(cookieString);
				parsedContent.add(httpCookie);
			}
			return parsedContent;
		}
		
		return null;
	}

	@Override
	public Document convert() throws Exception {
		
		// create output document
		Document document = this.docBuilder.newDocument();
		this.setDocument(document);
		
		// parse response
		Object content = this.resource.getContent();
		List<HTTPCookie> parsedContent = this.parseContent(content);
		
		// starting point
		Element templateRootEl = this.template.getDocumentElement();
		
		// process!
		Element documentRootEl = this.process(templateRootEl, parsedContent, this.data);
		document.appendChild(documentRootEl);
		
		this.logger.log("\n");
		
		return document;
	}
	
	// retrieve base elements pool
	protected List<Object> findContentElements(Element templateEl, List<HTTPCookie> cookies, ScriptEngine engine, Data<String> data) {
		List<HTTPCookie> contentElList;
		
		Object el = Template.getProperty(templateEl, "el", engine, data);
		
		if (!Template.hasProperty(templateEl, "el")) {
			// defaults to parent element
			contentElList = cookies;
			
		} else if (el == null) {
			// error: no element
			contentElList = new ArrayList<HTTPCookie>();
			
		} else if (el instanceof HTTPCookie) {
			// element returned directly
			contentElList = new ArrayList<HTTPCookie>();
			contentElList.add( (HTTPCookie) el);
			
		} else if (el instanceof List) {
			// assumes is List<HTTPCookie>
			// element collection returned directly
			contentElList = (List<HTTPCookie>) el;
			
		} else if (el instanceof String) {
			// el is the cookie name
			String cookieName = (String) el;
			
			if (cookieName.isEmpty()) {
				// defaults to parent element
				contentElList = new ArrayList<HTTPCookie>();
			} else {
				contentElList = new ArrayList<HTTPCookie>();
				for (HTTPCookie cookie : cookies) {
					if (cookie.getName().equals(cookieName)) {
						contentElList.add(cookie);
					}
				}
			}

		} else {
			// unsupported value
			return null;
		}
		
		return (List<Object>)(List<?>) contentElList;
	}
	
	public Element process(Element templateEl, List<HTTPCookie> cookies, Data<String> data) throws Exception {
		
        // create a JavaScript engine
        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
        engine = this.configureScriptEngine(engine, cookies);
		
        // update data
        data = Template.getData(templateEl, engine, data);
		
		Element documentEl;
		documentEl = this.processIfUnsupported(templateEl, engine, data);
		if (documentEl != null) return documentEl;
		documentEl = this.document.createElement(templateEl.getTagName());

		// retrieve base elements pool
		List<Object> contentElList = this.findContentElements(templateEl, cookies, engine, data);
				
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
		
		// get first content element
		HTTPCookie firstContentEl = null;
		if (contentElList.size() > 0) { // check to prevent exception
			firstContentEl = (HTTPCookie) contentElList.get(0);
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
				List<HTTPCookie> contentElCookies = new ArrayList<HTTPCookie>();
				contentElCookies.add((HTTPCookie) contentEl);
				
				Element documentListItemEl = this.process(valueListItemEl, contentElCookies, data);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
					this.logger.log(templateEl.getTagName() + ": script failed");
				}
				
			} else if (mode.equals("text")) {
				
				String documentElText;
				
				if (firstContentEl == null) {
					// el is strictly required here, leave content blank
					documentElText = "";
				} else {
					// cookie value
					documentElText = firstContentEl.getValue();
				}
				
				documentEl.setTextContent(documentElText);
				this.logger.log(templateEl.getTagName() + ": " + documentElText + " (text)");
				
			} else {
				// defaults
				
				List<Element> templateElNextLevel = Template.getNextLevel(templateEl);
				if (templateElNextLevel.size() > 0) {
					// proceed further with children
					for (Element templateElNextLevelChild : templateElNextLevel) {
						Element documentElChild = this.process(templateElNextLevelChild, cookies, data);
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

}

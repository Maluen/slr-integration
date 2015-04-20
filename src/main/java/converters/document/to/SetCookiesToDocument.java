package converters.document.to;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import network.http.HTTPCookie;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.cookie.CookieParser;
import parsers.xml.XMLParser;

public class SetCookiesToDocument extends ToDocument {

	protected List<String> content; // list of "Set-Cookie" header values
	protected CookieParser cookieParser;
	
	public SetCookiesToDocument() {
		this.cookieParser = new CookieParser();
	}
	
	@Override
	public String getFromContentType() {
		return "application/x-www-response-cookies";
	}
	
	@Override
	public List<String> getContent() {
		return this.content;
	}

	@Override
	public void setContent(Object content) {
		this.content = (List<String>) content;
	}
	
	public void setContent(List<String> content) {
		this.content = content;
	}

	@Override
	public Document convert() throws Exception {
		// starting point
		Element templateRootElement = this.template.getDocumentElement();
		
		// parse response
		List<HTTPCookie> parsedContent = new ArrayList<HTTPCookie>();
		for (String cookieString : this.content) {
			HTTPCookie httpCookie = this.cookieParser.parse(cookieString);
			parsedContent.add(httpCookie);
		}
		
		// create output document
		Document doc = this.docBuilder.newDocument();
		this.setDocument(doc);
		
		// process!
		Element docRootElement = this.process(templateRootElement, parsedContent);
		doc.appendChild(docRootElement);
		
		return doc;
	}
	
	public Element process(Element templateElement, List<HTTPCookie> cookies) throws Exception {
		
		String contentType = templateElement.getAttribute("contentType");
		if (!contentType.isEmpty() && !contentType.equals(this.getFromContentType())) {
			// unsupported element
			Element docElement = this.processUnsupported(templateElement);
			docElement = (Element) this.document.importNode(docElement, true);
			return docElement;
		}
		
		Element docElement = this.document.createElement(templateElement.getTagName());

		// retrieve base elements pool
		// -> el is just the cookie name
		HTTPCookie el = null;
		if (templateElement.hasAttribute("el")) {
			String elString = templateElement.getAttribute("el");
			for (HTTPCookie cookie : cookies) {
				if (cookie.getName().equals(elString)) {
					el = cookie;
					break;
				}
			}
		}

		// filter base elements pool if needed
		// -> not needed, there is only one element
		
		if (el == null) {
			System.out.println("Empty el for " + templateElement.getTagName());
		}
		
		// get value element (explicit in case of expand)
		Element valueElement;
		if (HTMLtoDocument.isExpand(templateElement)) {
			valueElement = (Element) templateElement.getElementsByTagName("value").item(0);
		} else {
			// implicit: is the element itself
			valueElement = templateElement;
		}
		
		String mode = valueElement.getAttribute("mode");
		if (mode.equals("script")) {
			
			String script = valueElement.getTextContent().trim();

	        // create a JavaScript engine
	        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
	        // evaluate JavaScript code from String
	        try {
	        	engine.put("el", el);
	        	String docElementText = (String) engine.eval(script);
	        	docElement.setTextContent(docElementText);
				System.out.println(templateElement.getTagName() + ": " + docElementText + " (script)");
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if (mode.equals("text")) {
			
			String docElementText;
			
			if (el == null) {
				// el is strictly required here, leave content blank
				docElementText = "";
			} else {
				// cookie value
				docElementText = el.getValue();				
			}
			
			docElement.setTextContent(docElementText);
			System.out.println(templateElement.getTagName() + ": " + docElementText + " (text)");
			
		} else {
			// nothing, just proceed further with children
			List<Element> childElementList = XMLParser.getChildElements(valueElement);
			for (Element childElement : childElementList) {
				Element docElementChild = this.process(childElement, cookies);
				docElement.appendChild(docElementChild);
			}
		}
		
		return docElement;
	}

}

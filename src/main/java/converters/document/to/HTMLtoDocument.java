package converters.document.to;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.html.HTMLParser;
import parsers.xml.XMLParser;

public class HTMLtoDocument extends TextToDocument {

	protected HTMLParser htmlParser;

	public HTMLtoDocument() {
		this.htmlParser = new HTMLParser();
	}
	
	public String getFromContentType() {
		return "text/html";
	}

	@Override
	public Document convert() throws Exception {

		// create output document
		Document doc = this.docBuilder.newDocument();
		this.setDocument(doc);
		
		// parse response
		org.jsoup.nodes.Document parsedContent = this.htmlParser.parse(this.content);
		
		// starting point
		org.jsoup.nodes.Element contentRootElement = parsedContent.getElementsByTag("html").first();
		Element templateRootElement = this.template.getDocumentElement();
		
		// process!
		Element docRootElement = this.process(templateRootElement, contentRootElement);
		doc.appendChild(docRootElement);
		
		return doc;
	}
	
	public Element process(Element templateElement, org.jsoup.nodes.Element fromHtmlEl) throws Exception {
		
		String contentType = templateElement.getAttribute("contentType");
		if (!contentType.isEmpty() && !contentType.equals(this.getFromContentType())) {
			// unsupported element
			Element docElement = this.processUnsupported(templateElement);
			docElement = (Element) this.document.importNode(docElement, true);
			return docElement;
		}
		
		Element docElement = this.document.createElement(templateElement.getTagName());
		
		// retrieve base elements pool
		org.jsoup.select.Elements htmlElementList;
		if (templateElement.hasAttribute("el")) {
			String el = templateElement.getAttribute("el");
			htmlElementList = fromHtmlEl.select(el);
		} else {
			// defaults to parent element
			htmlElementList = new org.jsoup.select.Elements(fromHtmlEl);
		}
				
		// filter base elements pool if needed
		if (HTMLtoDocument.isExpand(templateElement)) {
			Element conditionElement = (Element) templateElement.getElementsByTagName("condition").item(0);
			try {
				htmlElementList = this.filterHtmlElements(htmlElementList, conditionElement);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// This exception (name to refactor) means that the template has incorrect schema
				// => return
				e.printStackTrace();
				return null;
			}
		}
		
		if (htmlElementList.isEmpty()) {
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
		if (mode.equals("list")) {

			System.out.println("Processing list " + templateElement.getTagName());
			
			// retrieve list item descriptor
			Element listItem = XMLParser.getChildElements(valueElement).get(0);
			
			// create one list item for each html element
			for (org.jsoup.nodes.Element htmlElement : htmlElementList) {
				Element docElementListItem = this.process(listItem, htmlElement);
				docElement.appendChild(docElementListItem);
			}
			
		} else {
			// use first html element
			org.jsoup.nodes.Element htmlElement = htmlElementList.first();

			if (mode.equals("script")) {
				String script = valueElement.getTextContent().trim();

		        // create a JavaScript engine
		        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
		        // evaluate JavaScript code from String
		        try {
		        	engine.put("el", htmlElement);
		        	String docElementText = (String) engine.eval(script);
		        	docElement.setTextContent(docElementText);
					System.out.println(templateElement.getTagName() + ": " + docElementText + " (script)");
				} catch (ScriptException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (mode.equals("text")) {
				
				String docElementText;
				
				if (htmlElement == null) {
					// el is strictly required here, leave content blank
					docElementText = "";
				} else {
					
					String val = htmlElement.val();
					String content = htmlElement.text();
					
					if (!val.isEmpty()) {
						// element is a form element (input, textarea, etc.)
						docElementText = val;
					} else {
						// normal element
						docElementText = content.trim();
					}					
					
				}
				
				docElement.setTextContent(docElementText);
				System.out.println(templateElement.getTagName() + ": " + docElementText + " (text)");
				
			} else {
				// nothing, just proceed further with children
				
				System.out.println(templateElement.getTagName());
				
				List<Element> childElementList = XMLParser.getChildElements(valueElement);
				for (Element childElement : childElementList) {
					Element docElementChild = this.process(childElement, htmlElement);
					docElement.appendChild(docElementChild);
				}
			}
		}
		
		return docElement;
	}
	
	protected org.jsoup.select.Elements filterHtmlElements(org.jsoup.select.Elements htmlElementList, 
														Element conditionElement) throws Exception {
		
		org.jsoup.select.Elements filteredHtmlElementList = new org.jsoup.select.Elements();
		
		String mode = conditionElement.getAttribute("mode");
		if (mode.equals("script")) {
			
			String script = conditionElement.getTextContent().trim();
			
			for (org.jsoup.nodes.Element htmlElement : htmlElementList) {
		        // create a JavaScript engine
		        ScriptEngine engine = this.scriptFactory.getEngineByName("JavaScript");
		        // evaluate JavaScript code from String
		        try {
		        	engine.put("el", htmlElement);
		        	Boolean passes = (Boolean) engine.eval(script);
		        	if (passes) {
		        		filteredHtmlElementList.add(htmlElement);
		        	}
				} catch (ScriptException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// skip element
				}
			}
			
		} else {
			// only currently supported mode is "script"
			// TODO: add better exception model
			throw new Exception();
		}
		
		return filteredHtmlElementList;
	}
	
}

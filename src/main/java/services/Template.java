package services;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.w3c.dom.Element;

import parsers.xml.XMLParser;

public class Template {
	
	protected static Boolean isExpand(Element templateEl) {
		return templateEl.getAttribute("expand").equals("true");
	}
	
	public static Boolean hasProperty(Element templateEl, String propertyName) {
		if (Template.isExpand(templateEl)) {
			Element propertyEl = XMLParser.getChildElementByTagName(templateEl, propertyName);
			if (propertyEl != null) return true;
		}
		
		return templateEl.hasAttribute(propertyName);
	}
	
	// Note: a return value of null can either mean "property does not exist" or actual null property value (e.g. in case of evaluation),
	// use hasProperty to distinguish between the cases.
	public static Object getProperty(Element templateEl, String propertyName, ScriptEngine engine) {
		if (Template.isExpand(templateEl)) {
			// priority to expand property
			Object property = Template.getExpandProperty(templateEl, propertyName, engine);
			if (property != null) return property;
		}
		
		// default: attribute (plain text)
		return templateEl.getAttribute(propertyName);
	}
	
	// Get a property from an element with expand="true"
	protected static Object getExpandProperty(Element templateEl, String propertyName, ScriptEngine engine) {
		Element propertyEl = XMLParser.getChildElementByTagName(templateEl, propertyName);
		if (propertyEl == null) {
			// the property does not exist
			return null;
		}
		
		Object property = Template.evaluatePropertyElement(propertyEl, engine);
		return property;
	}
	
	protected static Object evaluatePropertyElement(Element propertyEl, ScriptEngine engine) {
		Object property;
		
		String propertyContent = propertyEl.getTextContent().trim();
		
		String mode = (String) Template.getProperty(propertyEl, "mode", engine);
		if (mode == null) mode = "";
		
		if (mode.equals("script")) {
			if (propertyContent.isEmpty()) {
				// executing an empty string returns null, avoid that and return an empty string instead
				property = "";
			} else {
				// evaluate JavaScript code from content
				try {
					property = engine.eval(propertyContent);
				} catch (ScriptException e) {
					// script execution error
					e.printStackTrace();
					property = null;
				}
			}
		} else {
			// default: plain text content
			property = propertyContent;
		}
		
		return property;
	}
	
	// get value element (explicit in case of expand)
	protected static Element getValueElement(Element templateEl) {
		Element valueEl;
		
		if (Template.isExpand(templateEl)) {
			// (will be null if the value child does not exist)
			valueEl = XMLParser.getChildElementByTagName(templateEl, "value");
		} else {
			// implicit: is the element itself
			valueEl = templateEl;
		}
		
		return valueEl;
	}
	
	// Note: assumes templateEl is a mode "list" element
	public static Element getListItemDescriptor(Element templateEl) {
		Element valueEl = Template.getValueElement(templateEl);
		if (valueEl == null) {
			return null;
		}
		
		// the first child (whatever its tag name is)
		Element listItemDescriptor = XMLParser.getChildElements(valueEl).get(0);
		return listItemDescriptor;
	}
	
	// Note: assumes templateEl is a mode "script" element
	public static String getScriptContent(Element templateEl) {
		Element valueEl = Template.getValueElement(templateEl);
		if (valueEl == null) {
			return null;
		}
		
		String scriptCode = valueEl.getTextContent().trim();
		return scriptCode;
	}
	
	// Note: assumes templateEl is not any of the previous modes.
	public static List<Element> getNextLevel(Element templateEl) {
		Element valueEl = Template.getValueElement(templateEl);
		if (valueEl == null) {
			return null;
		}
		
		return XMLParser.getChildElements(valueEl);
	}
	
}

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
	public static Object getProperty(Element templateEl, String propertyName, ScriptEngine engine, Data<String> data) {
		if (Template.isExpand(templateEl)) {
			// priority to expand property
			Object property = Template.getExpandProperty(templateEl, propertyName, engine, data);
			if (property != null) return property;
		}
		
		// default: attribute (plain text)
		String attribute = templateEl.getAttribute(propertyName);
		return data.apply(attribute);
	}
	
	// Get a property from an element with expand="true"
	protected static Object getExpandProperty(Element templateEl, String propertyName, ScriptEngine engine, Data<String> data) {
		Element propertyEl = XMLParser.getChildElementByTagName(templateEl, propertyName);
		if (propertyEl == null) {
			// the property does not exist
			return null;
		}
		
		Object property = Template.evaluatePropertyElement(propertyEl, engine, data);
		return property;
	}
	
	protected static Object evaluatePropertyElement(Element propertyEl, ScriptEngine engine, Data<String> data) {
		Object property;
		
		String propertyContent = propertyEl.getTextContent().trim();
		propertyContent = data.apply(propertyContent);
		
		String mode = (String) Template.getProperty(propertyEl, "mode", engine, data);
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
	
	// Returns a NEW data that extends the given data with the template element data
	public static Data<String> getData(Element templateEl, ScriptEngine engine, Data<String> data) {
		Data<String> newData = new Data<String>();
		newData.putAll(data);
				
		if (!Template.isExpand(templateEl)) return data;
				
		Element dataEl = XMLParser.getChildElementByTagName(templateEl, "data");
		if (dataEl == null) return data;
		
		List<Element> dataItemElList = XMLParser.getChildElements(dataEl);
		for (Element dataItemEl : dataItemElList) {
			Element dataItemNameEl = XMLParser.getChildElementByTagName(dataItemEl, "name");
			Element dataItemValueEl = XMLParser.getChildElementByTagName(dataItemEl, "value");

			// evaluate (uses current new data)
			String name = (String) Template.evaluatePropertyElement(dataItemNameEl, engine, newData);
			String value = (String) Template.evaluatePropertyElement(dataItemValueEl, engine, newData);
			
			newData.put(name, value);
		}
		
		return newData;
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
	public static String getScriptContent(Element templateEl, Data<String> data) {
		Element valueEl = Template.getValueElement(templateEl);
		if (valueEl == null) {
			return null;
		}
		
		String scriptCode = valueEl.getTextContent().trim();
		return data.apply(scriptCode);
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

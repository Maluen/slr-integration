package converters.document.from;

import java.util.ArrayList;
import java.util.List;

import network.http.HTTPEncoder;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;


public class DocumentToForm extends DocumentToText {

	public String getToContentType() {
		return "application/x-www-form-urlencoded";
	}
	
	@Override
	public String convert() {
		String formData;
		
		Element rootEl = this.document.getDocumentElement();
		List<Element> parameterElList = XMLParser.getChildElements(rootEl);
		
	    // convert post parameters in a "name=value" list (url-encoded)
	    // to be easily joined by the "&" separator
	    List<String> formDataParts = new ArrayList<String>();
		for (Element parameterEl : parameterElList) {
			Element nameEl = XMLParser.getChildElementByTagName(parameterEl, "name");
			Element valueEl = XMLParser.getChildElementByTagName(parameterEl, "value");
			
			String name = nameEl.getTextContent().trim();
			String value = valueEl.getTextContent().trim();
			
			name = this.data.apply(name);
			value = this.data.apply(value);
			
	    	name = HTTPEncoder.encodeForFormData(name);
			value = HTTPEncoder.encodeForFormData(value);
	    	
	    	String element = name + "=" + value;
	    	formDataParts.add(element);
		}
	    formData = StringUtils.join(formDataParts, "&");
	    
	    return formData;
	}

}

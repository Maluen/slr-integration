package converters.document.from;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
			
	    	try {
		    	name = URLEncoder.encode(name, "UTF-8");
				value = URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO: Wrong format
				e.printStackTrace();
				return null;
			}
	    	
	    	String element = name + "=" + value;
	    	formDataParts.add(element);
		}
	    formData = StringUtils.join(formDataParts, "&");
	    
	    return formData;
	}

}

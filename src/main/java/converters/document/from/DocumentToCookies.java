package converters.document.from;

import java.util.ArrayList;
import java.util.List;

import network.http.HTTPEncoder;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;

public class DocumentToCookies extends DocumentToText {

	public String getToContentType() {
		return "application/x-www-request-cookies";
	}
	
	@Override
	public String convert() {
		String cookiesData;
		
		Element rootEl = this.document.getDocumentElement();
		List<Element> cookieElList = XMLParser.getChildElements(rootEl);
		
	    // convert post parameters in a "name=value" list (url-encoded)
	    // to be easily joined by the "; " separator
	    List<String> cookiesDataParts = new ArrayList<String>();
		for (Element cookieEl : cookieElList) {
			Element nameEl = XMLParser.getChildElementByTagName(cookieEl, "name");
			Element valueEl = XMLParser.getChildElementByTagName(cookieEl, "value");
			
			String name = nameEl.getTextContent().trim();
			String value = valueEl.getTextContent().trim();
			
			name = this.data.apply(name);
			value = this.data.apply(value);
			
    		// TODO: only encode real special characters
    		// http://www.nczonline.net/blog/2009/05/05/http-cookies-explained/
	    	name = HTTPEncoder.encodeForFormData(name);
			value = HTTPEncoder.encodeForFormData(value);
	    	
	    	String element = name + "=" + value;
	    	cookiesDataParts.add(element);
		}
	    cookiesData = StringUtils.join(cookiesDataParts, "; ");
	    
	    return cookiesData;
	}

}

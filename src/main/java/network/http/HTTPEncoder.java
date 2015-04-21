package network.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class HTTPEncoder {
	
	public static enum EncodeMode {
		FORM_DATA,
		URL
	};

	public static String encode(String target, HTTPEncoder.EncodeMode encodeMode) {
		
		if (encodeMode == HTTPEncoder.EncodeMode.FORM_DATA) {
			return HTTPEncoder.encodeForFormData(target);
		} else if (encodeMode == HTTPEncoder.EncodeMode.URL) {
			return HTTPEncoder.encodeForURL(target);
		}
		
		return null;
	}
	
	public static String encodeForFormData(String target) {
		try {
			return URLEncoder.encode(target, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static String encodeForURL(String target) {
		
		URI uri = null;
		try {
			uri = new URI(null, null, null, "a="+target, null);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		String url = uri.toASCIIString(); // "?a=[encoded]"
		String encodedTarget = url.substring(url.indexOf("=") + 1);
		
		return encodedTarget;
	}
	
}

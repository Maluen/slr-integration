package network.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {

	private String body = "";
	Map<String, List<String>> headers;
	
	public HTTPResponse() {
		this.headers = new HashMap<String, List<String>>();
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	
	
	
}

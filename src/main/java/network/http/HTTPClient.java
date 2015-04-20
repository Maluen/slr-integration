package network.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HTTPClient {

	// Returned string is the response (full or body only? Don't know yet).
	// NOTE: do not uses cache.
	public HTTPResponse request(String url, String method, String contentType, 
								String cookies, String body) {
	
		try {
			
			URL urlParsed = new URL(url);
			
			String contentLength = Integer.toString(body.getBytes().length);
			
			// Create connection
		    HttpURLConnection connection = (HttpURLConnection) urlParsed.openConnection();
		    connection.setRequestMethod(method);
		    if (!body.isEmpty()) {
		    	// only set those headers if there is actually something to send
			    connection.setRequestProperty("Content-Type", contentType);
			    connection.setRequestProperty("Content-Length", contentLength);
		    }
		    if (!cookies.isEmpty()) {
			    connection.setRequestProperty("Cookie", cookies);
		    }
		    connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    
		    // Send request
		    DataOutputStream wr = new DataOutputStream( connection.getOutputStream() );
		    wr.writeBytes(body);
		    wr.flush();
		    wr.close();
		    
		    // Get response body
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    StringBuffer responseBuffer = new StringBuffer(); 
		    while ((line = rd.readLine()) != null) {
		    	responseBuffer.append(line);
		    	responseBuffer.append('\r');
		    }
		    rd.close();
		    String responseBody = responseBuffer.toString();
		    
		    // Get response headers
			Map<String, List<String>> responseHeaders = connection.getHeaderFields();
			
			HTTPResponse response = new HTTPResponse();
			response.setBody(responseBody);
			response.setHeaders(responseHeaders);
		    return response;
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
	}
	
}

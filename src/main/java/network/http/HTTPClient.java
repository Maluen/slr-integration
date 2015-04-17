package network.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class HTTPClient {

	// Returned string is the response (full or body only? Don't know yet).
	// NOTE: do not uses cache.
	public String request(String url, String method, String contentType, String body) {
	
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
		    connection.setUseCaches(false);
		    connection.setDoInput(true);
		    connection.setDoOutput(true);
		    
		    // Send request
		    DataOutputStream wr = new DataOutputStream( connection.getOutputStream() );
		    wr.writeBytes(body);
		    wr.flush();
		    wr.close();
		    
		    // Get Response    
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    StringBuffer responseBuffer = new StringBuffer(); 
		    while ((line = rd.readLine()) != null) {
		    	responseBuffer.append(line);
		    	responseBuffer.append('\r');
		    }
		    rd.close();
		    String responseString = responseBuffer.toString();
		    
		    return responseString;
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
	}
	
}

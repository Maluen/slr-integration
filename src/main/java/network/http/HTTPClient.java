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
	public String request(String url, String method, String contentType,
						Map<String, String> postParameters) {
	
		try {
			
			URL urlParsed = new URL(url);
			
			// Create connection
		    HttpURLConnection connection = (HttpURLConnection) urlParsed.openConnection();
		    connection.setRequestMethod(method);
		    connection.setRequestProperty("Content-Type", contentType);
		    connection.setUseCaches(false);
		    connection.setDoInput(true);
		    connection.setDoOutput(true);
		    
		    // convert post parameters in a "name=value" list (url-encoded)
		    // to be easily joined by the "&" separator
		    List<String> requestBodyElements = new ArrayList<String>();
		    for (Map.Entry<String, String> entry : postParameters.entrySet()) {
		    	String name = URLEncoder.encode(entry.getKey(), "UTF-8");
		    	String value = URLEncoder.encode(entry.getValue(), "UTF-8");
		    	String element = name + "=" + value;
		    	
		    	requestBodyElements.add(element);
		    }
		    String responseBody = StringUtils.join(requestBodyElements, "&");
		    
		    
		    // Send request
		    DataOutputStream wr = new DataOutputStream( connection.getOutputStream() );
		    wr.writeBytes(responseBody);
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

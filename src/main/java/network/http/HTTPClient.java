package network.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import misc.Logger;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class HTTPClient {

	static { // on class load
		// Enable cookies
		CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
	}
	
	protected Logger logger;
	
	public HTTPClient() {
		this.logger = new Logger("HTTPClient");
	}

	// Returned string is the response (full or body only? Don't know yet).
	// NOTE: do not uses cache.
	public HTTPResponse request(String url, String method, String contentType, 
								String cookies, String body) {
	
		try {
			
			this.logger.log("\nRequesting URL:\n" + url);
			
			URL urlParsed = new URL(url);
			
			String contentLength = Integer.toString(body.getBytes().length);
			
			// Create connection
		    HttpURLConnection connection = (HttpURLConnection) urlParsed.openConnection();
		    connection.setDoInput(true);
		    connection.setInstanceFollowRedirects(false); // manually handle redirects (to prevent infinite waiting in some cases)
		    //connection.setReadTimeout(60*1000);
		    connection.setUseCaches(false);
		    
		    if (!body.isEmpty()) {
		    	// there is something to write, tell the server to wait for it
		    	// (doing this in GET requests or similar will cause infinite waiting)
			    connection.setDoOutput(true);
		    }
		    
		    connection.setRequestMethod(method);
		    if (!body.isEmpty()) {
		    	// only set those headers if there is actually something to send
			    connection.setRequestProperty("Content-Type", contentType);
			    //connection.setRequestProperty("Content-Length", contentLength);
		    }
		    if (!cookies.isEmpty()) {
			    connection.setRequestProperty("Cookie", cookies);
		    }
		    connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
		    
		    
		    if (!body.isEmpty()) {
			    // Send request
			    DataOutputStream wr = new DataOutputStream( connection.getOutputStream() );
			    wr.writeBytes(body);
			    wr.flush();
			    wr.close();
		    }
		    
		    // Handle redirects
		    Integer responseCode = connection.getResponseCode();
		    if (responseCode.equals(HttpURLConnection.HTTP_MOVED_PERM) || responseCode.equals(HttpURLConnection.HTTP_MOVED_TEMP)) {
	           String location = connection.getHeaderField("Location"); 
	           
	           // location might be relative, reconstruct absolute url
	           url = new URL(urlParsed, location).toExternalForm(); // see http://stackoverflow.com/a/26046079
	           
	           // location might contain unencoded parameter values (server misconfiguration?)
	           url = this.fixUrlParameterValues(url);
	           
	           // do a simple GET request to the location
	           this.logger.log("Redirect:\n" + url);
	           return this.request(url, "GET", "", cookies, "");
		    }
		    
		    // Get response body
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    StringBuffer responseBuffer = new StringBuffer(); 
		    /*while ((line = rd.readLine()) != null) {
		    	responseBuffer.append(line);
		    	responseBuffer.append('\r');
		    }*/
		    int BUFFER_SIZE = 1024;
		    char[] buffer = new char[BUFFER_SIZE]; // or some other size, 
		    int charsRead = 0;
		    while ( (charsRead = rd.read(buffer, 0, BUFFER_SIZE)) != -1) {
		    	responseBuffer.append(buffer, 0, charsRead);
		    }
		    rd.close();
		    is.close();
		    String responseBody = responseBuffer.toString();
		    
		    // Get response headers
			Map<String, List<String>> responseHeaders = connection.getHeaderFields();
			// make header names case insensitive (as they should)
			responseHeaders = new CaseInsensitiveMap<String, List<String>>(responseHeaders);
			
			HTTPResponse response = new HTTPResponse();
			response.setBody(responseBody);
			response.setHeaders(responseHeaders);
		    return response;
	
		} catch (Exception e) {
			this.logger.log(ExceptionUtils.getStackTrace(e));
			
			// delayed and infinite retry
			// TODO: employ a more configurable solution
			int seconds = 5;
			try {
				this.logger.log("Retrying HTTP request in " + seconds + " second(s)...");
			    Thread.sleep(seconds*1000);
			    return this.request(url, method, contentType, cookies, body);
			    
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			    return null;
			}
		}	
	}
	
	// Fix urls with obvious unencoded parameter values by encoding them (partially)
	protected String fixUrlParameterValues(String url) {
		StringBuffer buffer = new StringBuffer();
		
		 // e.g. "&foo=value&" or "&foo=value" or "?foo=value", 
		// the final & is not part of the match (lookahead), otherwise would compromise the next match
		Pattern pattern = Pattern.compile("((?:\\?|&)(?:.+?)=)(.+?)(?=&|$)");
		
		Matcher matcher = pattern.matcher(url);
		while (matcher.find()) {
			
			String start = matcher.group(1);
			String value = matcher.group(2);
			
			// TODO: better and more complete encoder (without replacing already encoded characters!)
			String encodedValue = value.replace(" ", "%20")
									   .replace("\"", "%21")
									   .replace("*", "%2A")
									   .replace("(", "%28")
									   .replace(")", "%29");
			
			String replacement = start + encodedValue;
			
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
}

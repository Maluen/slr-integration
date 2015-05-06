import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import misc.Utils;

import org.antlr.runtime.ANTLRInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.owlike.genson.Genson;

import engines.Engine;
import engines.acm.ACMEngine;

/**
 * 
 */

/**
 * @author Maluen
 *
 */
public class Main {
	
	public static void html(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Elements newsHeadlines = doc.select("#mp-itn b a");
		System.out.println(newsHeadlines);
	}
	
	public static void csv(String resourcePath) throws IOException {
		File file = Utils.getResource(resourcePath);

        ICsvListReader listReader = null;
        try {
        	listReader = new CsvListReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
                
            listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
            
            List<String> customerList;
            while( (customerList = listReader.read()) != null ) {
                    System.out.println(customerList);
            }
                
        } finally {
            if( listReader != null ) {
                    listReader.close();
            }
        }
	}
	
	public static void json(String resourcePath) throws IOException {
		File file = Utils.getResource(resourcePath);
		String json = Utils.getFileContent(file);
		
		Genson genson = new Genson();
		Map<String, Object> map = Utils.castMap(genson.deserialize(json, Map.class), String.class, Object.class);
		
		System.out.println(map);
	}
	
	public static void http(String url) {
		GetRequest request = Unirest.get(url);
		HttpResponse<String> response;
		try {
			response = request.asString();
			String content = response.getBody();
			System.out.println(content);
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {		
		// TODO Auto-generated method stub
		//System.out.println("Hello, World!");
		
		//Main.html("http://en.wikipedia.org/");
		//Main.csv("csv/example.csv");
		//Main.json("json/example.json");
		//Main.http("http://www.google.com/");
		
		String queryText = "mde";
		//String queryText = Utils.getFileContent(new File("data/querystring.txt"));
		System.out.println("Query string: " + queryText);
		
		//Engine ieeeEngine = new IEEEEngine();
		//ieeeEngine.search(queryText);
		
		Engine acmEngine = new ACMEngine();
		acmEngine.search(queryText);
	}

}

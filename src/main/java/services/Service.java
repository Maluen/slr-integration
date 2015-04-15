package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import misc.Utils;
import network.http.HTTPClient;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import services.extractors.Extractor;
import services.extractors.ExtractorsFactory;

public class Service {

	private XMLParser xmlParser;
	private HTTPClient httpClient;
	
	private Map<String, String> data;
	
	private String url;
	private String method;
	private String contentType;
	private Map<String, String> postParameters;
	private Document template;
	
	public Service() {
		this.xmlParser = new XMLParser();
		this.httpClient = new HTTPClient();
		
		this.data = new HashMap<String, String>();
		
		this.postParameters = new HashMap<String, String>();
	}
	
	public void loadFromFile(File file) {
		try {
			String fileContent = Utils.getFileContent(file);
			Document document = this.xmlParser.parse(fileContent);
			
			// get endpoint information
			
			Element rootEl = document.getDocumentElement();
			Element endpointEl = (Element) rootEl.getElementsByTagName("endpoint").item(0);
			
			Element urlEl = (Element) endpointEl.getElementsByTagName("url").item(0);
			String url = urlEl.getTextContent().trim();
			this.setUrl(url);
			
			Element methodEl = (Element) endpointEl.getElementsByTagName("method").item(0);
			String method = methodEl.getTextContent().trim();
			this.setMethod(method);
			
			Element contentTypeEl = (Element) endpointEl.getElementsByTagName("contentType").item(0);
			String contentType = contentTypeEl.getTextContent().trim();
			this.setContentType(contentType);
			
			// get postParameters
			Element parametersEl = (Element) endpointEl.getElementsByTagName("parameters").item(0);
			List<Element> paramElList = XMLParser.getChildElements(parametersEl);
			for (Element paramEl : paramElList) {
				Element nameEl = (Element) paramEl.getElementsByTagName("name").item(0);
				String name = nameEl.getTextContent().trim();
				
				Element valueEl = (Element) paramEl.getElementsByTagName("value").item(0);
				String value = valueEl.getTextContent().trim();
				
				this.addPostParameter(name, value);
			}
			
			// get template
			Element templateEl = (Element) rootEl.getElementsByTagName("content").item(0);
			Document template = xmlParser.createDocumentFromElement(templateEl);
			this.setTemplate(template);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String applyData(String target) {
		
	    for (Map.Entry<String, String> entry : this.data.entrySet()) {
	    	
	    	String name = entry.getKey();
	    	String value = entry.getValue();
	    	
	    	try {
	    		// do this first since the other also matches this form
				target = target.replace("{{{"+name+"}}}", value);
				
				target = target.replace("{{"+name+"}}", URLEncoder.encode(value, "UTF-8"));
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
		
		return target;
	}
	
	private Map<String, String> applyData(Map<String, String> target) {
		// don't modify original collection
		Map<String, String> newTarget = new HashMap<String, String>();
		
		for (Map.Entry<String, String> entry : target.entrySet()) {
	    	String name = entry.getKey();
	    	String value = entry.getValue();
	    	
	    	// apply data to name and value
	    	name = this.applyData(name);
	    	value = this.applyData(value);
	    	
	    	newTarget.put(name, value);
		}

		return newTarget;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}
	
	public void addData(String name, String value) {
		this.data.put(name, value);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, String> getPostParameters() {
		return postParameters;
	}

	public void setPostParameters(Map<String, String> postParameters) {
		this.postParameters = postParameters;
	}

	public void addPostParameter(String name, String value) {
		this.postParameters.put(name, value);
	}

	public Document getTemplate() {
		return template;
	}

	public void setTemplate(Document template) {
		this.template = template;
	}
	
	public Document request() {
		// apply data to parameters
		String url = this.applyData(this.url);
		String method = this.applyData(this.method);
		String contentType = this.applyData(this.contentType);
		Map<String, String> postParameters = this.applyData(this.postParameters);
		
		// call service
		String response = httpClient.request(url, method, contentType, postParameters);
		
		// DEBUG: save page
		/*try {
			try {
				Utils.saveText(response, "data/index.html");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		// DEBUG: use local page
		/*String response;
		try {
			response = Utils.getFileContent( new File("data/index.html") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = null;
		}*/
		
		// extract data from service
		Extractor extractor = ExtractorsFactory.create(this.contentType);
		Document data = extractor.extractFrom(response, this.template);
		
		return data;
	};
	
}

package services;

import java.io.File;
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
import converters.ConvertersFactory;
import converters.DocumentToForm;
import converters.DocumentToText;
import converters.TextToDocument;

public class Service {

	private XMLParser xmlParser;
	private HTTPClient httpClient;
	
	private Data<String> data;
	
	private String url;
	private String method;
	private String contentType;
	private String body;
	private Document bodyDocument;
	private Document template;
	
	public Service() {
		this.xmlParser = new XMLParser();
		this.httpClient = new HTTPClient();
		
		this.data = new Data<String>();
	}
	
	public void loadFromFile(File file) {
		try {
			String fileContent = Utils.getFileContent(file);
			Document document = this.xmlParser.parse(fileContent);
			
			// request
			
			Element rootEl = document.getDocumentElement();
			Element requestEl = (Element) rootEl.getElementsByTagName("request").item(0);
			
			String url = "";
			if (requestEl != null) {
				Element urlEl = (Element) requestEl.getElementsByTagName("url").item(0);
				if (urlEl != null) url = urlEl.getTextContent().trim();
			}
			this.setUrl(url);
			
			String method = "";
			if (requestEl != null) {
				Element methodEl = (Element) requestEl.getElementsByTagName("method").item(0);
				if (methodEl != null) method = methodEl.getTextContent().trim();
			}
			this.setMethod(method);

			// body
			
			Element bodyEl = null;
			if (requestEl != null) bodyEl = (Element) requestEl.getElementsByTagName("body").item(0);
			
			Document bodyDocument = null;
			if (bodyEl != null) bodyDocument = xmlParser.createDocumentFromElement(bodyEl);
			this.setBodyDocument(bodyDocument);
			
			// TODO: add body as string instead that as document
			// if bodyEl content is a string/cdata
			this.setBody(""); // reset
			
			String contentType = "";
			if (bodyEl != null) contentType = bodyEl.getAttribute("contentType");
			this.setContentType(contentType);
			
			// template
			
			Document template = null;
			if (rootEl != null) {
				Element templateEl = (Element) rootEl.getElementsByTagName("response").item(0);
				if (templateEl != null) template = xmlParser.createDocumentFromElement(templateEl);
			}
			this.setTemplate(template);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Data<String> getData() {
		return this.data;
	}

	public void setData(Data<String> data) {
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

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public Document getBodyDocument() {
		return this.bodyDocument;
	}
	
	public void setBodyDocument(Document bodyDocument) {
		this.bodyDocument = bodyDocument;
	}

	public Document getTemplate() {
		return template;
	}

	public void setTemplate(Document template) {
		this.template = template;
	}
	
	public Document request() {
		// apply data to parameters
		String url = this.data.apply(this.url);
		String method = this.data.apply(this.method);
		
		// resolve content type
		String contentType = this.data.apply(this.contentType);
		if (contentType.isEmpty()) {
			// default content type
			contentType = DocumentToForm.getToContentType();
		}
		
		// resolve body
		String body;
		if (!this.body.isEmpty()) {
			// user provided body as string
			body = this.data.apply(this.body);
		} else if (this.bodyDocument != null) {
			// user provided body as document
			DocumentToText converter = ConvertersFactory.createDocumentToText(contentType);
			body = converter.convert(this.bodyDocument, this.data);
		} else {
			// default: no body
			body = "";
		}
		
		// call service
		String response = httpClient.request(url, method, contentType, body);
		
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
		
		// TODO: extract data from service
		String responseContentType = this.template.getDocumentElement().getAttribute("contentType");
		TextToDocument converter = ConvertersFactory.createTextToDocument(responseContentType);
		Document data = converter.convert(response, this.template);
		
		return data;
	};
	
}

package services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import misc.Utils;
import network.http.HTTPClient;
import network.http.HTTPResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import converters.document.DocumentConverterFactory;
import converters.document.from.DocumentToCookies;
import converters.document.from.DocumentToForm;
import converters.document.from.DocumentToText;
import converters.document.to.MixedToDocument;

public class Service {

	private XMLParser xmlParser;
	private HTTPClient httpClient;
	private DocumentToForm documentToForm;
	
	private Data<String> data;
	
	private String url;
	private String method;
	private String contentType;
	
	private String body;
	private Document bodyDocument;
	
	private String cookies;
	private Document cookiesDocument;
	
	private Document template;
	
	public Service() {
		this.xmlParser = new XMLParser();
		this.httpClient = new HTTPClient();
		this.documentToForm = new DocumentToForm();
		
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
			
			// body
			
			Element cookiesEl = null;
			if (requestEl != null) cookiesEl = (Element) requestEl.getElementsByTagName("cookies").item(0);
			
			Document cookiesDocument = null;
			if (cookiesEl != null) cookiesDocument = xmlParser.createDocumentFromElement(cookiesEl);
			this.setCookiesDocument(cookiesDocument);
			
			// TODO: add cookies as string instead that as document
			// if cookiesEl content is a string/cdata
			this.setCookies(""); // reset			
			
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
	
	public String getCookies() {
		return this.cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
	
	public Document getCookiesDocument() {
		return this.cookiesDocument;
	}
	
	public void setCookiesDocument(Document cookiesDocument) {
		this.cookiesDocument = cookiesDocument;
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
			contentType = this.documentToForm.getToContentType();
		}
		
		// resolve body
		String body;
		if (!this.body.isEmpty()) {
			// user provided body as string
			body = this.data.apply(this.body);
		} else if (this.bodyDocument != null) {
			// user provided body as document
			DocumentToText converter = (DocumentToText) DocumentConverterFactory.createFromDocument(contentType);
			converter.setDocument(this.bodyDocument);
			converter.setData(this.data);
			body = converter.convert();
		} else {
			// default: no body
			body = "";
		}
		
		// resolve cookies
		String cookies;
		if (!this.cookies.isEmpty()) {
			// user provided cookies as string
			cookies = this.data.apply(this.cookies);
		} else if (this.cookiesDocument != null) {
			// user provided cookies as document
			DocumentToCookies converter = new DocumentToCookies();
			converter.setDocument(this.cookiesDocument);
			converter.setData(this.data);
			cookies = converter.convert();
		} else {
			// default: no cookies
			cookies = "";
		}
		
		// call service
		HTTPResponse response = httpClient.request(url, method, contentType, cookies, body);
		
		// extract response contents
		String responseBody = response.getBody();
		Map<String, List<String>> responseHeaders = response.getHeaders();
		List<String> responseCookies = responseHeaders.get("Set-Cookie");
				
		// DEBUG: save page
		/*try {
			try {
				Utils.saveText(responseBody, "data/index.html");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		// DEBUG: use local page
		/*String responseBody;
		try {
			responseBody = Utils.getFileContent( new File("data/index.html") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseBody = null;
		}*/
		
		// Extract data from service
		
		MixedToDocument converter = new MixedToDocument();
		converter.setDefaultContent(responseBody);
		converter.addContent("application/x-www-response-cookies", responseCookies);
		converter.setTemplate(this.template);

		Document data = null;
		try {
			data = converter.convert();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	};
	
}

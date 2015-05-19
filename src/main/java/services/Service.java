package services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import misc.Utils;
import network.http.HTTPClient;
import network.http.HTTPEncoder;
import network.http.HTTPResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.XMLParser;
import services.resources.Resource;
import services.resources.ResourceList;
import converters.document.DocumentConverterFactory;
import converters.document.from.DocumentToCookies;
import converters.document.from.DocumentToForm;
import converters.document.from.DocumentToText;
import converters.document.to.MixedToDocument;

public class Service {

	private XMLParser xmlParser;
	private HTTPClient httpClient;
	private DocumentToForm documentToForm;
	
	private Map<String, Object> engineBaseScope;
	private Data<String> data;
	private ResourceList resourceList;
	
	private String name;
	
	private String url = "";
	private String method = "";
	private String contentType = "";
	
	private String body = "";
	private Document bodyDocument;
	
	private String cookies = "";
	private Document cookiesDocument;
	
	private Document template;
		
	public Service() {
		this.xmlParser = new XMLParser();
		this.httpClient = new HTTPClient();
		this.documentToForm = new DocumentToForm();
		
		this.engineBaseScope = new HashMap<String, Object>();
		this.data = new Data<String>();
		this.resourceList = new ResourceList();
	}
	
	public void loadFromFile(File file) {
		try {
			String fileContent = Utils.getFileContent(file);
			Document document = this.xmlParser.parse(fileContent);
			
			Element rootEl = document.getDocumentElement();
			
			// name
			
			String name = "";
			Element nameEl = (Element) rootEl.getElementsByTagName("name").item(0);
			if (nameEl != null) name = nameEl.getTextContent().trim();
			this.setName(name);
			
			// resources
			this.loadResourcesFromDocument(document);
			
			// request
			
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
			if (bodyEl != null) bodyDocument = XMLParser.createDocumentFromElement(bodyEl);
			this.setBodyDocument(bodyDocument);
			
			// TODO: add body as string instead that as document
			// if bodyEl content is a string/cdata
			this.setBody(""); // reset
			
			String contentType = "";
			if (bodyEl != null) contentType = bodyEl.getAttribute("contentType");
			this.setContentType(contentType);
			
			// cookies
			
			Element cookiesEl = null;
			if (requestEl != null) cookiesEl = (Element) requestEl.getElementsByTagName("cookies").item(0);
			
			Document cookiesDocument = null;
			if (cookiesEl != null) cookiesDocument = XMLParser.createDocumentFromElement(cookiesEl);
			this.setCookiesDocument(cookiesDocument);
			
			// TODO: add cookies as string instead that as document
			// if cookiesEl content is a string/cdata
			this.setCookies(""); // reset
			
			// template
			
			Document template = null;
			if (rootEl != null) {
				Element templateEl = (Element) rootEl.getElementsByTagName("response").item(0);
				if (templateEl != null) template = XMLParser.createDocumentFromElement(templateEl);
			}
			this.setTemplate(template);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadResourcesFromDocument(Document document) {
		Element rootEl = document.getDocumentElement();
		
		List<Element> resourceElList;
		try {
			resourceElList = XMLParser.select("/service/resources/item", rootEl);
		} catch (XPathExpressionException e) {
			// file has wrong schema
			e.printStackTrace();
			resourceElList = null;
		}
		
		ResourceList resourceList = new ResourceList();
		for (Element resourceEl : resourceElList) {
			Element nameEl = XMLParser.getChildElementByTagName(resourceEl, "name");
			Element contentTypeEl = XMLParser.getChildElementByTagName(resourceEl, "contentType");
			Element contentEl = XMLParser.getChildElementByTagName(resourceEl, "content");

			Resource resource = new Resource();
			if (nameEl != null) {
				String name = nameEl.getTextContent().trim();
				resource.setName(name);
			}
			if (contentTypeEl != null) {
				String contentType = contentTypeEl.getTextContent().trim();
				resource.setContentType(contentType);
			}
			if (contentEl != null) {
				String content = contentEl.getTextContent().trim();
				resource.setContent(content);
			}
			resourceList.add(resource);
		}
		this.setResourceList(resourceList);
	}

	public Map<String, Object> getEngineBaseScope() {
		return this.engineBaseScope;
	}

	public void setEngineBaseScope(Map<String, Object> engineBaseScope) {
		this.engineBaseScope = engineBaseScope;
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
	
	public ResourceList getResourceList() {
		return this.resourceList;
	}
	
	public void setResourceList(ResourceList resourceList) {
		this.resourceList = resourceList;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Resource execute() {
		
		// resolve name
		String name = this.data.apply(this.name);
		if (name.isEmpty()) {
			// default name
			name = "default";
		}
		
		// resolve url
		String url = this.data.apply(this.url, HTTPEncoder.EncodeMode.URL);
		
		// resolve content type
		String contentType = this.contentType;
		if (contentType.isEmpty()) {
			// default content type
			contentType = this.documentToForm.getToContentType();
		}
		contentType = this.data.apply(contentType);
		
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
		
		// resolve method
		String method = this.method;
		if (method.isEmpty()) {
			// default method
			method = (body.isEmpty()) ? "GET" : "POST";
		}
		method = this.data.apply(method);
		
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
		
		if (!url.isEmpty()) { // there is a request to be made
			
			// Call service
			HTTPResponse response = httpClient.request(url, method, contentType, cookies, body);
			
			// Get response contents
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
			
			// Response body resource
			
			Resource responseBodyResource = this.resourceList.getByName("response.body");
			if (responseBodyResource == null) {
				responseBodyResource = new Resource();
				responseBodyResource.setName("response.body");
				this.resourceList.add(responseBodyResource);
			}
			
			// override content
			responseBodyResource.setContent(responseBody);	
			
			if (responseBodyResource.getContentType().isEmpty()) {
				// use response content type (if any)
				List<String> responseBodyContentTypeHeaders = responseHeaders.get("Content-Type");
				if (responseBodyContentTypeHeaders != null && responseBodyContentTypeHeaders.size() > 0) {
					String responseBodyContentType = responseBodyContentTypeHeaders.get(0);
					responseBodyResource.setContentType(responseBodyContentType);
				}
			}
			
			// Response cookies resource
			
			Resource responseCookiesResource = this.resourceList.getByName("response.cookies");
			if (responseCookiesResource == null) {
				responseCookiesResource = new Resource();
				responseCookiesResource.setName("response.cookies");
				this.resourceList.add(responseCookiesResource);
			}
			
			// override content
			responseCookiesResource.setContent(responseCookies);
			
			if (responseCookiesResource.getContentType().isEmpty()) {
				// use default content type
				responseCookiesResource.setContentType("application/x-www-response-cookies");
			}
			
		} else {
			// remove old response resources (if any)
			this.resourceList.removeByName("response.body");
			this.resourceList.removeByName("response.cookies");
		}
		
		// Extract the document
		
		MixedToDocument converter = new MixedToDocument();
		converter.setEngineBaseScope(this.engineBaseScope);
		converter.setTemplate(this.template);
		converter.setResourceList(this.resourceList);
		converter.setData(this.data);
		converter.setDefaultResourceName("response.body");

		Document document = null;
		try {
			System.out.println("Converting " + name);
			document = converter.convert();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Resource response = new Resource();
		response.setName(name);
		response.setContentType("text/xml");
		response.setContent(document);
		
		return response;
	};
	
}

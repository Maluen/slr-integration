package converters.document.to;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import converters.document.DocumentConverter;

public abstract class ToDocument extends DocumentConverter {

	protected DocumentBuilderFactory docFactory;
	protected DocumentBuilder docBuilder;
	
	// optional
	protected ToDocument parent;
	
	protected Document template;
	
	public ToDocument() {
		this.docFactory = DocumentBuilderFactory.newInstance();
		try {
			this.docBuilder = this.docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public abstract String getFromContentType();
	
	public ToDocument getParent() {
		return this.parent;
	}
	
	public void setParent(ToDocument parent) {
		this.parent = parent;
	}
	
	public abstract Object getContent();
	
	public abstract void setContent(Object content);
	
	public Document getTemplate() {
		return this.template;
	}

	public void setTemplate(Document template) {
		this.template = template;
	}
	
	public abstract Document convert() throws Exception;
	
	public Element process(Element templateElement) throws UnsupportedOperationException, Exception {
		throw new UnsupportedOperationException();
	}
	
	protected Element processUnsupported(Element templateElement) throws Exception {
		if (this.parent != null) {
			// try with parent
			return this.parent.process(templateElement);
		}
		
		// TODO: add better exception
		throw new Exception("Unsupported template element");
	}
	
	protected static Boolean isExpand(Element templateElement) {
		return templateElement.getAttribute("expand").equals("true");
	}

}

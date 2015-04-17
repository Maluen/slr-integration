package converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class TextToDocument extends Converter {

	protected String content;
	protected Document template;
	protected Document document;
	
	public static String getFromContentType() {
		// DUMMY (static methods can't be abstract)
		return null;
	}
	
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Document getTemplate() {
		return this.template;
	}

	public void setTemplate(Document template) {
		this.template = template;
	}
	
	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public abstract Document convert(String content, Document template);
	
	protected static Boolean isExpand(Element templateElement) {
		return templateElement.getAttribute("expand").equals("true");
	}

}

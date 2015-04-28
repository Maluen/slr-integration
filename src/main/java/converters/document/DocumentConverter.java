package converters.document;

import org.w3c.dom.Document;

public abstract class DocumentConverter {

	protected Document document;
	
	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public abstract Object convert() throws Exception;
	
}

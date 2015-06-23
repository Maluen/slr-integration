package converters.document;

import misc.Logger;

import org.w3c.dom.Document;

public abstract class DocumentConverter {

	protected Logger logger;
	protected Document document;
	
	public DocumentConverter() {
		this.logger = new Logger("DocumentConverter");
	}
	
	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public abstract Object convert() throws Exception;
	
}

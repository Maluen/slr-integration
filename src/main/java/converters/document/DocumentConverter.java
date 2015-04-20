package converters.document;

import javax.script.ScriptEngineManager;

import org.w3c.dom.Document;

public abstract class DocumentConverter {

	protected Document document;

	protected ScriptEngineManager scriptFactory;
	
	public DocumentConverter() {
	    // create a script engine manager
        this.scriptFactory = new ScriptEngineManager();
	}
	
	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public abstract Object convert() throws Exception;
	
}

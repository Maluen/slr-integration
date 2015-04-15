package services.extractors;

import org.w3c.dom.Document;

public interface Extractor {

	public Document extractFrom(String content, Document template);

	
}

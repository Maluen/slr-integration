package parsers.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentFactory {

	protected static DocumentBuilderFactory docFactory;
	protected static DocumentBuilder docBuilder;
	
	static {
		DocumentFactory.docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentFactory.docBuilder = DocumentFactory.docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static DocumentBuilderFactory getDocFactory() {
		return DocumentFactory.docFactory;
	}

	public static void setDocFactory(DocumentBuilderFactory docFactory) {
		DocumentFactory.docFactory = docFactory;
	}

	public static DocumentBuilder getDocBuilder() {
		return DocumentFactory.docBuilder;
	}

	public static void setDocBuilder(DocumentBuilder docBuilder) {
		DocumentFactory.docBuilder = docBuilder;
	}
	
}

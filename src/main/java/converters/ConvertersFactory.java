package converters;

public class ConvertersFactory {

	public static TextToDocument createTextToDocument(String textContentType) {
		if (textContentType.equals("text/html")) {
			return new HTMLtoDocument();
		} else if (textContentType.equals("text/json")) {
			return new JSONtoDocument();
		} else if (textContentType.equals("text/xml")) {
			return new XMLtoDocument();
		}

		return null;
	};
	
	public static DocumentToText createDocumentToText(String textContentType) {
		if (textContentType.equals("application/x-www-form-urlencoded")) {
			return new DocumentToForm();
		} else if (textContentType.equals("text/json")) {
			return new DocumentToJSON();
		}

		return null;
	};
	
}

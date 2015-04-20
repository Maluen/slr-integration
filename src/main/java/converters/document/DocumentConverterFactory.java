package converters.document;

import converters.document.from.DocumentToForm;
import converters.document.from.DocumentToJSON;
import converters.document.from.FromDocument;
import converters.document.to.HTMLtoDocument;
import converters.document.to.JSONtoDocument;
import converters.document.to.MixedToDocument;
import converters.document.to.SetCookiesToDocument;
import converters.document.to.ToDocument;
import converters.document.to.XMLtoDocument;

public class DocumentConverterFactory {

	public static ToDocument createToDocument(String fromContentType) {
		if (fromContentType.equals("text/html")) {
			return new HTMLtoDocument();
		} else if (fromContentType.equals("text/json")) {
			return new JSONtoDocument();
		} else if (fromContentType.equals("multipart/mixed")) {
			return new MixedToDocument();
		} else if (fromContentType.equals("application/x-www-response-cookies")) {
			return new SetCookiesToDocument();
		} else if (fromContentType.equals("text/xml")) {
			return new XMLtoDocument();
		}

		return null;
	};
	
	public static FromDocument createFromDocument(String toContentType) {
		if (toContentType.equals("application/x-www-form-urlencoded")) {
			return new DocumentToForm();
		} else if (toContentType.equals("text/json")) {
			return new DocumentToJSON();
		}

		return null;
	};
	
}

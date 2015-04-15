package services.extractors;

public class ExtractorsFactory {

	public static Extractor create(String contentType) {
		if (contentType.equals("text/html")) {
			return new HTMLExtractor();
		}

		return null;
	};
	
}

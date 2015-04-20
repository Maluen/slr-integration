package parsers.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import parsers.Parser;

public class HTMLParser extends Parser {

	public Document parse(String content) {
		return Jsoup.parse(content);
	}

}

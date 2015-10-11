package parsers.bibtext;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import misc.Utils;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXParser;
import org.jbibtex.LaTeXObject;
import org.jbibtex.LaTeXParser;
import org.jbibtex.LaTeXPrinter;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.w3c.dom.Document;

import parsers.Parser;

public class BibtexParser extends Parser {

	@Override
	// The content is parsed into an XML document having
	// "entries" as root element and an "item" element
	// for each entry in the BibTex.
	// The "item" childs are its fields and thus depend on the content.
	public Document parse(String content) {

		Reader reader = new StringReader(content);		
		BibTeXParser bibtexParser;
		try {
			bibtexParser = new BibTeXParser();
		} catch (TokenMgrException | ParseException e) {
			// invalid bibtex
			e.printStackTrace();
			return null;
		}
		BibTeXDatabase database = bibtexParser.parseFully(reader);

		// Convert to XML document
		List<Map<String, String>> stringListMap = this.bibtextDatabaseToStringListMap(database, false);
		Document document = Utils.stringListMapToDocument(stringListMap, "entries", "item");
		return document;
	}
	
	protected List<Map<String, String>> bibtextDatabaseToStringListMap(BibTeXDatabase database, Boolean convertLatex) {
		List<Map<String, String>> stringListMap = new ArrayList<Map<String, String>>();
		
		// iterate over all the BibTeX entries
		Map<org.jbibtex.Key, org.jbibtex.BibTeXEntry> entryMap = database.getEntries();
		Collection<org.jbibtex.BibTeXEntry> entries = entryMap.values();
		for (org.jbibtex.BibTeXEntry entry : entries) {
			
			Map<String, String> stringMap = new HashMap<String, String>();
			
			// iterate over all the entry fields
			Map<org.jbibtex.Key, org.jbibtex.Value> fields = entry.getFields();
			for (Map.Entry<org.jbibtex.Key, org.jbibtex.Value> field : fields.entrySet()) {
				String fieldKeyString = field.getKey().toString();
				String fieldValueString = this.bibtextValueToPlaintext(field.getValue(), convertLatex);
				stringMap.put(fieldKeyString, fieldValueString);
			}
			
			stringListMap.add(stringMap);
		}
		
		return stringListMap;
	}
	
	protected String bibtextValueToPlaintext(org.jbibtex.Value value, Boolean convertLatex) {
	    String string = value.toUserString();
	    
	    if ((string.indexOf('\\') > -1 || string.indexOf('{') > -1) && convertLatex) {
	        // LaTeX string that needs to be translated to plain text string
	    	// TODO: raises exception when latex is mixed with unicode!
	    	LaTeXParser latexParser;
			try {
				latexParser = new LaTeXParser();
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
	    	List<LaTeXObject> latexObjects;
			try {
				latexObjects = latexParser.parse(string);
			} catch (TokenMgrException | ParseException e) {
				// invalid latex
				e.printStackTrace();
				return null;
			}
	    	LaTeXPrinter latexPrinter = new LaTeXPrinter();
	    	string = latexPrinter.print(latexObjects);
	    }
		
	    return string;
	}

}

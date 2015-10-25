package parsers.bibtext;

import java.io.Reader;
import java.io.StringReader;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXParser;
import org.jbibtex.LaTeXObject;
import org.jbibtex.LaTeXParser;
import org.jbibtex.LaTeXPrinter;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.Parser;
import parsers.xml.DocumentFactory;

public class BibtexParser extends Parser {

	public static Pattern fieldPattern = Pattern.compile("(?<key>.+?)(?:\\s+?)=(?:\\s+?)\"(?<value>[\\s\\S]*?)(?<!\\\\)\"(?<comma>,?)\r?\n");
	
	@Override
	// The content is parsed into an XML document having
	// "entries" as root element and an "item" element
	// for each entry in the BibTex.
	// The "item" childs are its fields and thus depend on the content.
	public Document parse(String content) {

		/*try {
			Utils.saveText(content, "data/bibtex/bibtex.bib");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		content = this.fixBibtexMultipleFields(content);
		content = this.fixBibtexSpecialChars(content);
		
		/*try {
			Utils.saveText(content, "data/bibtex/bibtex-fixed.bib");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		// read content by skipping illegal characters
		Reader reader = new org.jbibtex.CharacterFilterReader(new StringReader(content));
		
		BibTeXParser bibtexParser;
		try {
			bibtexParser = new BibTeXParser();
		} catch (TokenMgrException | ParseException e) {
			// invalid bibtex
			e.printStackTrace();
			return null;
		}
		BibTeXDatabase database = bibtexParser.parseFully(reader); // (skips exceptions)
		
		//for (Exception e : bibtexParser.getExceptions()) {
		//	e.printStackTrace();
		//}

		// Convert to XML document
		Document document = this.bibtextDatabaseToDocument(database, true);
		
		/*try {
			Utils.saveDocument(document, "data/bibtex/bibtex.xml");
		} catch (TransformerFactoryConfigurationError | TransformerException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		return document;
	}
	
	// Hack for fixing incorrect bibtex documents containing
	// multiple fields with the same key name.
	// The multiple fields are merged into a single one with comma separated values.
	// Assumptions:
	// - field format: key = "value"
	// - field ends with comma and newline, entry last field always ends without the comma.
	public String fixBibtexMultipleFields(String target) {
		StringBuffer newTargetBuffer = new StringBuffer();
		
		Map<String, String> fieldsMap = new HashMap<String, String>();
		
		Pattern pattern = BibtexParser.fieldPattern;
		Matcher matcher = pattern.matcher(target);
		while (matcher.find()) {
			//System.out.println(matcher.group());
			
			String fieldKey = matcher.group("key");
			String fieldValue = matcher.group("value");
			Boolean isLastField = matcher.group("comma").isEmpty();
			
			//System.out.println(fieldKey + ": " + fieldValue);
			//System.out.println("Is last: " + isLastField.toString());
			//System.out.println();
			
			String replacement = "";
			
			// Save fields
			if (fieldsMap.containsKey(fieldKey)) {
				// duplicate key => merge the value
				fieldsMap.put(fieldKey, fieldsMap.get(fieldKey) + ", " + fieldValue);
			} else {
				fieldsMap.put(fieldKey, fieldValue);
			}
			
			// Create replacement
			if (!isLastField) {
				
				// the global replacement is calculated
				// after the whole entry has been processed
				// (this works since fields are one after another)
				replacement = "";
			} else { // entry last field
				
				// the whole entry has been processed
				// => create the replacement
				Iterator<Map.Entry<String, String>> fieldsMapIterator = fieldsMap.entrySet().iterator();
			    while (fieldsMapIterator.hasNext()) {
			    	Map.Entry<String, String> field = fieldsMapIterator.next();
			    	
			    	replacement += field.getKey() + " = \"" + field.getValue() + "\"";
			    	if (fieldsMapIterator.hasNext()) {
			    		replacement += ",";
			    	}
			    	replacement += "\n";
			    }
				
				// prepare for the next entry (if any)
				fieldsMap.clear();
			}
			matcher.appendReplacement(newTargetBuffer, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(newTargetBuffer);
		
		return newTargetBuffer.toString();
	}
	
	// Get rid of accents and convert a whole string to regular letters
	// http://stackoverflow.com/a/3322174
	protected String fixBibtexSpecialChars(String target) {
		target = Normalizer.normalize(target, Normalizer.Form.NFD);
		target = target.replaceAll("\\p{M}", "");
		return target;
	}
	
	protected Document bibtextDatabaseToDocument(BibTeXDatabase database, Boolean convertLatex) {
		Document document = DocumentFactory.getDocBuilder().newDocument();
		
		Element rootEl = document.createElement("entries");
		document.appendChild(rootEl);
		
		// iterate over all the BibTeX entries
		Map<org.jbibtex.Key, org.jbibtex.BibTeXEntry> entryMap = database.getEntries();
		Collection<org.jbibtex.BibTeXEntry> entries = entryMap.values();
		for (org.jbibtex.BibTeXEntry entry : entries) {
			
			Element entryEl = document.createElement("item");
			rootEl.appendChild(entryEl);
			
			// iterate over all the entry fields
			Map<org.jbibtex.Key, org.jbibtex.Value> fields = entry.getFields();
			for (Map.Entry<org.jbibtex.Key, org.jbibtex.Value> field : fields.entrySet()) {
				String fieldKeyString = field.getKey().toString();
				String fieldValueString = this.bibtextValueToPlaintext(field.getValue(), convertLatex);
				
				Element fieldEl = document.createElement(fieldKeyString);
				fieldEl.setTextContent(fieldValueString);
				entryEl.appendChild(fieldEl);
			}
		}
		
		return document;
	}
	
	protected String bibtextValueToPlaintext(org.jbibtex.Value value, Boolean convertLatex) {
	    String string = value.toUserString();
	    
	    if ((string.indexOf('\\') > -1 || string.indexOf('{') > -1) && convertLatex) {
	        // LaTeX string that needs to be translated to plain text string
	    	// NOTE: raises exception when latex is mixed with unicode special characters!
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

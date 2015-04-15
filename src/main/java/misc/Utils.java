package misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;


public class Utils {

	@SuppressWarnings("unchecked")
	public static <K,V> Map<K,V> castMap(Map<?, ?> map, Class<K> kClass, Class<V> vClass) {
	    for (Map.Entry<?, ?> entry : map.entrySet()) {
	        kClass.cast(entry.getKey());
	        vClass.cast(entry.getValue());
	    }
	    return (Map<K,V>) map;
	}
	
	// Get file from resources folder
	public static File getResource(String resourcePath) {
		ClassLoader classLoader = Utils.class.getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		return file;
	}
	
	public static String getFileContent(File file, Charset encoding) throws IOException {
		Path path = file.toPath();
		List<String> lines = Files.readAllLines(path, encoding);
		String content = StringUtils.join(lines, "\n");
		return content;
	}
	
	// uses default encoding
	public static String getFileContent(File file) throws IOException {
		return getFileContent(file, StandardCharsets.UTF_8);
	}
	
	public static void saveText(String content, String path) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.write(content);
		writer.close();
	}
	
	// http://stackoverflow.com/a/4561785
	public static void saveDocument(Document document, String path) throws TransformerFactoryConfigurationError, TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result output = new StreamResult(new File(path));
		Source input = new DOMSource(document);
		
		// enable indentation (pretty printing)
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(input, output);
	}
	
}

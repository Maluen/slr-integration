package misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	
	// create list with single element
	public static <T> List<T> createList(T element) {
		List<T> list = new ArrayList<T>();
		list.add(element);
		return list;
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
	
	public static Boolean doFileExists(String path) {
		File file = new File(path);
		return file.exists() && !file.isDirectory();
	}
	
	public static void saveText(String content, String path) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.write(content);
		writer.close();
	}
	
	// Create path to file if it doesn't exist
	protected static File createPathToFile(String path) throws IOException {
		File file = new File(path);
		
		Path pathToFile = Paths.get(path);
		Files.createDirectories(pathToFile.getParent());
		if (!file.exists()) {
			file.createNewFile();
		}
		
		return file;
	}
	
	// http://stackoverflow.com/a/4561785
	public static void saveDocument(Document document, String path) throws TransformerFactoryConfigurationError, TransformerException, IOException {

		Utils.createPathToFile(path);
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result output = new StreamResult(new File(path));
		Source input = new DOMSource(document);
		
		// enable indentation (pretty printing)
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(input, output);
	}
	
	// Create a PrintStream for the file specified by path, with
	// append or auto-creation if it doesn't exist.
	public static PrintStream createFilePrinter(String path) throws IOException {
		File file = Utils.createPathToFile(path);
		return new PrintStream(new FileOutputStream(file, true));
	}
	
	public static String simplify(String target) {
		return target
			   .replaceAll("[^A-Za-z0-9\\s]", "") // remove any non-alphanumeric and non-whitespace character
			   .toLowerCase()
			   .replaceAll("\\s+", " ")
			   .trim();
	}
	
}

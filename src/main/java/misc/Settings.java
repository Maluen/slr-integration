package misc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import parsers.xml.DocumentFactory;
import parsers.xml.XMLParser;

public class Settings {
	
	// singleton
	protected static Settings instance;
	
	public String settingsFilePath = "data/settings.xml";
	public Map<String, String> settingsMap;
	public Map<String, String> settingsDescriptionMap;
	
	protected Settings() {
		this.settingsMap = new HashMap<String, String>();
		
		this.settingsDescriptionMap = new HashMap<String, String>();
		this.settingsDescriptionMap.put("springerApiKey", "Springer Link API Key");
		
		// load settings on init
		if (this.doFileExists()) {
			try {
				this.loadFromFile();
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Settings getInstance() {
		if (Settings.instance == null) {
			Settings.instance = new Settings();
		}
		
		return Settings.instance;
	}
	
	public boolean has(String settingsName) {
		return this.settingsMap.containsKey(settingsName);
	}
	
	public String get(String settingsName) {
		if (this.settingsMap.containsKey(settingsName)) {
			return this.settingsMap.get(settingsName);
		}
		
		return null;
	}
	
	public void set(String settingsName, String settingsValue) {
		this.settingsMap.put(settingsName, settingsValue);
	}
	
	public boolean doFileExists() {
		File settingsFile = new File(this.settingsFilePath);
		return settingsFile.exists() && !settingsFile.isDirectory();
	}
	
	public void loadFromFile() throws SAXException, IOException {
		this.settingsMap.clear();
		
		Document document = DocumentFactory.getDocBuilder().parse(this.settingsFilePath);
		
		List<Element> settingsEList = XMLParser.getChildElements(document.getDocumentElement());
		for (Element settingsEl : settingsEList) {
			String settingsName = settingsEl.getTagName();
			String settingsValue = settingsEl.getTextContent().trim();
			
			this.settingsMap.put(settingsName, settingsValue);
		}
	}
	
	public void saveToFile() throws TransformerFactoryConfigurationError, TransformerException, IOException {
		Document document = DocumentFactory.getDocBuilder().newDocument();
		
		Element documentRoot = document.createElement("settings");
		document.appendChild(documentRoot);
		
		for (Map.Entry<String, String> settingsMapEntry : this.settingsMap.entrySet()) {
			String settingsName = settingsMapEntry.getKey();
			String settingsValue = settingsMapEntry.getValue();
			
			Element settingsEl = document.createElement(settingsName);
			settingsEl.setTextContent(settingsValue);
			documentRoot.appendChild(settingsEl);
		}
		
		Utils.saveDocument(document, this.settingsFilePath);
	}
	
}

package search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import main.Main;
import misc.JavaProcess;
import misc.Utils;
import network.websocket.WebSocketClient;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.DocumentFactory;

public class RemoteSearch {
	
	private WebSocketClient clientEndPoint;
	
	public void start(String url, String machineId, String machineName, String machinePassword) throws Exception {
		System.out.println("Connecting to " + url);
		
        this.clientEndPoint = new WebSocketClient(new URI(url));

        final RemoteSearch self = this;
        this.clientEndPoint.addMessageHandler(new WebSocketClient.MessageHandler() {
	        public void handleMessage(String message) {
	        	System.out.println("Received: " + message);
	            JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
	            
	            String topic = jsonObject.getString("topic");
	            JsonObject detail = jsonObject.getJsonObject("detail");
	            self.handleMessage(topic, detail);
	        }
        });
        
        System.out.println("Connected.");
        
        this.login(machineId, machineName, machinePassword);
        
        while (true) {
        	// HACK: keep connection open
        }
	}
	
	private void handleMessage(String topic, JsonObject detail) {
		if (topic.equals("loginError")) {
			System.out.println("Authentication failed: " + detail.getString("message"));			
			
		} else if (topic.equals("loginSuccess")) {
			System.out.println("Authentication successful.");
			
		} else if (topic.equals("startSearch")) {
			System.out.println("Starting search...");
			
			JsonObject project = detail.getJsonObject("project");
			JsonObject search = detail.getJsonObject("search");
			
			String searchId = search.getString("id");
			
			JsonArray projectSettings = project.getJsonArray("settings");
			JsonArray searchSettings = search.getJsonArray("settings");

			this.saveProjectSettings(searchId, projectSettings);
			this.saveSearchSettings(searchId, searchSettings);
			
			try {
				System.out.println("Executing remote process...");
				Process process = JavaProcess.exec(Main.class, "-h");
				
				InputStream stdout = process.getInputStream ();
				BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
				String line;
				while ((line = reader.readLine ()) != null) {
					System.out.println ("Stdout: " + line);
					this.sendOutputLine(line);
				}
				
		        process.waitFor();
		        int status = process.exitValue();
				System.out.println("Remote process exited: " + status);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void login(String machineId, String machineName, String machinePassword) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("id", machineId)
        		.add("name", machineName)
        		.add("password", machinePassword);
        this.sendMessage("login", detail);
	}
	
	private void saveProjectSettings(String searchId, JsonArray projectSettings) {
		Document document = DocumentFactory.getDocBuilder().newDocument();
		
		// root element
		Element documentRootEl = document.createElement("settings");
		document.appendChild(documentRootEl);
		
		for (int i=0; i<projectSettings.size(); i++) {
			JsonObject setting = (JsonObject) projectSettings.get(i);
			String settingName = setting.getString("name");
			String settingValue = setting.getString("value");
			
			Element settingEl = document.createElement(settingName);
			settingEl.setTextContent(settingValue);
			documentRootEl.appendChild(settingEl);
		}
		
		try {
			Utils.saveDocument(document, "data/" + searchId + "/project.xml");
		} catch (TransformerFactoryConfigurationError
				| TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void saveSearchSettings(String searchId, JsonArray searchSettings) {
		Document document = DocumentFactory.getDocBuilder().newDocument();
		
		// root element
		Element documentRootEl = document.createElement("search");
		document.appendChild(documentRootEl);
		
		for (int i=0; i<searchSettings.size(); i++) {
			JsonObject setting = (JsonObject) searchSettings.get(i);
			String settingName = setting.getString("name");
			String settingValue = setting.getString("value");
			
			Element settingEl = document.createElement(settingName);
			settingEl.setTextContent(settingValue);
			documentRootEl.appendChild(settingEl);
		}
		// output path
		Element outputPathEl = document.createElement("outputpath");
		outputPathEl.setTextContent("data/" + searchId + "/output/output.csv");
		documentRootEl.appendChild(outputPathEl);
		
		try {
			Utils.saveDocument(document, "data/" + searchId + "/search.xml");
		} catch (TransformerFactoryConfigurationError
				| TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void sendOutputLine(String outputLine) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("line", outputLine);
		this.sendMessage("outputLine", detail);
	}
	
	
	private void sendMessage(String topic, JsonObjectBuilder detail) {
		this.clientEndPoint.sendMessage(this.createMessage(topic, detail));
	}
	
    /**
     * Create a json representation.
     * 
     * @param message
     * @return
     */
    private String createMessage(String topic, JsonObjectBuilder detail) {
        return Json.createObjectBuilder()
                       .add("type", "machine")
                       .add("topic", topic)
                       .add("detail", detail)
                   .build()
                   .toString();
    }
	
}

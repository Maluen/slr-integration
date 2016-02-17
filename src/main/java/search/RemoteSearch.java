package search;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Calendar;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import main.Main;
import misc.JavaProcess;
import misc.Utils;
import network.websocket.WebSocketClient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

			Boolean resume = detail.getBoolean("resume");
			
			String searchId = search.getString("id");
			
			String workingDirectory = "data/remote/" + searchId;

			String projectSettingsPath = workingDirectory + "/data/settings.xml";
			String searchSettingsPath = workingDirectory + "/data/search.xml";
			String outputPath = workingDirectory + "/data/output/output.csv";
			
			JsonArray projectSettings = project.getJsonArray("settings");
			JsonArray searchSettings = search.getJsonArray("settings");
			
			this.saveProjectSettings(projectSettings, projectSettingsPath);
			this.saveSearchSettings(searchSettings, searchSettingsPath, outputPath);
			
			// replace engines
			try {
				FileUtils.deleteDirectory(new File(workingDirectory + "/data/engines"));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				FileUtils.copyDirectory(new File("data/engines"), new File(workingDirectory + "/data/engines"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				System.out.println("Executing remote process...");

				// new search
				String[] args = new String[] {
					!resume ? "-n" : "",
					//"-h"
				};
				System.out.println(StringUtils.join(args, " "));
				final Process process = JavaProcess.exec(Main.class, StringUtils.join(args, " "), new File(workingDirectory));
				
				// exit child process on parent exit
				// (won't work in case of OS kills, etc.)
				Thread closeChildThread = new Thread() {
				    public void run() {
				    	process.destroy();
				    }
				};
				Runtime.getRuntime().addShutdownHook(closeChildThread);
				
				this.sendSearchStatus("running");
				
				/*InputStream stdout = process.getInputStream ();
				BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
				String line;
				while ((line = reader.readLine ()) != null) {
					System.out.println ("Stdout: " + line);
					this.sendOutputLine(line);
				}*/
				
				// redirect stdout and stderr to websocket
			    this.redirectIO(process.getInputStream());
			    this.redirectIO(process.getErrorStream());
				
		        process.waitFor();
		        int status = process.exitValue();
				System.out.println("Remote process exited: " + status);
				
				if (status == 0) {
					this.sendSearchStatus("success");
				} else {
					this.sendSearchStatus("failure");
				}
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
	
	private void saveProjectSettings(JsonArray projectSettings, String path) {
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
			Utils.saveDocument(document, path);
		} catch (TransformerFactoryConfigurationError
				| TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void saveSearchSettings(JsonArray searchSettings, String path, String outputPath) {
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
		outputPathEl.setTextContent(outputPath);
		documentRootEl.appendChild(outputPathEl);
		
		try {
			Utils.saveDocument(document, path);
		} catch (TransformerFactoryConfigurationError
				| TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	// based on http://stackoverflow.com/a/14168097
	private void redirectIO(final InputStream src) {
		final RemoteSearch self = this;
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(src);
	            while (sc.hasNextLine()) {
	            	String line = sc.nextLine();
					System.out.println ("Output: " + line);
					self.sendOutputLine(line);
	            }
	            sc.close();
	        }
	    }).start();
	}
	
	private void sendSearchStatus(String searchStatus) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("status", searchStatus);
		this.sendMessage("searchStatus", detail);
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
                       .add("timestamp", Calendar.getInstance().getTimeInMillis())
                   .build()
                   .toString();
    }
	
}

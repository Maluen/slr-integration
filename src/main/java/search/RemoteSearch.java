package search;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import main.Main;
import misc.JavaProcess;
import misc.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parsers.xml.DocumentFactory;

@ClientEndpoint
public class RemoteSearch {
    private Session userSession = null;
    
    private String url;
    private String machineId;
    private String machineName;
    private String machinePassword;
 
    private Map<String, Process> searchProcessMap = new HashMap<String, Process>();
    private String currentSearchId = null; // TODO: support multiple searches
	
    private Boolean connectionCompleted = false;
    private List<String> pendingOutMessages = new ArrayList<String>();
	
	public RemoteSearch(String url, String machineId, String machineName, String machinePassword) {
		this.url = url;
		this.machineId = machineId;
		this.machineName = machineName;
		this.machinePassword = machinePassword;
	}
	
	public void start() throws Exception {	
		System.out.println("Connecting to " + this.url);
        
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		
		Boolean connected = false;
		while (!connected) {
	        try {
	            container.connectToServer(this, new URI(this.url));
	            connected = true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("Reconnecting in 5 seconds...");
	            Thread.sleep(5000);
	        }
		}
        
        while (true) {
        	// HACK: keep connection open
        }
	}
 
    /**
     * Callback hook for Connection open events.
     * 
     * @param userSession
     *            the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
        System.out.println("Connected.");
        
        this.login(this.machineId, this.machineName, this.machinePassword);
    }
    
    /**
     * Callback hook for Connection close events.
     * 
     * @param userSession
     *            the userSession which is getting closed.
     * @param reason
     *            the reason for connection close
     * @throws Exception 
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) throws Exception {
    	System.out.println("Websocket: disconnected.");
        this.userSession = null;
        this.connectionCompleted = false;
        
        // auto reconnect
        this.start();
    }
    
    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }
    
	private void login(String machineId, String machineName, String machinePassword) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("id", machineId)
        		.add("name", machineName)
        		.add("password", machinePassword)
        		.add("currentSearchId", this.currentSearchId);
        this.sendMessage("login", detail, true);
	}
 
    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     * 
     * @param message
     *            The text message
     */
    @OnMessage
    public void onMessage(String message) {
    	System.out.println("Received: " + message);
        JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
        
        String topic = jsonObject.getString("topic");
        JsonObject detail = jsonObject.getJsonObject("detail");
        this.handleMessage(topic, detail);
        
        System.out.println("Message handled.");
    }

	private void handleMessage(String topic, JsonObject detail) {
		if (topic.equals("loginError")) {
			System.out.println("Authentication failed: " + detail.getString("message"));
			System.exit(1);
			return;
			
		} else if (topic.equals("loginSuccess")) {
			System.out.println("Authentication successful.");
			
			this.connectionCompleted = true;
			// send queued messages
			for (String rawMessage : this.pendingOutMessages) {
				this.sendRawMessage(rawMessage);
			}
			this.pendingOutMessages.clear();
			
		} else if (topic.equals("startSearch")) {
			System.out.println("Starting search...");
			
			JsonObject project = detail.getJsonObject("project");
			JsonObject search = detail.getJsonObject("search");

			Boolean resume = detail.getBoolean("resume");
			
			final String searchId = search.getString("id");
			
			String workingDirectory = "data/remote/" + searchId;

			String projectSettingsPath = workingDirectory + "/data/settings.xml";
			String searchSettingsPath = workingDirectory + "/data/search.xml";
			
			final String internalOutputPath = "data/output/output.csv"; // relative path as needed inside the process (where working path is already set)
			final String outputPath = workingDirectory + "/" + internalOutputPath;
			
			JsonArray projectSettings = project.getJsonArray("settings");
			JsonArray searchSettings = search.getJsonArray("settings");
			
			this.saveProjectSettings(projectSettings, projectSettingsPath);
			this.saveSearchSettings(searchSettings, searchSettingsPath, internalOutputPath);
			
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
					//"-h",
					//"-c"
				};
				System.out.println(StringUtils.join(args, " "));
				final Process process = JavaProcess.exec(Main.class, StringUtils.join(args, " "), new File(workingDirectory));
				
				this.searchProcessMap.put(searchId, process);
				
				// exit child process on parent exit
				// (won't work in case of OS kills, etc.)
				final RemoteSearch self = this;
				final Thread closeChildThread = new Thread() {
				    public void run() {
				    	self.stopSearch(searchId);
				    }
				};
				Runtime.getRuntime().addShutdownHook(closeChildThread);
				
				this.currentSearchId = searchId;
				this.sendSearchStatus("running", searchId);
				
				/*InputStream stdout = process.getInputStream ();
				BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
				String line;
				while ((line = reader.readLine ()) != null) {
					System.out.println ("Stdout: " + line);
					this.sendOutputLine(line);
				}*/
				
				// redirect stdout and stderr to websocket
			    this.redirectSearchIO(process.getInputStream(), searchId);
			    this.redirectSearchIO(process.getErrorStream(), searchId);
				
				Thread processThread = new Thread(new Runnable() {
				    public void run() {
				    	try {
							process.waitFor();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	
				        int status = process.exitValue();
						System.out.println("Remote process exited: " + status);
						
						if (status == 0) {
							self.sendSearchStatus("success", searchId);

							File resultCSVFile = new File(outputPath);
							if (resultCSVFile.exists()) {
								try {
									String resultCSV = FileUtils.readFileToString(new File(outputPath));
									self.sendSearchResult(resultCSV, searchId);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else {
							self.sendSearchStatus("failure", searchId);
						}
						
						Runtime.getRuntime().removeShutdownHook(closeChildThread);
						self.searchProcessMap.remove(searchId);
				    }
				});
				processThread.start();

			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (topic.equals("stopSearch")) {
			String searchId = detail.getString("searchId");
			this.stopSearch(searchId);
		}
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
	
	private void saveSearchSettings(JsonArray searchSettings, String path, String internalOutputPath) {
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
		outputPathEl.setTextContent(internalOutputPath);
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
	private void redirectSearchIO(final InputStream src, final String searchId) {
		final RemoteSearch self = this;
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(src);
	            while (sc.hasNextLine()) {
	            	String line = sc.nextLine();
					System.out.println ("Output: " + line);
					self.sendSearchOutputLine(line, searchId);
	            }
	            sc.close();
	            
	            System.out.println("Redirect " + src.toString() + ": ended.");
	        }
	    }).start();
	}
	
	private void stopSearch(String searchId) {
		System.out.println("Stopping search...");
		
		if (!this.searchProcessMap.containsKey(searchId)) {
			System.out.println("No matching process for the specified search.");
			return;
		}
		
		Process process = this.searchProcessMap.get(searchId);

		process.destroy();

		// streams need to be closed or threads will remain open
		// and the process won't ever close properly.
    	try {
			process.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
	    	process.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
	    	process.getErrorStream().close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	System.out.println("Stopped.");
	}
    
	private void sendSearchStatus(String searchStatus, String searchId) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("status", searchStatus)
        		.add("searchId", searchId);
		this.sendMessage("searchStatus", detail);
	}
	
	private void sendSearchOutputLine(String outputLine, String searchId) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("line", outputLine)
        		.add("searchId", searchId);
		this.sendMessage("outputLine", detail);
	}
	
	private void sendSearchResult(String csv, String searchId) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("csv", csv)
        		.add("searchId", searchId);
		this.sendMessage("searchResult", detail);
	}
	
	private void sendMessage(String topic, JsonObjectBuilder detail) {
		this.sendMessage(topic, detail, false);
	}
    
	private void sendMessage(String topic, JsonObjectBuilder detail, Boolean connectionMessage) {
		String rawMessage = this.createMessage(topic, detail);
		
		if (!this.connectionCompleted && !connectionMessage) {
			this.pendingOutMessages.add(rawMessage);
			return;
		}
		
		this.sendRawMessage(rawMessage);
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
    
    private void sendRawMessage(String rawMessage) {
    	this.userSession.getAsyncRemote().sendText(rawMessage);
    }
}

/*
public class RemoteSearch {
	
	private WebSocketClient clientEndPoint;
	
	Map<String, Process> searchProcessMap = new HashMap<String, Process>();
	
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
	            
	            System.out.println("Message handled.");
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
			System.exit(1);
			return;
			
		} else if (topic.equals("loginSuccess")) {
			System.out.println("Authentication successful.");
			
		} else if (topic.equals("startSearch")) {
			System.out.println("Starting search...");
			
			JsonObject project = detail.getJsonObject("project");
			JsonObject search = detail.getJsonObject("search");

			Boolean resume = detail.getBoolean("resume");
			
			final String searchId = search.getString("id");
			
			String workingDirectory = "data/remote/" + searchId;

			String projectSettingsPath = workingDirectory + "/data/settings.xml";
			String searchSettingsPath = workingDirectory + "/data/search.xml";
			
			final String internalOutputPath = "data/output/output.csv"; // relative path as needed inside the process (where working path is already set)
			final String outputPath = workingDirectory + "/" + internalOutputPath;
			
			JsonArray projectSettings = project.getJsonArray("settings");
			JsonArray searchSettings = search.getJsonArray("settings");
			
			this.saveProjectSettings(projectSettings, projectSettingsPath);
			this.saveSearchSettings(searchSettings, searchSettingsPath, internalOutputPath);
			
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
					//"-h",
					//"-c"
				};
				System.out.println(StringUtils.join(args, " "));
				final Process process = JavaProcess.exec(Main.class, StringUtils.join(args, " "), new File(workingDirectory));
				
				this.searchProcessMap.put(searchId, process);
				
				// exit child process on parent exit
				// (won't work in case of OS kills, etc.)
				final RemoteSearch self = this;
				final Thread closeChildThread = new Thread() {
				    public void run() {
				    	self.stopSearch(searchId);
				    }
				};
				Runtime.getRuntime().addShutdownHook(closeChildThread);
				
				this.sendSearchStatus("running");
				
				// redirect stdout and stderr to websocket
			    this.redirectIO(process.getInputStream());
			    this.redirectIO(process.getErrorStream());
				
				Thread processThread = new Thread(new Runnable() {
				    public void run() {
				    	try {
							process.waitFor();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	
				        int status = process.exitValue();
						System.out.println("Remote process exited: " + status);
						
						if (status == 0) {
							self.sendSearchStatus("success");

							File resultCSVFile = new File(outputPath);
							if (resultCSVFile.exists()) {
								try {
									String resultCSV = FileUtils.readFileToString(new File(outputPath));
									self.sendSearchResult(resultCSV);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else {
							self.sendSearchStatus("failure");
						}
						
						Runtime.getRuntime().removeShutdownHook(closeChildThread);
				    }
				});
				processThread.start();

			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (topic.equals("stopSearch")) {
			String searchId = detail.getString("searchId");
			this.stopSearch(searchId);
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
	
	private void saveSearchSettings(JsonArray searchSettings, String path, String internalOutputPath) {
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
		outputPathEl.setTextContent(internalOutputPath);
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
	            
	            System.out.println("Redirect " + src.toString() + ": ended.");
	        }
	    }).start();
	}
	
	private void stopSearch(String searchId) {
		System.out.println("Stopping search...");
		
		if (!this.searchProcessMap.containsKey(searchId)) {
			System.out.println("No matching process for the specified search.");
			return;
		}
		
		Process process = this.searchProcessMap.remove(searchId);

		process.destroy();

		// streams need to be closed or threads will remain open
		// and the process won't ever close properly.
    	try {
			process.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
	    	process.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
	    	process.getErrorStream().close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	System.out.println("Stopped.");
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
	
	private void sendSearchResult(String csv) {
        JsonObjectBuilder detail = Json.createObjectBuilder()
        		.add("csv", csv);
		this.sendMessage("searchResult", detail);
	}

	private void sendMessage(String topic, JsonObjectBuilder detail) {
		this.clientEndPoint.sendMessage(this.createMessage(topic, detail));
	}

    private String createMessage(String topic, JsonObjectBuilder detail) {
        return Json.createObjectBuilder()
                       .add("type", "machine")
                       .add("topic", topic)
                       .add("detail", detail)
                       .add("timestamp", Calendar.getInstance().getTimeInMillis())
                   .build()
                   .toString();
    }
	
}*/

package frontend.cli;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import misc.Settings;
import network.websocket.WebSocketClient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import search.MixedSearch;
import search.RemoteSearch;
import frontend.UI;

public class CommandLineUI extends UI {

	protected String programName = "slr-integration";
	protected Options options;
	protected Settings settings;
	
	public CommandLineUI() {
		this.options = this.createOptions();
		
		this.settings = Settings.getInstance();
	}
	
	protected Options createOptions() {
		Options options = new Options();
		
		options.addOption("n", "newsearch", false, "Start a new search");
		
		options.addOption("f", "file", true, "Load search configuration from file");
		
		options.addOption("q", "query", true, "Set the query to search for");
		options.addOption("s", "sites", true, "Set sites where to perform the search as a comma separated list");
		options.addOption("sy", "startyear", true, "Set article publishing year start range");
		options.addOption("ey", "endyear", true, "Set article publishing year end range");
		options.addOption("fo", "fastoutput", false, "Display only the number of results for each database");
		options.addOption("o", "outputpath", true, "Set CSV output path");

		options.addOption("c", "configure", false, "Create or update settings file");
		
		options.addOption("co", "connect", true, "Connect to the remote webapp (host:port)");
		
		options.addOption("h", "help", false, "Show this help");
		
		return options;
	}
	
	public void createSettingsFileInteractively() {
		System.out.println("== Interactive settings file creation ==");
		
	    Scanner scanner = new Scanner(System.in);
		for (Map.Entry<String, String> settingsDescriptionEntry : this.settings.settingsDescriptionMap.entrySet()) {
			String settingsName = settingsDescriptionEntry.getKey();
			String settingsDescription = settingsDescriptionEntry.getValue();
			
			// (in case this is an update)
			String settingsDefaultValue = "";
			if (this.settings.has(settingsName)) {
				settingsDefaultValue = this.settings.get(settingsName);
			}
			
			// get setting value from user
			System.out.print(settingsDescription + " ("+settingsDefaultValue+"): ");
			String settingsValue = scanner.nextLine();
			if (settingsValue == null || settingsValue.isEmpty()) {
				settingsValue = settingsDefaultValue;
			}
			
			this.settings.set(settingsName, settingsValue);
		}
		scanner.close();
		
		try {
			this.settings.saveToFile();
		} catch (TransformerFactoryConfigurationError | TransformerException
				| IOException e) {
			e.printStackTrace();
			System.out.println("== ERROR: Settings file creation failed ==");
			System.exit(1);
			return;
		}
		
		System.out.println("== Settings file created ==\n");
	}
	
	@Override
	public void execute() {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( this.options, this.args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
			return;
		}
		
		if (!this.settings.doFileExists() || cmd.hasOption("configure")) {
			this.createSettingsFileInteractively();
			System.exit(0);
			return;
		}
		
		if (cmd.hasOption("help")) {
			// show help
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(null); // keep the options in the order they were declared.
			formatter.printHelp(this.programName, this.options);
			System.exit(0);
			return;
		}
		
		if (cmd.hasOption("connect")) {
			String url;
			String machineId;
			String machineName;
			String machinePassword;

			/*
			url = cmd.getOptionValue("connect");
			if (url == null || url.isEmpty()) {
				System.out.println("Missing connect hostname.");
				System.exit(1);
				return;
			}

			Scanner scanner = new Scanner(System.in);
			System.out.print("Machine name: ");
			machineId = scanner.nextLine();
			System.out.print("Machine name: ");
			machineName = scanner.nextLine();
			System.out.print("Machine password: ");
			machinePassword = scanner.nextLine();
			scanner.close();*/
			
			// DEBUG
			machineId = "56bc5291afc769d42517fc55";
			machineName = "debug";
			machinePassword = "always";
			url = "ws://localhost:7667";

			RemoteSearch remoteSearch = new RemoteSearch();
			try {
				remoteSearch.start(url, machineId, machineName, machinePassword);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
				return;
			}
			return;
		}
		
		MixedSearch mixedSearch = new MixedSearch();
		Boolean isResumable = mixedSearch.isResumable();
		
		if (this.args.length > 0 && !cmd.hasOption("newsearch") && isResumable) {
			System.out.println("Parameters ignored. Explicitly use the \"newsearch\" parameter to start a new search.");
			System.exit(1);
			return;
		}
		
		Boolean isNewSearch = !isResumable || cmd.hasOption("newsearch");
		if (isNewSearch) {
			this.configureNewSearch(mixedSearch, cmd);
			mixedSearch.newSearch();
			
		} else {
			// Resume
			mixedSearch.resume();
		}		
	}
	
	protected void configureNewSearch(MixedSearch mixedSearch, CommandLine cmd) {
		
		// read from file
		String configurationPath = cmd.getOptionValue("file");
		if (configurationPath == null) {
			// default
			configurationPath = "data/search.xml";
		}
		try {
			mixedSearch.loadConfiguration(configurationPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return;
		}
		
		// read from command-line parameters
		// (eventually overriding some of the file values)
		
		if (cmd.hasOption("query")) mixedSearch.setQueryText(cmd.getOptionValue("query"));
		
		if (cmd.hasOption("sites")) {
			String[] sites = cmd.getOptionValue("sites").trim().split("\\s*,\\s*");
			mixedSearch.setSites(sites);
		}
		
		if (cmd.hasOption("startyear")) mixedSearch.setStartYear( Integer.parseInt(cmd.getOptionValue("startyear")) );
		if (cmd.hasOption("endyear")) mixedSearch.setEndYear( Integer.parseInt(cmd.getOptionValue("endyear")) );
	
		if (cmd.hasOption("fastoutput")) mixedSearch.setFastOutput(true);
		if (cmd.hasOption("outputpath")) mixedSearch.setOutputCSVFilename(cmd.getOptionValue("outputpath"));
		
	}
	
}

package frontend.cli;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import search.MixedSearch;
import frontend.UI;

public class CommandLineUI extends UI {

	String programName = "slr-integration";
	protected Options options;
	
	public CommandLineUI() {
		this.options = this.createOptions();
	}
	
	protected Options createOptions() {
		Options options = new Options();
		
		options.addOption("n", "newsearch", false, "Start a new search");
		
		options.addOption("f", "file", true, "Load configuration from file");
		
		options.addOption("q", "query", true, "Set the query to search for");
		options.addOption("s", "sites", true, "Set sites where to perform the search as a comma separated list");
		options.addOption("sy", "startyear", true, "Set article publishing year start range");
		options.addOption("ey", "endyear", true, "Set article publishing year end range");
		options.addOption("fo", "fastoutput", false, "Display only the number of results for each database");
		options.addOption("o", "outputpath", true, "Set CSV output path");
		
		options.addOption("h", "help", false, "Show this help");
		
		return options;
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
		
		
		if (cmd.hasOption("help")) {
			// show help
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(null); // keep the options in the order they were declared.
			formatter.printHelp(this.programName, this.options);
			System.exit(0);
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

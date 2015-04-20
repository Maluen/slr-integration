package engines.acm;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import misc.Utils;

import org.w3c.dom.Document;

import services.Service;
import engines.Engine;

public class ACMEngine extends Engine {

	public ACMEngine() {
		super("acm");
	}

	@Override
	public void search(String queryText) {
		
		
		String fileName = "services/home-html.xml"; // relative to base paths
		
		Service searchService = new Service();
		// load from file
		File serviceFile = new File(this.inputBasePath + fileName);
		searchService.loadFromFile(serviceFile);
		// set any needed data
		searchService.addData("query", queryText);
		searchService.addData("cfid", "665735219");
		searchService.addData("cftoken", "54508538");
		
		Document searchResultContent = searchService.request(); // TODO: make this async?
		
		// save result
		try {
			Utils.saveDocument(searchResultContent, this.outputBasePath + fileName);
		} catch (TransformerFactoryConfigurationError | TransformerException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}

}

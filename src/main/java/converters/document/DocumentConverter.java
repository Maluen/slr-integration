package converters.document;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import misc.Logger;
import misc.Utils;

import org.w3c.dom.Document;

public abstract class DocumentConverter {

	protected Logger logger;
	protected Document document;
	
	public DocumentConverter() {
		this.logger = new Logger("DocumentConverter");
		try {
			// log by appending to file
			String filename = "data/output/logs/DocumentConverter.txt";
			this.logger.setPrintStream(Utils.createFilePrinter(filename));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public abstract Object convert() throws Exception;
	
}

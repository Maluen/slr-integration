package converters.document.to;

import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import misc.Utils;

import org.w3c.dom.Document;

import parsers.bibtext.BibtexParser;
import services.resources.Resource;

public class BibtexToDocument extends XMLtoDocument {

	protected BibtexParser bibtexParser;
	protected XMLtoDocument xmlToDocument;
	
	public BibtexToDocument() {
		this.bibtexParser = new BibtexParser();
	}
	
	@Override
	public String getFromContentType() {
		return "application/x-bibtex";
	}

	@Override
	public void setResource(Resource resource) {
		Resource xmlResource;
		
		if (resource.getContentType().equals(this.getFromContentType())) {
			// BibTex string
			String content = (String) resource.getContent();
			Document parsedContent = this.bibtexParser.parse(content);
			
			xmlResource = new Resource();
			xmlResource.setName(resource.getName());
			xmlResource.setContentType("text/xml");
			xmlResource.setContent(parsedContent);
			
		} else {
			// XML Document representing the BibTex
			xmlResource = resource;
		}
		
		super.setResource(xmlResource);
	}	
	
}

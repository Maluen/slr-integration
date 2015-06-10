package search.ieee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import query.QueryMatcherVisitor;
import search.SearchEngine;
import services.Service;
import services.resources.Resource;
import data.ArticleList;

public abstract class IEEESearchEngine extends SearchEngine {
	
	public IEEESearchEngine() {
		super("ieee");
	}

}

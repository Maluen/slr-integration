package engines;

import org.antlr.v4.runtime.tree.ParseTree;

import services.resources.ResourceLoader;
import services.resources.ResourceSerializer;

public abstract class Engine {

	// NOTE: name must have unix-names format
	protected String name = "";
	
	protected String inputBasePath = "";
	protected String outputBasePath = "";
	
	protected ResourceSerializer resourceSerializer;
	protected ResourceLoader resourceLoader;
	
	protected ParseTree queryTree;
	
	public Engine(String name) {
		
		this.name = name;
		
		this.inputBasePath = "data/engines/" + this.name + "/";
		this.outputBasePath = "data/output/engines/" + this.name + "/";
		
		this.resourceSerializer = new ResourceSerializer();
		this.resourceLoader = new ResourceLoader();
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ParseTree getQueryTree() {
		return this.queryTree;
	}

	public void setQueryTree(ParseTree queryTree) {
		this.queryTree = queryTree;
	}

	// TODO: add search input parameters
	public abstract void search(ParseTree queryTree);
	
}

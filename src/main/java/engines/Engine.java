package engines;

public abstract class Engine {

	// NOTE: name must have unix-names format
	protected String name;
	
	protected String inputBasePath;
	protected String outputBasePath;
	
	public Engine(String name) {
		
		this.name = name;
		
		this.inputBasePath = "data/engines/" + this.name + "/";
		this.outputBasePath = "data/output/engines/" + this.name + "/";
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// TODO: add search input parameters
	public abstract void search(String queryText);
	
}

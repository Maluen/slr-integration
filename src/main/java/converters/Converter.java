package converters;

import javax.script.ScriptEngineManager;

public abstract class Converter {

	protected ScriptEngineManager scriptFactory;
	
	public Converter() {
	    // create a script engine manager
        this.scriptFactory = new ScriptEngineManager();
	}
	
}

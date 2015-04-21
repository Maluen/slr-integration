package services;

import java.util.HashMap;
import java.util.Map;

import network.http.HTTPEncoder;

public class Data<T> extends HashMap<String, T> {

	private static final long serialVersionUID = 3980218241538314470L;

	public String apply(String target) {
		// default encoding
		return this.apply(target, HTTPEncoder.EncodeMode.FORM_DATA);
	}
	
	public String apply(String target, HTTPEncoder.EncodeMode encodeMode) {
	    for (Map.Entry<String, T> entry : this.entrySet()) {
	    	
	    	String name = entry.getKey();
	    	T value = entry.getValue();
	    	
	    	String valueString = value.toString();
	    	
    		// do this first since the other also matches this form
			target = target.replace("{{{"+name+"}}}", HTTPEncoder.encode(valueString, encodeMode));
			
			target = target.replace("{{"+name+"}}", valueString);
	    	
	    }
		
		return target;
	}
	
	public Map<String, String> applyData(Map<String, String> target) {
		// don't modify original collection
		Map<String, String> newTarget = new HashMap<String, String>();
		
		for (Map.Entry<String, String> entry : target.entrySet()) {
	    	String name = entry.getKey();
	    	String value = entry.getValue();
	    	
	    	// apply data to name and value
	    	name = this.apply(name);
	    	value = this.apply(value);
	    	
	    	newTarget.put(name, value);
		}

		return newTarget;
	}
	
}

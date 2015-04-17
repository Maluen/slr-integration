package services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Data<T> extends HashMap<String, T> {

	private static final long serialVersionUID = 3980218241538314470L;

	public String apply(String target) {
	    for (Map.Entry<String, T> entry : this.entrySet()) {
	    	
	    	String name = entry.getKey();
	    	T value = entry.getValue();
	    	
	    	String valueString = value.toString();
	    	
	    	try {
	    		// do this first since the other also matches this form
				target = target.replace("{{{"+name+"}}}", URLEncoder.encode(valueString, "UTF-8"));
				
				target = target.replace("{{"+name+"}}", valueString);
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	    	
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

package services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import network.http.HTTPEncoder;

public class Data<T> extends HashMap<String, T> {

	private static final long serialVersionUID = 3980218241538314470L;
	
	public static Pattern variablePattern = Pattern.compile("(\\{\\{\\{(.+?)\\}\\}\\})|(\\{\\{(.+?)\\}\\})"); // formats: {{{name}}} or {{name}}

	public String apply(String target) {
		// default encoding
		return this.apply(target, HTTPEncoder.EncodeMode.FORM_DATA);
	}
	
	public String apply(String target, HTTPEncoder.EncodeMode encodeMode) {
		
		target = this.applyVariables(target, encodeMode);
		
		return target;
	}
	
	public String applyVariables(String target, HTTPEncoder.EncodeMode encodeMode) {
		StringBuffer newTargetBuffer = new StringBuffer();
		
		Matcher variableMatcher = Data.variablePattern.matcher(target);
		while (variableMatcher.find()) {
			//System.out.println(variableMatcher.group());
			//System.out.println("Groups: " + variableMatcher.group(1) + " " + variableMatcher.group(2) + " " + variableMatcher.group(3) + " " + variableMatcher.group(4));
			
			String variableName;
			if (variableMatcher.group(1) != null) {
				// 3-brackets version
				variableName = variableMatcher.group(2);
			} else {
				// 2-brackets version
				variableName = variableMatcher.group(4);
			}	
			
			String variableValue = this.containsKey(variableName) ? this.get(variableName).toString() : "";
			if (variableMatcher.group(1) != null && !variableValue.isEmpty()) {
				// encode value in 3-brackets version
				variableValue = HTTPEncoder.encode(variableValue, encodeMode);
			}
			
			variableMatcher.appendReplacement(newTargetBuffer, Matcher.quoteReplacement(variableValue));
		}
		variableMatcher.appendTail(newTargetBuffer);
		
		return newTargetBuffer.toString();
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

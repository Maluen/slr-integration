package services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import network.http.HTTPEncoder;

public class Data<T> extends HashMap<String, T> {

	private static final long serialVersionUID = 3980218241538314470L;
	
	// formats: {{{name}}} or {{name}}
	public static Pattern variablePattern = Pattern.compile("(\\{\\{\\{(.+?)\\}\\}\\})|(\\{\\{(.+?)\\}\\})");
	// 'if' formats: (\1 refers to name)
	// - if not null: {{#name}}body{{/name}}
	// - if null: {{^name}}body{{/name}}
	// Note: the dot will also match newlines, since the body could contain them
	public static Pattern ifNotNullPattern = Pattern.compile("\\{\\{#(.+?)\\}\\}(.*?)\\{\\{/\\1\\}\\}", Pattern.DOTALL);
	public static Pattern ifNullPattern = Pattern.compile("\\{\\{\\^(.+?)\\}\\}(.*?)\\{\\{/\\1\\}\\}", Pattern.DOTALL);
	
	public String apply(String target) {
		// default encoding
		return this.apply(target, HTTPEncoder.EncodeMode.FORM_DATA);
	}
	
	public String apply(String target, HTTPEncoder.EncodeMode encodeMode) {
		
		target = this.applyIf(target, true, encodeMode);
		target = this.applyIf(target, false, encodeMode);
		target = this.applyVariables(target, encodeMode);
		
		return target;
	}
	
	// Note: doesn't support nested if-statements with same name
	// Set isIfTrue to "false" to use an "if false".
	public String applyIf(String target, Boolean isNotNullIf, HTTPEncoder.EncodeMode encodeMode) {
		StringBuffer newTargetBuffer = new StringBuffer();
		
		Pattern ifPattern = (isNotNullIf) ? Data.ifNotNullPattern : Data.ifNullPattern;
		Matcher ifMatcher = ifPattern.matcher(target);
		while (ifMatcher.find()) {
			//System.out.println(ifMatcher.group());
			
			String variableName = ifMatcher.group(1);
			String body = ifMatcher.group(2);
			
			String variableValue = null;
			if (this.containsKey(variableName)) {
				T thisValue = this.get(variableName);
				if (thisValue != null) variableValue = thisValue.toString();
			}
			
			String replacement;
			Boolean isNotNull = (variableValue != null && !variableValue.isEmpty());
			if (isNotNull == isNotNullIf) {
				// keep body
				if (body != null && !body.isEmpty()) {
					// recursive call to process nested if-statements in body (if any)
					body = this.applyIf(body, isNotNullIf, encodeMode);
				}
				replacement = body;
			} else {
				replacement = "";
			}
			
			if (replacement == null) replacement = "";
			ifMatcher.appendReplacement(newTargetBuffer, Matcher.quoteReplacement(replacement));
		}
		ifMatcher.appendTail(newTargetBuffer);
		
		// (the result is trimmed as whitespace borders might have been created by the replacements)
		return newTargetBuffer.toString().trim();
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
			
			String variableValue = null;
			if (this.containsKey(variableName)) {
				T thisValue = this.get(variableName);
				if (thisValue != null) variableValue = thisValue.toString();
			}
			
			if (variableMatcher.group(1) != null && variableValue != null && !variableValue.isEmpty()) {
				// encode value in 3-brackets version
				variableValue = HTTPEncoder.encode(variableValue, encodeMode);
			}
			
			if (variableValue == null) variableValue = "";
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

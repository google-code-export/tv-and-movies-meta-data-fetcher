package org.stanwood.media.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReverseFilePatternMatcher {

	private Map<String,String> values;

	public void parse(String path,String pattern) {
		List<String> tokens = new ArrayList<String>();
		StringBuilder regexp = new StringBuilder();
		for (int i=0;i<pattern.length();i++) {
			if (pattern.charAt(i)=='%') {
				i++;
				char token = pattern.charAt(i);
				switch (token) {
				case 'h':
				case 'n':
				case 't':
				case 'x':
					tokens.add(""+token);
				    regexp.append("(.*)");
				    break;
				case 's':
				case 'e':
					tokens.add(""+token);
					regexp.append("(\\d+)");
					break;
				case '%' :
					regexp.append("%");
				}
			}
			else {
				regexp.append(pattern.charAt(i));
			}
		}

		Pattern p = Pattern.compile(regexp.toString());
		Matcher m = p.matcher(path);
		if (m.matches()) {
			this.values= new HashMap<String,String>();
			for (int i=1;i<m.groupCount();i++) {
				values.put(tokens.get(i-1),m.group(i));
			}
		}
	}

	public Map<String,String> getValues() {
		return values;
	}


}

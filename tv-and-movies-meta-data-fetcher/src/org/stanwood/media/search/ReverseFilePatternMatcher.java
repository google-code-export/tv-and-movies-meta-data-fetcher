package org.stanwood.media.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides methods that will take a file path and a search pattern, then
 * parse the file path using the pattern. This is used to find the value of the tokens
 * in the pattern when applyed to the file path.
 */
public class ReverseFilePatternMatcher {

	private Map<String,String> values = new HashMap<String,String>();

	/**
	 * Parse the file path using the rename pattern and find the tokens in the
	 * pattern's values
	 * @param path The file path
	 * @param pattern The rename pattern
	 */
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

	/**
	 * A map of tokens found when a file path was parsed using a rename pattern
	 * @return The tokens
	 */
	public Map<String,String> getValues() {
		return values;
	}


}

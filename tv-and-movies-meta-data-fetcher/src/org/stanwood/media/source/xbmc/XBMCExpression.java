package org.stanwood.media.source.xbmc;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


/**
 * This class is used to store details about a XBMC regular expression entry from the XML
 */
public class XBMCExpression {

	private Pattern pattern;
	private boolean clear = true;
	private List<Integer>clean = new ArrayList<Integer>();
	private List<Integer>trim = new ArrayList<Integer>();
	
	/**
	 * Used to get the pattern as a have REGEXP pattern
	 * @return the pattern as a have REGEXP pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Used to set the REGEXP pattern of the expression
	 * @param pattern the REGEXP pattern of the expression
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	/**
	 * Used to find out if a field should not be cleaned
	 * @param field The field to check
	 * @return find out if a field should not be cleaned
	 */
	public boolean getNoClean(int field) {
		return clean.contains(field);
	}	
	
	/**
	 * Used to find out if a field should be trimmed. 
	 * @param field The field to check
	 * @return True if whitespace should be trimmed from a field.
	 */
	public boolean getTrim(int field) {
		return trim.contains(field);
	}

	public boolean getClear() {
		return clear;
	}

	public void setClear(boolean clear) {
		this.clear = clear;
	}

	public void setNoClean(String value) {
		if (!value.equals("")) {
			StringTokenizer tok = new StringTokenizer(value,",");
			while (tok.hasMoreTokens()) {
				int num = Integer.parseInt(tok.nextToken());
				clean.add(num);
			}			
		}
	}

	public void setTrim(String value) {
		if (!value.equals("")) {
			StringTokenizer tok = new StringTokenizer(value,",");
			while (tok.hasMoreTokens()) {
				int num = Integer.parseInt(tok.nextToken());
				trim.add(num);
			}			
		}
	}	
		
	
}

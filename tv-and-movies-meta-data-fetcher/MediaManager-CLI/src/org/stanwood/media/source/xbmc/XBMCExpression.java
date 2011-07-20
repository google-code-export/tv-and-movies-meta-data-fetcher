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
	private boolean repeat = false;

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

	/**
	 * Used to find out if the dest field should be cleared when a expression fails
	 * @return true if the dest field should be cleared when a expression fails
	 */
	public boolean getClear() {
		return clear;
	}

	/**
	 * If set to true, then if the expression fails the dest field is clear
	 * @param clear If set to true, then if the expression fails the dest field is clear
	 */
	public void setClear(boolean clear) {
		this.clear = clear;
	}

	/**
	 * By default HTML tags and special characters are stripped from the
	 * matches. By setting noclean to a comma separated list of field numbers,
	 *  you can stop this behaviour.
	 * @param value A comma separated list of field numbers that should not be cleaned
	 */
	public void setNoClean(String value) {
		if (!value.equals("")) { //$NON-NLS-1$
			StringTokenizer tok = new StringTokenizer(value,","); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				int num = Integer.parseInt(tok.nextToken());
				clean.add(num);
			}
		}
	}

	/**
	 * trim white spaces from the end of matches
	 * @param value A comma separated list of field numbers that should trimmed
	 */
	public void setTrim(String value) {
		if (!value.equals("")) { //$NON-NLS-1$
			StringTokenizer tok = new StringTokenizer(value,","); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				int num = Integer.parseInt(tok.nextToken());
				trim.add(num);
			}
		}
	}

	/**
	 * Used to work out if the expression should match until it does not match
	 * @return True to keep trying to match
	 */
	public boolean getRepeat() {
		return repeat;
	}

	/**
	 * Used to set if the expression should match until it does not match
	 * @param repeat True to keep matching
	 */
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return pattern.toString();
	}


}

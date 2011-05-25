/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.search;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class provides some helper functions that can be used to do things like
 * construct a normalised search query from a file name.
 */
public class SearchHelper {

	private final static Pattern PATTERN_PART = Pattern.compile("(^.+)(?:cd|part) *(\\d+)(.*$)",Pattern.CASE_INSENSITIVE);

	private static final String STRIPED_CHARS[] = {"?","'",","};

	private final static String IGNORED_TOKENS[] = {"dvdrip","xvid","proper","ac3"};
	private final static Pattern PATTERN_STRIP_CHARS = Pattern.compile("^[\\s|\\-]*(.*?)[\\s|\\-]*$");

	/**
	 * This is a helper method that will replace the dot's sometimes found in place of spaces of filenames.
	 * @param term The term that might contains dot's instead of spaces. This buffer will have the dot's
	 * replaced.
	 */
	public static void replaceDots(StringBuilder term) {
		int pos = -1;
		int currentPos = 0;
		while ((pos = term.indexOf(".",currentPos))!=-1) {
			boolean doit=true;
			if (pos>=0 && term.charAt(pos-1)==' ') {
				doit = false;
			}
			if (pos+1<term.length() && term.charAt(pos+1)==' ') {
				doit = false;
			}
			if (doit) {
				term.replace(pos, pos+1, " ");
			}
			currentPos = pos+1;
		}
	}

	/**
	 * This is used to remove any unwanted characters from search terms
	 * @param term The search term
	 */
	public static void removeUnwantedCharacters(StringBuilder term) {
		for (String c : STRIPED_CHARS) {
			int pos = -1;
			while ((pos = term.indexOf(c))!=-1) {
				term.replace(pos, pos+c.length(), "");
			}
		}
	}

	/**
	 * This is used to replace underscore characters with spaces in the search term
	 * @param term The search term
	 */
	public static void replaceUnderscore(StringBuilder term) {
		int pos = -1;
		int currentPos = 0;
		while ((pos = term.indexOf("_",currentPos))!=-1) {
			boolean doit=true;
			if (pos>=0 && term.charAt(pos-1)==' ') {
				doit = false;
			}
			if (pos+1<term.length() && term.charAt(pos+1)==' ') {
				doit = false;
			}
			if (doit) {
				term.replace(pos, pos+1, " ");
			}
			currentPos = pos+1;
		}
	}

	/**
	 * This is used to replace hyphens characters with spaces in the search term
	 * @param term The search term
	 */
	public static void replaceHyphens(StringBuilder term) {
		int pos = -1;
		int currentPos = 0;
		while ((pos = term.indexOf("-",currentPos))!=-1) {
			boolean doit=true;
			if (pos>=0 && term.charAt(pos-1)==' ') {
				doit = false;
			}
			if (pos+1<term.length() && term.charAt(pos+1)==' ') {
				doit = false;
			}
			if (doit) {
				term.replace(pos, pos+1, " ");
			}
			currentPos = pos+1;
		}
	}

	/**
	 * This is used to replace word seperator characters such as underscores with spaces.
	 * @param term The search term
	 */
	public static void replaceWithSpaces(StringBuilder term) {
		replaceUnderscore(term);
		replaceDots(term);
	}

	/**
	 * Used to get a part number for the search term
	 * @param term The search term
	 * @return The part number, or null if it could not be found
	 */
	public static Integer extractPart(StringBuilder term) {
		Matcher m = PATTERN_PART.matcher(term);
		Integer part = null;
		if (m.matches()) {
			try {
				part = Integer.valueOf(m.group(2));
			}
			catch (NumberFormatException e) {
				return null;
			}
			String first = m.group(1);
			String last = m.group(3);
			term.delete(0, term.length());
			term.append(first);
			term.append(last);
		}
		return part;
	}

	/**
	 * Used to remove tokens that should be ignored from a search term
	 * @param term The search term
	 */
	public static void removeIgnoredTokens(StringBuilder term) {
		for (String it : IGNORED_TOKENS) {
			int pos = -1;
			while ((pos = term.indexOf(it))!=-1) {
				term.replace(pos, pos+it.length(), "");
			}
		}
	}

	/**
	 * Used to check if a search term has ignored tokens
	 * @param term The search term
	 * @return True if ignore tokens are found
	 */
	public static boolean hasIgnoredTokens(StringBuilder term) {
		StringTokenizer tok = new StringTokenizer(term.toString()," -");
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			for (String it : IGNORED_TOKENS) {
				if (token.equalsIgnoreCase(it)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Used to strip white space from either end of the search terms
	 * @param term The search term
	 */
	public static void trimRubishFromEnds(StringBuilder term) {
		Matcher m = PATTERN_STRIP_CHARS.matcher(term);
		if (m.matches()) {
			String first = m.group(1);
			term.delete(0, term.length());
			term.append(first);
		}
	}
}

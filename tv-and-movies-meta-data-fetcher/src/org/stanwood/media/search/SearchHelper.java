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


/**
 * This class provides some helper functions that can be used to do things like
 * construct a normalised search query from a file name.
 */
public class SearchHelper {

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
	 * Used to decode HTML entities in strings
	 * @param s The string to decode
	 * @return The decoded string
	 */
	public static String decodeHtmlEntities(String s) {
//		int i = 0, j = 0, pos = 0;
		StringBuilder sb = new StringBuilder();
		int pos = 0;

		while (pos< s.length()) {
			if (s.length()>pos+2 && s.substring(pos,pos+2).equals("&#")) {
				int n = -1;
				int j = s.indexOf(';', pos);
				pos+=2;
				while (pos < j) {
					char c = s.charAt(pos);
					if ('0' <= c && c <= '9') {
						n = (n == -1 ? 0 : n * 10) + c - '0';
					} else {
						break;
					}
					pos++;
				}
				if (n!=-1) {
					sb.append((char) n);
				}
			}
			else {
				sb.append(s.charAt(pos));
			}

			pos++;
		}

		return sb.toString();
	}

	/**
	 * This will normalise a string. It lower cases the string and then replaces any accented
	 * characters. Other punciation is removed.
	 * @param s The string to normalise
	 * @return The normalised string
	 */
	public static String normalizeQuery(String s) {
		s =s.toLowerCase();

		s = s.replaceAll("ä|á","a");
		s = s.replaceAll("ñ","n");
		s = s.replaceAll("ö","o");
		s = s.replaceAll("ü","u");
		s = s.replaceAll("ÿ","y");
		s = s.replaceAll("é","e");

		s = s.replaceAll("ß","ss");  //  German beta “ß” -> “ss”
		s = s.replaceAll("Æ","AE");  //  Æ
		s = s.replaceAll("æ","ae");  //  æ
		s = s.replaceAll("Ĳ","IJ");  //  Ĳ
		s = s.replaceAll("ĳ","ij");  //  ĳ
		s = s.replaceAll("Œ","Oe");  //  Œ
		s = s.replaceAll("œ","oe");  //  œ
//
//		s = s.replaceAll("\\x{00d0}\\x{0110}\\x{00f0}\\x{0111}\\x{0126}\\x{0127}","DDddHh");
//		s = s.replaceAll("\\x{0131}\\x{0138}\\x{013f}\\x{0141}\\x{0140}\\x{0142}","ikLLll");
//		s = s.replaceAll("\\x{014a}\\x{0149}\\x{014b}\\x{00d8}\\x{00f8}\\x{017f}","NnnOos");
//		s = s.replaceAll("\\x{00de}\\x{0166}\\x{00fe}\\x{0167}","TTtt");

		s =s.replaceAll(":|-|,|'", "");
		return s;
	}
}

package org.stanwood.media.source;

import java.io.File;

/**
 * This class provides some helper functions that can be used to do things like
 * construct a normalised search query from a file name. 
 */
public class SearchHelper {

	
	/**
	 * Get a search query from the filename of a file. This will strip out various 
	 * characters that are not wanted.
	 * @param regexpToReplace A regular expression, that when found in the query, is removed. 
	 *                        Passing a null value will cause this to be ignored. 
	 * @param mediaFile The file to which we want to lookup in a source or store
	 * @return The query, or null if the query could not be constructed.
	 */
	public static String getQuery(File mediaFile,String regexpToReplace) {
		String file = mediaFile.getName().toLowerCase().trim();
		int pos = file.lastIndexOf(".");
		if (pos == -1) {
			return null;
		}
		file = file.substring(0, pos);
		file = file.replaceAll("\\.|_", " ");
		file = file.replaceAll("(\\[|\\().*(\\]|\\))", "");	
		file = file.replaceAll("dvdrip|dvd-rip|scr|dvd|xvid|divx|xv|xvi|full", "");
		file = file.replaceAll("[:|-|,|']", "");
		if (regexpToReplace!=null) {
			file = file.replaceAll(regexpToReplace.toLowerCase(),"");
		}
		return file.trim();
	}
}

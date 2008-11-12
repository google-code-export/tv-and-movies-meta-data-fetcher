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
package org.stanwood.tv.renamer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse a filename and work out the correct season and episode number 
 * of the file. 
 * 
 * It does this my attempting to match a series of regular expressions against the
 * file. It attempts to do a case insensitive match against each of the following
 * regular expressions The first one that is matched, is used to get the episode and
 * season number. Group 1 is always the season number and group 2 is always the
 * episode number. 
 * <ul>
 * 	<li>.*[s]([\d]+)[e]([\d]+).*<li>
 *  <li>.*[s]([\d]+)\.[e]([\d]+).*</li>
 *  <li>.*([\d]+)[x]([\d]+).*</li>
 *  <li>.*([\d]{2,2})([\d]{2,2}).*</li>
 *  <li>.*([\d]{1,1})([\d]{2,2}).*</li>
 *  <li>^([\d])[\s]([\d]{2,2}).*</li>
 *  <li>^([\d]{1,2})[\s]([\d]{2,2}).*</li>
 * </ul>
 */
public class FileNameParser {

	private static Pattern PATTERNS[] = new Pattern[] {
			Pattern.compile(".*[s]([\\d]+)[e]([\\d]+).*",Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*[s]([\\d]+)\\.[e]([\\d]+).*",Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*([\\d]+)[x]([\\d]+).*",Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*([\\d]{2,2})([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*([\\d]{1,1})([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE),
			Pattern.compile("^([\\d])[\\s]([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE),			
			Pattern.compile("^([\\d]{1,2})[\\s]([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*season\\s([\\d]+)\\sepisode\\s([\\d]+).*",Pattern.CASE_INSENSITIVE)
	};
	

	/**
	 * Parse the filename and work out the episode and season number
	 * @param filename The filename to parse
	 * @return The parsed information
	 */
	public static ParsedFileName parse(String filename) {			
		for (Pattern pattern : PATTERNS) {
			Matcher m = pattern.matcher(filename);
			if (m.matches()) {
				ParsedFileName result = new ParsedFileName();
				int seasonNumber = Integer.parseInt(m.group(1));
				int episodeNumber = Integer.parseInt(m.group(2));
				result.setSeason(seasonNumber);
				result.setEpisode(episodeNumber);
				return result;
			}
 		}
		return null;
	}
	
}
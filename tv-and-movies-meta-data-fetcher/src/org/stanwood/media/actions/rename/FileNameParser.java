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
package org.stanwood.media.actions.rename;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.source.xbmc.expression.ValueType;

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
			Pattern.compile(".*[s]([\\d]+)[e]([\\d]+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile(".*[s]([\\d]+)\\.[e]([\\d]+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile(".*([\\d]+)[x]([\\d]+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile("^([\\d])[\\s]([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile("^([\\d]{1,2})[\\s]([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile(".*season\\s([\\d]+)\\sepisode\\s([\\d]+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile(".*S([\\d]{1,2}) E([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile(".*([\\d]{2,2})([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
			Pattern.compile(".*([\\d]{1,1})([\\d]{2,2}).*",Pattern.CASE_INSENSITIVE) //$NON-NLS-1$
	};


	/**
	 * Parse the filename and work out the episode and season number
	 * @param dirConfig The root media directory
	 * @param file The file been renamed
	 * @return The parsed information
	 */
	public static ParsedFileName parse(MediaDirConfig dirConfig,File file) {
		for (Pattern pattern : PATTERNS) {
			Matcher m = pattern.matcher(file.getName());
			if (m.matches()) {
				ParsedFileName result = new ParsedFileName();
				int seasonNumber = Integer.parseInt(m.group(1));
				int episodeNumber = Integer.parseInt(m.group(2));
				result.setSeason(seasonNumber);
				result.setEpisode(episodeNumber);
				return result;
			}
 		}

		Map<Token,String> tokens = getTokens(dirConfig.getMediaDir(),dirConfig.getPattern(),file.getAbsolutePath());
		if (tokens!=null) {
			String episodeNumber = tokens.get(Token.EPISODE);
			String seasonNumber = tokens.get(Token.SEASON);
			if (episodeNumber!=null && seasonNumber!=null) {
				ParsedFileName result = new ParsedFileName();
				try {
					result.setSeason(Integer.parseInt(seasonNumber));
					result.setEpisode(Integer.parseInt(episodeNumber));
					return result;
				}
				catch (NumberFormatException e) {
					return null;
				}
			}
		}
		return null;
	}

	private static Map<Token, String> getTokens(File rootMediaDir,String renamePattern,String filename) {
		List<String>groups = new ArrayList<String>();
		Pattern p = Pattern.compile("(%.)"); //$NON-NLS-1$
		renamePattern = renamePattern.replaceAll("\\"+File.separator,"\\\\"+File.separator );  //$NON-NLS-1$//$NON-NLS-2$
		renamePattern = renamePattern.replaceAll("\\.","\\\\." );  //$NON-NLS-1$//$NON-NLS-2$
		StringBuffer buffer = new StringBuffer();
		Matcher m = p.matcher(renamePattern);
		while (m.find()) {
			String group = m.group();
			if (Token.fromFull(group).getType()==ValueType.INTEGER){
				m.appendReplacement(buffer,"([\\\\d]+)"); //$NON-NLS-1$
				groups.add(group);
			}
			else {
				m.appendReplacement(buffer,"(.*)"); //$NON-NLS-1$
				groups.add(group);
			}
		}
		m.appendTail(buffer);
		renamePattern = buffer.toString();

		p  = Pattern.compile(renamePattern);
		if (filename.startsWith(rootMediaDir.getAbsolutePath())) {
			filename=filename.substring(rootMediaDir.getAbsolutePath().length()+1);
		}
		m = p.matcher(filename);
		if (m.matches()) {
			Map<Token,String>result = new HashMap<Token,String>();
			for (int i=0;i<m.groupCount();i++) {
				result.put(Token.fromFull(groups.get(i)),m.group(i+1));
			}
			return result;
		}
		return null;
	}

}
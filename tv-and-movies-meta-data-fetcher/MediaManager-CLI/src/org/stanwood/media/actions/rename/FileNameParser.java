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

import org.stanwood.media.model.SearchResult;
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

	private static String ONLY_EP_PATTERN="[e ]?[\\d]{2,2}"; //$NON-NLS-1$
	private static String FULL_EP_PATTERN="s?[\\d]{1,2}"+ONLY_EP_PATTERN; //$NON-NLS-1$
	private static String AND_SEP = "[\\&\\+\\. ]"; //$NON-NLS-1$
	private static String WHITE_SPACE = "[\\. ]*"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static Pattern MULTI_PATTERNS[] = new Pattern[] {
		Pattern.compile(".*?("+FULL_EP_PATTERN+")"+WHITE_SPACE+AND_SEP+WHITE_SPACE+"("+FULL_EP_PATTERN+").*",Pattern.CASE_INSENSITIVE ),
		Pattern.compile(".*?("+FULL_EP_PATTERN+")"+WHITE_SPACE+AND_SEP+WHITE_SPACE+"("+ONLY_EP_PATTERN+").*",Pattern.CASE_INSENSITIVE ),
	};

	@SuppressWarnings("nls")
	private static Pattern PATTERNS[] = new Pattern[] {
		// Single episode patterns
		compile(".*[s]([\\d]+)[e]([\\d]+).*"),
		compile(".*[s]([\\d]+)\\.[e]([\\d]+).*"),
		compile(".*?([\\d]{1,2})\\D([\\d]{2,2}).*"),
		compile(".*season"+WHITE_SPACE+"([\\d]{1,2})"+WHITE_SPACE+"episode"+WHITE_SPACE+"([\\d]{1,2}).*"),
		compile(".*S([\\d]{1,2})"+WHITE_SPACE+"E([\\d]{2,2}).*"),
		compile("^([\\d]{2,2})([\\d]{2,2})"+WHITE_SPACE+".*"),
		compile("^([\\d]{1,1})([\\d]{2,2})"+WHITE_SPACE+".*"),
		compile(".*[\\. ]([\\d]{2,2})([\\d]{2,2})[\\. ].*"),
		compile(".*[\\. ]([\\d]{1,1})([\\d]{2,2})[\\. ].*")
	};

	private static Pattern compile(String pattern) {
		return Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Parse the filename and work out the episode and season number. This does not use the media directory
	 * to do reverse pattern lookups.
	 * @param file The file been renamed
	 * @return The parsed information
	 */
	public static ParsedFileName parse(File file) {
		for (Pattern multiPattern : MULTI_PATTERNS) {
			Matcher multiMatcher = multiPattern.matcher(file.getName());
			if (multiMatcher.matches()) {
				ParsedFileName result1 = matchSinglePattern(multiMatcher.group(1));
				if (result1!=null) {
					ParsedFileName result2 = matchSinglePattern(multiMatcher.group(2));
					if (result2!=null && result1.getSeason()==result2.getSeason()) {
						ParsedFileName result = new ParsedFileName();
						result.setSeason(result1.getSeason());
						List<Integer> episodes = new ArrayList<Integer>();
						for (int i=result1.getEpisodes().get(0);i<=result2.getEpisodes().get(0);i++) {
							episodes.add(i);
						}
						result.setEpisodes(episodes);
						return result;
					}
					else if (isInteger(multiMatcher.group(2))) {
						ParsedFileName result = new ParsedFileName();
						result.setSeason(result1.getSeason());
						List<Integer> episodes = new ArrayList<Integer>();
						for (int i=result1.getEpisodes().get(0);i<=Integer.valueOf(multiMatcher.group(2));i++) {
							episodes.add(i);
						}
						result.setEpisodes(episodes);
						return result;
					}
				}
			}
		}

		ParsedFileName result = matchSinglePattern(file.getName());
		return result;
	}

	private static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;

	}

	protected static ParsedFileName matchSinglePattern(String text) {
		for (Pattern singlePattern : PATTERNS) {
			Matcher singleMatcher = singlePattern.matcher(text);
			if (singleMatcher.matches()) {
				ParsedFileName result = new ParsedFileName();
				int seasonNumber = Integer.parseInt(singleMatcher.group(1));
				int episodeNumber = Integer.parseInt(singleMatcher.group(2));
				List<Integer>episodes = new ArrayList<Integer>();
				episodes.add(episodeNumber);
				result.setSeason(seasonNumber);
				result.setEpisodes(episodes);
				return result;
			}
		}
		return null;
	}

	/**
	 * Parse the filename and work out the episode and season number
	 * @param dirConfig The root media directory
	 * @param file The file been renamed
	 * @param lookupResults The results of a show/film search
	 * @return The parsed information
	 */
	public static ParsedFileName parse(MediaDirConfig dirConfig,File file,SearchResult lookupResults) {
		if (lookupResults!=null && lookupResults.getSeason()!=null && lookupResults.getEpisodes()!=null && lookupResults.getEpisodes().size()>0) {
			ParsedFileName parsed = new ParsedFileName();
			parsed.setEpisodes(lookupResults.getEpisodes());
			parsed.setSeason(lookupResults.getSeason());
			return parsed;
		}

		ParsedFileName result = parse(file);
		if (result!=null) {
			return result;
		}

		Map<Token,String> tokens = getTokens(dirConfig.getMediaDir(),dirConfig.getPattern(),file.getAbsolutePath());
		if (tokens!=null) {
			// TODO Handle multiple episode numbers
			String episodeNumber = tokens.get(Token.EPISODE);
			String seasonNumber = tokens.get(Token.SEASON);
			if (episodeNumber!=null && seasonNumber!=null) {
				result = new ParsedFileName();
				try {
					result.setSeason(Integer.parseInt(seasonNumber));
					List<Integer>episodes = new ArrayList<Integer>();
					episodes.add(Integer.parseInt(episodeNumber));
					result.setEpisodes(episodes);
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
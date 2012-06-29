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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.SearchResult;
import org.stanwood.media.search.ReverseFilePatternMatcher;
import org.stanwood.media.setup.MediaDirConfig;

/**
 * Used to parse a filename and work out the correct season and episode number
 * of the file.
 *
 * It does this my attempting to match a series of regular expressions against the
 * file. It attempts to do a case insensitive match against each of the following
 * regular expressions The first one that is matched, is used to get the episode and
 * season number. Group 1 is always the season number and group 2 is always the
 * episode number.
 *
 */
public class FileNameParser {

	private static String ONLY_EP_PATTERN="[e ]?[\\d]{2,2}"; //$NON-NLS-1$
	private static String FULL_EP_PATTERN="s?[\\d]{1,2}"+ONLY_EP_PATTERN; //$NON-NLS-1$
	private static String SEP = "[\\&\\+\\.\\, \\-]"; //$NON-NLS-1$
	private static String WHITE_SPACE = "[\\. ]*"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static Pattern MULTI_PATTERNS[] = new Pattern[] {
		Pattern.compile("(.*?)("+FULL_EP_PATTERN+")"+WHITE_SPACE+"("+SEP+")"+WHITE_SPACE+"("+FULL_EP_PATTERN+")(.*)",Pattern.CASE_INSENSITIVE ),
		Pattern.compile("(.*?)("+FULL_EP_PATTERN+")"+WHITE_SPACE+"("+SEP+")"+WHITE_SPACE+"("+ONLY_EP_PATTERN+")(.*)",Pattern.CASE_INSENSITIVE ),
	};

	@SuppressWarnings("nls")
	private static Pattern PATTERNS[] = new Pattern[] {
		// Single episode patterns
		compile("(.*)[s]([\\d]+)[e]([\\d]+).*"),
		compile("(.*)[s]([\\d]+)\\.[e]([\\d]+).*"),
		compile("(.*?)([\\d]{1,2})\\D([\\d]{2,2}).*"),
		compile("(.*)season"+WHITE_SPACE+"([\\d]{1,2})"+WHITE_SPACE+"episode"+WHITE_SPACE+"([\\d]{1,2}).*"),
		compile("(.*)S([\\d]{1,2})"+WHITE_SPACE+"E([\\d]{2,2}).*"),
		compile("^()([\\d]{2,2})([\\d]{2,2})"+WHITE_SPACE+".*"),
		compile("^()([\\d]{1,1})([\\d]{2,2})"+WHITE_SPACE+".*"),
		compile("(.*)[\\. ]([\\d]{2,2})([\\d]{2,2})[\\. ].*"),
		compile("(.*)[\\. ]([\\d]{1,1})([\\d]{2,2})[\\. ].*")
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
		return parse(file.getName());
	}

	/**
	 * Parse the filename and work out the episode and season number. This does not use the media directory
	 * to do reverse pattern lookups.
	 * @param file The file been renamed
	 * @return The parsed information
	 */
	public static ParsedFileName parse(String file) {
		for (Pattern multiPattern : MULTI_PATTERNS) {
			Matcher multiMatcher = multiPattern.matcher(file);
			if (multiMatcher.matches()) {
				ParsedFileName result1 = matchSinglePattern(multiMatcher.group(2));
				if (result1!=null) {
					ParsedFileName result2 = matchSinglePattern(multiMatcher.group(4));
					if (result2!=null && result1.getSeason()==result2.getSeason()) {
						int endEp = result2.getEpisodes().get(0);
						return getMultiResult(result1, endEp,multiMatcher.group(3),multiMatcher.group(1),multiMatcher.group(5));
					}
					else if (isInteger(multiMatcher.group(4))) {
						int endEp = Integer.valueOf(multiMatcher.group(4));
						ParsedFileName result = getMultiResult(result1, endEp,multiMatcher.group(3),multiMatcher.group(1),multiMatcher.group(5));
						if (result!=null) {
							return result;
						}
					}
				}
			}
		}

		ParsedFileName result = matchSinglePattern(file);
		return result;
	}

	protected static ParsedFileName getMultiResult(ParsedFileName result1,
			int endEp,String sep,String term,String right) {
		if (sep.equals("-")) { //$NON-NLS-1$
			if (right.startsWith(".") || isInteger(right.substring(0,1))) {
				return null;
			}
			ParsedFileName result = new ParsedFileName();
			result.setTerm(term);
			result.setSeason(result1.getSeason());
			List<Integer> episodes = new ArrayList<Integer>();
			for (int i=result1.getEpisodes().get(0);i<=endEp;i++) {
				episodes.add(i);
			}
			result.setEpisodes(episodes);
			return result;
		}
		else {
			ParsedFileName result = new ParsedFileName();
			result.setTerm(term);
			result.setSeason(result1.getSeason());
			List<Integer> episodes = new ArrayList<Integer>();
			episodes.add(result1.getEpisodes().get(0));
			episodes.add(endEp);
			result.setEpisodes(episodes);
			return result;
		}
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
				int seasonNumber = Integer.parseInt(singleMatcher.group(2));
				int episodeNumber = Integer.parseInt(singleMatcher.group(3));
				List<Integer>episodes = new ArrayList<Integer>();
				episodes.add(episodeNumber);
				result.setSeason(seasonNumber);
				result.setEpisodes(episodes);
				result.setTerm(singleMatcher.group(1));
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
			String episodeNumber = tokens.get(Token.EPISODE);
			String episodeNumberMax = tokens.get(Token.EPISODE_MAX);
			String seasonNumber = tokens.get(Token.SEASON);
			if (episodeNumber!=null && seasonNumber!=null) {
				result = new ParsedFileName();
				try {
					result.setSeason(Integer.parseInt(seasonNumber));
					List<Integer>episodes = new ArrayList<Integer>();
					int startEp = Integer.parseInt(episodeNumber);
					if (episodeNumberMax==null) {
						episodes.add(startEp);
					}
					else {
						int endEp = Integer.parseInt(episodeNumberMax);
						for (int i=startEp;i<=endEp;i++) {
							episodes.add(i);
						}
					}
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
		ReverseFilePatternMatcher reverseFilePatternMatcher = new ReverseFilePatternMatcher();
		reverseFilePatternMatcher.parse(filename, renamePattern);
		return reverseFilePatternMatcher.getValues();
	}

}
package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.Token;

/**
 * This class is used to find a films name using a series of different searching strategies
 */
public abstract class FilmSearcher extends AbstractMediaSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();
	private final static Pattern PATTERN_YEAR = Pattern.compile("(^.+)[\\(|\\[](\\d\\d\\d\\d)[\\)|\\]](.*$)");
	private final static Pattern PATTERN_YEAR2 = Pattern.compile("(^.+)\\.(\\d\\d\\d\\d)\\.(.*$)");
	private final static Pattern PATTERN_EXT = Pattern.compile("(^.*)\\.(.+)$");
	private final static Pattern PATTERN_HYPHON = Pattern.compile("^.*?\\-(.+)$");
	private final static Pattern PATTERN_STRIP_CHARS = Pattern.compile("^[\\s|\\-]*(.*?)[\\s|\\-]*$");

	private final static String IGNORED_TOKENS[] = {"dvdrip","xvid","proper","ac3"};


	static {
		strategies.add(new ReversePatternSearchStrategy(Token.TITLE));

		// Search using a NFO file next to the film if it exists
		strategies.add(new FilmNFOSearchStrategy());

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir,
					String renamePattern,MediaDirectory mediaDir) {
				StringBuilder term = new StringBuilder(mediaFile.getName());
				if (term.indexOf(" ")==-1) {
					extractExtension(term);

					Matcher m = PATTERN_YEAR2.matcher(term);
					if (m.matches()) {
						StringBuilder start= new StringBuilder(m.group(1));
						String year= m.group(2);
						StringBuilder end= new StringBuilder(m.group(3));
						SearchHelper.replaceWithSpaces(start);
						SearchHelper.replaceWithSpaces(end);
						if (hasIgnoredTokens(start)) {
							term.delete(0, term.length());
							term.append(end);
							return new SearchDetails(term.toString().trim(), year,null);
						}
						else if (hasIgnoredTokens(end)) {
							term.delete(0, term.length());
							term.append(start);
							return new SearchDetails(term.toString().trim(), year,null);
						}
					}
				}
				return null;
			}


		});
		// Excact search on film name
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
				StringBuilder term = new StringBuilder(mediaFile.getName());

				extractExtension(term);
				String year = extractYear(term);
				Integer part = SearchHelper.extractPart(term);
				removeUpToHyphon(term);
				removeIgnoredTokens(term);
				SearchHelper.replaceWithSpaces(term);
				trimRubishFromEnds(term);

				String sTerm = term.toString();
				if (sTerm.length()==0) {
					return null;
				}
				return new SearchDetails(sTerm,year,part);
			}
		});
	}

	private static String extractYear(StringBuilder term) {
		Matcher m = PATTERN_YEAR.matcher(term);
		String year = null;
		if (m.matches()) {
			year = m.group(2);
			String first = m.group(1);
			String last = m.group(3);
			term.delete(0, term.length());
			term.append(first);
			term.append(last);
		}
		return year;
	}

	private static String extractExtension(StringBuilder term) {
		Matcher m = PATTERN_EXT.matcher(term);
		String ext = null;
		if (m.matches()) {
			ext = m.group(2);
			String first = m.group(1);
			term.delete(0, term.length());
			term.append(first);
		}
		return ext;
	}

	private static void removeUpToHyphon(StringBuilder term) {
		Matcher m = PATTERN_HYPHON.matcher(term);
		if (m.matches()) {
			String first = m.group(1);
			term.delete(0, term.length());
			term.append(first);
		}
	}

	private static void removeIgnoredTokens(StringBuilder term) {
		for (String it : IGNORED_TOKENS) {
			int pos = -1;
			while ((pos = term.indexOf(it))!=-1) {
				term.replace(pos, pos+it.length(), "");
			}
		}
	}

	private static void trimRubishFromEnds(StringBuilder term) {
		Matcher m = PATTERN_STRIP_CHARS.matcher(term);
		if (m.matches()) {
			String first = m.group(1);
			term.delete(0, term.length());
			term.append(first);
		}
	}

	private static boolean hasIgnoredTokens(StringBuilder term) {
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
	 * Used to create a instance of this class
	 */
	public FilmSearcher() {
		super(strategies);
	}
}

package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to find a films name using a series of different searching strategies
 */
public abstract class FilmSearcher extends AbstractMediaSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();
	private final static Pattern PATTERN_YEAR = Pattern.compile("(^.+)[\\(|\\[](\\d\\d\\d\\d)[\\)|\\]](.*$)");
	private final static Pattern PATTERN_YEAR2 = Pattern.compile("(^.+)\\.(\\d\\d\\d\\d)\\.(.*$)");
	private final static Pattern PATTERN_PART = Pattern.compile("(^.+)cd(\\d+)(.*$)",Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_EXT = Pattern.compile("(^.*)\\.(.+)$");
	private final static Pattern PATTERN_HYPHON = Pattern.compile("^.*?\\-(.+)$");
	private final static Pattern PATTERN_STRIP_CHARS = Pattern.compile("^[\\s|\\-]*(.*?)[\\s|\\-]*$");

	private final static String IGNORED_TOKENS[] = {"dvdrip","xvid","proper","ac3"};

	static {
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir,
					String renamePattern) {
				StringBuilder term = new StringBuilder(mediaFile.getName());
				if (term.indexOf(" ")==-1) {
					extractExtension(term);

					Matcher m = PATTERN_YEAR2.matcher(term);
					if (m.matches()) {
						StringBuilder start= new StringBuilder(m.group(1));
						String year= m.group(2);
						StringBuilder end= new StringBuilder(m.group(3));
						replaceDots(start);
						replaceDots(end);
						if (hasIgnoredTokens(start)) {
							term.delete(0, term.length());
							term.append(end);
							return new SearchDetails(term.toString().trim(), year);
						}
						else if (hasIgnoredTokens(end)) {
							term.delete(0, term.length());
							term.append(start);
							return new SearchDetails(term.toString().trim(), year);
						}
					}
				}
				return null;
			}


		});
		// Excact search on film name
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern) {
				StringBuilder term = new StringBuilder(mediaFile.getName());

				extractExtension(term);
				String year = extractYear(term);
				extractPart(term);
				removeUpToHyphon(term);
				removeIgnoredTokens(term);
				replaceDots(term);
				trimRubishFromEnds(term);

				String sTerm = term.toString();
				if (sTerm.length()==0) {
					return null;
				}
				return new SearchDetails(sTerm,year);
			}

		});
	}

	private static void replaceDots(StringBuilder term) {
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

	private static Integer extractPart(StringBuilder term) {
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

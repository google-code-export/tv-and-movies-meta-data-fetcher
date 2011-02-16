package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to find a films name using a series of different searching strategies
 */
public abstract class FilmSearcher extends AbstractMediaSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();
	private final static Pattern PATTERN_YEAR = Pattern.compile("(^.+)[\\(|\\[](\\d\\d\\d\\d)[\\)|\\]](.*$)");
	private final static Pattern PATTERN_PART = Pattern.compile("(^.+)(cd\\d+)(.*$)",Pattern.CASE_INSENSITIVE);

	static {
		// Excact search on film name
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern) {
				StringBuilder term = new StringBuilder(mediaFile.getName());

				String year = extractYear(term);
				Integer part = extractPart(term);

				// TODO do something with the part number
				return new SearchDetails(term.toString().trim(),year);
			}
		});
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

	/**
	 * Used to create a instance of this class
	 */
	public FilmSearcher() {
		super(strategies);
	}
}

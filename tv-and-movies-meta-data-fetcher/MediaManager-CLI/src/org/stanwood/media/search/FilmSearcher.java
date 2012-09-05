package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.Token;

/**
 * This class is used to find a films name using a series of different searching strategies
 */
public abstract class FilmSearcher extends AbstractMediaSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();
	private final static Pattern PATTERN_YEAR = Pattern.compile("(^.+)[\\(|\\[](\\d\\d\\d\\d)[\\)|\\]](.*$)"); //$NON-NLS-1$
	private final static Pattern PATTERN_YEAR2 = Pattern.compile("(^.+)[\\. (\\[](\\d\\d\\d\\d)[\\. )\\]](.*$)"); //$NON-NLS-1$
	private final static Pattern PATTERN_EXT = Pattern.compile("(^.*)\\.(.+)$"); //$NON-NLS-1$
	private final static Pattern PATTERN_HYPHON = Pattern.compile("^.*?\\-(.+)$"); //$NON-NLS-1$

	static {
		strategies.add(new ReversePatternSearchStrategy(Token.TITLE,true,true));

		// Search using a NFO file next to the film if it exists
		strategies.add(new FilmNFOSearchStrategy());

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir,
					String renamePattern,MediaDirectory mediaDir) {
				StringBuilder term = new StringBuilder(mediaFile.getName());
				List<Pattern> stripTokens = mediaDir.getMediaDirConfig().getStripTokens();
//				if (term.indexOf(" ")==-1) { //$NON-NLS-1$
					extractExtension(term);

					Matcher m = PATTERN_YEAR2.matcher(term);
					if (m.matches()) {
						StringBuilder start= new StringBuilder(m.group(1));
						String year= m.group(2);
						StringBuilder end= new StringBuilder(m.group(3));
						SearchHelper.replaceWithSpaces(start);
						SearchHelper.replaceWithSpaces(end);
						if (SearchHelper.hasStripTokens(stripTokens,start)) {
							term.delete(0, term.length());
							term.append(end);
							return new SearchDetails(term.toString().trim(), year,null);
						}
						else if (SearchHelper.hasStripTokens(stripTokens,end)) {
							term.delete(0, term.length());
							term.append(start);
							return new SearchDetails(term.toString().trim(), year,null);
						}
					}
//				}
				return null;
			}


		});
		// Excact search on film name
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
				List<Pattern> stripTokens = mediaDir.getMediaDirConfig().getStripTokens();
				StringBuilder term = new StringBuilder(mediaFile.getName());

				extractExtension(term);
				String year = extractYear(term);
				Integer part = SearchHelper.extractPart(term);
				removeUpToHyphon(term);
				SearchHelper.removeStripTokens(stripTokens,term);
				SearchHelper.replaceWithSpaces(term);
				SearchHelper.trimRubishFromEnds(term);

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

	/**
	 * Used to create a instance of this class
	 */
	public FilmSearcher() {
		super(strategies);
	}
}

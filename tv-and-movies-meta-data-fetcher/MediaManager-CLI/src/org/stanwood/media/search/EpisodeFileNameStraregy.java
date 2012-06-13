package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.MediaDirectory;

/**
 * This searching strategy will search using common show naming conventions
 */
public class EpisodeFileNameStraregy implements ISearchStrategy {

	private final static Pattern PATTERNS[] = new Pattern[] {
		Pattern.compile("(.+?)S(\\d+)E(\\d+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?) (\\d+)x(\\d+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?)S(\\d+) E(\\d+) .*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?) (\\d+)[^0-9](\\d+).*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?) (\\d)(\\d\\d) .*",Pattern.CASE_INSENSITIVE) //$NON-NLS-1$
	};

	private final static Pattern PATTERN_WEB_ADDRESS = Pattern.compile("^(\\[ *www\\..*\\.com *] *\\- *).*",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	/** {@inheritDoc} */
	@Override
	public SearchDetails getSearch(File mediaFile, File rootMediaDir,String renamePattern, MediaDirectory mediaDir) {
		StringBuilder term = new StringBuilder(mediaFile.getName());
		if (mediaFile.isDirectory()) { // Is it a NFO dir
			stripWebAddresses(term);
		}
		SearchHelper.replaceWithSpaces(term);
		SearchHelper.replaceHyphens(term);
		List<Pattern> ingoredTokens;
		if (mediaDir!=null) {
			ingoredTokens = mediaDir.getMediaDirConfig().getIgnoredTokens();
		}
		else {
			ingoredTokens = new ArrayList<Pattern>();
		}
		for (Pattern p : PATTERNS) {
			Matcher m = p.matcher(term);
			if (m.matches()) {
				return createSearchDetails(ingoredTokens,m.group(1),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
			}
		}

		return null;
	}

	private void stripWebAddresses(StringBuilder term) {
		Matcher m = PATTERN_WEB_ADDRESS.matcher(term);
		if (m.matches()) {
			term.replace(0, m.group(1).length(), ""); //$NON-NLS-1$
		}
	}

	protected SearchDetails createSearchDetails(List<Pattern>ignoredTokens,String rawTerm,int season,int episode) {
		StringBuilder term = new StringBuilder(rawTerm);
		SearchHelper.removeIgnoredTokens(ignoredTokens,term);
		SearchHelper.trimRubishFromEnds(term);
		SearchDetails details = new SearchDetails(term.toString(), null, null);
		details.setSeason(season);
		details.setEpisode(episode);
		return details;
	}

}

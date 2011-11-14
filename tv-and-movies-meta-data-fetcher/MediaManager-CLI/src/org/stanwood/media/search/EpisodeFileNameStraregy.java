package org.stanwood.media.search;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.MediaDirectory;

/**
 * This searching strategy will search using common show naming conventions
 */
public class EpisodeFileNameStraregy implements ISearchStrategy {

	private final static Pattern PATTERNS[] = new Pattern[] {
		Pattern.compile("(.+?)S\\d+E\\d+.*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?) \\d+x\\d+.*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?)S\\d+ E\\d+ .*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?) \\d+[^0-9]\\d+.*",Pattern.CASE_INSENSITIVE), //$NON-NLS-1$
		Pattern.compile("(.+?) \\d\\d\\d .*",Pattern.CASE_INSENSITIVE) //$NON-NLS-1$
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

		for (Pattern p : PATTERNS) {
			Matcher m = p.matcher(term);
			if (m.matches()) {
				return createSearchDetails(m.group(1));
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

	protected SearchDetails createSearchDetails(String rawTerm) {
		StringBuilder term = new StringBuilder(rawTerm);
		SearchHelper.removeIgnoredTokens(term);
		SearchHelper.trimRubishFromEnds(term);
		return new SearchDetails(term.toString(), null, null);
	}

}

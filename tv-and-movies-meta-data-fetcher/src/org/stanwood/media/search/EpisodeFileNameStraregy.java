package org.stanwood.media.search;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.MediaDirectory;

/**
 * This searching strategy will search using common show naming conventions
 */
public class EpisodeFileNameStraregy implements ISearchStrategy {

	private final static Pattern PATTERN_EP1 = Pattern.compile("(.*?) S\\d+E\\d+ .*",Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_EP2 = Pattern.compile("(.*?) \\d+x\\d+ .*",Pattern.CASE_INSENSITIVE);

	/** {@inheritDoc} */
	@Override
	public SearchDetails getSearch(File mediaFile, File rootMediaDir,String renamePattern, MediaDirectory mediaDir) {
		StringBuilder term = new StringBuilder(mediaFile.getName());
		SearchHelper.replaceWithSpaces(term);

		Matcher m = PATTERN_EP1.matcher(term);
		if (m.matches()) {
			return new SearchDetails(m.group(1), null, null);
		}
		m = PATTERN_EP2.matcher(term);
		if (m.matches()) {
			return new SearchDetails(m.group(1), null, null);
		}
		return null;
	}

}

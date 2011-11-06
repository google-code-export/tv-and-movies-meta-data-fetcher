package org.stanwood.media.search;

import java.io.File;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.Token;
import org.stanwood.media.setup.WatchDirConfig;

/**
 * This is a search strategy that tries to match the media directory pattern against
 * the media file filename and work out it's search results from that.
 */
public class ReversePatternSearchStrategy implements ISearchStrategy {

	private Token termToken;
	private boolean doComplexityCheck;
	private boolean disallowIgnoreTokens;

	/**
	 * The constructor
	 * @param termToken The token type that would be used when searching for this file
	 * @param doComplexityCheck True to reject patterns that don't have a certian complexity
	 * @param disallowIgnoreTokens If true, them don't make if the file name contains ingored tokens
	 */
	public ReversePatternSearchStrategy(Token termToken,boolean doComplexityCheck,boolean disallowIgnoreTokens) {
		this.termToken = termToken;
		this.doComplexityCheck = doComplexityCheck;
		this.disallowIgnoreTokens = disallowIgnoreTokens;
	}

	/**
	 * Look up the media file details using the renamePattern
	 * @param mediaFile The media file that is been processed
	 * @param rootMediaDir The root media directory
	 * @param renamePattern The pattern that is been used to rename media files
	 * @param mediaDir The media directory
	 * @return The search details
	 */
	@Override
	public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
		if (doComplexityCheck && patternComplextity(renamePattern)<4) {
			return null;
		}
		String fileName = mediaFile.getAbsolutePath();
		if (renamePattern != null && !hasIgnoreTokens(mediaFile)) {
			boolean stripped = false;
			if (mediaDir!=null) {
				for (WatchDirConfig c : mediaDir.getController().getWatchDirectories()) {
					if (fileName.startsWith(c.getWatchDir().getAbsolutePath())) {
						fileName = fileName.substring(c.getWatchDir().getAbsolutePath().length()+1);
						stripped = true;
						break;
					}
				}
			}
			if (!stripped) {
				if (fileName.startsWith(rootMediaDir.getAbsolutePath())) {
					fileName = fileName.substring(rootMediaDir.getAbsolutePath().length()+1);
					stripped = true;
				}
			}

			ReverseFilePatternMatcher rfpm = new ReverseFilePatternMatcher();
			rfpm.parse(fileName, renamePattern);
			if (rfpm.getValues()!=null) {
				String term =rfpm.getValues().get(termToken);
				if (term!=null) {
					String value = rfpm.getValues().get(Token.PART);
					Integer part = null;
					if (value!=null) {
						part = Integer.parseInt(value);
					}

					value = rfpm.getValues().get(Token.YEAR);

					return new SearchDetails(term,value,part);
				}
			}
		}
		return null;
	}

	private boolean hasIgnoreTokens(File file) {
		if (disallowIgnoreTokens) {
			return false;
		}
		StringBuilder term = new StringBuilder(file.getName());
		SearchHelper.replaceWithSpaces(term);
		return SearchHelper.hasIgnoredTokens(term);
	}

	private int patternComplextity(String renamePattern) {
		int count = 0;
		for (int i=0;i<renamePattern.length();i++) {
			if (renamePattern.charAt(i)=='%') {
				count++;
			}
		}

		return count;
	}
}
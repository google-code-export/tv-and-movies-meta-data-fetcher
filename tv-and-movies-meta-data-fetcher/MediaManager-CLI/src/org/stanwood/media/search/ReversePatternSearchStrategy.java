package org.stanwood.media.search;

import java.io.File;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.Token;

/**
 * This is a search strategy that tries to match the media directory pattern against
 * the media file filename and work out it's search results from that.
 */
public class ReversePatternSearchStrategy implements ISearchStrategy {

	private Token termToken;
	private boolean doComplexityCheck;

	/**
	 * The constructor
	 * @param termToken The token type that would be used when searching for this file
	 * @param doComplexityCheck True to reject patterns that don't have a certian complexity
	 */
	public ReversePatternSearchStrategy(Token termToken,boolean doComplexityCheck) {
		this.termToken = termToken;
		this.doComplexityCheck = doComplexityCheck;
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
		if (renamePattern != null && fileName.startsWith(rootMediaDir.getAbsolutePath())) {
			fileName = fileName.substring(rootMediaDir.getAbsolutePath().length()+1);

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
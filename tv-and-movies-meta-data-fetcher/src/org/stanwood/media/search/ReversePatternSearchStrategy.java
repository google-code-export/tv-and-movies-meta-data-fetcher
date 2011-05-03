package org.stanwood.media.search;

import java.io.File;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.Token;

public class ReversePatternSearchStrategy implements ISearchStrategy {

	private Token termToken;

	public ReversePatternSearchStrategy(Token termToken) {
		this.termToken = termToken;
	}

	@Override
	public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
		String fileName = episodeFile.getAbsolutePath();
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
}
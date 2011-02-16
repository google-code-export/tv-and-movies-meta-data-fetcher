package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to find a shows name using a series of different searching strategies
 */
public abstract class ShowSearcher extends AbstractMediaSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();

	static {
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern) {
				String fileName = episodeFile.getAbsolutePath();
				if (renamePattern != null && fileName.startsWith(rootMediaDir.getAbsolutePath())) {
					fileName = fileName.substring(rootMediaDir.getAbsolutePath().length()+1);

					ReverseFilePatternMatcher rfpm = new ReverseFilePatternMatcher();
					rfpm.parse(fileName, renamePattern);
					if (rfpm.getValues()!=null) {
						return new SearchDetails(rfpm.getValues().get("n"),null);
					}
				}
				return null;
			}
		});

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern) {
				return new SearchDetails(episodeFile.getParentFile().getName(),null);
			}
		});
	}

	/**
	 * Used to create a instance of this class
	 */
	public ShowSearcher() {
		super(strategies);
	}
}

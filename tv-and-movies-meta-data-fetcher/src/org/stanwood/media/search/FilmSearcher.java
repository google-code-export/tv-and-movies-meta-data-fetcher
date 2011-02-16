package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to find a films name using a series of different searching strategies
 */
public abstract class FilmSearcher extends AbstractMediaSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();

	static {
		// Excact search on film name
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern) {
				return new SearchDetails(mediaFile.getName().trim(),null);
			}
		});
	}

	/**
	 * Used to create a instance of this class
	 */
	public FilmSearcher() {
		super(strategies);
	}
}

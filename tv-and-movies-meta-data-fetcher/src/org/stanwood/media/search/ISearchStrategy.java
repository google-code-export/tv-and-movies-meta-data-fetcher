package org.stanwood.media.search;

import java.io.File;

/**
 * This interface should be implemented by classes that provide
 * a search strategy that can be used to search for tv shows.
 */
public interface ISearchStrategy {


	/**
	 * This method should be implemented to find a shows name
	 * @param episodeFile The episode file that is been processed
	 * @param rootMediaDir The root media directory
	 * @param renamePattern The pattern that is been used to rename media files
	 * @return The shows name
	 */
	public String getSearchTerm(File episodeFile, File rootMediaDir, String renamePattern);

}

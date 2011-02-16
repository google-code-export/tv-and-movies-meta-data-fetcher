package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;

/**
 * This should be implemented by classes that can search for media
 */
public interface IMediaSearcher {

	/**
	 * Used to search for a show id
	 * @param mediaFile The episode file been processed
	 * @param rootMediaDir The root media directory
	 * @param renamePattern The rename pattern been used
	 * @return The results of the search, or null if nothing could be found
	 * @throws MalformedURLException Thrown if their is a problem construction URL's
	 * @throws IOException Thrown if their is a IO problem
	 * @throws SourceException Thrown if their are any source problems
	 */
	public SearchResult search(File mediaFile, File rootMediaDir,String renamePattern) throws MalformedURLException, IOException,SourceException;

}

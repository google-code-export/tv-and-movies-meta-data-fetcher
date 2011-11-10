package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;

/**
 * This should be implemented by classes that can search for media
 */
public interface IMediaSearcher {


	/**
	 * Used to search for a media id
	 * @param mediaFile The episode file been processed
	 * @param mediaDir The root media directory
	 * @param useSources True to search sources, otherwise will only use stores
	 * @return The results of the search, or null if nothing could be found
	 * @throws MalformedURLException Thrown if their is a problem construction URL's
	 * @throws IOException Thrown if their is a IO problem
	 * @throws SourceException Thrown if their are any source problems
	 * @throws StoreException Thrown if their is a problem related to stores
	 */
	public SearchResult search(File mediaFile, MediaDirectory mediaDir,boolean useSources) throws MalformedURLException, IOException, SourceException, StoreException;

}

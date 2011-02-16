package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;

/**
 * This class is used to search for the a media files name
 */
public abstract class AbstractMediaSearcher implements IMediaSearcher {

	private List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();

	/**
	 * Used to create a instance of this class
	 * @param strategies The search strategies that should be used
	 */
	public AbstractMediaSearcher(List<ISearchStrategy> strategies) {
		this.strategies =strategies;

	}

	/**
	 * Used todo a search on the term and the year and return the result
	 * @param term The term to search for (Usally a film name or tv show name).
	 * @param year The year of the media or null if not to be used in the search
	 * @return The search result or null if nothing could be found
	 * @throws MalformedURLException Thrown if their is a URL construction problem
	 * @throws IOException Thrown if their is a IO problem
	 * @throws SourceException Thrown if their is a problem searching via a source
	 */
	protected abstract SearchResult doSearch(String term,String year) throws MalformedURLException, IOException, SourceException;

	/**
	 * Usd to search for a show id
	 * @param mediaFile The episode file been processed
	 * @param rootMediaDir The root media directory
	 * @param renamePattern The rename pattern been used
	 * @return The results of the search, or null if nothing could be found
	 * @throws MalformedURLException Thrown if their is a problem construction URL's
	 * @throws IOException Thrown if their is a IO problem
	 * @throws SourceException Thrown if their are any source problems
	 */
	@Override
	public final SearchResult search(File mediaFile, File rootMediaDir, String renamePattern) throws MalformedURLException, IOException, SourceException {
		for (ISearchStrategy strategy :strategies) {
			SearchDetails searchDetails = strategy.getSearch(mediaFile,rootMediaDir,renamePattern);
			if (searchDetails!=null) {
				SearchResult result = doSearch(searchDetails.getTerm(),searchDetails.getYear());
				if (result!=null) {
					return result;
				}
			}
		}
		return null;
	}
}

package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;

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
	 * @param term The term to search for (Usually a film name or tv show name).
	 * @param year The year of the media or null if not to be used in the search
	 * @param mediaFile The media file we are searching for
	 * @param useSources True to search sources, otherwise will only use stores
	 * @return The search result or null if nothing could be found
	 * @throws MalformedURLException Thrown if their is a URL construction problem
	 * @throws IOException Thrown if their is a IO problem
	 * @throws SourceException Thrown if their is a problem searching via a source
	 */
	protected abstract SearchResult doSearch(File mediaFile,String term,String year,Integer part,boolean useSources) throws MalformedURLException, IOException, SourceException, StoreException;

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
	@Override
	public final SearchResult search(File mediaFile, MediaDirectory mediaDir,boolean useSources) throws MalformedURLException, IOException, SourceException, StoreException {
		File rootMediaDir = mediaDir.getMediaDirConfig().getMediaDir();
		String renamePattern = mediaDir.getMediaDirConfig().getPattern();
		for (ISearchStrategy strategy :strategies) {
			SearchDetails searchDetails = strategy.getSearch(mediaFile,rootMediaDir,renamePattern,mediaDir);
			if (searchDetails!=null) {
				SearchResult result = doSearch(mediaFile,searchDetails.getTerm(),searchDetails.getYear(),searchDetails.getPart(),useSources);
				if (result!=null) {
					result.setEpisode(searchDetails.getEpisode());
					result.setSeason(searchDetails.getSeason());
					return result;
				}
				return null;
			}
		}
		return null;
	}
}

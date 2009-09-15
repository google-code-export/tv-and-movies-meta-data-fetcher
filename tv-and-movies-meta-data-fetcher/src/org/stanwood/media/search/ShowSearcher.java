package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.SearchResult;

/**
 * This class is used to find a shows name using a series of different searching strategies
 */
public abstract class ShowSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();

	static {
		strategies.add(new ISearchStrategy() {
			@Override
			public String getSearchTerm(File episodeFile, File rootMediaDir, String renamePattern) {
				String fileName = episodeFile.getAbsolutePath();
				if (renamePattern != null && fileName.startsWith(rootMediaDir.getAbsolutePath())) {
					fileName = fileName.substring(rootMediaDir.getAbsolutePath().length()+1);

					ReverseFilePatternMatcher rfpm = new ReverseFilePatternMatcher();
					rfpm.parse(fileName, renamePattern);
					if (rfpm.getValues()!=null) {
						return rfpm.getValues().get("n");
					}
				}
				return null;
			}
		});

		strategies.add(new ISearchStrategy() {
			@Override
			public String getSearchTerm(File episodeFile, File rootMediaDir, String renamePattern) {
				return episodeFile.getParentFile().getName();
			}
		});
	}

	protected abstract SearchResult doSearch(String name) throws MalformedURLException, IOException;

	/**
	 * Usd to search for a show id
	 * @param episodeFile The episode file been processed
	 * @param rootMediaDir The root media directory
	 * @param renamePattern The rename pattern been used
	 * @return The results of the search, or null if nothing could be found
	 * @throws MalformedURLException Thrown if their is a problem construction URL's
	 * @throws IOException Thrown if their is a IO problem
	 */
	public final SearchResult search(File episodeFile, File rootMediaDir, String renamePattern) throws MalformedURLException, IOException {
		for (ISearchStrategy strategy :strategies) {
			String term = strategy.getSearchTerm(episodeFile,rootMediaDir,renamePattern);
			if (term!=null) {
				SearchResult result = doSearch(term);
				if (result!=null) {
					return result;
				}
			}
		}
		return null;
	}
}

package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.SearchResult;

public abstract class ShowSearcher {

	private static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();

	static {
		strategies.add(new ISearchStrategy() {
			@Override
			public String getSearchTerm(File episodeFile, File rootMediaDir, String renamePattern) {
				String fileName = episodeFile.getAbsolutePath();
				if (fileName.startsWith(rootMediaDir.getAbsolutePath())) {
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

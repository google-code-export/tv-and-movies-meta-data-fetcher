package org.stanwood.media.search;

import java.io.File;

public interface ISearchStrategy {

	public String getSearchTerm(File episodeFile, File rootMediaDir, String renamePattern);

}

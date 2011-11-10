package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.Token;

/**
 * This class is used to find a shows name using a series of different searching strategies
 */
public abstract class ShowSearcher extends AbstractMediaSearcher {



	private final static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();
	private final static ISearchStrategy episodeFileStrategy;

	static {
		strategies.add(new ReversePatternSearchStrategy(Token.SHOW_NAME,true,false));

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
				File nfo = NFOSearchHelper.findNFOfile(mediaDir, episodeFile);
				if (nfo!=null && nfo.exists()) {
					SearchDetails result = episodeFileStrategy.getSearch(nfo.getParentFile(), rootMediaDir, renamePattern,mediaDir);
					if (result!=null) {
						return result;
					}
				}
				return null;
			}
		});

		episodeFileStrategy = new EpisodeFileNameStraregy();
		strategies.add(episodeFileStrategy);

		// Search using tho parent directory as a show name
		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
				return new SearchDetails(episodeFile.getParentFile().getName(),null,1);
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

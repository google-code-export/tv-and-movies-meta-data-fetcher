package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to find a shows name using a series of different searching strategies
 */
public abstract class ShowSearcher extends AbstractMediaSearcher {

	private final static List<ISearchStrategy> strategies = new ArrayList<ISearchStrategy>();
	private final static ISearchStrategy episodeFileStrategy;

	private final static Pattern PATTERN_EP1 = Pattern.compile("(.*?)\\.S\\d+E\\d+\\..*",Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_EP2 = Pattern.compile("(.*?)\\.\\d+x\\d+\\..*",Pattern.CASE_INSENSITIVE);

	static {

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern) {
				String fileName = episodeFile.getAbsolutePath();
				if (renamePattern != null && fileName.startsWith(rootMediaDir.getAbsolutePath())) {
					fileName = fileName.substring(rootMediaDir.getAbsolutePath().length()+1);

					ReverseFilePatternMatcher rfpm = new ReverseFilePatternMatcher();
					rfpm.parse(fileName, renamePattern);
					if (rfpm.getValues()!=null) {
						String term =rfpm.getValues().get("n");
						if (term!=null) {
							return new SearchDetails(term,null,1);
						}
					}
				}
				return null;
			}
		});

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern) {
				if (!episodeFile.getParentFile().equals(rootMediaDir)) {
					String fullName = episodeFile.getName();
					int pos = fullName.lastIndexOf(".");
					String name = fullName.substring(0,pos);
					File nfo = new File(episodeFile.getParentFile(),name+".nfo");
					if (nfo.exists()) {
						SearchDetails result = episodeFileStrategy.getSearch(episodeFile.getParentFile(), rootMediaDir, renamePattern);
						if (result!=null) {
							return result;
						}
					}

				}
				return null;
			}
		});

		episodeFileStrategy = new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern) {
				StringBuilder fileName = new StringBuilder(episodeFile.getName());
				Matcher m = PATTERN_EP1.matcher(fileName);
				StringBuilder term = null;
				if (m.matches()) {
					term = new StringBuilder(m.group(1));

				}
				m = PATTERN_EP2.matcher(fileName);
				if (m.matches()) {
					term = new StringBuilder(m.group(1));
				}

				if (term!=null) {
					int pos = term.lastIndexOf(" ");
					if (pos!=-1) {
						term.replace(0, pos+1, "");
					}
					SearchHelper.replaceWithSpaces(term);
					return new SearchDetails(term.toString(),null,1);
				}

				return null;
			}
		};
		strategies.add(episodeFileStrategy);

		strategies.add(new ISearchStrategy() {
			@Override
			public SearchDetails getSearch(File episodeFile, File rootMediaDir, String renamePattern) {
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

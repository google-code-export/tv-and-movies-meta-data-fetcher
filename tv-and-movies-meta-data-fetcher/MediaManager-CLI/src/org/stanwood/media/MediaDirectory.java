package org.stanwood.media;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.search.AbstractMediaSearcher;
import org.stanwood.media.search.FilmSearcher;
import org.stanwood.media.search.SearchHelper;
import org.stanwood.media.search.ShowSearcher;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

/**
 * Used to represent a directory that contains media and can be managed by MediaManager
 */
public class MediaDirectory {

	private final static Log log = LogFactory.getLog(MediaDirectory.class);

	private List<ISource> sources = null;
	private List<IStore> stores = null;
	private List<IAction> actions = null;
	private Controller controller;

	private MediaDirConfig dirConfig;

	private AbstractMediaSearcher filmSearcher;

	private AbstractMediaSearcher showSearcher;

	/**
	 * The constructor
	 * @param controller The media controller
	 * @param config The configuration of the media directory
	 * @param mediaDir The location of the media directory
	 * @throws ConfigException Thrown if their is a problem reading the configuration
	 */
	public MediaDirectory(Controller controller,ConfigReader config,File mediaDir) throws ConfigException {
		this.controller = controller;

		createSearchers();

		dirConfig = config.getMediaDirectory(mediaDir);
		if (controller!=null) {
			stores = config.loadStoresFromConfigFile(controller,dirConfig);
			sources = config.loadSourcesFromConfigFile(controller,dirConfig);
			actions = config.loadActionsFromConfigFile(controller,dirConfig);

			try {
				for (ISource source : sources) {
					source.setMediaDirConfig(this);
				}
			} catch (SourceException e) {
				throw new ConfigException(Messages.getString("MediaDirectory.UNABLE_TO_SETUP_SOURCES"),e); //$NON-NLS-1$
			}


			for (IStore store : stores) {
				try {
					store.init(getController(),getController().getNativeFolder());
					store.upgrade(this);
				} catch (StoreException e) {
					log.error(MessageFormat.format(Messages.getString("MediaDirectory.UNABLE_UPGRADE_STORES"),store.getClass()),e); //$NON-NLS-1$
				}
			}
		}
	}

	protected void createSearchers() {
		this.filmSearcher = new FilmSearcher() {
			@Override
			public SearchResult doSearch(File mediaFile,String name,String year,Integer part,boolean useSources) throws MalformedURLException, IOException, SourceException, StoreException {
				StringBuilder term = new StringBuilder(name);
				SearchHelper.removeUnwantedCharacters(term);
				if (name==null) {
					return null;
				}
				if (year==null) {
					year =""; //$NON-NLS-1$
				}
				return searchMedia(term.toString(),year,dirConfig.getMode(),part,dirConfig,mediaFile,useSources);
			}
		};

		this.showSearcher = new ShowSearcher() {
			@Override
			public SearchResult doSearch(File mediaFile,String name,String year,Integer part,boolean useSources) throws MalformedURLException, IOException, SourceException, StoreException {
				if (name==null) {
					return null;
				}
				if (year==null) {
					year =""; //$NON-NLS-1$
				}
				StringBuilder term = new StringBuilder(name);
				SearchHelper.removeUnwantedCharacters(term);
				return searchMedia(term.toString(),year,dirConfig.getMode(),part,dirConfig,mediaFile,useSources);
			}
		};
	}

	/**
	 * Get a show with a given show id and source id. This will first try to retrieve the show from the stores. If it is
	 * not able to do this, then it will try the sources. If it can't retrieve it from either the sources or the stores,
	 * then null is returned. If the refresh parameter is set too true, then the stores are ignored and it retrieves
	 * data strait from the sources. If data is retrieved from the source, then it makes an attempt to write it too the
	 * stores.
	 *
	 * @param rootMediaDir The root media directory
	 * @param episodeFile The file the episode is stored in
	 * @param searchResult The search results from looking for a show
	 * @param refresh If true, then the stores are ignored.
	 * @return The show, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public IShow getShow(File rootMediaDir,File episodeFile, SearchResult searchResult, boolean refresh) throws MalformedURLException,
			SourceException, IOException, StoreException {

		IShow show = null;
		if (!refresh) {
			for (IStore store : stores) {
				show = store.getShow(rootMediaDir,episodeFile, searchResult.getId());
				if (show != null) {
					break;
				}
			}
		}

		if (show == null) {
			log.info(Messages.getString("MediaDirectory.UNABLE_TO_READ_FROM_SOURCES")); //$NON-NLS-1$
			URL showUrl = new URL(searchResult.getUrl());
			String sourceId = searchResult.getSourceId();
			for (ISource source : sources) {
				try {
					if (sourceId==null || sourceId.equals("") || source.getInfo().getId().equals(sourceId)) { //$NON-NLS-1$
						show = source.getShow(searchResult.getId(),showUrl,episodeFile);
						if (show != null) {
							for (IStore store : stores) {
								store.cacheShow(rootMediaDir,episodeFile, show);
							}
							break;
						}
					}
				}
				catch (SourceException e) {
					log.error(e.getMessage(),e);
					return null;
				}
			}
		}

		return show;
	}

	/**
	 * Get a film with a given film id and source id. This will first try to retrieve the film from the stores. If it is
	 * not able to do this, then it will try the sources. If it can't retrieve it from either the sources or the stores,
	 * then null is returned. If the refresh parameter is set too true, then the stores are ignored and it retrieves
	 * data strait from the sources. If data is retrieved from the source, then it makes an attempt to write it too the
	 * stores.
	 *
	 * @param rootMediaDir The root media directory
	 * @param filmFile The file the film is stored in
	 * @param searchResult The resulting film from a search
	 * @param refresh If true, then the stores are ignored.
	 * @return The film, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public IFilm getFilm(File rootMediaDir,File filmFile, SearchResult searchResult, boolean refresh) throws MalformedURLException,
			SourceException, IOException, StoreException {
		IFilm film = null;
		if (!refresh) {
			for (IStore store : stores) {
				film = store.getFilm(rootMediaDir,filmFile, searchResult.getId());
				if (film != null) {
					break;
				}
			}
		}

		if (film == null) {
			log.info(MessageFormat.format(Messages.getString("MediaDirectory.READING_FILE_FROM_SOURCE_WITH_ID"),searchResult.getSourceId(),searchResult.getId() )); //$NON-NLS-1$
			URL url = new URL(searchResult.getUrl());
			String sourceId = searchResult.getSourceId();
			for (ISource source : sources) {
				try {
					if (source.getInfo().getId().equals(sourceId)) {
						film = source.getFilm(searchResult.getId(),url,filmFile);
						if (film != null) {
							for (IStore store : stores) {
								if (!controller.isTestRun()) {
									store.cacheFilm(rootMediaDir,filmFile, film,searchResult.getPart());
								}
							}
							break;
						}
					}
				}
				catch (SourceException e) {
					log.error(e.getMessage(),e);
					return null;
				}
			}
		}

		return film;
	}

	/**
	 * Get a season with a given season number. This will first try to retrieve the season from the stores. If it is not
	 * able to do this, then it will try the sources. If it can't retrieve it from either the sources or the stores,
	 * then null is returned. If the refresh parameter is set too true, then the stores are ignored and it retrieves
	 * data strait from the sources. If data is retrieved from the source, then it makes an attempt to write it too the
	 * stores.
	 *
	 * @param rootMediaDir The root media directory
	 * @param episodeFile The file the episode is stored in
	 * @param show The show the season belongs too
	 * @param seasonNum The season number
	 * @param refresh If true, then the stores are ignored.
	 * @return The season, or null if it can't be found.
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public ISeason getSeason(File rootMediaDir,File episodeFile, IShow show, int seasonNum, boolean refresh) throws SourceException,
			IOException, StoreException {
		String sourceId = show.getSourceId();
		ISeason season = null;
		if (!refresh) {
			for (IStore store : stores) {
				season = store.getSeason(rootMediaDir,episodeFile, show, seasonNum);
				if (season != null) {
					break;
				}
			}
		}

		if (season == null) {
			log.info(Messages.getString("MediaDirectory.READING_SEASON_FROM_SOURCE")); //$NON-NLS-1$
			for (ISource source : sources) {
				try {
					if (source.getInfo().getId().equals(sourceId)) {
						season = source.getSeason(show, seasonNum);
						if (season != null) {
							for (IStore store : stores) {
								store.cacheSeason(rootMediaDir,episodeFile, season);
							}
							break;
						}
					}
				}
				catch (SourceException e) {
					log.error(e.getMessage(),e);
					return null;
				}
			}
		}

		return season;
	}

	/**
	 * Get a episode with a given episode number. This will first try to retrieve the episode from the stores. If it is
	 * not able to do this, then it will try the sources. If it can't retrieve it from either the sources or the stores,
	 * then null is returned. If the refresh parameter is set too true, then the stores are ignored and it retrieves
	 * data strait from the sources. If data is retrieved from the source, then it makes an attempt to write it too the
	 * stores.
	 *
	 * @param rootMediaDir The root media directory
	 * @param season The season the episode belongs too
	 * @param episodeNums The episode numbers
	 * @param refresh If true, then the stores are ignored.
	 * @param episodeFile The file the episode is stored in
	 * @return The episode, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public IEpisode getEpisode(File rootMediaDir,File episodeFile, ISeason season, List<Integer> episodeNums, boolean refresh) throws SourceException,
			MalformedURLException, IOException, StoreException {
		IEpisode episode = null;
		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getEpisode(rootMediaDir,episodeFile, season, episodeNums);
				if (episode != null) {
					break;
				}
			}
		}

		if (episode == null) {
			log.info(Messages.getString("MediaDirectory.READING_EPISODE_FROM_SOURCES")); //$NON-NLS-1$
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				try {
					if (source.getInfo().getId().equals(sourceId)) {
						episode = source.getEpisode(season, episodeNums.get(0),episodeFile);
						if (episode != null) {
							episode.setEpisodes(episodeNums);
							for (IStore store : stores) {
								store.cacheEpisode(rootMediaDir,episodeFile, episode);
							}
							break;
						}
					}
				}
				catch (SourceException e) {
					log.error(e.getMessage(),e);
					return null;
				}
			}
		}

		return episode;
	}

	/**
	 * Get a special episode with a given special episode number. This will first try to retrieve the special episode
	 * from the stores. If it is not able to do this, then it will try the sources. If it can't retrieve it from either
	 * the sources or the stores, then null is returned. If the refresh parameter is set too true, then the stores are
	 * ignored and it retrieves data strait from the sources. If data is retrieved from the source, then it makes an
	 * attempt to write it too the stores.
	 *
	 * @param rootMediaDir The root media directory
	 * @param season The season the episode belongs too
	 * @param specialNums The special episode numbers
	 * @param refresh If true, then the stores are ignored.
	 * @param specialFile The file the special episode is stored in
	 * @return The special episode, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public IEpisode getSpecial(File rootMediaDir,File specialFile, ISeason season, List<Integer> specialNums, boolean refresh) throws SourceException,
			MalformedURLException, IOException, StoreException {
		IEpisode episode = null;

		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getSpecial(rootMediaDir,specialFile, season, specialNums);
				if (episode != null) {
					break;
				}
			}
		}

		if (episode == null) {
			log.info(Messages.getString("MediaDirectory.READING_SPECIAL_FROM_SOURCES")); //$NON-NLS-1$
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getInfo().getId().equals(sourceId)) {
					episode = source.getSpecial(season, specialNums.get(0),specialFile);
					if (episode != null) {
						for (IStore store : stores) {
							episode.setEpisodes(specialNums);
							store.cacheEpisode(rootMediaDir,specialFile, episode);
						}
						break;
					}
				}
			}
		}

		return episode;
	}

	/**
	 * This is called to notify stores that a file has changed.
	 * @param file The file that has changed
	 * @throws StoreException Thrown if their are any problems
	 */
	public void fileChanged(File file) throws StoreException {
		for (IStore store : stores) {
			store.fileUpdated(this, file);
		}
	}


	/**
	 * This will search for a show id in the stores and sources. It will use the show directory as the name of the show
	 * if needed.
	 * @param mediaFile The file the media is stored in
	 * @param useSources True to search sources, otherwise will only use stores
	 * @return The results of searching for the show, or null if it can't be found.
	 * @throws SourceException Thrown if their is a problem reading from a source
	 * @throws StoreException Thrown if their is a problem reading for a store
	 * @throws IOException Throw if their is a IO problem
	 * @throws MalformedURLException Throw if their is a problem creating a URL
	 */
	public SearchResult searchForVideoId(File mediaFile,boolean useSources) throws SourceException, StoreException,
			MalformedURLException, IOException {
		AbstractMediaSearcher s = null;
		if (dirConfig.getMode() == Mode.TV_SHOW) {
			s = showSearcher;
		}
		else if (dirConfig.getMode() == Mode.FILM) {
			s = filmSearcher;
		}
		else {
			return null;
		}

		return s.search(mediaFile,this,useSources);
	}

	private SearchResult searchMedia(String name,String year, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile,boolean useSources) throws StoreException, SourceException {
		SearchResult result = null;
		for (IStore store : stores) {
			result = store.searchMedia(name,mode,part,dirConfig,mediaFile);
			if (result != null) {
				return result;
			}
		}

		if (useSources) {
			for (ISource source : sources) {
				result = source.searchMedia(name,year,mode,part);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	/**
	 * This is used when a file that holds a episode or film has been renamed
	 *
	 * @param rootMediaDir The root media directory
	 * @param oldFile The old file
	 * @param newFile The new file
	 * @throws StoreException Thrown if their is a problem renaming files
	 */
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) throws StoreException {
		for (IStore store : stores) {
			store.renamedFile(rootMediaDir,oldFile, newFile);
		}
	}

	/**
	 * Get the configuration of the media directory
	 * @return the configuration of the media directory
	 */
	public MediaDirConfig getMediaDirConfig() {
		return dirConfig;
	}

	/**
	 * Get the stores been used by the media directory
	 * @return The stores
	 */
	public List<IStore>getStores() {
		return stores;
	}

	/**
	 * Get the sources been used by the media directory
	 * @return The sources
	 */
	public List<ISource>getSources() {
		return sources;
	}

	/**
	 * Get the actions been used by the media directory
	 * @return The actions
	 */
	public List<IAction>getActions() {
		return actions;
	}

	/**
	 * Used to get the full location of a file within the media directory

	 * @param name the path of the file relative to the media directory
	 * @return the full location of a file within the media directory
	 */
	public File getPath(String name) {
		File dir = getMediaDirConfig().getMediaDir();
		StringTokenizer tok = new StringTokenizer(name,""+File.separatorChar); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			dir = new File(dir,tok.nextToken());
		}
		return dir;
	}

	/**
	 * Used to get the media controller
	 * @return the media controller
	 */
	public Controller getController() {
		return controller;
	}
}

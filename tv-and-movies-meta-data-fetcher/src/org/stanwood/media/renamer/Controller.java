/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.renamer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.memory.MemoryStore;
import org.stanwood.media.store.xmlstore.XMLStore2;

/**
 * The controller is used to control access to the stores and and sources. This is a singleton class, and just first be
 * setup using the @see initWithDefaults() or @see initFromConfigFile() methods. From then on, getInstance() can be
 * called to a access the methods used to control stores and sources.
 */
public class Controller {

	private final static Log log = LogFactory.getLog(Controller.class);

	private static Controller instance = null;

	private static List<ISource> sources = null;
	private static List<IStore> stores = null;
//	private static XBMCAddonManager xbmcMgr;
	private static ConfigReader configReader = null;

	private Controller() {

	}

	/**
	 * Initialise the controller using the default settings. This will add a MemoryStore, XMLStore and a TVCOMSource.
	 * Once the store has been initialised, it can't be Initialised again.
	 * @throws SourceException Thrown if their is a problem
	 */
	public static void initWithDefaults() throws SourceException {
		synchronized (Controller.class) {
			if (stores != null || sources != null) {
				throw new IllegalStateException("Controller allready initialized");
			}
		}

		stores = new ArrayList<IStore>();
		stores.add(new MemoryStore());
		stores.add(new XMLStore2());

		sources = new ArrayList<ISource>();
		for (Mode mode : Mode.values()) {
			sources.add(configReader.getDefaultSource(mode));
		}
	}

	/**
	 * Used to get the default source ID
	 * @param mode The mode that were looking for a source id in
	 * @return The default source ID for a given mode
	 * @throws SourceException Thrown if their is a problem getting the default source ID
	 */
	public static String getDefaultSourceID(Mode mode) throws SourceException {
		return configReader.getDefaultSourceID(mode);
	}

	/**
	 * Initialise the stores used a configuration file. Once the store has been initialised, it can't be Initialised
	 * again.
	 *
	 * @param config The parsed configuration file.
	 */
	public static void initFromConfigFile(ConfigReader config) {
		if (stores != null || sources != null) {
			throw new IllegalStateException("Controller allready initialized");
		}
		configReader = config;

		try {
			stores = config.loadStoresFromConfigFile();
			sources = config.loadSourcesFromConfigFile();
		} catch (ConfigException e) {
			log.fatal(e.getMessage(),e);
		}
	}



	/**
	 * Get a instance of the controller.
	 *
	 * @return A instance of the controller.
	 */
	public static Controller getInstance() {
		if (stores == null || sources == null) {
			throw new IllegalStateException(
					"The controller must be initialized before use by calling initWithDefaults() or initFromConfigFile() ");
		}
		if (instance == null) {
			instance = new Controller();
		}
		return instance;
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
	public Show getShow(File rootMediaDir,File episodeFile, SearchResult searchResult, boolean refresh) throws MalformedURLException,
			SourceException, IOException, StoreException {

		Show show = null;
		if (!refresh) {
			for (IStore store : stores) {
				show = store.getShow(rootMediaDir,episodeFile, searchResult.getId());
				if (show != null) {
					break;
				}
			}
		} else {
			show = stores.get(0).getShow(rootMediaDir,episodeFile, searchResult.getId());
		}

		if (show == null) {
			log.info("Reading show from sources");
			URL showUrl = new URL(searchResult.getUrl());
			String sourceId = searchResult.getSourceId();
			for (ISource source : sources) {
				if (sourceId==null || sourceId.equals("") || source.getSourceId().equals(sourceId)) {
					show = source.getShow(searchResult.getId(),showUrl);
					if (show != null) {
						for (IStore store : stores) {
							store.cacheShow(rootMediaDir,episodeFile, show);
						}
						break;
					}
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
	public Film getFilm(File rootMediaDir,File filmFile, SearchResult searchResult, boolean refresh) throws MalformedURLException,
			SourceException, IOException, StoreException {
		Film film = null;
		if (!refresh) {
			for (IStore store : stores) {
				film = store.getFilm(rootMediaDir,filmFile, searchResult.getId());
				if (film != null) {
					break;
				}
			}
		} else {
			film = stores.get(0).getFilm(rootMediaDir,filmFile, searchResult.getId());
		}

		if (film == null) {
			log.info("Reading film from sources");
			URL url = new URL(searchResult.getUrl());
			String sourceId = searchResult.getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					film = source.getFilm(searchResult.getId(),url);
					if (film != null) {
						for (IStore store : stores) {
							store.cacheFilm(rootMediaDir,filmFile, film);
						}
						break;
					}
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
	public Season getSeason(File rootMediaDir,File episodeFile, Show show, int seasonNum, boolean refresh) throws SourceException,
			IOException, StoreException {
		String sourceId = show.getSourceId();
		Season season = null;
		if (!refresh) {
			for (IStore store : stores) {
				season = store.getSeason(rootMediaDir,episodeFile, show, seasonNum);
				if (season != null) {
					break;
				}
			}
		} else {
			season = stores.get(0).getSeason(rootMediaDir,episodeFile, show, seasonNum);
		}

		if (season == null) {
			log.info("Reading season from sources");
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					season = source.getSeason(show, seasonNum);
					if (season != null) {
						for (IStore store : stores) {
							store.cacheSeason(rootMediaDir,episodeFile, season);
//							if (season.getEpisodes() != null) {
//								for (Episode episode : season.getEpisodes()) {
//									store.cacheEpisode(rootMediaDir,episodeFile, episode);
//								}
//							}
//							if (season.getSpecials() != null) {
//								for (Episode episode : season.getSpecials()) {
//									store.cacheEpisode(rootMediaDir,episodeFile, episode);
//								}
//							}
						}
						break;
					}
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
	 * @param episodeNum The episode number
	 * @param refresh If true, then the stores are ignored.
	 * @param episodeFile The file the episode is stored in
	 * @return The episode, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public Episode getEpisode(File rootMediaDir,File episodeFile, Season season, int episodeNum, boolean refresh) throws SourceException,
			MalformedURLException, IOException, StoreException {
		Episode episode = null;
		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getEpisode(rootMediaDir,episodeFile, season, episodeNum);
				if (episode != null) {
					break;
				}
			}
		} else {
			episode = stores.get(0).getEpisode(rootMediaDir,episodeFile, season, episodeNum);
		}

		if (episode == null) {
			log.info("Reading episode from sources");
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					episode = source.getEpisode(season, episodeNum);
					if (episode != null) {
						for (IStore store : stores) {
							store.cacheEpisode(rootMediaDir,episodeFile, episode);
						}
						break;
					}
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
	 * @param specialNum The special episode number
	 * @param refresh If true, then the stores are ignored.
	 * @param specialFile The file the special episode is stored in
	 * @return The special episode, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public Episode getSpecial(File rootMediaDir,File specialFile, Season season, int specialNum, boolean refresh) throws SourceException,
			MalformedURLException, IOException, StoreException {
		Episode episode = null;

		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getSpecial(rootMediaDir,specialFile, season, specialNum);
				if (episode != null) {
					break;
				}
			}
		} else {
			episode = stores.get(0).getEpisode(rootMediaDir,specialFile, season, specialNum);
		}

		if (episode == null) {
			log.info("Reading special from sources");
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					episode = source.getSpecial(season, specialNum);
					if (episode != null) {
						for (IStore store : stores) {
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
	 * This will search for a show id in the stores and sources. It will use the show directory as the name of the show
	 * if needed.
	 *
	 * @param rootMediaDir The root media directory
	 * @param mode The mode that the search operation should be performed in
	 * @param mediaFile The file the media is stored in
	 * @param renamePattern The rename pattern been used, or null if one is not been used
	 * @return The results of searching for the show, or null if it can't be found.
	 * @throws SourceException Thrown if their is a problem reading from a source
	 * @throws StoreException Thrown if their is a problem reading for a store
	 * @throws IOException Throw if their is a IO problem
	 * @throws MalformedURLException Throw if their is a problem creating a URL
	 */
	public SearchResult searchForVideoId(File rootMediaDir,Mode mode, File mediaFile,String renamePattern) throws SourceException, StoreException,
			MalformedURLException, IOException {
		SearchResult result = null;
		for (IStore store : stores) {
			result = store.searchForVideoId(rootMediaDir,mode, mediaFile,renamePattern);
			if (result != null) {
				return result;
			}
		}

		for (ISource source : sources) {
			result = source.searchForVideoId(rootMediaDir,mode, mediaFile,renamePattern);
			if (result != null) {
				return result;
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

	public static ISource getSource(String sourceId) {
		if (sourceId == null) {
			return null;
		}

		for (ISource source : sources) {
			if (source.getSourceId().equals(sourceId)) {
				return source;
			}
		}
		return null;
	}

	/* package for test */final static void destoryController() {
		instance = null;
		stores = null;
		sources = null;
	}


}

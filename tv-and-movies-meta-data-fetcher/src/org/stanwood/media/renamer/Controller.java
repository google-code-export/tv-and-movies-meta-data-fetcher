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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.setup.SourceConfig;
import org.stanwood.media.setup.StoreConfig;
import org.stanwood.media.source.IMDBSource;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.TVCOMSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.XMLStore;
import org.stanwood.media.store.memory.MemoryStore;

/**
 * The controller is used to control access to the stores and and sources. This is a singleton class, and just first be
 * setup using the @see initWithDefaults() or @see initFromConfigFile() methods. From then on, getInstance() can be
 * called to a access the methods used to control stores and sources.
 */
public class Controller {

	private static Controller instance = null;

	private static List<ISource> sources = null;
	private static ArrayList<IStore> stores = null;

	private Controller() {
	}

	/**
	 * Initialise the controller using the default settings. This will add a MemoryStore, XMLStore and a TVCOMSource.
	 * Once the store has been initialised, it can't be Initialised again.
	 */
	public static void initWithDefaults() {
		if (stores != null || sources != null) {
			throw new IllegalStateException("Controller allready initialized");
		}

		stores = new ArrayList<IStore>();
		stores.add(new MemoryStore());
		stores.add(new XMLStore());
		sources = new ArrayList<ISource>();
		sources.add(new TVCOMSource());
		sources.add(new IMDBSource());
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

		loadStoresFromConfigFile(config);
		loadSourcesFromConfigFile(config);
	}

	private static void loadSourcesFromConfigFile(ConfigReader config) {
		sources = new ArrayList<ISource>();
		for (SourceConfig sourceConfig : config.getSources()) {
			String sourceClass = sourceConfig.getID();
			try {
				Class<? extends ISource> c = Class.forName(sourceClass).asSubclass(ISource.class);
				ISource source = c.newInstance();
				if (sourceConfig.getParams() != null) {
					for (String key : sourceConfig.getParams().keySet()) {
						String value = sourceConfig.getParams().get(key);
						setParamOnSource(c, source, key, value);
					}
				}
				sources.add(source);
			} catch (ClassNotFoundException e) {
				System.err.println("Unable to add source '" + sourceClass + "' because " + e.getMessage());
			} catch (InstantiationException e) {
				System.err.println("Unable to add source '" + sourceClass + "' because " + e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println("Unable to add source '" + sourceClass + "' because " + e.getMessage());
			} catch (InvocationTargetException e) {
				System.err.println("Unable to add source '" + sourceClass + "' because " + e.getMessage());
			}
		}
	}

	private static void loadStoresFromConfigFile(ConfigReader config) {
		stores = new ArrayList<IStore>();
		for (StoreConfig storeConfig : config.getStores()) {
			String storeClass = storeConfig.getID();
			try {
				Class<? extends IStore> c = Class.forName(storeClass).asSubclass(IStore.class);
				IStore store = c.newInstance();
				if (storeConfig.getParams() != null) {
					for (String key : storeConfig.getParams().keySet()) {
						String value = storeConfig.getParams().get(key);
						setParamOnStore(c, store, key, value);
					}
				}
				stores.add(store);
			} catch (ClassNotFoundException e) {
				System.err.println("Unable to add store '" + storeClass + "' because " + e.getMessage());
			} catch (InstantiationException e) {
				System.err.println("Unable to add store '" + storeClass + "' because " + e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println("Unable to add store '" + storeClass + "' because " + e.getMessage());
			} catch (IllegalArgumentException e) {
				System.err.println("Unable to add store '" + storeClass + "' because " + e.getMessage());
			} catch (InvocationTargetException e) {
				System.err.println("Unable to add store '" + storeClass + "' because " + e.getMessage());
			}
		}
	}

	private static void setParamOnSource(Class<? extends ISource> c, ISource source, String key, String value)
			throws IllegalAccessException, InvocationTargetException {
		for (Method method : c.getMethods()) {
			if (method.getName().toLowerCase().equals("set" + key.toLowerCase())) {
				method.invoke(source, value);
				break;
			}
		}
	}

	private static void setParamOnStore(Class<? extends IStore> c, IStore store, String key, String value)
			throws IllegalAccessException, InvocationTargetException {
		for (Method method : c.getMethods()) {
			if (method.getName().toLowerCase().equals("set" + key.toLowerCase())) {
				method.invoke(store, value);
				break;
			}
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
	 * @param episodeFile The file the episode is stored in
	 * @param sourceId The ID of the source too use
	 * @param showId The id of the show
	 * @param refresh If true, then the stores are ignored.
	 * @return The show, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public Show getShow(File episodeFile, String sourceId, String showId, boolean refresh) throws MalformedURLException,
			SourceException, IOException, StoreException {
		Show show = null;
		if (!refresh) {
			for (IStore store : stores) {
				show = store.getShow(episodeFile, showId);
				if (show != null) {
					break;
				}
			}
		} else {
			show = stores.get(0).getShow(episodeFile, showId);
		}

		if (show == null) {
			System.out.println("Reading show from sources");
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					show = source.getShow(showId);
					if (show != null) {
						for (IStore store : stores) {
							store.cacheShow(episodeFile, show);
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
	 * @param filmFile The file the film is stored in
	 * @param sourceId The ID of the source too use
	 * @param filmId The id of the film
	 * @param refresh If true, then the stores are ignored.
	 * @return The film, or null if it can't be found.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public Film getFilm(File filmFile, String sourceId, String filmId, boolean refresh) throws MalformedURLException,
			SourceException, IOException, StoreException {
		Film film = null;
		if (!refresh) {
			for (IStore store : stores) {
				film = store.getFilm(filmFile, filmId);
				if (film != null) {
					break;
				}
			}
		} else {
			film = stores.get(0).getFilm(filmFile, filmId);
		}

		if (film == null) {
			System.out.println("Reading film from sources");
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					film = source.getFilm(filmId);
					if (film != null) {
						for (IStore store : stores) {
							store.cacheFilm(filmFile, film);
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
	 * @param episodeFile The file the episode is stored in
	 * @param show The show the season belongs too
	 * @param seasonNum The season number
	 * @param refresh If true, then the stores are ignored.
	 * @return The season, or null if it can't be found.
	 * @throws SourceException Thrown if their is a source related problem.
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws StoreException Thrown if their is a store related problem.
	 */
	public Season getSeason(File episodeFile, Show show, int seasonNum, boolean refresh) throws SourceException,
			IOException, StoreException {
		String sourceId = show.getSourceId();
		Season season = null;
		if (!refresh) {
			for (IStore store : stores) {
				season = store.getSeason(episodeFile, show, seasonNum);
				if (season != null) {
					break;
				}
			}
		} else {
			season = stores.get(0).getSeason(episodeFile, show, seasonNum);
		}

		if (season == null) {
			System.out.println("Reading season from sources");
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					season = source.getSeason(show, seasonNum);
					if (season != null) {
						for (IStore store : stores) {
							store.cacheSeason(episodeFile, season);
							if (season.getEpisodes() != null) {
								for (Episode episode : season.getEpisodes()) {
									store.cacheEpisode(episodeFile, episode);
								}
							}
							if (season.getSpecials() != null) {
								for (Episode episode : season.getSpecials()) {
									store.cacheEpisode(episodeFile, episode);
								}
							}
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
	public Episode getEpisode(File episodeFile, Season season, int episodeNum, boolean refresh) throws SourceException,
			MalformedURLException, IOException, StoreException {
		Episode episode = null;
		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getEpisode(episodeFile, season, episodeNum);
				if (episode != null) {
					break;
				}
			}
		} else {
			episode = stores.get(0).getEpisode(episodeFile, season, episodeNum);
		}

		if (episode == null) {
			System.out.println("Reading episode from sources");
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					episode = source.getEpisode(season, episodeNum);
					if (episode != null) {
						for (IStore store : stores) {
							store.cacheEpisode(episodeFile, episode);
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
	public Episode getSpecial(File specialFile, Season season, int specialNum, boolean refresh) throws SourceException,
			MalformedURLException, IOException, StoreException {
		Episode episode = null;

		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getSpecial(specialFile, season, specialNum);
				if (episode != null) {
					break;
				}
			}
		} else {
			episode = stores.get(0).getEpisode(specialFile, season, specialNum);
		}

		if (episode == null) {
			System.out.println("Reading special from sources");
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					episode = source.getSpecial(season, specialNum);
					if (episode != null) {
						for (IStore store : stores) {
							store.cacheEpisode(specialFile, episode);
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
	 * @param mode The mode that the search operation should be performed in
	 * @param mediaFile The file the media is stored in
	 * @return The results of searching for the show, or null if it can't be found.
	 * @throws SourceException Thrown if their is a problem reading from a source
	 * @throws StoreException Thrown if their is a problem reading for a store
	 * @throws IOException Throw if their is a IO problem
	 * @throws MalformedURLException Throw if their is a problem creating a URL
	 */
	public SearchResult searchForVideoId(Mode mode, File mediaFile) throws SourceException, StoreException,
			MalformedURLException, IOException {
		SearchResult result = null;
		for (IStore store : stores) {
			result = store.searchForVideoId(mode, mediaFile);
			if (result != null) {
				return result;
			}
		}

		for (ISource source : sources) {
			result = source.searchForVideoId(mode, mediaFile);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * This is used when a file that holds a episode or film has been renamed
	 * 
	 * @param oldFile The old file
	 * @param newFile The new file
	 * @throws StoreException Thrown if their is a problem renaming files
	 */
	public void renamedFile(File oldFile, File newFile) throws StoreException {
		for (IStore store : stores) {
			store.renamedFile(oldFile, newFile);
		}
	}

	/* package for test */final static void destoryController() {
		instance = null;
		stores = null;
		sources = null;
	}
}

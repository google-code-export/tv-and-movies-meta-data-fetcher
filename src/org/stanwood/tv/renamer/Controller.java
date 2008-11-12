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
package org.stanwood.tv.renamer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.tv.model.Episode;
import org.stanwood.tv.model.Season;
import org.stanwood.tv.model.Show;
import org.stanwood.tv.setup.ConfigReader;
import org.stanwood.tv.source.ISource;
import org.stanwood.tv.source.SourceException;
import org.stanwood.tv.source.TVCOMSource;
import org.stanwood.tv.store.IStore;
import org.stanwood.tv.store.MemoryStore;
import org.stanwood.tv.store.StoreException;
import org.stanwood.tv.store.XMLStore;

/**
 * The controller is used to control access to the stores and and sources. This
 * is a singleton class, and just first be setup using the @see
 * initWithDefaults() or @see initFromConfigFile() methods. From then on,
 * getInstance() can be called to a access the methods used to controll stores
 * and sources.
 */
public class Controller {

	private static Controller instance = null;

	private static List<ISource> sources = null;
	private static ArrayList<IStore> stores = null;

	private Controller() {
	}

	public static void initWithDefaults() {
		if (stores != null || sources != null) {
			throw new IllegalStateException("Controller allready initialized");
		}

		stores = new ArrayList<IStore>();
		stores.add(new MemoryStore());
		stores.add(new XMLStore());
		sources = new ArrayList<ISource>();
		sources.add(new TVCOMSource());
	}
	
	public static void initFromConfigFile(ConfigReader config) {
		if (stores != null || sources != null) {
			throw new IllegalStateException("Controller allready initialized");
		}

		stores = new ArrayList<IStore>();
		for (String storeClass : config.getStores()) {
			try {
				Class<? extends IStore> c = Class.forName(storeClass)
						.asSubclass(IStore.class);
				stores.add(c.newInstance());
			} catch (ClassNotFoundException e) {
				System.err.println("Unable to add store '" + storeClass
						+ "' because " + e.getMessage());
			} catch (InstantiationException e) {
				System.err.println("Unable to add store '" + storeClass
						+ "' because " + e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println("Unable to add store '" + storeClass
						+ "' because " + e.getMessage());
			}
		}
		sources = new ArrayList<ISource>();
		for (String sourceClass : config.getSources()) {
			try {
				Class<? extends ISource> c = Class.forName(sourceClass)
						.asSubclass(ISource.class);
				sources.add(c.newInstance());
			} catch (ClassNotFoundException e) {
				System.err.println("Unable to add source '" + sourceClass
						+ "' because " + e.getMessage());
			} catch (InstantiationException e) {
				System.err.println("Unable to add source '" + sourceClass
						+ "' because " + e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println("Unable to add source '" + sourceClass
						+ "' because " + e.getMessage());
			}
		}
	}

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

	public Show getShow(String sourceId, File showDirectory, long showId,
			boolean refresh) throws MalformedURLException, SourceException,
			IOException, StoreException {
		Show show = null;
		if (!refresh) {
			for (IStore store : stores) {
				show = store.getShow(showDirectory, showId);
				if (show != null) {
					break;
				}
			}
		} else {
			show = stores.get(0).getShow(showDirectory, showId);
		}

		if (show == null) {
			System.out.println("Reading show from sources");
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					show = source.getShow(showDirectory, showId);
					if (show != null) {
						for (IStore store : stores) {
							store.cacheShow(show);
						}
						break;
					}
				}
			}
		}

		return show;
	}

	public Season getSeason(Show show, int seasonNum, boolean refresh)
			throws SourceException, IOException, StoreException {
		String sourceId = show.getSourceId();
		Season season = null;
		if (!refresh) {
			for (IStore store : stores) {
				season = store.getSeason(show, seasonNum);
				if (season != null) {
					break;
				}
			}
		} else {
			season = stores.get(0).getSeason(show, seasonNum);
		}

		if (season == null) {
			System.out.println("Reading season from sources");
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					season = source.getSeason(show, seasonNum);
					if (season != null) {
						for (IStore store : stores) {
							store.cacheSeason(season);
							if (season.getEpisodes() != null) {
								for (Episode episode : season.getEpisodes()) {
									store.cacheEpisode(episode);
								}
							}
							if (season.getSpecials() != null) {
								for (Episode episode : season.getSpecials()) {
									store.cacheEpisode(episode);
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

	public Episode getEpisode(Season season, int episodeNum, boolean refresh)
			throws SourceException, MalformedURLException, IOException,
			StoreException {
		Episode episode = null;
		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getEpisode(season, episodeNum);
				if (episode != null) {
					break;
				}
			}
		} else {
			episode = stores.get(0).getEpisode(season, episodeNum);
		}

		if (episode == null) {
			System.out.println("Reading episode from sources");
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					episode = source.getEpisode(season, episodeNum);
					if (episode != null) {
						for (IStore store : stores) {
							store.cacheEpisode(episode);
						}
						break;
					}
				}
			}
		}

		return episode;
	}

	public Episode getSpecial(Season season, int specialNum, boolean refresh)
			throws SourceException, MalformedURLException, IOException,
			StoreException {
		Episode episode = null;

		if (!refresh) {
			for (IStore store : stores) {
				episode = store.getSpecial(season, specialNum);
				if (episode != null) {
					break;
				}
			}
		} else {
			episode = stores.get(0).getEpisode(season, specialNum);
		}

		if (episode == null) {
			System.out.println("Reading special from sources");
			String sourceId = season.getShow().getSourceId();
			for (ISource source : sources) {
				if (source.getSourceId().equals(sourceId)) {
					episode = source.getSpecial(season, specialNum);
					if (episode != null) {
						for (IStore store : stores) {
							store.cacheEpisode(episode);
						}
						break;
					}
				}
			}
		}

		return episode;
	}

	/**
	 * This will search for a show id in the stores and sources. It will use the
	 * show directory as the name of the show if needed.
	 * 
	 * @param showDirectory
	 *            The directory of the show.
	 * @return The results of searching for the show, or null if it can't be
	 *         found.
	 * @throws SourceException
	 *             Thrown if their is a problem reading from a source
	 * @throws StoreException
	 *             Thrown if their is a problem reading for a store
	 * @throws IOException
	 *             Throw if their is a IO problem
	 * @throws MalformedURLException
	 *             Throw if their is a problem creating a url
	 */
	public SearchResult searchForShowId(File showDirectory)
			throws SourceException, StoreException, MalformedURLException,
			IOException {
		SearchResult result = null;
		for (IStore store : stores) {
			result = store.searchForShowId(showDirectory);
			if (result != null) {
				return result;
			}
		}

		for (ISource source : sources) {
			result = source.searchForShowId(showDirectory);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	
}

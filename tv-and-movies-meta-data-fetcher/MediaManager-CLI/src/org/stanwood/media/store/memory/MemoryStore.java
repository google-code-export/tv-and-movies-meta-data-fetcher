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
package org.stanwood.media.store.memory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.IVideoFile;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Show;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

/**
 * This store is used to store the show information in memory. This allows the tool
 * reuse the show information without having to fetch it from other stores or sources
 * (which would be slower). This information will be lost once the application exits.
 */
public class MemoryStore implements IStore {

	private List<CacheShow> shows = new ArrayList<CacheShow>();
	private Map<File,IFilm> films = new HashMap<File,IFilm>();

	/**
	 * This does nothing as it's all done by the cacheSeason and cacheShow methods
	 * @param episodeFile the file witch the episode is stored in
	 * @param episode The episode to write to the store
	 */
	@Override
	public void cacheEpisode(File rootMediaDir,File episodeFile,IEpisode episode) {

	}

	/**
	 * This is used to write a season too the store.
	 * @param season The season too write
	 * @param episodeFile The file the episode is stored in
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	@Override
	public void cacheSeason(File rootMediaDir,File episodeFile,ISeason season) throws StoreException {
		IShow show = season.getShow();
		for (CacheShow cs : shows) {
			if (cs.equals(show)) {
				if (cs.getSeason(season.getSeasonNumber())!=null) {
					cs.removeSeason(season.getSeasonNumber());
				}
				cs.addSeason(new CacheSeason(cs,season));
			}
		}
	}

	/**
	 * This is used to write a show too the store.
	 * @param episodeFile The file the episode is stored in
	 * @param show The show too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	@Override
	public void cacheShow(File rootMediaDir,File episodeFile,IShow show) throws StoreException {
		Iterator<CacheShow> it = shows.iterator();
		while (it.hasNext()) {
			CacheShow foundShow = it.next();
			if (foundShow.getShowId().equals(show.getShowId()) && foundShow.getSourceId().equals(show.getSourceId())) {
				it.remove();
			}
		}
		shows.add(new CacheShow(show));
	}

	/**
	 * This gets a episode from the store. If it can't be found, then it will
	 * return null;
	 * @param episodeFile The file the episode is stored in
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return The episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public IEpisode getEpisode(File rootMediaDir,File episodeFile,ISeason season, int episodeNum)
			throws StoreException, MalformedURLException, IOException {
		if (!(season instanceof CacheSeason)) {
			season = this.getSeason(rootMediaDir, episodeFile, season.getShow(), season.getSeasonNumber());
		}

		if (season instanceof CacheSeason) {
			return ((CacheSeason)season).getEpisode(episodeNum);
		}
		return null;
	}

	/**
	 * This gets a special episode from the store. If it can't be found, then it will
	 * return null;
	 * @param episodeFile The file the episode is stored in
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public IEpisode getSpecial(File rootMediaDir,File episodeFile,ISeason season, int specialNumber)
			throws MalformedURLException, IOException, StoreException {
		if (!(season instanceof CacheSeason)) {
			season = this.getSeason(rootMediaDir, episodeFile, season.getShow(), season.getSeasonNumber());
		}

		if (season instanceof CacheSeason) {
			return ((CacheSeason)season).getSpecial(specialNumber);
		}
		return null;
	}

	/**
	 * This will get the season from the store
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	@Override
	public ISeason getSeason(File rootMediaDir,File episodeFile,IShow show, int seasonNum) throws StoreException,
			IOException {
		if (show instanceof CacheShow) {
			((CacheShow)show).getSeason(seasonNum);
		}
		else {
			for (CacheShow cs : shows) {
				if (cs.getShowId().equals(show.getShowId()) && cs.getSourceId().equals(show.getSourceId())) {
					return cs.getSeason(seasonNum);
				}
			}
		}
		return null;
	}

	/**
	 * This will get a show from the store. If the season can't be found, then it
	 * will return null.
	 * @param episodeFile The file the episode is stored in
	 * @param showId The id of the show to get.
	 * @return The show if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Show getShow(File rootMediaDir,File episodeFile, String showId)
			throws StoreException, MalformedURLException, IOException {
		for (CacheShow show : shows) {
			if (show.getShowId().equals(showId)) {
				return show;
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		if (mode==Mode.TV_SHOW) {
			for (CacheShow show : shows) {
				if (show.getName().equals(name)) {
					//TODO look for the media file and work out the part
					return new SearchResult(show.getShowId(), show.getSourceId(), show.getShowURL().toExternalForm(), null,mode);
				}
			}
		}
		else if (mode==Mode.FILM) {
			IFilm f = films.get(mediaFile);
			if (f!=null) {
				for (IVideoFile vf : f.getFiles()) {
					if (vf.getLocation().equals(mediaFile)) {
						return new SearchResult(f.getTitle(), f.getSourceId(), f.getFilmUrl().toExternalForm(), vf.getPart(),mode);
					}
				}
				return new SearchResult(f.getTitle(), f.getSourceId(), f.getFilmUrl().toExternalForm(), null,mode);
			}
		}
		return null;
	}

	/**
	 * This is used to write a film to the store.
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File rootMediaDir,File filmFile, IFilm film,Integer part) throws StoreException {
		films.put(filmFile,film);
	}

	/**
	 * This will update all references of the old file to the new file
	 * @param oldFile The old file
	 * @param newFile The new file
	 */
	@Override
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) {
		IFilm film = films.get(oldFile);
		if (film!=null) {
			films.remove(oldFile);
			films.put(newFile,film);
		}
	}

	/**
	 * Used to read a film from the store.
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 */
	@Override
	public IFilm getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return films.get(filmFile);
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) {
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void performedActions(MediaDirectory dir) {
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) {
		films.remove(file);

		for (CacheShow show : shows) {
			for (CacheSeason season : show.getSeasons()) {
				for (IEpisode episode : season.getEpisodes()) {
					Iterator<IVideoFile> it  = episode.getFiles().iterator();
					while (it.hasNext()) {
						IVideoFile f = it.next();
						if (f.getLocation().equals(file)) {
							it.remove();
						}
					}
				}
				for (IEpisode special : season.getSpecials()) {
					Iterator<IVideoFile> it  = special.getFiles().iterator();
					while (it.hasNext()) {
						IVideoFile f = it.next();
						if (f.getLocation().equals(file)) {
							it.remove();
						}
					}
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		for (CacheShow show : shows) {
			for (CacheSeason season : show.getSeasons()) {
				for (IEpisode episode : season.getEpisodes()) {
					Iterator<IVideoFile> it  = episode.getFiles().iterator();
					while (it.hasNext()) {
						IVideoFile f = it.next();
						if (f.getLocation().equals(file)) {
							return episode;
						}
					}
				}
				for (IEpisode special : season.getSpecials()) {
					Iterator<IVideoFile> it  = special.getFiles().iterator();
					while (it.hasNext()) {
						IVideoFile f = it.next();
						if (f.getLocation().equals(file)) {
							return special;
						}
					}
				}
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(MediaDirectory dir, File file) throws StoreException {

		return films.get(file);
	}

	/** {@inheritDoc} */
	@Override
	public void init(File nativeDir) throws StoreException {
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IEpisode> listEpisodes(MediaDirConfig dirConfig,IProgressMonitor monitor) {
		ArrayList<IEpisode>episodes = new ArrayList<IEpisode>();
		for (CacheShow show : shows) {
			for (CacheSeason season : show.getSeasons()) {
				for (IEpisode episode : season.getEpisodes()) {
					episodes.add(episode);
				}
			}
		}
		return episodes;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IFilm> listFilms(MediaDirConfig dirConfig,IProgressMonitor monitor) {
		return films.values();
	}

	/** {@inheritDoc} */
	@Override
	public void upgrade(MediaDirectory mediaDirectory) {

	}

	/** {@inheritDoc} */
	@Override
	public void fileUpdated(MediaDirectory mediaDirectory, File file)
			throws StoreException {

	}
}

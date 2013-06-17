/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.store;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.memory.MemoryStore;

/**
 * This store is used to log access to a wrapped store. It's meant for testing store useage
 */
@SuppressWarnings("nls")
public class LoggingStore implements IStore {

//	private static List<String>events = new ArrayList<String>();
	private static Queue<String> events= new LinkedList<String>();

	private IStore store = new MemoryStore();

	/** {@inheritDoc} */
	@Override
	public void cacheEpisode(File rootMediaDir, File episodeFile,File oldFile,IEpisode episode) throws StoreException {
		events.add("cacheEpisode("+rootMediaDir.getAbsolutePath()+","+episodeFile.getAbsolutePath()+")");
		if (store!=null) {
			store.cacheEpisode(rootMediaDir, episodeFile,oldFile, episode);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void cacheSeason(File rootMediaDir, File episodeFile, ISeason season) throws StoreException {
		events.add("cacheSeason()");
		if (store!=null) {
			store.cacheSeason(rootMediaDir, episodeFile, season);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void cacheShow(File rootMediaDir, File episodeFile, IShow show)
			throws StoreException {
		events.add("cacheShow()");
		if (store!=null) {
			store.cacheShow(rootMediaDir, episodeFile, show);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void cacheFilm(File rootMediaDir, File filmFile,File oldFile, IFilm film, Integer part) throws StoreException {
		events.add("cacheFilm("+rootMediaDir.getAbsolutePath()+","+filmFile.getAbsolutePath()+")");
		if (store!=null) {
			store.cacheFilm(rootMediaDir, filmFile,oldFile, film, part);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ISeason getSeason(File rootMediaDir, File episodeFile, IShow show,
			int seasonNum) throws StoreException, IOException {
		events.add("getSeason()");
		if (store!=null) {
			return store.getSeason(rootMediaDir, episodeFile, show, seasonNum);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IShow getShow(File rootMediaDir, File episodeFile, String showId)
			throws StoreException, MalformedURLException, IOException {
		events.add("getShow()");
		if (store!=null) {
			return store.getShow(rootMediaDir, episodeFile, showId);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,
			MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		SearchResult result = null;
		if (store!=null) {
			result = store.searchMedia(name, mode, part, dirConfig, mediaFile);
		}
		events.add("searchMedia("+name+","+mode+","+part+","+dirConfig.getMediaDir()+","+mediaFile.getAbsolutePath()+") -> " + result);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void renamedFile(File rootMediaDir, File oldFile, File newFile)
			throws StoreException {
		events.add("renamedFile("+oldFile.getAbsolutePath()+","+newFile.getAbsolutePath()+")");
		if (store!=null) {
			store.renamedFile(rootMediaDir, oldFile, newFile);
		}
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(File rootMediaDir, File filmFile, String filmId)
			throws StoreException, MalformedURLException, IOException {
		events.add("getFilm()");
		if (store!=null) {
			return store.getFilm(rootMediaDir,filmFile,filmId);
		}
		return null;
	}

	/**
	 * Used to get the logged events
	 * @return the logged events
	 */
	public static Queue<String>getEvents() {
		return events;
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) throws StoreException {
		events.add("setParameter()");
		if (store!=null) {
			store.setParameter(key, value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		events.add("getParameter()");
		if (store!=null) {
			return store.getParameter(key);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void performedActions(MediaDirectory dir) throws StoreException {
		events.add("performedActions("+dir.getMediaDirConfig().getMediaDir()+")");
		if (store!=null) {
			store.performedActions(dir);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) throws StoreException {
		events.add("fileDeleted()");
		if (store!=null) {
			store.fileDeleted(dir, file);
		}
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		if (store!=null) {
			return store.getEpisode(dir, file);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(MediaDirectory dir, File file) throws StoreException {
		events.add("getFilm("+dir.getMediaDirConfig().getMediaDir()+","+file.getAbsolutePath()+")");
		if (store!=null) {
			return store.getFilm(dir, file);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws StoreException {
		events.add("init()");
		if (store!=null) {
			store.init();
		}
	}

	/** {@inheritDoc}*/
	@Override
	public List<IEpisode> listEpisodes(MediaDirConfig dirConfig,IProgressMonitor monitor) throws StoreException {
		events.add("listEpisodes()");
		if (store!=null) {
			return (List<IEpisode>) store.listEpisodes(dirConfig, monitor);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public List<IFilm> listFilms(MediaDirConfig dirConfig,IProgressMonitor monitor) throws StoreException {
		events.add("listFilms()");
		if (store!=null) {
			return (List<IFilm>) store.listFilms(dirConfig, monitor);
		}
		return null;
	}

	/** {@inheritDoc}  */
	@Override
	public void upgrade(MediaDirectory mediaDirectory) throws StoreException {
		events.add("upgrade()");
		if (store!=null) {
			store.upgrade(mediaDirectory);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fileUpdated(MediaDirectory mediaDirectory, File file)
			throws StoreException {
		events.add("fileUpdated()");
		if (store!=null) {
			store.fileUpdated(mediaDirectory,file);
		}
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(File rootMediaDir, File episodeFile,
			ISeason season, List<Integer> episodeNums) throws StoreException,
			MalformedURLException, IOException {
		events.add("getEpisode()");
		if (store!=null) {
			return store.getEpisode(rootMediaDir,episodeFile,season,episodeNums);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getSpecial(File rootMediaDir, File episodeFile,
			ISeason season, List<Integer> specialNumbers)
			throws MalformedURLException, IOException, StoreException {
		events.add("getSpecial()");
		if (store!=null) {
			return store.getSpecial(rootMediaDir, episodeFile, season, specialNumbers);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean fileKnownByStore(MediaDirectory mediaDirectory, File file)
			throws StoreException {
		events.add("fileKnownByStore()");
		if (store!=null) {
			return store.fileKnownByStore(mediaDirectory, file);
		}
		return false;
	}

	/**
	 * Used to print all the events left in the event queue. Event queue will be empty after calling
	 * this.
	 */
	public static void printEvents() {
		String item = null;
		try {
			item = events.remove();
		}
		catch (NoSuchElementException e) {
			item = null;
		}
		while (item !=null) {
			System.out.println("Event: " + item);
			try {
				item = events.remove();
			}
			catch (NoSuchElementException e) {
				item = null;
			}
		}
	}

	/**
	 * Used to clear the events
	 */
	public static void clearEvents() {
		String item = null;
		try {
			item = events.remove();
		}
		catch (NoSuchElementException e) {
			item = null;
		}
		while (item !=null) {
			try {
				item = events.remove();
			}
			catch (NoSuchElementException e) {
				item = null;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void aboutToRenamedFile(File rootMediaDir, File oldFile, File newFile)
			throws StoreException {
		events.add("aboutToRenamedFile()");
	}

}
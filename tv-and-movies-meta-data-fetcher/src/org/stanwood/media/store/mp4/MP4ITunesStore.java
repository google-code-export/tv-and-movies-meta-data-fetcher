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
package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

/**
 * <p>
 * This store is used to store and retrieve film/TV show information from .mp4 files used
 * by iTunes. This allows iTuness to use the meta data retrieved by this tool and this tool
 * too use the meta data of iTunes.
 * </p>
 * <p>
 * Reading and writing too the .mp4 files is done via the application AtomicParsley
 * {@link "http://atomicparsley.sourceforge.net/"}. In order for this store to work, the AtomicParsley
 * command line tool must be installed. Use the method <code>setAtomicParsleyPath</code> too set the
 * of the application.
 * </p>
 */
public class MP4ITunesStore implements IStore {

	private File atomicParsleyPath;

	/**
	 * This is used to store episode information of a TVShow MP4 file into the
	 * file as meta data so that iTunes can read it.
	 * @param episodeFile The mp4 episode file
	 * @param episode The episode details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheEpisode(File rootMediaDir,File episodeFile,Episode episode) throws StoreException {
		String name = episodeFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {
			validate();
			writeEpisode(episodeFile,episode);
		}
	}

	private void writeEpisode(File file, Episode episode) throws StoreException {
		AtomicParsley ap = new AtomicParsley(atomicParsleyPath);
		try {
			ap.updateEpsiode(file,episode);
		} catch (AtomicParsleyException e) {
			throw new StoreException(e.getMessage(),e);
		}
	}

	/**
	 * This does nothing as the season information can't be stored by this store
	 * @param episodeFile The mp4 episode file
	 * @param season The season details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheSeason(File rootMediaDir,File episodeFile,Season season) throws StoreException {
		validate();
	}

	/**
	 * This does nothing as the show information can't be stored by this store
	 * @param episodeFile The mp4 episode file
	 * @param show The show details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheShow(File rootMediaDir,File episodeFile,Show show) throws StoreException {
		validate();
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the episode is stored in
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Episode getEpisode(File rootMediaDir,File episodeFile,Season season, int episodeNum) throws StoreException {
		validate();
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the episode is stored in
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Season getSeason(File rootMediaDir,File episodeFile,Show show, int seasonNum) throws StoreException {
		validate();
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the episode is stored in
	 * @param showId The show Id of the show too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Show getShow(File rootMediaDir,File episodeFile, String showId) throws StoreException {
		validate();
		return null;
	}

	/**
	 * This does nothing because this source does not support searching for show ID's.
	 * @param episodeFile The file the episode is stored in
	 * @param mode The mode that the search operation should be performed in
	 * @param renamePattern The pattern been used in rename operations, or null if not renaming
	 * @return Will always return null.
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	@Override
	public SearchResult searchForVideoId(File rootMediaDir,Mode mode,File episodeFile,String renamePattern)
			throws StoreException {
		return null;
	}

	/**
	 * Used to set the store parameter used to find the atomic parsley application
	 * @param atomicParsleyPath The path to the atomic parsley path
	 */
	public void setAtomicParsleyPath(String atomicParsleyPath) {
		this.atomicParsleyPath = new File(atomicParsleyPath);
	}

	/**
	 * Used to get the store parameter used to find the atomic parsley application
	 * @return The path to the atomic parsley path
	 */
	public String getAtomicParsleyPath() {
		return atomicParsleyPath.getAbsolutePath();
	}

	private void validate() throws StoreException {
		if (atomicParsleyPath==null) {
			throw new StoreException("The atomicParsleyPath property must be set before the store can be used");
		}
		if (!atomicParsleyPath.exists()) {
			throw new StoreException("Unable to locate the AtomicParsley application at the path '"+atomicParsleyPath.getAbsolutePath()+"'.");
		}
	}

	/**
	 * This is used to write a film to the store.
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File rootMediaDir,File filmFile, Film film) throws StoreException {
		String name = filmFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {
			validate();
			writeFilm(filmFile,film);
		}
	}

	private void writeFilm(File filmFile, Film film) throws StoreException {
		AtomicParsley ap = new AtomicParsley(atomicParsleyPath);
		try {
			ap.updateFilm(filmFile,film);
		} catch (AtomicParsleyException e) {
			throw new StoreException(e.getMessage(),e);
		}
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the special episode is stored in
	 * @param season The season the episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Episode getSpecial(File rootMediaDir,File episodeFile, Season season, int specialNumber) throws MalformedURLException,
			IOException, StoreException {
		return null;
	}

	/**
	 * This does nothing as the meta data is stored in the actual file
	 * @param oldFile The old file
	 * @param newFile The new file
	 */
	@Override
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) {

	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 */
	@Override
	public Film getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return null;
	}
}

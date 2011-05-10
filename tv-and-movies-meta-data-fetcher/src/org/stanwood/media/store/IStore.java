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
package org.stanwood.media.store;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.MediaDirConfig;

/**
 * Stores are similar too sources, except that they are also writable. Once
 * information has been retrieved from a source, it is written too a store.
 * Next time the information is needed, it can be retrieved from the store.
 * This makes retrieving information a lot faster.
 */
public interface IStore  {

	/**
	 * This is used to write a episode or special too the store
	 * @param episode The episode or special too write
	 * @param episodeFile the file witch the episode is stored in
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public void cacheEpisode(File rootMediaDir,File episodeFile,Episode episode) throws StoreException;

	/**
	 * This is used to write a season too the store.
	 * @param season The season too write
	 * @param episodeFile The file the episode is stored in
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public void cacheSeason(File rootMediaDir,File episodeFile,Season season) throws StoreException;

	/**
	 * This is used to write a show too the store.
	 * @param show The show too write
	 * @param episodeFile The file the episode is stored in
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public void cacheShow(File rootMediaDir,File episodeFile,Show show) throws StoreException;

	/**
	 * This is used to write a film to the store.
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @param part The part number of the film
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public void cacheFilm(File rootMediaDir,File filmFile,Film film,Integer part) throws StoreException;

	/**
	 * This gets a episode from the store. If it can't be found, then it will
	 * return null;
	 * @param episodeFile the file which the episode is stored in
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Episode getEpisode(File rootMediaDir,File episodeFile,Season season, int episodeNum) throws StoreException, MalformedURLException, IOException;

	/**
	 * This will get a season from the store. If the season can't be found,
	 * then it will return null.
	 * @param episodeFile the file which the episode is stored in
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The season if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Season getSeason(File rootMediaDir,File episodeFile,Show show, int seasonNum) throws StoreException, IOException;

	/**
	 * This will get a show from the store. If the season can't be found, then it
	 * will return null.
	 * @param episodeFile the file which the episode is stored in
	 * @param showId The id of the show to get.
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The show if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Show getShow(File rootMediaDir,File episodeFile, String showId) throws StoreException, MalformedURLException, IOException;

	/**
	 * This gets a special episode from the store. If it can't be found, then it will
	 * return null;
	 * @param episodeFile the file which the episode is stored in
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The special episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Episode getSpecial(File rootMediaDir,File episodeFile,Season season, int specialNumber) throws MalformedURLException, IOException, StoreException;


	public SearchResult searchMedia(String name, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile) throws StoreException;

	/**
	 * This is used when a file that holds a episode or film has been renamed
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @param oldFile The old file
	 * @param newFile The new file
	 * @throws StoreException Thrown if their is a problem renaming files
	 */
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) throws StoreException;

	/**
	 * This will get a film from the store. If the film can't be found, then it will return null.
	 * @param filmFile The file the film is located in.
	 * @param filmId The id of the film
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The film, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Film getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException;

	public void setParameter(String key, String value);

	public String getParameter(String key);

	public void performedActions(MediaDirectory dir) throws StoreException;

	public void fileDeleted(MediaDirectory dir, File file) throws StoreException;

}

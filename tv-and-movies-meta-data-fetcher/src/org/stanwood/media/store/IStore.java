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

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;

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
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	public void cacheEpisode(Episode episode) throws StoreException;

	/**
	 * This is used to write a season too the store.
	 * @param season The season too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	public void cacheSeason(Season season) throws StoreException;
	
	/**
	 * This is used to write a show too the store.
	 * @param show The show too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	public void cacheShow(Show show) throws StoreException;
	
	/**
	 * This gets a episode from the store. If it can't be found, then it will
	 * return null;
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return The episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Episode getEpisode(Season season, int episodeNum) throws StoreException, MalformedURLException, IOException;
	
	/**
	 * This will get a season from the store. If the season can't be found,
	 * then it will return null.
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Season getSeason(Show show, int seasonNum) throws StoreException, IOException;
	
	/**
	 * This will get a show from the store. If the season can't be found, then it 
	 * will return null. 
	 * @param showDirectory The directory the show's media files are located in.
	 * @param showId The id of the show to get.
	 * @return The show if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Show getShow(File showDirectory, long showId) throws StoreException, MalformedURLException, IOException;
	
	/**
	 * This gets a special episode from the store. If it can't be found, then it will
	 * return null;
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Episode getSpecial(Season season, int specialNumber) throws MalformedURLException, IOException, StoreException;

	/**
	 * This is called to search the store for a show id. If it can't be found, then
	 * it will return null.
	 * @param showDirectory The directory the show is located in.
	 * @return The results of the search if it was found, otherwise null
	 * @throws StoreException Thrown if their is a problem with the store 
	 */
	public SearchResult searchForShowId(File showDirectory) throws StoreException;

}

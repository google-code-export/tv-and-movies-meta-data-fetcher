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
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.store.xmlstore.FilmXMLStore;
import org.stanwood.media.store.xmlstore.TVXMLStore;

/**
 * This store is used to store the show and film information in a XML called .show.xml or .films.xml. 
 * This is located in the directory were the show or film is located. For more information see
 * {@link org.stanwood.media.store.xmlstore.TVXMLStore} and {@link org.stanwood.media.store.xmlstore.FilmXMLStore}.
 */
public class XMLStore implements IStore {

	private TVXMLStore tvStore;
	private FilmXMLStore filmStore;

	/**
	 * Used to created a instance of the XMLStore class
	 */
	public XMLStore() {
		tvStore = new TVXMLStore();
		filmStore = new FilmXMLStore();
	}

	
	/**
	 * This will update all references of the old file to the new file
	 * @param oldFile The old file
	 * @param newFile The new file
	 * @throws StoreException Thrown if their is a problem renaming files 
	 */
	@Override
	public void renamedFile(File oldFile, File newFile) throws StoreException {
		filmStore.renamedFile(oldFile,newFile);
	}
	
	/**
	 * This is used to write a film to the store.
	 * 
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File filmFile, Film film) throws StoreException {
		filmStore.cacheFilm(filmFile, film);
	}
	
	/**
	 * Used to get the details of a film with the given id. If it can't be found,
	 * then null is returned.
	 * 
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 * @return The film details, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem. 
	 */
	@Override
	public Film getFilm(File filmFile, long filmId) throws StoreException, MalformedURLException, IOException {
		return filmStore.getFilm(filmFile, filmId);
	}

	/**
	 * This is used to write a episode or special too the store
	 * @param episodeFile The file that contains the episode
	 * @param episode The episode or special too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */	
	@Override
	public void cacheEpisode(File episodeFile, Episode episode) throws StoreException {
		tvStore.cacheEpisode(episodeFile, episode);
	}
	
	/**
	 * This is used to write a season too the store.
	 * @param episodeFile The file that contains the episode
	 * @param season The season too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */	
	@Override
	public void cacheSeason(File episodeFile, Season season) throws StoreException {
		tvStore.cacheSeason(episodeFile, season);
	}

	/**
	 * This is used to write a show too the store.
	 * @param episodeFile The file that contains the episode
	 * @param show The show too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */	
	@Override
	public void cacheShow(File episodeFile, Show show) throws StoreException {
		tvStore.cacheShow(episodeFile, show);
	}

	/**
	 * This gets a episode from the store. If it can't be found, then it will
	 * return null;
	 * @param episodeFile The file that contains the episode file
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return The episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 */	
	@Override
	public Episode getEpisode(File episodeFile, Season season, int episodeNum) throws StoreException,
			MalformedURLException, IOException {
		return tvStore.getEpisode(episodeFile, season, episodeNum);
	}
	
	/**
	 * This will get a season from the store. If the season can't be found,
	 * then it will return null.
	 * @param episodeFile The file that contains the episode file
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 */	
	@Override
	public Season getSeason(File episodeFile, Show show, int seasonNum) throws StoreException, IOException {	
		return tvStore.getSeason(episodeFile, show, seasonNum);
	}

	/**
	 * This will get a show from the store. If the season can't be found, then it 
	 * will return null. 
	 * @param episodeFile The file the episode is located in
	 * @param showId The id of the show to get.
	 * @return The show if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Show getShow(File episodeFile, long showId) throws StoreException, MalformedURLException, IOException {	
		return tvStore.getShow(episodeFile, showId);
	}

	/**
	 * This gets a special episode from the store. If it can't be found, then it will
	 * return null;
	 * @param specialFile The file that contains the special episode file
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */	
	@Override
	public Episode getSpecial(File specialFile, Season season, int specialNumber) throws MalformedURLException,
			IOException, StoreException {
		return tvStore.getSpecial(specialFile, season, specialNumber);
	}

	/**
	 * This is called to search the store for a show id or film Id, depending on the mode. 
	 * If it can't be found, then it will return null. The search is done be reading the 
	 * .show.xml file within the shows directory  or the .films.xml file. 
	 * @param episodeFile The file the episode is stored in
	 * @param mode The mode that the search operation should be performed in 
	 * @return The results of the search if it was found, otherwise null
	 * @throws StoreException Thrown if their is a problem with the store 
	 */	
	@Override
	public SearchResult searchForVideoId(Mode mode,File episodeFile) throws StoreException {
		if (mode==Mode.TV_SHOW) {
			return tvStore.searchForShowId(episodeFile);
		}
		else {
			return filmStore.searchForFilmId(episodeFile);
		}
	}
}

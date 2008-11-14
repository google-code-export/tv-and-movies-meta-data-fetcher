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
package org.stanwood.media.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;

/**
 * This interfaces should be implemented by classes used to retrive information from a source.
 */
public interface ISource {

	/**
	 * Called to retrieve the information on a episode
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode
	 * @return The episode
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Throw if their is a IO related problem
	 */
	public Episode getEpisode(Season season, int episodeNum) throws SourceException, MalformedURLException, IOException;
	
	/**
	 * This will get a season from the source. If the season can't be found,
	 * then it will return null.
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Season getSeason(Show show, int seasonNum) throws SourceException, IOException;
	
	/**
	 * This will get a show from the source. If the season can't be found, then it 
	 * will return null. 
	 * @param showDirectory The directory the show's media files are located in.
	 * @param showId The id of the show to get.
	 * @return The show if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Show getShow(File showDirectory, long showId) throws SourceException, MalformedURLException, IOException;
	
	/**
	 * This gets a special episode from the source. If it can't be found, then it will
	 * return null;
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Episode getSpecial(Season season, int specialNumber) throws SourceException,MalformedURLException, IOException;

	/**
	 * Get the id of the source
	 * @return The id of the source
	 */
	public String getSourceId();

	/**
	 * Used to search for a show within the source
	 * @param showDirectory The directory the shows media files are located in
	 * @return The results of the search, or null if nothing was found
	 * @throws SourceException Thrown if their is a problem retrieving the data 
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public SearchResult searchForShowId(File showDirectory) throws SourceException, MalformedURLException, IOException;
	
}

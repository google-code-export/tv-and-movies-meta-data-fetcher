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
import java.net.URL;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.extensions.IExtension;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;

/**
 * This interfaces should be implemented by classes used to retrive information from a source.
 */
public interface ISource extends IExtension {

	/**
	 * Called to retrieve the information on a episode
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode
	 * @param file The special file if looking up a files details, or NULL
	 * @return The episode
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Throw if their is a IO related problem
	 */
	public IEpisode getEpisode(ISeason season, int episodeNum,File file) throws SourceException, MalformedURLException, IOException;

	/**
	 * This will get a season from the source. If the season can't be found,
	 * then it will return null.
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public ISeason getSeason(IShow show, int seasonNum) throws SourceException, IOException;

	/**
	 * This will get a show from the source. If the show can't be found, then it
	 * will return null.
	 * @param showId The id of the show to get.
	 * @param url String url of the show
	 * @param file The media file if looking up a files details, or NULL
	 * @return The show if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public IShow getShow(String showId,URL url,File file) throws SourceException, MalformedURLException, IOException;

	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 * @param filmId The id of the film
	 * @param url The URL used to lookup the film
	 * @param filmFile The film file if looking up a files details, or NULL
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public IFilm getFilm(String filmId,URL url,File filmFile) throws SourceException, MalformedURLException, IOException;

	/**
	 * This gets a special episode from the source. If it can't be found, then it will
	 * return null;
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @param file The special file if looking up a files details, or NULL
	 * @return The special episode, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public IEpisode getSpecial(ISeason season, int specialNumber,File file) throws SourceException,MalformedURLException, IOException;

	/**
	 * Get the id of the source
	 * @return The id of the source
	 */
	@Deprecated // To be removed
	public String getSourceId();

	/**
	 * <p>Used to set source parameters. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	public void setParameter(String key,String value) throws SourceException;

	/**
	 * <p>Used to get the value of a source parameter. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * @param key The key of the parameter
	 * @return The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	public String getParameter(String key) throws SourceException;


	/**
	 * Called to tell the source which media directory is been processed
	 * @param dir The media directory
	 * @throws SourceException Thrown if their is a problem with the source
	 */
	public void setMediaDirConfig(MediaDirectory dir) throws SourceException;

	/**
	 * This is called to search the source for media details
 	 * @param name The search term
 	 * @param year The year to search for or empty string if not needed
	 * @param mode The mode of the search
	 * @param part The part number to search for
	 * @return The search result or NULL if it could not be found
	 * @throws SourceException Thrown if their is a problem with the source
	 */
	public SearchResult searchMedia(String name,String year, Mode mode, Integer part) throws SourceException;

}

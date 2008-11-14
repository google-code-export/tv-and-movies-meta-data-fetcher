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
	
	public Season getSeason(Show show, int seasonNum) throws SourceException, IOException;
	
	public Show getShow(File showDirectory, long showId) throws SourceException, MalformedURLException, IOException;
	
	public Episode getSpecial(Season season, int specialNumber) throws SourceException,MalformedURLException, IOException;

	public String getSourceId();

	public SearchResult searchForShowId(File showDirectory) throws SourceException, MalformedURLException, IOException;
	
}

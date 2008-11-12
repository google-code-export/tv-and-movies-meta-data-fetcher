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
package org.stanwood.tv.store;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.tv.model.Episode;
import org.stanwood.tv.model.Season;
import org.stanwood.tv.model.Show;
import org.stanwood.tv.renamer.SearchResult;
import org.stanwood.tv.source.SourceException;

public interface IStore  {

	public void cacheEpisode(Episode episode) throws SourceException;

	public void cacheSeason(Season season) throws SourceException;
	
	public void cacheShow(Show show) throws SourceException;
	
	public Episode getEpisode(Season season, int episodeNum) throws SourceException, MalformedURLException, IOException;
	
	public Season getSeason(Show show, int seasonNum) throws SourceException, IOException;
	
	public Show getShow(File showDirectory, long showId) throws SourceException, MalformedURLException, IOException;
	
	public Episode getSpecial(Season season, int specialNumber) throws MalformedURLException, IOException, SourceException;

	public SearchResult searchForShowId(File showDirectory) throws SourceException;

}

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.stanwood.tv.model.Episode;
import org.stanwood.tv.model.Season;
import org.stanwood.tv.model.Show;
import org.stanwood.tv.renamer.SearchResult;

/**
 * This store is used to store the show information in memory. This allows the tool
 * reuse the show information without having to fetch it from other stores or sources 
 * (which would be slower). This information will be lost once the application exits. 
 */
public class MemoryStore implements IStore {

	private List<Show> shows = new ArrayList<Show>();
	
	@Override
	public void cacheEpisode(Episode episode) throws StoreException {
		
	}

	@Override
	public void cacheSeason(Season season) throws StoreException {
		Show show = season.getShow();
		if (show.getSeason(season.getSeasonNumber())!=null) {
			show.removeSeason(season.getSeasonNumber());
		}
		show.addSeason(season);
	}

	@Override
	public void cacheShow(Show show) throws StoreException {
		Iterator<Show> it = shows.iterator();
		while (it.hasNext()) {
			Show foundShow = it.next();
			if (foundShow.getShowId() == show.getShowId()) {
				it.remove();
			}
		}
		shows.add(show);
	}

	@Override
	public Episode getEpisode(Season season, int episodeNum)
			throws StoreException, MalformedURLException, IOException {
		return season.getEpisode(episodeNum);		
	}
	
	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws MalformedURLException, IOException, StoreException {	
		return season.getSpecial(specialNumber);	
	}

	/**
	 * This will get the season from the store
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	@Override
	public Season getSeason(Show show, int seasonNum) throws StoreException,
			IOException {		
		return show.getSeason(seasonNum);
	}

	@Override
	public Show getShow(File showDirectory, long showId)
			throws StoreException, MalformedURLException, IOException {		
		for (Show show : shows) {
			if (show.getShowId() == showId) {
				return show;
			}
		}
		return null;
	}
	
	/**
	 * This does nothing because this source does not support searching for show ID's.
	 * @param showDirectory The directory the show is located in
	 * @return Will always return null.
	 * @throws StoreException Thrown if their is a problem with the source
	 */
	@Override
	public SearchResult searchForShowId(File showDirectory)
			throws StoreException {		
		return null;
	}

	
}

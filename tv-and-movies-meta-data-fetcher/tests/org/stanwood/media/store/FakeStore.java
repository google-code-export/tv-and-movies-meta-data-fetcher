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

public class FakeStore implements IStore {

	public static String testParam1;
	
	@Override
	public void cacheEpisode(Episode episode) throws StoreException {
	}

	@Override
	public void cacheSeason(Season season) throws StoreException {
	}

	@Override
	public void cacheShow(Show show) throws StoreException {
	}

	@Override
	public Episode getEpisode(Season season, int episodeNum) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public Season getSeason(Show show, int seasonNum) throws StoreException, IOException {
		return null;
	}

	@Override
	public Show getShow(File showDirectory, long showId) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public Episode getSpecial(Season season, int specialNumber) throws MalformedURLException, IOException,
			StoreException {
		return null;
	}

	@Override
	public SearchResult searchForShowId(File showDirectory) throws StoreException {
		return null;
	}

	public String getTestParam1() {
		return FakeStore.testParam1;
	}

	public void setTestParam1(String testParam1) {
		FakeStore.testParam1 = testParam1;
	}

	
	
}

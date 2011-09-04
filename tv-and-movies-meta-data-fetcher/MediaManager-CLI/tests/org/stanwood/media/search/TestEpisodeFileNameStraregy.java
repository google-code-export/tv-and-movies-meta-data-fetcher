/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.search;

import java.io.File;

import org.junit.Test;

@SuppressWarnings("nls")
/**
 * Used to test the searching strategy {@link TestEpisodeFileNameStraregy}
 */
public class TestEpisodeFileNameStraregy {


	/**
	 * Used to test that we can find the correct show name
	 */
	@Test
	public void testCanGetEpisodeAndSeason() {
		TestFilmSearcher.assertSearchDetails("A Show", null, null, doSearch("A Show 3-01 The show title.m4v", "%s %e - %t.%x"));
	}

	private TSearchDetails doSearch(String filename, String pattern) {
		EpisodeFileNameStraregy strategy = new EpisodeFileNameStraregy();
		File rootMediaDir = new File(File.separator+"media");
		File originalFile = new File(rootMediaDir,filename);
		SearchDetails searchDetails = strategy.getSearch(originalFile, rootMediaDir, pattern, null);
		if (searchDetails == null) {
			return null;
		}
		TSearchDetails sd = new TSearchDetails(originalFile, searchDetails.getTerm(), searchDetails.getYear(), searchDetails.getPart());
		return sd;
	}

}

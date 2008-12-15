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

import java.io.IOException;
import java.net.URL;

import org.stanwood.media.model.Film;
import org.stanwood.media.testdata.Data;

import au.id.jericho.lib.html.Source;
import junit.framework.TestCase;

/**
 * Used to test the {@link FindFilmPosters} class.
 */
public class TestFindFilmPosters extends TestCase {

	/**
	 * This will test that movie posters can be fetched from movieposter.com
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testFindPoster() throws Exception {
		FindFilmPosters findPosters = getPosterFinder();
		Film film = new Film(371746L);
		film.setTitle("Iron Man");
		URL posterUrl = findPosters.findViaMoviePoster(film);
		assertTrue("Check the url",posterUrl.toExternalForm().endsWith("movieposter.com/posters/archive/main/68/MPW-34217"));
	}

	private FindFilmPosters getPosterFinder() {
		return new FindFilmPosters() {
			@Override
			Source getSource(URL searchUrl) throws IOException {
				String url = searchUrl.toExternalForm().toLowerCase();
				if (url.startsWith("http://eu.movieposter.com/") && url.contains("iron+man")) {
					return new Source(Data.class.getResource("filmposters/movieposter-ironman.html"));
				}
				return null;
			}			
		};		
	}
}

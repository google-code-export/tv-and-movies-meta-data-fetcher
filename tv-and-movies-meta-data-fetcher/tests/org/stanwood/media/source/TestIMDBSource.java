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
import java.util.List;

import junit.framework.TestCase;

import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.testdata.Data;

import au.id.jericho.lib.html.Source;

/**
 * Used to test the {@link IMDBSource} class.
 */
public class TestIMDBSource extends TestCase {

	private final static long FILM_ID_IRON_MAN = 371746L;
	
	/**
	 * Test that film details are correctly read from the source
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testIronManFilm() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		Film film = source.getFilm( FILM_ID_IRON_MAN);
		assertEquals("Check id",FILM_ID_IRON_MAN,film.getId());
		assertEquals("Check title","Iron Man",film.getTitle().trim());
		assertEquals("Check summary","When wealthy industrialist Tony Stark is forced to build an armored suit after a life-threatening incident, he ultimately decides to use its technology to fight against evil.",film.getSummary());
		assertEquals("Check rating",8.1F,film.getRating());
		assertEquals("Check the release date","Fri May 02 00:00:00 BST 2008",film.getDate().toString());
		
		List<String>genres = film.getGenres();
		assertEquals(5,genres.size());
		assertEquals("Action",genres.get(0));		
		assertEquals("Adventure",genres.get(1));
		assertEquals("Drama",genres.get(2));
		assertEquals("Sci-Fi",genres.get(3));
		assertEquals("Thriller",genres.get(4));
		
		List<Link>directors = film.getDirectors();
		assertEquals(1,directors.size());
		assertEquals("Jon Favreau",directors.get(0).getTitle());
		assertEquals("http://www.imdb.com/name/nm0269463/",directors.get(0).getURL());
		
		List<Link>writers = film.getWriters();
		assertEquals(2,writers.size());
		assertEquals("Mark Fergus",writers.get(0).getTitle());
		assertEquals("http://www.imdb.com/name/nm1318843/",writers.get(0).getURL());
		assertEquals("Hawk Ostby",writers.get(1).getTitle());
		assertEquals("http://www.imdb.com/name/nm1319757/",writers.get(1).getURL());
		
		List<Certification>certs = film.getCertifications();
		assertEquals(33,certs.size());
		assertEquals("12",certs.get(0).getCountry());
		assertEquals("South Korea",certs.get(0).getCertification());		
		assertEquals("12",certs.get(5).getCountry());
		assertEquals("Netherlands",certs.get(5).getCertification());		
		assertEquals("IIA",certs.get(11).getCountry());
		assertEquals("Hong Kong",certs.get(11).getCertification());		
		assertEquals("M/12",certs.get(21).getCountry());
		assertEquals("Portugal",certs.get(21).getCertification());	
		assertEquals("12",certs.get(31).getCountry());
		assertEquals("Austria",certs.get(31).getCertification());		
		assertEquals("PG",certs.get(32).getCountry());
		assertEquals("Canada",certs.get(32).getCertification());
	}
	
	private IMDBSource getIMDBSource(final long filmId) {
		IMDBSource source = new IMDBSource() {
			@Override
			Source getSource(URL url) throws IOException {
				String strFilmId = String.valueOf(filmId);
				while (strFilmId.length()<7) {
					strFilmId="0"+strFilmId;
				}
				
				String strUrl = url.toExternalForm();				
				if (strUrl.equals("http://www.imdb.com/title/tt"+strFilmId+"/")) {
					String file = "film-"+strFilmId+".html";
					return new Source(Data.class.getResource(file));
				}				
				return null;
			}			
		};
		return source;
	}
}

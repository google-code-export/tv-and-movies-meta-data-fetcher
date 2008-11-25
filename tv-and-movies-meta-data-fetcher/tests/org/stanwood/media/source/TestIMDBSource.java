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
import java.net.URL;

import junit.framework.TestCase;

import org.stanwood.media.model.Film;
import org.stanwood.media.testdata.Data;

import au.id.jericho.lib.html.Source;

public class TestIMDBSource extends TestCase {

	private final static long FILM_ID_IRON_MAN = 371746L;
	
	public void testIronManFilm() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		Film film = source.getFilm(new File("/tmp/blah"), FILM_ID_IRON_MAN);
		assertEquals("Check id",FILM_ID_IRON_MAN,film.getId());
		assertEquals("Check title","Iron Man",film.getTitle().trim());
		assertEquals("Check summary","When wealthy industrialist Tony Stark is forced to build an armored suit after a life-threatening incident, he ultimately decides to use its technology to fight against evil.",film.getSummary());
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

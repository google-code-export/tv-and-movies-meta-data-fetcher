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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import junit.framework.TestCase;

import org.stanwood.media.FileHelper;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.testdata.Data;

/**
 * Used to test the {@link IMDBSource} class.
 */
public class TestIMDBSource extends TestCase {

	private final DateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	private final static String FILM_ID_IRON_MAN = "371746";

	/**
	 * Used to test the searching of films
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testSearchWithRedirectToFilm() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		File dir = FileHelper.createTmpDir("films");
		try {
			File tmpFile = new File(dir,"Harvard Man.avi");
			SearchResult result = source.searchForVideoId(Mode.FILM,tmpFile);
			assertEquals("0242508",result.getId());
			assertEquals("imdb",result.getSourceId());
			assertEquals("http://www.imdb.com/title/tt0242508/",result.getUrl());
		}
		finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Used to test the searching of films
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testSearch() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		File dir = FileHelper.createTmpDir("films");
		try {
			File tmpFile = new File(dir,"The iron man.avi");
			SearchResult result = source.searchForVideoId(Mode.FILM,tmpFile);
			assertEquals("0772174",result.getId());
			assertEquals("imdb",result.getSourceId());
			assertEquals("http://www.imdb.com/title/tt0772174/",result.getUrl());
		}
		finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Test the HTML entity decoding
	 */
	public void testHTMLEntityDecode() {
		String result = SearchHelper.decodeHtmlEntities("Jam&#243;n, jam&#243;n.avi");
		assertEquals("Check the result","Jamón, jamón.avi",result);
		result = SearchHelper.decodeHtmlEntities("&#243;Jam&#243;n, jam&#243;n.avi&#243;&#243;");
		assertEquals("Check the result","óJamón, jamón.avióó",result);
	}

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
		assertEquals("Check rating",8.0F,film.getRating());
		assertEquals("Check the release date","00:00:00 2008-05-02",df.format(film.getDate()));
		assertEquals("Check the image url","http://ia.media-imdb.com/images/M/MV5BMTM0MzgwNTAzNl5BMl5BanBnXkFtZTcwODkyNjg5MQ@@._V1._SX284_SY400_.jpg",film.getImageURL().toExternalForm());

		assertEquals("Check the country","USA",film.getCountry().getTitle());
		assertEquals("Check the country","http://www.imdb.com/Sections/Countries/USA/",film.getCountry().getURL());

		List<String>genres = film.getGenres();
		assertEquals(4,genres.size());
		assertEquals("Action",genres.get(0));
		assertEquals("Adventure",genres.get(1));
		assertEquals("Sci-Fi",genres.get(2));
		assertEquals("Thriller",genres.get(3));

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
		assertEquals(32,certs.size());
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
	}

	private IMDBSource getIMDBSource(final String filmId) {
		IMDBSource source = new IMDBSource() {
			@Override
			String getSource(URL url) throws IOException {

				String strFilmId = filmId;
				while (strFilmId.length()<7) {
					strFilmId="0"+strFilmId;
				}

				String strUrl = url.toExternalForm();
				if (strUrl.equals("http://www.imdb.com/title/tt"+strFilmId+"/")) {
					String file = "film-"+strFilmId+".html";
					return FileHelper.readFileContents(Data.class.getResourceAsStream(file));
				}
				else if (strUrl.endsWith("find?q=the+iron+man")) {
					return FileHelper.readFileContents(Data.class.getResourceAsStream("imdb-search.html"));
				}
				else if (strUrl.endsWith("find?q=harvard+man")) {
					return FileHelper.readFileContents(Data.class.getResourceAsStream("imdb-search-excact.html"));
				}
				return null;
			}
		};
		return source;
	}
}

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

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.search.SearchHelper;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link IMDBSource} class.
 */
public class TestIMDBSource {

	private final DateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	private final static String FILM_ID_IRON_MAN = "371746";

	/**
	 * Used to test the searching of films
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testSearchWithRedirectToFilm() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		File dir = FileHelper.createTmpDir("films");
		try {
			File tmpFile = new File(dir,"Harvard Man.avi");
			SearchResult result = source.searchForVideoId(tmpFile.getParentFile(),Mode.FILM,tmpFile,null);
			Assert.assertEquals("0242508",result.getId());
			Assert.assertEquals("imdb",result.getSourceId());
			Assert.assertEquals("http://www.imdb.com/title/tt0242508/",result.getUrl());
		}
		finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Used to test the searching of films
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testSearch() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		File dir = FileHelper.createTmpDir("films");
		try {
			File tmpFile = new File(dir,"The iron man.avi");
			SearchResult result = source.searchForVideoId(tmpFile.getParentFile(),Mode.FILM,tmpFile,null);
			Assert.assertEquals("0772174",result.getId());
			Assert.assertEquals("imdb",result.getSourceId());
			Assert.assertEquals("http://www.imdb.com/title/tt0772174/",result.getUrl());
		}
		finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Test the HTML entity decoding
	 */
	@Test
	public void testHTMLEntityDecode() {
		String result = SearchHelper.decodeHtmlEntities("Jam&#243;n, jam&#243;n.avi");
		Assert.assertEquals("Check the result","Jamón, jamón.avi",result);
		result = SearchHelper.decodeHtmlEntities("&#243;Jam&#243;n, jam&#243;n.avi&#243;&#243;");
		Assert.assertEquals("Check the result","óJamón, jamón.avióó",result);
	}

	/**
	 * Test that film details are correctly read from the source
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testIronManFilm() throws Exception {
		IMDBSource source = getIMDBSource(FILM_ID_IRON_MAN);
		Film film = source.getFilm( FILM_ID_IRON_MAN);
		Assert.assertEquals("Check id",FILM_ID_IRON_MAN,film.getId());
		Assert.assertEquals("Check title","Iron Man",film.getTitle().trim());
		Assert.assertEquals("Check summary","When wealthy industrialist Tony Stark is forced to build an armored suit after a life-threatening incident, he ultimately decides to use its technology to fight against evil.",film.getSummary());
		Assert.assertEquals("Check rating",8.0F,film.getRating(),0);
		Assert.assertEquals("Check the release date","00:00:00 2008-05-02",df.format(film.getDate()));
		Assert.assertEquals("Check the image url","http://ia.media-imdb.com/images/M/MV5BMTM0MzgwNTAzNl5BMl5BanBnXkFtZTcwODkyNjg5MQ@@._V1._SX284_SY400_.jpg",film.getImageURL().toExternalForm());

		Assert.assertEquals("Check the country","USA",film.getCountry().getTitle());
		Assert.assertEquals("Check the country","http://www.imdb.com/Sections/Countries/USA/",film.getCountry().getURL());

		List<String>genres = film.getGenres();
		Assert.assertEquals(4,genres.size());
		Assert.assertEquals("Action",genres.get(0));
		Assert.assertEquals("Adventure",genres.get(1));
		Assert.assertEquals("Sci-Fi",genres.get(2));
		Assert.assertEquals("Thriller",genres.get(3));

		List<Link>directors = film.getDirectors();
		Assert.assertEquals(1,directors.size());
		Assert.assertEquals("Jon Favreau",directors.get(0).getTitle());
		Assert.assertEquals("http://www.imdb.com/name/nm0269463/",directors.get(0).getURL());

		List<Link>writers = film.getWriters();
		Assert.assertEquals(2,writers.size());
		Assert.assertEquals("Mark Fergus",writers.get(0).getTitle());
		Assert.assertEquals("http://www.imdb.com/name/nm1318843/",writers.get(0).getURL());
		Assert.assertEquals("Hawk Ostby",writers.get(1).getTitle());
		Assert.assertEquals("http://www.imdb.com/name/nm1319757/",writers.get(1).getURL());

		List<Certification>certs = film.getCertifications();
		Assert.assertEquals(32,certs.size());
		Assert.assertEquals("12",certs.get(0).getCountry());
		Assert.assertEquals("South Korea",certs.get(0).getCertification());
		Assert.assertEquals("12",certs.get(5).getCountry());
		Assert.assertEquals("Netherlands",certs.get(5).getCertification());
		Assert.assertEquals("IIA",certs.get(11).getCountry());
		Assert.assertEquals("Hong Kong",certs.get(11).getCertification());
		Assert.assertEquals("M/12",certs.get(21).getCountry());
		Assert.assertEquals("Portugal",certs.get(21).getCertification());
		Assert.assertEquals("12",certs.get(31).getCountry());
		Assert.assertEquals("Austria",certs.get(31).getCertification());
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

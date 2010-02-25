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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Assert;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.testdata.EpisodeData;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link XMLStore} class.
 */
public class TestXMLStore extends XMLTestCase {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private static final String SHOW_ID = "58448";

	/**
	 * Test that the XML is read correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testReadXML() throws Exception {
		XMLStore xmlSource = new XMLStore();

		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Eureka");
			File episodeFile = new File(eurekaDir,"1x01 - blah.avi");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}
			FileHelper.copy(Data.class.getResourceAsStream("eureka.xml"),
					new File(eurekaDir, ".show.xml"));
			Show show = xmlSource.getShow(eurekaDir,episodeFile, SHOW_ID);
			assertNotNull(show);
			assertEquals("Eureka", show.getName());
			StringBuilder summary = new StringBuilder();
			summary.append("Small town. Big secret.\n");
			summary.append("\n");
			summary.append("A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand.\n");
			summary.append("\n");
			summary.append("Eureka is produced by NBC Universal Cable Studio and filmed in Vancouver, British Columbia, Canada.\n");
			assertEquals(summary.toString(), show.getLongSummary());
			assertEquals("tvcom",show.getSourceId());
			assertEquals("http://image.com.com/tv/images/b.gif", show.getImageURL().toExternalForm());
			assertEquals("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC...", show.getShortSummary());
			assertEquals("58448", show.getShowId());
			assertEquals("http://www.tv.com/show/58448/summary.html", show.getShowURL().toExternalForm());

			Season season = xmlSource.getSeason(eurekaDir,episodeFile,show, 1);
			assertEquals("http://www.tv.com/show/58448/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());
			assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=1",season.getListingUrl().toExternalForm());
			assertEquals(1,season.getSeasonNumber());
	        assertEquals(show,season.getShow());

	        Episode episode = xmlSource.getEpisode(eurekaDir,episodeFile,season, 1);
	        assertNotNull(episode);
	        assertEquals(1,episode.getEpisodeNumber());
	        assertEquals(784857,episode.getEpisodeId());
	        assertEquals(1,episode.getShowEpisodeNumber());
	        assertEquals("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.",episode.getSummary());
	        assertEquals("http://www.tv.com/eureka/pilot/episode/784857/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Pilot",episode.getTitle());
	        assertEquals("2006-10-10",df.format(episode.getDate()));

	        episodeFile = new File(eurekaDir,"1x02 - blah.avi");
	        episode = xmlSource.getEpisode(eurekaDir,episodeFile,season, 2);
	        assertNotNull(episode);
	        assertEquals(2,episode.getEpisodeNumber());
	        assertEquals(800578,episode.getEpisodeId());
	        assertEquals(2,episode.getShowEpisodeNumber());
	        assertEquals("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!",episode.getSummary());
	        assertEquals("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Many Happy Returns",episode.getTitle());
	        assertEquals("2006-10-11",df.format(episode.getDate()));

	        episodeFile = new File(eurekaDir,"2x02 - blah.avi");
			season = xmlSource.getSeason(eurekaDir,episodeFile,show, 2);
			assertEquals("http://www.tv.com/show/58448/episode_guide.html?printable=2",season.getDetailedUrl().toExternalForm());
			assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=2",season.getListingUrl().toExternalForm());
			assertEquals(2,season.getSeasonNumber());
	        assertEquals(show,season.getShow());

	        episode = xmlSource.getEpisode(eurekaDir,episodeFile,season, 2);
	        assertNotNull(episode);
	        assertEquals(2,episode.getEpisodeNumber());
	        assertEquals(800578,episode.getEpisodeId());
	        assertEquals(13,episode.getShowEpisodeNumber());

	        assertEquals("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.",episode.getSummary());
	        assertEquals("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Phoenix Rising",episode.getTitle());
	        assertEquals("2007-07-10",df.format(episode.getDate()));

	        episodeFile = new File(eurekaDir,"000 - blah.avi");
	        episode = xmlSource.getSpecial(eurekaDir,episodeFile,season, 0);
	        assertNotNull(episode);
	        assertEquals(0,episode.getEpisodeNumber());
	        assertEquals(800578,episode.getEpisodeId());
	        assertEquals(-1,episode.getShowEpisodeNumber());
	        assertEquals("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.",episode.getSummary());
	        assertEquals("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Countdown to the Premiere",episode.getTitle());
	        assertEquals("2007-07-09",df.format(episode.getDate()));
		} finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Used to test that a film can be read from the XML cache
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testReadFilm() throws Exception {
		XMLStore xmlSource = new XMLStore();

		File dir = FileHelper.createTmpDir("film");
		try {
			File filmFile = new File(dir,"1x01 - blah.avi");
			FileHelper.copy(Data.class.getResourceAsStream("films.xml"),new File(dir, ".films.xml"));
			Film film = xmlSource.getFilm(dir,filmFile, "114814");
			assertEquals("The Usual Suspects",film.getTitle());

			assertEquals("Check URL","http://test/image.jpg",film.getImageURL().toExternalForm());

			assertEquals("Check the country","USA",film.getCountry().getTitle());
			assertEquals("Check the country","http://www.imdb.com/Sections/Countries/USA/",film.getCountry().getURL());

			assertEquals("Crime",film.getGenres().get(0));
			assertEquals("Drama",film.getGenres().get(1));
			assertEquals("Mystery",film.getGenres().get(2));
			assertEquals("Thriller",film.getGenres().get(3));

			assertEquals("Drama",film.getPreferredGenre());

			assertEquals(27,film.getCertifications().size());
			assertEquals("Iceland",film.getCertifications().get(0).getCountry());
			assertEquals("16",film.getCertifications().get(0).getCertification());
			assertEquals("Hungary",film.getCertifications().get(12).getCountry());
			assertEquals("16",film.getCertifications().get(12).getCertification());
			assertEquals("USA",film.getCertifications().get(26).getCountry());
			assertEquals("R",film.getCertifications().get(26).getCertification());

			assertEquals("1995-08-25",df.format(film.getDate()));

			assertEquals(1,film.getDirectors().size());
			assertEquals("Bryan Singer",film.getDirectors().get(0).getTitle());
			assertEquals("http://www.imdb.com/name/nm0001741/",film.getDirectors().get(0).getURL());

			assertEquals("http://www.imdb.com/title/tt0114814/",film.getFilmUrl().toString());

			assertEquals(15,film.getGuestStars().size());
			assertEquals("Stephen Baldwin",film.getGuestStars().get(0).getTitle());
			assertEquals("http://www.imdb.com/name/nm0000286/",film.getGuestStars().get(0).getURL());

			assertEquals("Pete Postlethwaite",film.getGuestStars().get(6).getTitle());
			assertEquals("http://www.imdb.com/name/nm0000592/",film.getGuestStars().get(6).getURL());

			assertEquals("Christine Estabrook",film.getGuestStars().get(14).getTitle());
			assertEquals("http://www.imdb.com/name/nm0261452/",film.getGuestStars().get(14).getURL());

			assertEquals(8.7F,film.getRating());
			assertEquals("imdb",film.getSourceId());

			assertEquals("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.",
			             film.getSummary());

			assertEquals(1,film.getWriters().size());
			assertEquals("Christopher McQuarrie",film.getWriters().get(0).getTitle());
			assertEquals("http://www.imdb.com/name/nm0003160/",film.getWriters().get(0).getURL());
		} finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Test that the store rename function works
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testFilmRename() throws Exception {
		XMLStore xmlSource = new XMLStore();

		File dir = FileHelper.createTmpDir("film");
		try {
			File filmCache = new File(dir, ".films.xml");

			Map<String,String> params = new HashMap<String,String>();
			params.put("filmdir", dir.getAbsolutePath());
			FileHelper.copy(Data.class.getResourceAsStream("films.xml"),filmCache,params);

			String contents=FileHelper.readFileContents(filmCache);
			File oldFile = new File(dir,"The Usual Suspects part1.avi");
			File newFile = new File(dir,"The Usual Suspects (renamed) part1.avi");

			assertTrue(contents.contains(oldFile.getAbsolutePath()));
			assertTrue(!contents.contains(newFile.getAbsolutePath()));

			xmlSource.renamedFile(dir,oldFile, newFile);
			contents=FileHelper.readFileContents(filmCache);
			assertTrue(!contents.contains(oldFile.getAbsolutePath()));
			assertTrue(contents.contains(newFile.getAbsolutePath()));
		} finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Test that the film is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testCacheFilm() throws Exception {
		XMLStore xmlSource = new XMLStore();
		File dir = FileHelper.createTmpDir("film");
		try {
			File filmFile1 = new File(dir,"The Usual Suspects part1.avi");
			filmFile1.createNewFile();
			File filmFile2 = new File(dir,"The Usual Suspects part2.avi");
			filmFile2.createNewFile();
			Film film = Data.createFilm();

			xmlSource.cacheFilm(dir, filmFile1, film);
			xmlSource.cacheFilm(dir, filmFile2, film);

			File actualFile = new File(dir,".films.xml");
			String actualContents = FileHelper.readFileContents(actualFile);
			String expectedContents = FileHelper.readFileContents(Data.class.getResourceAsStream("films.xml"));
			expectedContents = expectedContents.replaceAll("\\$filmdir\\$", dir.getAbsolutePath());
			Assert.assertEquals("Check the results",expectedContents,actualContents);

		} finally {
			FileHelper.deleteDir(dir);
		}
	}

	/**
	 * Test that the show is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testCacheShow() throws Exception {
		XMLStore xmlSource = new XMLStore();

		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			List<EpisodeData> epsiodes = Data.createEurekaShow(eurekaDir);
			for (EpisodeData ed : epsiodes) {
				File episodeFile = ed.getFile();
				Episode episode = ed.getEpisode();
				Season season =  episode.getSeason();
				Show show=  season.getShow();

				xmlSource.cacheShow(eurekaDir, episodeFile, show);
				xmlSource.cacheSeason(eurekaDir, episodeFile, season);
				xmlSource.cacheEpisode(eurekaDir, episodeFile, episode);
			}

			File actualFile = new File(eurekaDir,".show.xml");
			String actualContents = FileHelper.readFileContents(actualFile);
			String expectedContents = FileHelper.readFileContents(Data.class.getResourceAsStream("eureka.xml"));
			Assert.assertEquals("Check the results",expectedContents,actualContents);

		} finally {
			FileHelper.deleteDir(dir);
		}
	}




}

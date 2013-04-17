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
package org.stanwood.media.store.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.testdata.EpisodeData;
import org.stanwood.media.util.FileHelper;

/**
 * This is used to test the class {@FileDatabaseStore}
 */
@SuppressWarnings("nls")
public class TestFileDatabaseStore {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private DatabaseStore createStore(final File configDir) throws StoreException {

		Controller controller = new Controller(null) {
			@Override
			public File getConfigDir() throws ConfigException {
				return configDir;
			}

		};
		FileDatabaseStore store = new FileDatabaseStore(controller);
		store.init();
		return store;
	}

	/**
	 * Used to test that TV shows/seasons/episodes can be stored and retrieved in the FileDatabaseStore
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testStore() throws Exception {
		File configDir = FileHelper.createTmpDir("config");
		File dir = FileHelper.createTmpDir("test");
		try {
			DatabaseStore store = createStore(configDir);
			File eurekaDir = new File(dir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File heroesDir = new File(dir, "Heroes");
			if (!heroesDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			List<EpisodeData> episodes = Data.createEurekaShow(eurekaDir);
			DatabaseStoreTest.cacheEpisodes(store, dir, episodes);
			Film filmData = Data.createFilm();
			File filmFile1 = new File(dir,"The Usual Suspects part1.avi");
			store.cacheFilm(dir, filmFile1,null, filmData, 1);

			File episodeFile = episodes.get(0).getFile();
			IShow show = store.getShow(dir, episodeFile, Data.SHOW_ID_EUREKA);
			Assert.assertNotNull(show);
			Assert.assertEquals("Eureka", show.getName());
			StringBuilder summary = new StringBuilder();
			summary.append("Small town. Big secret.\n");
			summary.append("\n");
			summary.append("A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand.\n");
			summary.append("\n");
			summary.append("Eureka is produced by NBC Universal Cable Studio and filmed in Vancouver, British Columbia, Canada.\n");
			Assert.assertEquals(summary.toString(), show.getLongSummary());
			Assert.assertEquals(XBMCSource.class.getName()+"#metadata.tvdb.com",show.getSourceId());
			Assert.assertEquals("http://image.com.com/tv/images/b.gif", show.getImageURL().toExternalForm());
			Assert.assertEquals("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC...", show.getShortSummary());
			Assert.assertEquals("58448", show.getShowId());
			Assert.assertEquals("http://www.tv.com/show/58448/summary.html", show.getShowURL().toExternalForm());

			ISeason season = store.getSeason(dir,episodeFile,show, 1);
			Assert.assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=1",season.getURL().toExternalForm());
			Assert.assertEquals(1,season.getSeasonNumber());
			Assert.assertEquals(show,season.getShow());

			List<Integer>episodeNums = new ArrayList<Integer>();
			episodeNums.add(1);
	        IEpisode episode = store.getEpisode(dir,episodeFile,season, episodeNums);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(1,episode.getEpisodeNumber());
	        Assert.assertEquals("784857",episode.getEpisodeId());
	        Assert.assertEquals("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/eureka/pilot/episode/784857/summary.html",episode.getUrl().toExternalForm());
	        Assert.assertEquals("Pilot",episode.getTitle());
	        Assert.assertEquals("2006-10-10",df.format(episode.getDate()));
	        Assert.assertEquals(1, episode.getDirectors().size());
	        Assert.assertEquals("Harry", episode.getDirectors().get(0));
	        Assert.assertEquals(2, episode.getActors().size());
	        Assert.assertEquals("betty", episode.getActors().get(0).getRole());
	        Assert.assertEquals("sally", episode.getActors().get(0).getName());
	        Assert.assertEquals("steve", episode.getActors().get(1).getRole());
	        Assert.assertEquals("Cedric", episode.getActors().get(1).getName());
	        Assert.assertEquals(1, episode.getWriters().size());
	        Assert.assertEquals("Write a lot", episode.getWriters().get(0));
	        Assert.assertEquals(1.0F,episode.getRating().getRating(),0);
	        Assert.assertEquals("http://blah/image.jpg",episode.getImageURL().toExternalForm());
	        Assert.assertFalse(episode.isSpecial());
	        Assert.assertEquals(1,episode.getEpisodes().size());
	        Assert.assertTrue(episode.getEpisodes().contains(Integer.valueOf(1)));
	        Assert.assertFalse(episode.getEpisodes().contains(Integer.valueOf(3)));

	        episodeFile = episodes.get(1).getFile();
	        episodeNums = new ArrayList<Integer>();
			episodeNums.add(2);
	        episode = store.getEpisode(dir,episodeFile,season, episodeNums);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(2,episode.getEpisodeNumber());
	        Assert.assertEquals("800578",episode.getEpisodeId());
	        Assert.assertEquals("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html",episode.getUrl().toExternalForm());
	        Assert.assertEquals("Many Happy Returns",episode.getTitle());
	        Assert.assertEquals("2006-10-11",df.format(episode.getDate()));
	        Assert.assertNull(episode.getImageURL());
	        Assert.assertFalse(episode.isSpecial());
	        Assert.assertEquals(2,episode.getEpisodes().size());
	        Assert.assertTrue(episode.getEpisodes().contains(Integer.valueOf(2)));
	        Assert.assertTrue(episode.getEpisodes().contains(Integer.valueOf(3)));

	        episodeFile = episodes.get(2).getFile();
//	        episodeFile = new File(eurekaDir,"2x02 - blah.avi");
			season = store.getSeason(dir,episodeFile,show, 2);
			Assert.assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=2",season.getURL().toExternalForm());
			Assert.assertEquals(2,season.getSeasonNumber());
			Assert.assertEquals(show,season.getShow());

			episodeNums = new ArrayList<Integer>();
			episodeNums.add(2);
	        episode = store.getEpisode(dir,episodeFile,season, episodeNums);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(2,episode.getEpisodeNumber());
	        Assert.assertEquals("800578",episode.getEpisodeId());
	        Assert.assertFalse(episode.isSpecial());
	        Assert.assertEquals("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html",episode.getUrl().toExternalForm());
	        Assert.assertEquals("Phoenix Rising",episode.getTitle());
	        Assert.assertEquals("2007-07-10",df.format(episode.getDate()));

	        episodeFile = episodes.get(3).getFile();
//	        episodeFile = new File(eurekaDir,"000 - blah.avi");
	        episodeNums = new ArrayList<Integer>();
			episodeNums.add(0);
	        episode = store.getSpecial(dir,episodeFile,season, episodeNums);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(0,episode.getEpisodeNumber());
	        Assert.assertEquals("800578",episode.getEpisodeId());
	        Assert.assertEquals("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html",episode.getUrl().toExternalForm());
	        Assert.assertEquals("Countdown to the Premiere",episode.getTitle());
	        Assert.assertEquals("2007-07-09",df.format(episode.getDate()));
	        Assert.assertTrue(episode.isSpecial());

	        IFilm film = store.getFilm(dir, filmFile1, "114814");
	        Assert.assertNotNull(film);
	        Assert.assertEquals(15,film.getActors().size());
			Assert.assertEquals("Stephen Baldwin",film.getActors().get(0).getName());
			Assert.assertEquals("Michael McManus",film.getActors().get(0).getRole());
			Assert.assertEquals("Chazz Palminteri",film.getActors().get(5).getName());
			Assert.assertEquals("Dave Kujan, US Customs",film.getActors().get(5).getRole());
			Assert.assertEquals("Christine Estabrook",film.getActors().get(14).getName());
			Assert.assertEquals("Dr. Plummer",film.getActors().get(14).getRole());
			Assert.assertEquals(27,film.getCertifications().size());
			Assert.assertEquals("Iceland",film.getCertifications().get(0).getType());
			Assert.assertEquals("16",film.getCertifications().get(0).getCertification());
			Assert.assertEquals("Germany",film.getCertifications().get(10).getType());
			Assert.assertEquals("16",film.getCertifications().get(10).getCertification());
			Assert.assertEquals("USA",film.getCertifications().get(26).getType());
			Assert.assertEquals("R",film.getCertifications().get(26).getCertification());
			Assert.assertEquals(3,film.getChapters().size());
			Assert.assertEquals(1,film.getChapters().get(0).getNumber());
			Assert.assertEquals("The start",film.getChapters().get(0).getName());
			Assert.assertEquals(2,film.getChapters().get(1).getNumber());
			Assert.assertEquals("Second Chapter",film.getChapters().get(1).getName());
			Assert.assertEquals(3,film.getChapters().get(2).getNumber());
			Assert.assertEquals("The end",film.getChapters().get(2).getName());
			Assert.assertEquals("USA",film.getCountry());
			Assert.assertEquals("Test description of the film",film.getDescription());
			Assert.assertEquals(1,film.getDirectors().size());
			Assert.assertEquals("Bryan Singer",film.getDirectors().get(0));
			Assert.assertEquals("http://www.imdb.com/title/tt0114814/",film.getFilmUrl().toExternalForm());
			Assert.assertEquals(4,film.getGenres().size());
			Assert.assertEquals("Crime",film.getGenres().get(0));
			Assert.assertEquals("Drama",film.getGenres().get(1));
			Assert.assertEquals("Mystery",film.getGenres().get(2));
			Assert.assertEquals("Thriller",film.getGenres().get(3));
			Assert.assertEquals("114814",film.getId());
			Assert.assertEquals("http://test/image.jpg",film.getImageURL().toExternalForm());
			Assert.assertEquals("Drama",film.getPreferredGenre());
			Assert.assertEquals(8.7F,film.getRating().getRating(),0);
			Assert.assertEquals(35,film.getRating().getNumberOfVotes());
			Assert.assertEquals(XBMCSource.class.getName()+"#metadata.themoviedb.org",film.getSourceId());
			Assert.assertEquals("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.",film.getSummary());
			Assert.assertEquals("The Usual Suspects",film.getTitle());
			Assert.assertEquals(1,film.getWriters().size());
			Assert.assertEquals("Christopher McQuarrie",film.getWriters().get(0));

			File file = new File(store.getController().getConfigDir(),"mediaInfo.db"); //$NON-NLS-1$
			Connection c = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				c = DriverManager.getConnection("jdbc:hsqldb:file:"+file.getAbsolutePath(), "sa", "");
				stmt = c.prepareStatement("select version from media_dir");
				rs = stmt.executeQuery();
				rs.next();
				Assert.assertEquals("1.0",rs.getString(1));
			}
			finally {
				if (rs!=null) {
					rs.close();
				}
				if (stmt!=null) {
					stmt.close();
				}
				if (c!=null) {
					c.close();
				}
			}

		}
		finally {
			FileHelper.delete(configDir);
			if (dir!=null) {
				FileHelper.delete(dir);
			}
		}
	}
}

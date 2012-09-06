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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.Helper;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link SapphireStore} class.
 */
@SuppressWarnings("nls")
public class TestSapphireStore  {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final String SHOW_ID = "58448";

	/**
	 * Test that the film is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testCacheFilm() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		SapphireStore xmlSource = new SapphireStore();
		xmlSource.setParameter("PreferredCertificationCounrty", "UK");
		Assert.assertEquals("UK",xmlSource.getParameter("PreferredCertificationCounrty"));
		File dir = FileHelper.createTmpDir("show");
		try {
			File filmFile = new File(dir,"The Usual Suspects.avi");
			Film film = new Film("114814",new URL("http://www.imdb.com/title/tt0114814/"));
			film.setDescription("This is a test description");
			film.setTitle("The Usual Suspects");
			List<String> genres = new ArrayList<String>();
			genres.add("Crime");
			genres.add("Drama");
			genres.add("Mystery");
			genres.add("Thriller");
			film.setGenres(genres);
			List<Certification> certifications= new ArrayList<Certification>();
			certifications.add(new Certification("16","Iceland"));
			certifications.add(new Certification("R-18","Philippines"));
			certifications.add(new Certification("16","Argentina"));
			certifications.add(new Certification("MA","Australia"));
			certifications.add(new Certification("16","Brazil"));
			certifications.add(new Certification("14A","Canada"));
			certifications.add(new Certification("18","Chile"));
			certifications.add(new Certification("16","Denmark"));
			certifications.add(new Certification("K-16","Finland"));
			certifications.add(new Certification("U","France"));
			certifications.add(new Certification("16","Germany"));
			certifications.add(new Certification("IIB","Hong Kong"));
			certifications.add(new Certification("16","Hungary"));
			certifications.add(new Certification("18","Ireland"));
			certifications.add(new Certification("T","Italy"));
			certifications.add(new Certification("PG-12","Japan"));
			certifications.add(new Certification("16","Netherlands"));
			certifications.add(new Certification("R18","New Zealand"));
			certifications.add(new Certification("15","Norway"));
			certifications.add(new Certification("M/16","Portugal"));
			certifications.add(new Certification("M18","Singapore"));
			certifications.add(new Certification("PG (cut)","Singapore"));
			certifications.add(new Certification("18","South Korea"));
			certifications.add(new Certification("18","Spain"));
			certifications.add(new Certification("15","Sweden"));
			certifications.add(new Certification("18","UK"));
			certifications.add(new Certification("R","USA"));
			film.setCertifications(certifications);
			film.setDate(df.parse("1995-08-25"));
			List<String> directors = new ArrayList<String>();
			directors.add("Bryan Singer");
			film.setDirectors(directors);
//			film.setFilmUrl(new URL("http://www.imdb.com/title/tt0114814/"));
			List<Actor> guestStars = new ArrayList<Actor>();
			guestStars.add(new Actor("Stephen Baldwin",""));
			guestStars.add(new Actor("Gabriel Byrne",""));
			guestStars.add(new Actor("Benicio Del Toro",""));
			guestStars.add(new Actor("Kevin Pollak",""));
			guestStars.add(new Actor("Kevin Spacey",""));
			guestStars.add(new Actor("Chazz Palminteri",""));
			guestStars.add(new Actor("Pete Postlethwaite",""));
			guestStars.add(new Actor("Giancarlo Esposito",""));
			guestStars.add(new Actor("Suzy Amis",""));
			guestStars.add(new Actor("Dan Hedaya",""));
			guestStars.add(new Actor("Paul Bartel",""));
			guestStars.add(new Actor("Carl Bressler",""));
			guestStars.add(new Actor("Phillip Simon",""));
			guestStars.add(new Actor("Jack Shearer",""));
			guestStars.add(new Actor("Christine Estabrook",""));
			film.setActors(guestStars);
			film.setRating(new Rating(8.7F,434));
			film.setSourceId(Data.TEST_FILM_SOURCE_ID);
			film.setSummary("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.");
			List<String>writers = new ArrayList<String>();
			writers.add("Christopher McQuarrie");
			film.setWriters(writers);

			xmlSource.cacheFilm(dir,filmFile, film,1);

			File actualFile = new File(dir,"The Usual Suspects.xml");
			Assert.assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			Helper.assertXMLEquals(Data.class.getResourceAsStream("sapphire/film-0114814.xml"), new FileInputStream(actualFile));

		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Test that the show is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testCacheShow() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		SapphireStore xmlSource = new SapphireStore();

		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}
			File episodeFile = new File(eurekaDir,"1x01 - blah.avi");

			Show show = createShow(eurekaDir);
			xmlSource.cacheShow(eurekaDir,episodeFile,show);

			Season season = new Season(show,1);
			season.setURL(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_listings.html?season=1"));

			xmlSource.cacheSeason(eurekaDir,episodeFile,season);
			Episode episode1 = new Episode(1,season,false);
			episode1.setDate(df.parse("2006-10-10"));
			episode1.setSummary("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.");
			episode1.setUrl(new URL("http://www.tv.com/eureka/pilot/episode/784857/summary.html"));
			episode1.setTitle("Pilot");
			episode1.setRating(new Rating(1,1));
			episode1.setDirectors(Data.createStringList(new String[]{"Harry"}));
			episode1.setWriters(Data.createStringList(new String[]{"Write a lot"}));
			episode1.setActors(Data.createActorsList(new Actor[]{new Actor("sally","betty"),new Actor("Cedric","steve")}));
			episode1.setEpisodeId("784857");
			xmlSource.cacheEpisode(eurekaDir,episodeFile,episode1);

			episodeFile = new File(eurekaDir,"1x02 - blah.avi");
			Episode episode2 = new Episode(2,season,false);
			episode2.setDate(df.parse("2006-10-11"));
			episode2.setSummary("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!");
			episode2.setUrl(new URL("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html"));
			episode2.setTitle("Many Happy Returns");
			episode2.setRating(new Rating(9.5F,12345));
			episode2.setEpisodeId("800578");
			xmlSource.cacheEpisode(eurekaDir,episodeFile,episode2);

			season = new Season(show,2);
			season.setURL(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_listings.html?season=2"));

			xmlSource.cacheSeason(eurekaDir,episodeFile,season);

			episodeFile = new File(eurekaDir,"2x13 - blah.avi");
			episode1 = new Episode(13,season,false);
			episode1.setDate(df.parse("2007-7-10"));
			episode1.setSummary("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.");
			episode1.setUrl(new URL("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html"));
			episode1.setTitle("Phoenix Rising");
			episode1.setEpisodeId("800578");
			episode1.setRating(new Rating(0.4F,3434));
			xmlSource.cacheEpisode(eurekaDir,episodeFile,episode1);

			episodeFile = new File(eurekaDir,"000 - blah.avi");
			Episode special1 = new Episode(0,season,true);
			special1.setDate(df.parse("2007-7-09"));
			special1.setSummary("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.");
			special1.setUrl(new URL("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html"));
			special1.setTitle("Countdown to the Premiere");
			special1.setRating(new Rating(0.4F,345));
			special1.setEpisodeId("800578");
			special1.setDirectors(Data.createStringList(new String[]{"JP"}));
			special1.setWriters(Data.createStringList(new String[]{"Write a lot","Write a little"}));
			List<Actor> actors = new ArrayList<Actor>();
			actors.add(new Actor("bob","fred"));
			actors.add(new Actor("Write a little","blah"));
			special1.setActors(actors);

			xmlSource.cacheEpisode(eurekaDir,episodeFile,special1);

			File actualFile = new File(eurekaDir,"1x01 - blah.xml");
			Assert.assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			Helper.assertXMLEquals(Data.class.getResourceAsStream("sapphire/eureka-101.xml"), new FileInputStream(actualFile));

			actualFile = new File(eurekaDir,"1x02 - blah.xml");
			Assert.assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			Helper.assertXMLEquals(Data.class.getResourceAsStream("sapphire/eureka-102.xml"), new FileInputStream(actualFile));

			actualFile = new File(eurekaDir,"2x13 - blah.xml");
			Assert.assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			Helper.assertXMLEquals(Data.class.getResourceAsStream("sapphire/eureka-213.xml"), new FileInputStream(actualFile));

			actualFile = new File(eurekaDir,"000 - blah.xml");
			Assert.assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			Helper.assertXMLEquals(Data.class.getResourceAsStream("sapphire/eureka-000.xml"), new FileInputStream(actualFile));

		} finally {
			FileHelper.delete(dir);
		}
	}

	private Show createShow(File eurekaDir) throws MalformedURLException {
		Show show = new Show(SHOW_ID);
		show.setSourceId(Data.TEST_TV_SOURCE_ID);
		show.setImageURL(new URL("http://image.com.com/tv/images/b.gif"));
		StringBuilder summary = new StringBuilder();
		summary.append("Small town. Big secret.\n");
		summary.append("\n");
		summary.append("A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand.\n");
		summary.append("\n");
		summary.append("Eureka is produced by NBC Universal Cable Studio and filmed in Vancouver, British Columbia, Canada.\n");
		show.setLongSummary(summary.toString());
		show.setName("Eureka");
		show.setShortSummary("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC...");
		show.setShowURL(new URL("http://www.tv.com/show/58448/summary.html"));
		List<String> genres = new ArrayList<String>();
		genres.add("SCIFI");
		genres.add("Drama");
		show.setGenres(genres);
		return show;
	}
}

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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLTestCase;
import org.stanwood.media.FileHelper;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.IMDBSource;
import org.stanwood.media.source.TVCOMSource;
import org.stanwood.media.testdata.Data;
import org.xml.sax.InputSource;

/**
 * Used to test the {@link SapphireStore} class.
 */
public class TestSapphireStore extends XMLTestCase {

	private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final String SHOW_ID = "58448";	
	
	/**
	 * Test that the film is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testCacheFilm() throws Exception {
		SapphireStore xmlSource = new SapphireStore();
		xmlSource.setPreferredCertificationCounrty("UK");
		assertEquals("UK",xmlSource.getPreferredCertificationCounrty());
		File dir = FileHelper.createTmpDir("show");
		try {
			File filmFile = new File(dir,"The Usual Suspects.avi");
			Film film = new Film("114814");
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
			List<Link> directors = new ArrayList<Link>();
			directors.add(new Link("Bryan Singer","http://www.imdb.com/name/nm0001741/"));
			film.setDirectors(directors);
			film.setFilmUrl(new URL("http://www.imdb.com/title/tt0114814/"));			
			List<Link> guestStars = new ArrayList<Link>();
			guestStars.add(new Link("Stephen Baldwin","http://www.imdb.com/name/nm0000286/"));
			guestStars.add(new Link("Gabriel Byrne","http://www.imdb.com/name/nm0000321/"));
			guestStars.add(new Link("Benicio Del Toro","http://www.imdb.com/name/nm0001125/"));
			guestStars.add(new Link("Kevin Pollak","http://www.imdb.com/name/nm0001629/"));
			guestStars.add(new Link("Kevin Spacey","http://www.imdb.com/name/nm0000228/"));
			guestStars.add(new Link("Chazz Palminteri","http://www.imdb.com/name/nm0001590/"));
			guestStars.add(new Link("Pete Postlethwaite","http://www.imdb.com/name/nm0000592/"));
			guestStars.add(new Link("Giancarlo Esposito","http://www.imdb.com/name/nm0002064/"));
			guestStars.add(new Link("Suzy Amis","http://www.imdb.com/name/nm0000751/"));
			guestStars.add(new Link("Dan Hedaya","http://www.imdb.com/name/nm0000445/"));
			guestStars.add(new Link("Paul Bartel","http://www.imdb.com/name/nm0000860/"));
			guestStars.add(new Link("Carl Bressler","http://www.imdb.com/name/nm0107808/"));
			guestStars.add(new Link("Phillip Simon","http://www.imdb.com/name/nm0800342/"));
			guestStars.add(new Link("Jack Shearer","http://www.imdb.com/name/nm0790436/"));
			guestStars.add(new Link("Christine Estabrook","http://www.imdb.com/name/nm0261452/"));
			film.setGuestStars(guestStars);
			film.setRating(8.7F);
			film.setSourceId(IMDBSource.SOURCE_ID);
			film.setSummary("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.");
			List<Link>writers = new ArrayList<Link>();
			writers.add(new Link("Christopher McQuarrie","http://www.imdb.com/name/nm0003160/"));
			film.setWriters(writers);
			
			xmlSource.cacheFilm(filmFile, film);
			
			File actualFile = new File(dir,"The Usual Suspects.xml");
			assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			assertXMLEqual(new InputSource(Data.class.getResourceAsStream("sapphire/film-0114814.xml")), new InputSource(new FileInputStream(actualFile)));
			
		} finally {
			FileHelper.deleteDir(dir);
		}
	}
	
	/**
	 * Test that the show is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testCacheShow() throws Exception {
		SapphireStore xmlSource = new SapphireStore();
		
		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Eureka");
			eurekaDir.mkdir();
			File episodeFile = new File(eurekaDir,"1x01 - blah.avi");
			
			Show show = createShow(eurekaDir);
			xmlSource.cacheShow(episodeFile,show);		
			
			Season season = new Season(show,1);
			season.setDetailedUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_guide.html?printable=1"));
			season.setListingUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_listings.html?season=1"));
			show.addSeason(season);		
			xmlSource.cacheSeason(episodeFile,season);			
			Episode episode1 = new Episode(1,season);
			episode1.setDate(df.parse("2006-10-10"));
			episode1.setProductionCode("001");
			episode1.setSiteId("1");
			episode1.setSpecial(false);
			episode1.setSpecialName(null);
			episode1.setSummary("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.");
			episode1.setSummaryUrl(new URL("http://www.tv.com/eureka/pilot/episode/784857/summary.html"));
			episode1.setTitle("Pilot");			
			episode1.setRating(1);
			episode1.setDirectors(createLinks(new Link[]{new Link("Harry","http://test/")}));
			episode1.setWriters(createLinks(new Link[]{new Link("Write a lot","http://test/a")}));
			episode1.setGuestStars(createLinks(new Link[]{new Link("sally","http://test/sally"),new Link("Cedric","http://test/cedric")}));
			episode1.setEpisodeId(784857);
			season.addEpisode(episode1);
			xmlSource.cacheEpisode(episodeFile,episode1);
			
			episodeFile = new File(eurekaDir,"1x02 - blah.avi");
			Episode episode2 = new Episode(2,season);
			episode2.setDate(df.parse("2006-10-11"));
			episode2.setProductionCode("002");
			episode2.setSiteId("2");
			episode2.setSpecial(false);
			episode2.setSpecialName(null);
			episode2.setSummary("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!");
			episode2.setSummaryUrl(new URL("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html"));
			episode2.setTitle("Many Happy Returns");
			episode2.setRating(9.5F);			
			episode2.setEpisodeId(800578);
			season.addEpisode(episode2);			
			xmlSource.cacheEpisode(episodeFile,episode2);
			
			season = new Season(show,2);
			season.setDetailedUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_guide.html?printable=2"));
			season.setListingUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_listings.html?season=2"));
			show.addSeason(season);	
			xmlSource.cacheSeason(episodeFile,season);
			
			episodeFile = new File(eurekaDir,"2x13 - blah.avi");
			episode1 = new Episode(2,season);
			episode1.setDate(df.parse("2007-7-10"));
			episode1.setProductionCode("013");
			episode1.setSiteId("13");
			episode1.setSpecial(false);
			episode1.setSpecialName(null);
			episode1.setSummary("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.");
			episode1.setSummaryUrl(new URL("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html"));
			episode1.setTitle("Phoenix Rising");
			episode1.setEpisodeId(800578);
			episode1.setRating(0.4F);
			season.addEpisode(episode1);			
			xmlSource.cacheEpisode(episodeFile,episode1);
			
			episodeFile = new File(eurekaDir,"000 - blah.avi");
			Episode special1 = new Episode(0,season);
			special1.setDate(df.parse("2007-7-09"));
			special1.setProductionCode("200");
			special1.setSiteId("Special");
			special1.setSpecial(true);
			special1.setSpecialName("Special");
			special1.setSummary("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.");
			special1.setSummaryUrl(new URL("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html"));
			special1.setTitle("Countdown to the Premiere");
			special1.setRating(0.4F);
			special1.setEpisodeId(800578);
			special1.setDirectors(createLinks(new Link[]{new Link("JP","http://test/")}));
			special1.setWriters(createLinks(new Link[]{new Link("Write a lot","http://test/a"),new Link("Write a little","http://test/b")}));
			special1.setGuestStars(createLinks(new Link[]{new Link("bob","http://test/bob"),new Link("Write a little","http://test/fred")}));
						
			season.addSepcial(special1);			
			xmlSource.cacheEpisode(episodeFile,special1);			
			
			File actualFile = new File(eurekaDir,"1x01 - blah.xml");
			assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			assertXMLEqual(new InputSource(Data.class.getResourceAsStream("sapphire/eureka-101.xml")), new InputSource(new FileInputStream(actualFile)));
			
			actualFile = new File(eurekaDir,"1x02 - blah.xml");
			assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			assertXMLEqual(new InputSource(Data.class.getResourceAsStream("sapphire/eureka-102.xml")), new InputSource(new FileInputStream(actualFile)));
			
			actualFile = new File(eurekaDir,"2x13 - blah.xml");
			assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			assertXMLEqual(new InputSource(Data.class.getResourceAsStream("sapphire/eureka-213.xml")), new InputSource(new FileInputStream(actualFile)));
			
			actualFile = new File(eurekaDir,"000 - blah.xml");
			assertTrue(actualFile.exists());
//			FileHelper.displayFile(actualFile, System.out);
			assertXMLEqual(new InputSource(Data.class.getResourceAsStream("sapphire/eureka-000.xml")), new InputSource(new FileInputStream(actualFile)));
			
		} finally {
			FileHelper.deleteDir(dir);
		}			
	}
	
	private List<Link>createLinks(Link[] links) {
		List<Link> result = new ArrayList<Link>();
		for (Link link : links ) {
			result.add(link);
		}
		return result;
	}

	private Show createShow(File eurekaDir) throws MalformedURLException {
		Show show = new Show(SHOW_ID);
		show.setSourceId(TVCOMSource.SOURCE_ID);
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

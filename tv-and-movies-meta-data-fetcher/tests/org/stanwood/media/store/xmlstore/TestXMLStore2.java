package org.stanwood.media.store.xmlstore;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.testdata.EpisodeData;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Used to test the {@link XMLStore2} class.
 */
public class TestXMLStore2 {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Used to check that a couple of shows and a film can be correctly added
	 * to the store
	 * @throws Exception Thrown if their is a problem with the test
	 */
	@Test
	public void testStore() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");

		XMLStore2 xmlSource = new XMLStore2();
		File dir = FileHelper.createTmpDir("test");
		try {
			File eurekaDir = new File(dir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File heroesDir = new File(dir, "Heroes");
			if (!heroesDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			List<EpisodeData> epsiodes = Data.createEurekaShow(eurekaDir);
			epsiodes.addAll(Data.createHeroesShow(heroesDir));

			for (EpisodeData ed : epsiodes) {
				File episodeFile = ed.getFile();
				Episode episode = ed.getEpisode();
				Season season =  episode.getSeason();
				Show show=  season.getShow();

				xmlSource.cacheShow(dir, episodeFile, show);
				xmlSource.cacheSeason(dir, episodeFile, season);
				xmlSource.cacheEpisode(dir, episodeFile, episode);
			}

			File filmFile1 = new File(dir,"The Usual Suspects part1.avi");
			filmFile1.createNewFile();
			File filmFile2 = new File(dir,"The Usual Suspects part2.avi");
			filmFile2.createNewFile();
			Film film = Data.createFilm();

			xmlSource.cacheFilm(dir, filmFile1, film);
			xmlSource.cacheFilm(dir, filmFile2, film);

			File actualFile = new File(dir,".mediaInfoFetcher-xmlStore.xml");
			String actualContents = FileHelper.readFileContents(actualFile);
			String expectedContents = FileHelper.readFileContents(TestXMLStore2.class.getResourceAsStream("expectedXmlStoreResults.xml"));
			expectedContents = expectedContents.replaceAll("\\$rootMedia\\$", dir.getAbsolutePath());
			Assert.assertEquals("Check the results",formatXML(expectedContents),formatXML(actualContents));
//			FileHelper.displayFile(actualFile, System.out);
		} finally {
			FileHelper.deleteDir(dir);
		}


	}

	private String formatXML(String xml) throws XMLParserException, IOException {
		Document document = XMLParser.strToDom(xml);
		OutputFormat format = new OutputFormat(document);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(document);

		return out.toString();
	}



	/**
	 * Used to test that data can be read back from the store
	 * @throws Exception Thrown if their is a problem in the test
	 */
	@Test
	public void testReadShows() throws Exception{
		File dir = FileHelper.createTmpDir("test");
		XMLStore2 xmlSource = new XMLStore2();
		try {
			File cacheFile = new File(dir,".mediaInfoFetcher-xmlStore.xml");
			Map<String, String> params = new HashMap<String,String>();
			params.put("rootMedia", dir.getAbsolutePath());
			FileHelper.copy(TestXMLStore2.class.getResourceAsStream("expectedXmlStoreResults.xml"),cacheFile,params);

			File eurekaDir = new File(dir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File heroesDir = new File(dir, "Heroes");
			if (!heroesDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

//			File episodeFile = new File(eurekaDir,"1x01 - blah");
			File episodeFile = new File(eurekaDir,"1x02 - blah");
			episodeFile.createNewFile();
//			episodeFile = new File(eurekaDir,"2x13 - blah");
//			episodeFile = new File(eurekaDir,"000 - blah");

			Show show = xmlSource.getShow(dir, episodeFile, Data.SHOW_ID_EUREKA);
			Assert.assertNotNull(show);
			Assert.assertEquals("Eureka", show.getName());
			StringBuilder summary = new StringBuilder();
			summary.append("Small town. Big secret.\n");
			summary.append("\n");
			summary.append("A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand.\n");
			summary.append("\n");
			summary.append("Eureka is produced by NBC Universal Cable Studio and filmed in Vancouver, British Columbia, Canada.\n");
			Assert.assertEquals(summary.toString(), show.getLongSummary());
			Assert.assertEquals("xbmc-metadata.tvdb.com",show.getSourceId());
			Assert.assertEquals("http://image.com.com/tv/images/b.gif", show.getImageURL().toExternalForm());
			Assert.assertEquals("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC...", show.getShortSummary());
			Assert.assertEquals("58448", show.getShowId());
			Assert.assertEquals("http://www.tv.com/show/58448/summary.html", show.getShowURL().toExternalForm());

			Season season = xmlSource.getSeason(dir,episodeFile,show, 1);
			Assert.assertEquals("http://www.tv.com/show/58448/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());
			Assert.assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=1",season.getListingUrl().toExternalForm());
			Assert.assertEquals(1,season.getSeasonNumber());
			Assert.assertEquals(show,season.getShow());

	        Episode episode = xmlSource.getEpisode(dir,episodeFile,season, 1);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(1,episode.getEpisodeNumber());
	        Assert.assertEquals(784857,episode.getEpisodeId());
	        Assert.assertEquals(1,episode.getShowEpisodeNumber());
	        Assert.assertEquals("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/eureka/pilot/episode/784857/summary.html",episode.getSummaryUrl().toExternalForm());
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
	        Assert.assertFalse(episode.isSpecial());

	        episodeFile = new File(eurekaDir,"1x02 - blah.avi");
	        episode = xmlSource.getEpisode(dir,episodeFile,season, 2);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(2,episode.getEpisodeNumber());
	        Assert.assertEquals(800578,episode.getEpisodeId());
	        Assert.assertEquals(2,episode.getShowEpisodeNumber());
	        Assert.assertEquals("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html",episode.getSummaryUrl().toExternalForm());
	        Assert.assertEquals("Many Happy Returns",episode.getTitle());
	        Assert.assertEquals("2006-10-11",df.format(episode.getDate()));
	        Assert.assertFalse(episode.isSpecial());

	        episodeFile = new File(eurekaDir,"2x02 - blah.avi");
			season = xmlSource.getSeason(dir,episodeFile,show, 2);
			Assert.assertEquals("http://www.tv.com/show/58448/episode_guide.html?printable=2",season.getDetailedUrl().toExternalForm());
			Assert.assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=2",season.getListingUrl().toExternalForm());
			Assert.assertEquals(2,season.getSeasonNumber());
			Assert.assertEquals(show,season.getShow());

	        episode = xmlSource.getEpisode(dir,episodeFile,season, 2);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(2,episode.getEpisodeNumber());
	        Assert.assertEquals(800578,episode.getEpisodeId());
	        Assert.assertEquals(13,episode.getShowEpisodeNumber());
	        Assert.assertFalse(episode.isSpecial());
	        Assert.assertEquals("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html",episode.getSummaryUrl().toExternalForm());
	        Assert.assertEquals("Phoenix Rising",episode.getTitle());
	        Assert.assertEquals("2007-07-10",df.format(episode.getDate()));

	        episodeFile = new File(eurekaDir,"000 - blah.avi");
	        episode = xmlSource.getSpecial(dir,episodeFile,season, 0);
	        Assert.assertNotNull(episode);
	        Assert.assertEquals(0,episode.getEpisodeNumber());
	        Assert.assertEquals(800578,episode.getEpisodeId());
	        Assert.assertEquals(-1,episode.getShowEpisodeNumber());
	        Assert.assertEquals("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.",episode.getSummary());
	        Assert.assertEquals("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html",episode.getSummaryUrl().toExternalForm());
	        Assert.assertEquals("Countdown to the Premiere",episode.getTitle());
	        Assert.assertEquals("2007-07-09",df.format(episode.getDate()));
	        Assert.assertTrue(episode.isSpecial());

	        File filmFile1 = new File(dir,"The Usual Suspects part1.avi");
	        Film film = xmlSource.getFilm(dir, filmFile1, "114814");
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
			Assert.assertEquals("xbmc-metadata.themoviedb.org",film.getSourceId());
			Assert.assertEquals("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.",film.getSummary());
			Assert.assertEquals("The Usual Suspects",film.getTitle());
			Assert.assertEquals(1,film.getWriters().size());
			Assert.assertEquals("Christopher McQuarrie",film.getWriters().get(0));

		} finally {
			FileHelper.deleteDir(dir);
		}
	}

}

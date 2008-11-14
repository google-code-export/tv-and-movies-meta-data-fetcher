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
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.source.TVCOMSource;
import org.xml.sax.InputSource;

public class TestXMLStore extends XMLTestCase {

	private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final int SHOW_ID = 58448;

	public void testReadOfOldXML() throws Exception {
		XMLStore xmlSource = new XMLStore();

		File dir = FileHelper.createTmpDir();
		try {
			File eurekaDir = new File(dir, "Eureka");
			eurekaDir.mkdir();
			FileHelper.copy(Data.class.getResourceAsStream("old-source.xml"),
					new File(eurekaDir, ".series.xml"));
			Show show = xmlSource.getShow(eurekaDir, SHOW_ID);
			assertNull(show);			
		} finally {
			FileHelper.deleteDir(dir);
		}

		// showGenres = show.getGenres();
	}
	
	public void testReadXML() throws Exception {
		XMLStore xmlSource = new XMLStore();

		File dir = FileHelper.createTmpDir();
		try {
			File eurekaDir = new File(dir, "Eureka");
			eurekaDir.mkdir();
			FileHelper.copy(Data.class.getResourceAsStream("eureka.xml"),
					new File(eurekaDir, ".show.xml"));
			Show show = xmlSource.getShow(eurekaDir, SHOW_ID);
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
			assertEquals(eurekaDir.getAbsolutePath(), show.getShowDirectory().getAbsolutePath());
			assertEquals(58448, show.getShowId());
			assertEquals("http://www.tv.com/show/58448/summary.html", show.getShowURL().toExternalForm());
						
			Season season = xmlSource.getSeason(show, 1);
			assertEquals("http://www.tv.com/show/58448/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());			
			assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=1",season.getListingUrl().toExternalForm());
			assertEquals(1,season.getSeasonNumber());
	        assertEquals(show,season.getShow());	  
	        
	        Episode episode = xmlSource.getEpisode(season, 1);	 
	        assertNotNull(episode);
	        assertEquals(1,episode.getEpisodeNumber());
	        assertEquals(784857,episode.getEpisodeId());
	        assertEquals("1",episode.getEpisodeSiteId());
	        assertEquals("001",episode.getProductionCode());
	        assertNull(episode.getSpecialName());
	        assertEquals("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.",episode.getSummary());
	        assertEquals("http://www.tv.com/eureka/pilot/episode/784857/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Pilot",episode.getTitle());
	        assertEquals(1,episode.getTotalNumber());
	        assertEquals("2006-10-10",df.format(episode.getAirDate()));
	        
	        episode = xmlSource.getEpisode(season, 2);	 
	        assertNotNull(episode);
	        assertEquals(2,episode.getEpisodeNumber());
	        assertEquals(800578,episode.getEpisodeId());
	        assertEquals("2",episode.getEpisodeSiteId());
	        assertEquals("002",episode.getProductionCode());
	        assertNull(episode.getSpecialName());
	        assertEquals("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!",episode.getSummary());
	        assertEquals("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Many Happy Returns",episode.getTitle());
	        assertEquals(2,episode.getTotalNumber());
	        assertEquals("2006-10-11",df.format(episode.getAirDate()));
	        
			season = xmlSource.getSeason(show, 2);
			assertEquals("http://www.tv.com/show/58448/episode_guide.html?printable=2",season.getDetailedUrl().toExternalForm());			
			assertEquals("http://www.tv.com/show/58448/episode_listings.html?season=2",season.getListingUrl().toExternalForm());
			assertEquals(2,season.getSeasonNumber());
	        assertEquals(show,season.getShow());	
	        
	        episode = xmlSource.getEpisode(season, 2);	 
	        assertNotNull(episode);
	        assertEquals(2,episode.getEpisodeNumber());
	        assertEquals(800578,episode.getEpisodeId());
	        assertEquals("13",episode.getEpisodeSiteId());
	        assertEquals("013",episode.getProductionCode());
	        assertNull(episode.getSpecialName());
	        assertEquals("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.",episode.getSummary());
	        assertEquals("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Phoenix Rising",episode.getTitle());
	        assertEquals(13,episode.getTotalNumber());
	        assertEquals("2007-07-10",df.format(episode.getAirDate()));
	        
	        episode = xmlSource.getSpecial(season, 0);	 
	        assertNotNull(episode);
	        assertEquals(0,episode.getEpisodeNumber());
	        assertEquals(800578,episode.getEpisodeId());
	        assertEquals("Special",episode.getEpisodeSiteId());
	        assertEquals("200",episode.getProductionCode());
	        assertEquals("Special",episode.getSpecialName());
	        assertEquals("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.",episode.getSummary());
	        assertEquals("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html",episode.getSummaryUrl().toExternalForm());
	        assertEquals("Countdown to the Premiere",episode.getTitle());
	        assertEquals(-1,episode.getTotalNumber());
	        assertEquals("2007-07-09",df.format(episode.getAirDate()));
			
			
		} finally {
			FileHelper.deleteDir(dir);
		}		
	}
	
	public void testCacheShow() throws Exception {
		XMLStore xmlSource = new XMLStore();
		
		File dir = FileHelper.createTmpDir();
		try {
			File eurekaDir = new File(dir, "Eureka");
			eurekaDir.mkdir();
			
			Show show = createShow(eurekaDir);
			xmlSource.cacheShow(show);		
			
			Season season = new Season(show,1);
			season.setDetailedUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_guide.html?printable=1"));
			season.setListingUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_listings.html?season=1"));
			show.addSeason(season);		
			xmlSource.cacheSeason(season);			
			Episode episode1 = new Episode(1,season);
			episode1.setAirDate(df.parse("2006-10-10"));
			episode1.setProductionCode("001");
			episode1.setSiteId("1");
			episode1.setSpecial(false);
			episode1.setSpecialName(null);
			episode1.setSummary("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.");
			episode1.setSummaryUrl(new URL("http://www.tv.com/eureka/pilot/episode/784857/summary.html"));
			episode1.setTitle("Pilot");
			episode1.setTotalNumber(1);
			episode1.setRating(1);
			episode1.setDirectors(createLinks(new Link[]{new Link("Harry","http://test/")}));
			episode1.setWriters(createLinks(new Link[]{new Link("Write a lot","http://test/a")}));
			episode1.setGuestStars(createLinks(new Link[]{new Link("sally","http://test/sally"),new Link("Cedric","http://test/cedric")}));
			episode1.setEpisodeId(784857);
			season.addEpisode(episode1);
			xmlSource.cacheEpisode(episode1);
			
			Episode episode2 = new Episode(2,season);
			episode2.setAirDate(df.parse("2006-10-11"));
			episode2.setProductionCode("002");
			episode2.setSiteId("2");
			episode2.setSpecial(false);
			episode2.setSpecialName(null);
			episode2.setSummary("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!");
			episode2.setSummaryUrl(new URL("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html"));
			episode2.setTitle("Many Happy Returns");
			episode2.setTotalNumber(2);
			episode2.setRating(9.5F);			
			episode2.setEpisodeId(800578);
			season.addEpisode(episode2);			
			xmlSource.cacheEpisode(episode2);
			
			season = new Season(show,2);
			season.setDetailedUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_guide.html?printable=2"));
			season.setListingUrl(new URL("http://www.tv.com/show/"+SHOW_ID+"/episode_listings.html?season=2"));
			show.addSeason(season);	
			xmlSource.cacheSeason(season);
			
			episode1 = new Episode(2,season);
			episode1.setAirDate(df.parse("2007-7-10"));
			episode1.setProductionCode("013");
			episode1.setSiteId("13");
			episode1.setSpecial(false);
			episode1.setSpecialName(null);
			episode1.setSummary("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.");
			episode1.setSummaryUrl(new URL("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html"));
			episode1.setTitle("Phoenix Rising");
			episode1.setTotalNumber(13);
			episode1.setEpisodeId(800578);
			episode1.setRating(0.4F);
			season.addEpisode(episode1);			
			xmlSource.cacheEpisode(episode1);
			
			Episode special1 = new Episode(0,season);
			special1.setAirDate(df.parse("2007-7-09"));
			special1.setProductionCode("200");
			special1.setSiteId("Special");
			special1.setSpecial(true);
			special1.setSpecialName("Special");
			special1.setSummary("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.");
			special1.setSummaryUrl(new URL("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html"));
			special1.setTitle("Countdown to the Premiere");
			special1.setTotalNumber(13);
			special1.setRating(0.4F);
			special1.setEpisodeId(800578);
			special1.setDirectors(createLinks(new Link[]{new Link("JP","http://test/")}));
			special1.setWriters(createLinks(new Link[]{new Link("Write a lot","http://test/a"),new Link("Write a little","http://test/b")}));
			special1.setGuestStars(createLinks(new Link[]{new Link("bob","http://test/bob"),new Link("Write a little","http://test/fred")}));
						
			season.addSepcial(special1);			
			xmlSource.cacheEpisode(special1);
			
			File actualFile = new File(eurekaDir,".show.xml");
//			FileHelper.displayFile(actualFile,System.out);
			assertXMLEqual(new InputSource(Data.class.getResourceAsStream("eureka.xml")), new InputSource(new FileInputStream(actualFile)));
			
		} finally {
			FileHelper.deleteDir(dir);
		}			
	}
	
	public List<Link>createLinks(Link[] links) {
		List<Link> result = new ArrayList<Link>();
		for (Link link : links ) {
			result.add(link);
		}
		return result;
	}

	private Show createShow(File eurekaDir) throws MalformedURLException {
		Show show = new Show(eurekaDir,SHOW_ID);
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

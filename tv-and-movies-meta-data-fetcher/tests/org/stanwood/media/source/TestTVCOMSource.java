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

import junit.framework.TestCase;

import org.stanwood.media.FileHelper;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;

import au.id.jericho.lib.html.Source;

/**
 * Used to test the {@link TVCOMSource} class.
 */
public class TestTVCOMSource extends TestCase {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final String SHOW_ID_EUREKA = "58448";
	private static final String SHOW_ID_HEROES = "17552";
	
	/**
	 * Test that the show details are read correctly.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testEurekaReadShow() throws Exception {
		
		TVCOMSource source = getTVCOMSource(SHOW_ID_EUREKA);		
		Show show = source.getShow( SHOW_ID_EUREKA);
		
		assertEquals("tvcom",show.getSourceId());
		assertEquals("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC Universal Cable Studio and filmed in Vancouver, British Columbia, Canada.",show.getLongSummary());
		assertEquals("Eureka",show.getName());
		assertEquals("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC...",show.getShortSummary());
		assertEquals(SHOW_ID_EUREKA,show.getShowId());
		assertEquals("http://image.com.com/tv/images/content_headers/program_new/58448.jpg",show.getImageURL().toExternalForm());				
		assertEquals("http://www.tv.com/show/58448/summary.html",show.getShowURL().toExternalForm());
		assertEquals(2,show.getGenres().size());
		assertEquals("Science-Fiction",show.getGenres().get(0));
		assertEquals("Drama",show.getGenres().get(1));
		
	}
	
	/**
	 * Test that the season details are read correctly.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testEurekaReadSeasons() throws Exception {
		TVCOMSource source = getTVCOMSource(SHOW_ID_EUREKA);		
		Show show = source.getShow( SHOW_ID_EUREKA);
		
		Season season = source.getSeason(show, 1);
		assertEquals("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_listings.html?season=1",season.getListingUrl().toExternalForm());
		assertEquals("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());
		assertEquals(1,season.getSeasonNumber());
		
		season = source.getSeason(show, 2);
		assertEquals("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_listings.html?season=2",season.getListingUrl().toExternalForm());
		assertEquals("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());
		assertEquals(2,season.getSeasonNumber());
		
		season = source.getSeason(show, 3);
		assertEquals("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_listings.html?season=3",season.getListingUrl().toExternalForm());
		assertEquals("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());
		assertEquals(3,season.getSeasonNumber());
		
		season = source.getSeason(show, 4);
		assertNull(season);		
	}

	/**
	 * Test that the episode details are read correctly.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testEurekaReadEpisodes() throws Exception {
		TVCOMSource source = getTVCOMSource(SHOW_ID_EUREKA);		
		Show show = source.getShow( SHOW_ID_EUREKA);
		
		Season season = source.getSeason(show, 1);
		Episode episode = source.getEpisode(season, 1);
		assertEquals(1,episode.getEpisodeNumber());
		assertEquals("1",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka. After a strange accident sidelines Eureka's sheriff, Jack Carter takes over the investigation into the mysterious phenomenon that led to the death of a resident. Carter learns about the secret purpose of the town while trying to re-establish a connection with his angry and bitter daughter, Zoe. He develops a friendly working relationship with government liaison Allison Blake and meets some of the more eccentric residents of Eureka.",episode.getSummary());
		assertEquals("Pilot",episode.getTitle());		
		assertEquals("2006-07-18",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/pilot/episode/784857/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(784857,episode.getEpisodeId());
		assertEquals(8.9F,episode.getRating());
		assertEquals(1,episode.getDirectors().size());
		assertEquals("Eric Laneuville",episode.getDirectors().get(0).getTitle());
		assertEquals("http://www.tv.com/eric-laneuville/person/9830/summary.html",episode.getDirectors().get(0).getURL());
		assertEquals(1,episode.getWriters().size());
		assertEquals("Thania St. John",episode.getWriters().get(0).getTitle());
		assertEquals("http://www.tv.com/thania-st.-john/person/426/summary.html",episode.getWriters().get(0).getURL());
		assertEquals(9,episode.getGuestStars().size());
		assertEquals("Ever Carradine",episode.getGuestStars().get(1).getTitle());
		assertEquals("http://www.tv.com/ever-carradine/person/5974/summary.html",episode.getGuestStars().get(1).getURL());
		assertEquals("Christopher Gauthier",episode.getGuestStars().get(2).getTitle());
		assertEquals("http://www.tv.com/christopher-gauthier/person/265916/summary.html",episode.getGuestStars().get(2).getURL());
		assertEquals("Vanya Asher",episode.getGuestStars().get(3).getTitle());
		assertEquals("http://www.tv.com/vanya-asher/person/597981/summary.html",episode.getGuestStars().get(3).getURL());
		assertEquals("Neil Grayston",episode.getGuestStars().get(4).getTitle());
		assertEquals("http://www.tv.com/neil-grayston/person/81764/summary.html",episode.getGuestStars().get(4).getURL());
		assertEquals("Barclay Hope",episode.getGuestStars().get(5).getTitle());
		assertEquals("http://www.tv.com/barclay-hope/person/38709/summary.html",episode.getGuestStars().get(5).getURL());
		assertEquals("David Richmond-Peck",episode.getGuestStars().get(6).getTitle());
		assertEquals("http://www.tv.com/david-richmond-peck/person/168186/summary.html",episode.getGuestStars().get(6).getURL());
		assertEquals("Julius Chapple",episode.getGuestStars().get(7).getTitle());
		assertEquals("http://www.tv.com/julius-chapple/person/267036/summary.html",episode.getGuestStars().get(7).getURL());
		assertEquals("Bridget Hoffman",episode.getGuestStars().get(8).getTitle());
		assertEquals("http://www.tv.com/bridget-hoffman/person/51407/summary.html",episode.getGuestStars().get(8).getURL());
		
		episode = source.getEpisode(season, 2);
		assertEquals(2,episode.getEpisodeNumber());
		assertEquals("2",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!",episode.getSummary());
		assertEquals("Many Happy Returns",episode.getTitle());				
		assertEquals("2006-07-25",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(800578,episode.getEpisodeId());
		
		episode = source.getEpisode(season, 8);
		assertEquals(8,episode.getEpisodeNumber());
		assertEquals("8",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("When the return of Eureka's star pupil causes problems all over town, Stark is forced to make a hard choice. Carter attempts to track down Zoe who has run off with the former resident.",episode.getSummary());
		assertEquals("Right as Raynes",episode.getTitle());		
		assertEquals("2006-09-05",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/right-as-raynes/episode/836518/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(836518,episode.getEpisodeId());
		
		episode = source.getEpisode(season, 12);
		assertEquals(12,episode.getEpisodeNumber());
		assertEquals("12",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("Following an experiment on the artifact, Eureka warps to 2010. However, temporal anomalies begin to cause problems for Eureka, which leads to a shocking revelation.",episode.getSummary());
		assertEquals("Once in a Lifetime",episode.getTitle());		
		assertEquals("2006-10-03",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/once-in-a-lifetime/episode/868398/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(868398,episode.getEpisodeId());
		
		assertNull(source.getEpisode(season, 13));		
		
		season = source.getSeason(show, 2);
		episode = source.getEpisode(season, 1);
		assertEquals(1,episode.getEpisodeNumber());
		assertEquals("13",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.",episode.getSummary());
		assertEquals("Phoenix Rising",episode.getTitle());		
		assertEquals("2007-07-10",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(1038982,episode.getEpisodeId());
		
		episode = source.getEpisode(season, 13);
		assertEquals(13,episode.getEpisodeNumber());
		assertEquals("25",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("Global Dynamics goes into lockdown to protect itself from the now airborne metal-eating bacteria. Stuck inside GD, Sheriff Carter and Stark must work together to save both the town and Kevin, Alison's son.",episode.getSummary());
		assertEquals("A Night at Global Dynamics (2)",episode.getTitle());
		assertEquals("2007-10-02",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/a-night-at-global-dynamics-2/episode/1128794/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(1128794,episode.getEpisodeId());
		
		assertNull(source.getEpisode(season, 14));
		
		season = source.getSeason(show, 3);
		episode = source.getEpisode(season, 8);
		assertEquals(8,episode.getEpisodeNumber());
		assertEquals("33",episode.getEpisodeSiteId());
		assertEquals("",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("As Eva Thorne prepares to seal off the subterranean military complex, Zoe's life is endangered as her body begins to rapidly age.",episode.getSummary());
		assertEquals("From Fear to Eternity",episode.getTitle());		
		assertEquals("2008-09-23",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/eureka/from-fear-to-eternity/episode/1203120/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(1203120,episode.getEpisodeId());
		
		assertNull(source.getEpisode(season, 9));
		assertNull(source.getEpisode(season, 10));
		assertNull(source.getEpisode(season, 11));
		
		season = source.getSeason(show, 4);		
		assertNull("Check that the last season was detected",season);
	}
	
	/**
	 * Test that the show details are read correctly.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testHeroesReadShow() throws Exception {		
		TVCOMSource source = getTVCOMSource(SHOW_ID_HEROES);		
		Show show = source.getShow( SHOW_ID_HEROES);
		
		assertEquals("Heroes is a serial saga about people all over the world discovering that they have superpowers and trying to deal with how this change affects their lives. Some of the superheroes who will be introduced to the viewing audience include Peter Petrelli, an almost 30-something male nurse who suspects he might be able to fly, Isaac Mendez, a 28-year-old junkie who has the ability to paint images of the future when he is high, Niki Sanders, a 33-year-old Las Vegas showgirl who begins seeing strange things in mirrors, Hiro Nakamura, a 24-year-old Japanese comic-book geek who literally makes time stand still, D.L. Hawkins, a 31-year-old inmate who can walk through walls, Matt Parkman, a beat cop who can hear other people's thoughts, and Claire Bennet, a 17-year-old cheerleader who defies death at every turn. As the viewing audience is discovering the nature of each hero's powers, the heroes themselves are discovering what having superpowers means to them as well as the larger picture of where their superpowers come from. Tune in each week to see how these heroes are drawn together by their common interest of evading the series' antagonist who wants to harvest their super-DNA for himself. Their ultimate destiny is nothing less than saving the world! The series will star Greg Grunberg (Alias), Leonard Roberts (Buffy the Vampire Slayer), Milo Ventimiglia (Gilmore Girls), and Hayden Panettiere (Ally McBeal, Guiding Light). Tim Kring (Crossing Jordan, Chicago Hope) is the series' creator. The pilot is set to be directed by Dave Semel (American Dreams, Buffy, the Vampire Slayer, Beverly Hills, 90210). Heroes will be produced by NBC/Universal/Tailwind. Summary revised with help from: space-cowboy.",show.getLongSummary());
		assertEquals("Heroes",show.getName());
		assertEquals("Heroes is a serial saga about people all over the world discovering that they have superpowers and trying to deal with how this change affects their lives. Some of the superheroes who will be introduced to the viewing audience include Peter Petrelli, an almost 30-something male nurse who suspects he might be able to fly, Isaac Mendez, a 28-year-old junkie who has the ability to paint...",show.getShortSummary());
		assertEquals(SHOW_ID_HEROES,show.getShowId());
		assertEquals("http://image.com.com/tv/images/content_headers/program_new/17552.jpg",show.getImageURL().toExternalForm());
		assertEquals("http://www.tv.com/show/17552/summary.html",show.getShowURL().toExternalForm());
		assertEquals(3,show.getGenres().size());
		assertEquals("Drama",show.getGenres().get(0));
		assertEquals("Science-Fiction",show.getGenres().get(1));
		assertEquals("Action/Adventure",show.getGenres().get(2));		
	}
	
	/**
	 * Test that the special episode details are read correctly.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testHeroesSpecials() throws Exception {
		TVCOMSource source = getTVCOMSource(SHOW_ID_HEROES);		
		Show show = source.getShow( SHOW_ID_HEROES);
		
		Season season = source.getSeason(show, 3);
		assertEquals("http://www.tv.com/show/"+SHOW_ID_HEROES+"/episode_listings.html?season=3",season.getListingUrl().toExternalForm());
		assertEquals("http://www.tv.com/show/"+SHOW_ID_HEROES+"/episode_guide.html?printable=1",season.getDetailedUrl().toExternalForm());
		assertEquals(3,season.getSeasonNumber());
		
		Episode episode = source.getEpisode(season, 1);
		assertEquals(1,episode.getEpisodeNumber());
		assertEquals("35",episode.getEpisodeSiteId());
		assertEquals("301",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("After a look into the future, Nathan's shooter is revealed. Matt chases him and winds up in a desert. Hiro receives an important message from his father. Sylar visits Claire. Maya gives Mohinder an idea for his research. Nathan recovers and gets a visit from Linderman.",episode.getSummary());
		assertEquals("(Volume Three: Villains) The Second Coming",episode.getTitle());		
		assertEquals("2008-09-22",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/heroes/volume-three-villains-the-second-coming/episode/1181337/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(1181337,episode.getEpisodeId());
		
		episode = source.getEpisode(season, 2);
		assertEquals(2,episode.getEpisodeNumber());
		assertEquals("36",episode.getEpisodeSiteId());
		assertEquals("302",episode.getProductionCode());
		assertNull(episode.getSpecialName());
		assertEquals("Sylar goes to the Company and inadvertently releases 12 super-powered criminals in a fight with Elle. Future Peter tries to fix his mistake. Angela takes over the company. Claire learns more about her powers. Hiro and Ando travel to Paris to track down the speedster who robbed them. Maya discovers that Mohinder's injection has changed him. Matt meats Usutu, an African with a familiar power.",episode.getSummary());
		assertEquals("The Butterfly Effect",episode.getTitle());		
		assertEquals("2008-09-22",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/heroes/the-butterfly-effect/episode/1196088/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(1196088,episode.getEpisodeId());
		
		episode =  source.getSpecial(season,1); 
		assertEquals(0,episode.getEpisodeNumber());
		assertEquals("Special",episode.getEpisodeSiteId());
		assertEquals("300",episode.getProductionCode());
		assertEquals("Special",episode.getSpecialName());
		assertEquals("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.",episode.getSummary());
		assertEquals("Heroes: Countdown to the Premiere",episode.getTitle());		
		assertEquals("2008-09-22",df.format(episode.getDate()));
		assertEquals("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html",episode.getSummaryUrl().toExternalForm());
		assertEquals(1228258,episode.getEpisodeId());
	}	

	/**
	 * Used to test the searching of TV shows
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testSearch() throws Exception {
		TVCOMSource source = getTVCOMSource("0");
		File dir = FileHelper.createTmpDir("TVShows");
		try {
			File eurekaFile = new File(dir,"Eureka");
			File episodeFile = new File(eurekaFile,"1 01 - Blah.avi");						
			SearchResult result = source.searchForVideoId(Mode.TV_SHOW,episodeFile);
			assertEquals(SHOW_ID_EUREKA,result.getId());
			assertEquals("tvcom",result.getSourceId());			
		}
		finally {
			FileHelper.deleteDir(dir);
		}		
	}	
	
	private TVCOMSource getTVCOMSource(final String showId) {
		TVCOMSource source = new TVCOMSource() {
			@Override
			Source getSource(URL url) throws IOException {
				String strUrl = url.toExternalForm();				
				if (strUrl.equals("http://www.tv.com/show/"+showId+"/summary.html")) {
					return new Source(Data.class.getResource(showId+"-summary.html"));
				}
				else if (strUrl.indexOf("episode_listings.html?season=")!=-1) {
					return new Source(Data.class.getResource(showId+"-"+strUrl.substring(strUrl.lastIndexOf('/')+1).replaceAll("\\?","-")));
				}
				else if (strUrl.indexOf("episode_guide.html?printable=")!=-1) {
					return new Source(Data.class.getResource(showId+"-"+strUrl.substring(strUrl.lastIndexOf('/')+1).replaceAll("\\?","-")));
				}
				else if (strUrl.indexOf("http://www.tv.com/search.php?type=Search&stype=ajax_search")!=-1) {
					return new Source(Data.class.getResource("eureka-search.html"));
				}
				return null;
			}			
		};
		return source;	
	}
	
}

package org.stanwood.media.source.xbmc;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.XMLParser;

/**
 * This test is used to test that TV show information can be correctly retrieved from XBMC sources
 */
public class TestXMBCSourceTVDB extends XBMCAddonTestBase {

	private static final SimpleDateFormat EPISODE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Used to the addon works correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testAddon() throws Exception {
		XBMCAddon addon = getAddonManager().getAddon("metadata.tvdb.com");
		Assert.assertEquals("metadata.tvdb.com", addon.getId());
		Assert.assertEquals("The TVDB", addon.getName());
		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("1.0.8", addon.getVersion());
		Assert.assertEquals("Fetch TV show metadata from TheTVDB.com",addon.getSummary());
		Assert.assertEquals("TheTVDB.com is a TV Scraper. The site is a massive open database that can be modified by anybody and contains full meta data for many shows in different languages. All content and images on the site have been contributed by their users for users and have a high standard or quality. The database schema and website are open source under the GPL.",addon.getDescription());
		Assert.assertTrue(addon.supportsMode(Mode.TV_SHOW));
		Assert.assertFalse(addon.supportsMode(Mode.FILM));

		XBMCScraper scraper = addon.getScraper(Mode.TV_SHOW);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<url>http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes%20(2009)&amp;language=en</url>\n",XMLParser.domToStr(scraper.getCreateSearchUrl("Heroes","2009")));
	}

	/**
	 * Used to test that show information is correctly retrieved from the XBMC source
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testShow() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.tvdb.com");
		source.setParameter("posters", "true");
		SearchResult result = source.searchMedia("Heroes",Mode.TV_SHOW);
		Assert.assertEquals("79501",result.getId());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip", result.getUrl());

		Show show = source.getShow("79501", new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));
		Assert.assertNotNull(show);

		Assert.assertEquals("Heroes is a cult serial saga about people all over the world discovering that they have superpowers and trying to deal with how this change affects their lives. Not only are they discovering what having superpowers means to them but also the larger picture of where those powers come from and what they might mean to the rest of the world.",show.getLongSummary());
		Assert.assertEquals("Heroes is a cult serial saga about people all over the world discovering that they have superpowers...",show.getShortSummary());
		Assert.assertEquals("Heroes",show.getName());
		Assert.assertEquals(4,show.getGenres().size());
		Assert.assertEquals("Action and Adventure",show.getGenres().get(0));
		Assert.assertEquals("Drama",show.getGenres().get(1));
		Assert.assertEquals("Fantasy",show.getGenres().get(2));
		Assert.assertEquals("Science-Fiction",show.getGenres().get(3));
		Assert.assertEquals("79501",show.getShowId());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",show.getShowURL().toExternalForm());
		Assert.assertEquals("xbmc-metadata.tvdb.com",show.getSourceId());
		Assert.assertEquals(show.getExtraInfo().get("episodeGuideURL"), "http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip");
		Assert.assertEquals("http://thetvdb.com/banners/posters/79501-2.jpg",show.getImageURL().toExternalForm());
	}

	/**
	 * Used to test that season information is correctly retrieved from the XBMC source
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testSeason() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.tvdb.com");

		Show show = new Show("79501");
		show.setShowURL(new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));
		show.setSourceId(source.getSourceId());
		Map<String, String> params = new HashMap<String,String>();
		params.put("episodeGuideURL", "http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip");
		show.setExtraInfo(params);

		Season season = source.getSeason(show, 1);
		Assert.assertNotNull(season);
		Assert.assertEquals(1,season.getSeasonNumber());
		Assert.assertEquals(show,season.getShow());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",season.getURL().toExternalForm());

		season = source.getSeason(show, 2);
		Assert.assertNotNull(season);
		Assert.assertEquals(2,season.getSeasonNumber());
		Assert.assertEquals(show,season.getShow());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",season.getURL().toExternalForm());

		season = source.getSeason(show, 3);
		Assert.assertNotNull(season);
		Assert.assertEquals(3,season.getSeasonNumber());
		Assert.assertEquals(show,season.getShow());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",season.getURL().toExternalForm());

		season = source.getSeason(show, 4);
		Assert.assertNotNull(season);
		Assert.assertEquals(4,season.getSeasonNumber());
		Assert.assertEquals(show,season.getShow());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",season.getURL().toExternalForm());

		season = source.getSeason(show, 5);
		Assert.assertNull(season);
	}

	/**
	 * Used to test that episode information is correctly retrieved from the XBMC source
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testEpisode() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.tvdb.com");

		Show show = new Show("79501");
		show.setShowURL(new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));
		show.setSourceId(source.getSourceId());
		Map<String, String> params = new HashMap<String,String>();
		params.put("episodeGuideURL", "http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip");
		show.setExtraInfo(params);

		Season season = new Season(show,1);
		season.setURL(new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));

		Episode episode = source.getEpisode(season, 1);
		Assert.assertNotNull(episode);

		Assert.assertEquals(10,episode.getActors().size());
		Assert.assertEquals("John Prosky",episode.getActors().get(0).getName());
		Assert.assertEquals("",episode.getActors().get(0).getRole());
		Assert.assertEquals("Claudia Difolco",episode.getActors().get(5).getName());
		Assert.assertEquals("",episode.getActors().get(5).getRole());
		Assert.assertEquals("Shishir Kurup",episode.getActors().get(9).getName());
		Assert.assertEquals("",episode.getActors().get(9).getRole());
		Assert.assertEquals(1,episode.getDirectors().size());
		Assert.assertEquals("David Semel",episode.getDirectors().get(0));
		Assert.assertEquals(1,episode.getWriters().size());
		Assert.assertEquals("Tim Kring",episode.getWriters().get(0));
		Assert.assertEquals("2006-09-25",EPISODE_DATE_FORMAT.format(episode.getDate()));
		Assert.assertEquals("308906",episode.getEpisodeId());
		Assert.assertEquals(1,episode.getEpisodeNumber());
		Assert.assertEquals(8.3F,episode.getRating().getRating(),0);
		Assert.assertEquals(1,episode.getRating().getNumberOfVotes());
		Assert.assertEquals(season,episode.getSeason());
		Assert.assertEquals("In this episode, we are introduced to Peter Petrelli, a young man who dreams of flying, and his brother Nathan, a ruthless politician who thinks that Peter is dreaming his life away. Meanwhile, ordinary people from all around the world are starting to suspect that they have abilities beyond those of normal humans. Artist Isaac Mendez believes that he is painting the future, high school cheerleader Claire Bennet is suddenly able to recover from any wound almost instantly, Japanese businessman Hiro Nakamura tries to convince a friend that he can bend space and time, and single mother Niki Sanders starts seeing strange things in mirrors.Upon learning of his father's death, genetics Professor Mohinder Suresh begins to look into his research for a clue to why he was killed. When he follows his father's trail to New York City, however, Mohinder learns that someone else is on the trail of the same research.  Someone who may kill to protect the secrets of the next step in human evolution.",episode.getSummary());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",episode.getUrl().toExternalForm());
		Assert.assertEquals("Genesis",episode.getTitle());
		Assert.assertEquals("http://thetvdb.com/banners/episodes/79501/308906.jpg",episode.getImageURL().toExternalForm());
	}

	/**
	 * Used to test that special episode information is correctly retrieved from the XBMC source
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testSpecial() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.tvdb.com");

		Show show = new Show("79501");
		show.setShowURL(new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));
		show.setSourceId(source.getSourceId());
		Map<String, String> params = new HashMap<String,String>();
		params.put("episodeGuideURL", "http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip");
		show.setExtraInfo(params);

		Season season = new Season(show,3);
		season.setURL(new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));
		Episode special = source.getSpecial(season,1);
		Assert.assertNotNull(special);

		Assert.assertEquals(0,special.getActors().size());
		Assert.assertEquals(0,special.getDirectors().size());
		Assert.assertEquals(0,special.getWriters().size());
		Assert.assertEquals("2008-09-22",EPISODE_DATE_FORMAT.format(special.getDate()));
		Assert.assertEquals("389760",special.getEpisodeId());
		Assert.assertEquals(1,special.getEpisodeNumber());
		Assert.assertEquals(8.0F,special.getRating().getRating(),0);
		Assert.assertEquals(1,special.getRating().getNumberOfVotes());
		Assert.assertEquals("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.",special.getSummary());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",special.getUrl().toExternalForm());
		Assert.assertEquals("Countdown to Season 3",special.getTitle());
		Assert.assertEquals("http://thetvdb.com/banners/episodes/79501/389760.jpg",special.getImageURL().toExternalForm());
		Assert.assertEquals(season,special.getSeason());
	}

	/**
	 * Used to test that we can get a URL from a NFO file
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testNFOFile() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		XBMCAddon addon = getAddonManager().getAddon("metadata.tvdb.com");

		Assert.assertFalse(addon.getScraper(Mode.TV_SHOW).supportsURL(new URL("http://blah")));
		Assert.assertTrue(addon.getScraper(Mode.TV_SHOW).supportsURL(new URL("http://www.thetvdb.com/index.php?tab=series&id=79501")));
	}

	private XBMCSource getXBMCSource(String id) throws SourceException{
		XBMCSource source = new XBMCSource(getAddonManager(),id);
		return source;
	}
}

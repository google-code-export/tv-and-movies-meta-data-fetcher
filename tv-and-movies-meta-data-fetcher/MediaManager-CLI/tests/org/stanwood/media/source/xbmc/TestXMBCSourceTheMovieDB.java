package org.stanwood.media.source.xbmc;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.xml.XMLParser;

/**
 * This test is used to test that film information can be correctly retrieved from XBMC sources
 */
@SuppressWarnings("nls")
public class TestXMBCSourceTheMovieDB extends XBMCAddonTestBase {

	/**
	 * Used to the addon works correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testTVDBAddonDetails() throws Exception {
		XBMCAddon addon = getAddonManager().getAddon("metadata.themoviedb.org");
		Assert.assertEquals("metadata.themoviedb.org", addon.getId());
		Assert.assertEquals("The MovieDB", addon.getName());

		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("1.2.0", addon.getVersion().toString());
		Assert.assertEquals("TMDb Movie Scraper",addon.getSummary());
		Assert.assertEquals("themoviedb.org is a free and open movie database. It's completely user driven by people like you. TMDb is currently used by millions of people every month and with their powerful API, it is also used by many popular media centers like XBMC to retrieve Movie Metadata, Posters and Fanart to enrich the user's experience.",addon.getDescription());
		Assert.assertFalse(addon.supportsMode(Mode.TV_SHOW));
		Assert.assertTrue(addon.supportsMode(Mode.FILM));

		XBMCScraper scraper = addon.getScraper(Mode.FILM);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<url>http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man</url>\n",XMLParser.domToStr(scraper.getCreateSearchUrl("Iron Man","2009")));
	}

	/**
	 * Used to test that the source returns the correct film information
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testSource() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.themoviedb.org");
		SearchResult result = source.searchMedia("Iron Man","",Mode.FILM,1);
		Assert.assertEquals("1726",result.getId());
		Assert.assertEquals("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726", result.getUrl());

		try {
			source.getShow("1726", new URL("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726"),null);
			Assert.fail("Did not detected the exception");
		}
		catch (SourceException e) {
			Assert.assertEquals("Scraper 'metadata.themoviedb.org' is not of type 'TV Shows'",e.getMessage());
		}

		Film film = source.getFilm("1726",new URL("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726"),null);
		Assert.assertNotNull(film);
		Assert.assertEquals(15,film.getActors().size());
		Assert.assertEquals("Robert Downey Jr",film.getActors().get(0).getName());
		Assert.assertEquals("Tony Stark",film.getActors().get(0).getRole());
		Assert.assertEquals("Faran Tahir",film.getActors().get(5).getName());
		Assert.assertEquals("Raza",film.getActors().get(5).getRole());
		Assert.assertEquals("Shaun Toub",film.getActors().get(6).getName());
		Assert.assertEquals("Yinsin",film.getActors().get(6).getRole());
		Assert.assertEquals(1,film.getCertifications().size());
		Assert.assertEquals("mpaa",film.getCertifications().get(0).getType());
		Assert.assertEquals("PG-13",film.getCertifications().get(0).getCertification());
		Assert.assertEquals(0,film.getChapters().size());
		Assert.assertEquals("United States of America",film.getCountry());
		Assert.assertEquals("Tony Stark (Robert Downey Jr.) is a wealthy playboy and talented weapons manufacturer. When he discovers a deadly conspiracy that could destabilize the entire globe, he develops a powerful robotic suit to fight the villians and save the world.",film.getDescription());
		Assert.assertEquals(1,film.getDirectors().size());
		Assert.assertEquals("Jon Favreau",film.getDirectors().get(0));
		Assert.assertEquals("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726",film.getFilmUrl().toExternalForm());
		Assert.assertEquals(5,film.getGenres().size());
		Assert.assertEquals("Action",film.getGenres().get(0));
		Assert.assertEquals("Adventure",film.getGenres().get(1));
		Assert.assertEquals("Fantasy",film.getGenres().get(2));
		Assert.assertEquals("Science Fiction",film.getGenres().get(3));
		Assert.assertEquals("Thriller",film.getGenres().get(4));
		Assert.assertEquals("1726",film.getId());
		Assert.assertEquals("http://cf2.imgobject.com/t/p/original/i0SQvqddrAs1qS7K3M20qVfBmge.jpg",film.getImageURL().toExternalForm());
		Assert.assertEquals("Action",film.getPreferredGenre());
		Assert.assertEquals(8.4F,film.getRating().getRating());
		Assert.assertEquals(82,film.getRating().getNumberOfVotes());
		Assert.assertEquals(XBMCSource.class.getName()+"#metadata.themoviedb.org",film.getSourceId());
		Assert.assertEquals("Tony Stark (Robert Downey Jr.) is a wealthy playboy and talented weapons manufacturer. When he discovers a deadly conspiracy that could destabilize the entire globe, he develops a powerful robotic suit to fight the villians and save the world.",film.getSummary());
		Assert.assertEquals("Iron Man",film.getTitle());
		Assert.assertEquals(2,film.getWriters().size());
		Assert.assertEquals("Art Marcum",film.getWriters().get(0));
		Assert.assertEquals("Matt Holloway",film.getWriters().get(1));

		Calendar cal = Calendar.getInstance();
		cal.setTime(film.getDate());

		Assert.assertEquals(2008,cal.get(Calendar.YEAR));
	}

	/**
	 * Used to test character encodings
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testEncodings() throws Exception {
		XBMCSource source = getXBMCSource("metadata.themoviedb.org");
		SearchResult result = source.searchMedia("Dude Where’s My Car","",Mode.FILM,1);
		Assert.assertEquals("8859",result.getId());
		Assert.assertEquals("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/8859", result.getUrl());

		Film film = source.getFilm("8859",new URL("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/8859"),null);
		Assert.assertEquals("Dude, Where’s My Car?",film.getTitle());
	}

	/**
	 * Used to test that the scrapers applyParams method works correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testParams() throws Exception {
		XBMCAddon addon = getAddonManager().getAddon("metadata.themoviedb.org");
		XBMCScraper scraper = addon.getScraper(Mode.FILM);
		Map<Integer, String> params = new HashMap<Integer,String>();
		params.put(1, "test");
		String output = "$$1";
		output = scraper.applyParams(output, params);

		output = "aaa $$1 ccc $$2 eee $$3";
		params.put(1, "bbbbb bb");
		params.put(2, "ddd");
		params.put(3, "fff");
		output = scraper.applyParams(output, params);
		Assert.assertEquals("aaa bbbbb bb ccc ddd eee fff",output);
	}

	/**
	 * Used to test that we can get a URL from a NFO file
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testNFOFile() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		XBMCAddon addon = getAddonManager().getAddon("metadata.themoviedb.org");

		Assert.assertFalse(addon.getScraper(Mode.TV_SHOW).supportsURL(new URL("http://blah")));
		Assert.assertTrue(addon.getScraper(Mode.TV_SHOW).supportsURL(new URL("http://www.imdb.com/title/tt0371746/")));
	}

	private XBMCSource getXBMCSource(String id) throws SourceException{
		XBMCAddon addon = getAddonManager().getAddon(id);
		XBMCSource source = new XBMCSource(new XBMCSourceInfo(getAddonManager(),addon),getAddonManager(),id);
		return source;
	}

}

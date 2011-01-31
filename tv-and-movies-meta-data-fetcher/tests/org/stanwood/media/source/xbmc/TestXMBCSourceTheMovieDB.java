package org.stanwood.media.source.xbmc;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.XMLParser;

public class TestXMBCSourceTheMovieDB extends XBMCAddonTestBase {

	@Test
	public void testTVDBAddonDetails() throws Exception {
		XBMCAddon addon = getAddonManager().getAddon("metadata.themoviedb.org");
		Assert.assertEquals("metadata.themoviedb.org", addon.getId());
		Assert.assertEquals("The MovieDB", addon.getName());

		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("1.2.0", addon.getVersion());
		Assert.assertEquals("TMDb Movie Scraper",addon.getSummary());
		Assert.assertEquals("themoviedb.org is a free and open movie database. It's completely user driven by people like you. TMDb is currently used by millions of people every month and with their powerful API, it is also used by many popular media centers like XBMC to retrieve Movie Metadata, Posters and Fanart to enrich the user's experience.",addon.getDescription());
		Assert.assertFalse(addon.supportsMode(Mode.TV_SHOW));
		Assert.assertTrue(addon.supportsMode(Mode.FILM));

		XBMCScraper scraper = addon.getScraper(Mode.FILM);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<url>http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man</url>\n",XMLParser.domToStr(scraper.getCreateSearchUrl("Iron Man","2009")));
	}

	@Test
	public void testSource() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.themoviedb.org");
		SearchResult result = source.searchMedia("Iron Man",Mode.FILM);
		Assert.assertEquals("1726",result.getId());
		Assert.assertEquals("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726", result.getUrl());

		try {
			Show show = source.getShow("1726", new URL("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726"));
		}
		catch (SourceException e) {
			Assert.assertEquals("Scraper 'metadata.themoviedb.org' is not of type 'TV Shows'",e.getMessage());
		}

		Film film = source.getFilm("1726",new URL("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726"));
		Assert.assertNotNull(film);
		Assert.assertEquals(14,film.getActors().size());
		Assert.assertEquals("Robert Downey Jr",film.getActors().get(0).getName());
		Assert.assertEquals("Tony Stark",film.getActors().get(0).getRole());
		Assert.assertEquals("Faran Tahir",film.getActors().get(5).getName());
		Assert.assertEquals("Raza",film.getActors().get(5).getRole());
		Assert.assertEquals("Shaun Toub",film.getActors().get(13).getName());
		Assert.assertEquals("Yinsin",film.getActors().get(13).getRole());
		Assert.assertEquals(1,film.getCertifications().size());
		Assert.assertEquals("mpaa",film.getCertifications().get(0).getType());
		Assert.assertEquals("PG-13",film.getCertifications().get(0).getCertification());
		Assert.assertEquals(0,film.getChapters().size());
		Assert.assertEquals("United States of America",film.getCountry());
		Assert.assertEquals("After escaping from kidnappers using makeshift power armor, an ultrarich inventor and weapons maker turns his creation into a force for good by using it to fight crime. But his skills are stretched to the limit when he must face the evil Iron Monger.",film.getDescription());
		Assert.assertEquals(1,film.getDirectors().size());
		Assert.assertEquals("Jon Favreau",film.getDirectors().get(0));
		Assert.assertEquals("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726",film.getFilmUrl().toExternalForm());
		Assert.assertEquals(6,film.getGenres().size());
		Assert.assertEquals("Action",film.getGenres().get(0));
		Assert.assertEquals("Adventure",film.getGenres().get(1));
		Assert.assertEquals("Drama",film.getGenres().get(2));
		Assert.assertEquals("Fantasy",film.getGenres().get(3));
		Assert.assertEquals("Science Fiction",film.getGenres().get(4));
		Assert.assertEquals("Thriller",film.getGenres().get(5));
		Assert.assertEquals("1726",film.getId());
		Assert.assertEquals("http://cf1.themoviedb.org/posters/eae/4bc912a2017a3c57fe006eae/iron-man-original.jpg",film.getImageURL().toExternalForm());
		Assert.assertEquals("Action",film.getPreferredGenre());
		Assert.assertEquals(8.4F,film.getRating().getRating());
		Assert.assertEquals(59,film.getRating().getNumberOfVotes());
		Assert.assertEquals("xbmc-metadata.themoviedb.org",film.getSourceId());
		Assert.assertEquals("After escaping from kidnappers using makeshift power armor, an ultrarich inventor and weapons maker turns his creation into a force for good by using it to fight crime. But his skills are stretched to the limit when he must face the evil Iron Monger.",film.getSummary());
		Assert.assertEquals("Iron Man",film.getTitle());
		Assert.assertEquals(2,film.getWriters().size());
		Assert.assertEquals("Art Marcum",film.getWriters().get(0));
		Assert.assertEquals("Matt Holloway",film.getWriters().get(1));

		Calendar cal = Calendar.getInstance();
		cal.setTime(film.getDate());

		Assert.assertEquals(2008,cal.get(Calendar.YEAR));
	}

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

	private XBMCSource getXBMCSource(String id) throws SourceException{
		XBMCSource source = new XBMCSource(getAddonManager(),id);
		return source;
	}

}

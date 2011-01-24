package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.IMDBSource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCScraper;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.XMLParser;
import org.w3c.dom.Document;

public class TestXMBCScraperIMDB {
	
	private static File tmpDir;
	private static XMBCAddonManager addonManager;

	/**
	 * Used to setup the scraper for use within the test
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@BeforeClass
	public static void setupTestFile() throws Exception {
		tmpDir = FileHelper.createTmpDir("xbmc");
		FileHelper.unzip(TestXMBCScraperIMDB.class.getResourceAsStream("xbmc-addons.zip"),tmpDir);
		addonManager = new XMBCAddonManager(new File(tmpDir,"addons"),Locale.ENGLISH);	
	}
	
	/**
	 * Used to clean up after the tests in the class have finished
	 */
	@AfterClass
	public static void cleanup() {
		FileHelper.deleteDir(tmpDir);
	}
	
	/**
	 * Used to test that the correct search URL is found
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@Test
	public void testURL() throws Exception {															
//		String url = scraper.getCreateSearchUrl("Iron Man");
//		Assert.assertEquals("Check url", "<url>http://www.tv.com/search.php?type=Search&amp;stype=ajax_search&amp;qs=Eureka&amp;search_type=program&amp;pg_results=0&amp;sort=</url>",url);				
	}
	
	/**
	 * Used to test that the correct mode is read by the scraper
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@Test	
	public void testMode() throws Exception {
//		Assert.assertEquals(Mode.FILM, scraper.getMode());
	}
	
	/**
	 * Used to test that the correct result are scraped from the html results of doing a search
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@Test
	public void testGetSearchResults() throws Exception {
//		String html = FileHelper.readFileContents(Data.class.getResourceAsStream("xbmc-eureka-search.html"));		
//		String results = scraper.getGetSearchResults(html);
//		Assert.assertEquals("", results);
	}
	
	@Test
	public void testIMDBAddonDetails() throws Exception {
		XBMCAddon addon = addonManager.getAddon("metadata.common.imdb.com");
		Assert.assertEquals("metadata.common.imdb.com", addon.getId());
		Assert.assertEquals("IMDB common scraper functions", addon.getName());
		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("2.0.7", addon.getVersion());
		Assert.assertEquals("IMDB Scraper Library",addon.getSummary());
		Assert.assertEquals("Download Movie information from www.imdb.com",addon.getDescription());
	}
		
	@Test
	public void testTVDBAddonDetails() throws Exception {
		XBMCAddon addon = addonManager.getAddon("metadata.tvdb.com");
		Assert.assertEquals("metadata.tvdb.com", addon.getId());
		Assert.assertEquals("The TVDB", addon.getName());
		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("1.0.8", addon.getVersion());
		Assert.assertEquals("Fetch TV show metadata from TheTVDB.com",addon.getSummary());
		Assert.assertEquals("TheTVDB.com is a TV Scraper. The site is a massive open database that can be modified by anybody and contains full meta data for many shows in different languages. All content and images on the site have been contributed by their users for users and have a high standard or quality. The database schema and website are open source under the GPL.",addon.getDescription());
		
		XBMCScraper scraper = addon.getScraper();
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<url>http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes%20(2009)&amp;language=en</url>\n",XMLParser.domToStr(scraper.getCreateSearchUrl("Heroes","2009")));					
	}	
	
	@Test
	public void testSource() throws Exception {
		XBMCSource source = getXBMCSource("metadata.tvdb.com");
		SearchResult result = source.searchForTvShow("Heroes");
		Assert.assertEquals("79501",result.getId());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip", result.getUrl());
	}
	
	private XBMCSource getXBMCSource(String id) throws SourceException{
		XBMCSource source = new XBMCSource(addonManager,id) {
			@Override
			String getSource(URL url) throws IOException {
				String strUrl = url.toExternalForm();				
				if (strUrl.contains("GetSeries")) {
					String file = "tvdb-search-heroes.html";
					String contents = FileHelper.readFileContents(Data.class.getResourceAsStream(file));
					return contents;
				}				
				return null;
			}
		};
		return source;
	}

}

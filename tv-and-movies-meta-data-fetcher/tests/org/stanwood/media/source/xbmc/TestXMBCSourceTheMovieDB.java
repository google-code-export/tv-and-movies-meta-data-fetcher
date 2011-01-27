package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.XMLParser;

public class TestXMBCSourceTheMovieDB extends XBMCAddonTestBase {
	
	private static final Pattern SERIES_PATTERN = Pattern.compile(".*thetvdb.*/series/(\\d+)/all.*");	
			
	@Test
	public void testTVDBAddonDetails() throws Exception {
		XBMCAddon addon = getAddonManager().getAddon("metadata.themoviedb.org");
		Assert.assertEquals("metadata.themoviedb.org", addon.getId());
		Assert.assertEquals("The MovieDB", addon.getName());
		
		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("1.2.0", addon.getVersion());
		Assert.assertEquals("TMDb Movie Scraper",addon.getSummary());
		Assert.assertEquals("themoviedb.org is a free and open movie database. It's completely user driven by people like you. TMDb is currently used by millions of people every month and with their powerful API, it is also used by many popular media centers like XBMC to retrieve Movie Metadata, Posters and Fanart to enrich the user's experience.",addon.getDescription());
		Assert.assertEquals(Mode.FILM,addon.getMode());
		
		XBMCScraper scraper = addon.getScraper();
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<url>http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man</url>\n",XMLParser.domToStr(scraper.getCreateSearchUrl("Iron Man","2009")));
	}	
	
	@Test
	public void testSource() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.themoviedb.org");
		SearchResult result = source.searchMedia("Iron Man");
		Assert.assertEquals("79501",result.getId());
		Assert.assertEquals("http://www.thethemoviedb.org/api/1D62F2F90030C444/series/79501/all/en.zip", result.getUrl());			
		
		Show show = source.getShow("79501", new URL("http://www.thethemoviedb.org/api/1D62F2F90030C444/series/79501/all/en.zip"));		
	}
	
	private XBMCSource getXBMCSource(String id) throws SourceException{
		XBMCSource source = new XBMCSource(getAddonManager(),id) {
			@Override
			InputStream getSource(URL url) throws IOException {
				String strUrl = url.toExternalForm();		
				if (strUrl.contains("Movie.search")) {
					String file = "themoviedb-search-iron-man.html";
					return Data.class.getResourceAsStream(file);
				}				
				else {
					Matcher m = SERIES_PATTERN.matcher(strUrl);
					if (m.matches()) {
						
						return new ZipInputStream(Data.class.getResourceAsStream("tvdb-series-"+m.group(1)+".zip"));						
					}
				}
				
				throw new IOException("Unable to find test data for url: " + url);				
			}
		};
		return source;
	}

}

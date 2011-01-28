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

public class TestXMBCSourceTVDB extends XBMCAddonTestBase {
	
	private static final Pattern SERIES_PATTERN = Pattern.compile(".*thetvdb.*/series/(\\d+)/all.*");
		
	@Test
	public void testTVDBAddonDetails() throws Exception {
		XBMCAddon addon = getAddonManager().getAddon("metadata.tvdb.com");
		Assert.assertEquals("metadata.tvdb.com", addon.getId());
		Assert.assertEquals("The TVDB", addon.getName());
		Assert.assertEquals("Team XBMC", addon.getProviderName());
		Assert.assertEquals("1.0.8", addon.getVersion());
		Assert.assertEquals("Fetch TV show metadata from TheTVDB.com",addon.getSummary());
		Assert.assertEquals("TheTVDB.com is a TV Scraper. The site is a massive open database that can be modified by anybody and contains full meta data for many shows in different languages. All content and images on the site have been contributed by their users for users and have a high standard or quality. The database schema and website are open source under the GPL.",addon.getDescription());
		Assert.assertTrue(addon.supportsMode(Mode.TV_SHOW));
		Assert.assertFalse(addon.supportsMode(Mode.FILM));
		
		XBMCScraper scraper = addon.getScraper();		
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<url>http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes%20(2009)&amp;language=en</url>\n",XMLParser.domToStr(scraper.getCreateSearchUrl("Heroes","2009")));					
	}	
	
	@Test
	public void testSource() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		XBMCSource source = getXBMCSource("metadata.tvdb.com");
		SearchResult result = source.searchMedia("Heroes");
		Assert.assertEquals("79501",result.getId());
		Assert.assertEquals("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip", result.getUrl());			
		
		Show show = source.getShow("79501", new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"));
		Assert.assertNotNull(show);
		Assert.assertEquals("http://thetvdb.com/banners/posters/79501-2.jpg",show.getImageURL().toExternalForm());
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
		
	}
	
	private XBMCSource getXBMCSource(String id) throws SourceException{
		XBMCSource source = new XBMCSource(getAddonManager(),id) {
			@Override
			InputStream getSource(URL url) throws IOException {
				String strUrl = url.toExternalForm();		
				if (strUrl.contains("GetSeries")) {
					String file = "tvdb-search-heroes.html";
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

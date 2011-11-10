package org.stanwood.media.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test that the class {@link ShowSearcher} works as expected
 */
@SuppressWarnings("nls")
public class TestShowSearcher {

	private Map<File,String> patterns = new HashMap<File,String>();

	/**
	 * Used to test the show searcher
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testSearch() throws Exception {
		File filmsDir = createTVShows();
		try {
			final List<TSearchDetails>names = new ArrayList<TSearchDetails>();
			ShowSearcher searcher = new ShowSearcher() {
				@Override
				protected SearchResult doSearch(File mediaFile,String name,String year,Integer part,boolean useSources) throws MalformedURLException,
						IOException, SourceException {
					names.add(new TSearchDetails(mediaFile,name,year,part));
					return null;
				}
			};
			for (File mediaFile : FileHelper.listFiles(filmsDir)) {
				String pattern = patterns.get(mediaFile);
				Assert.assertNotNull(pattern);
				searcher.search(mediaFile, TestFilmSearcher.getMediaDir(filmsDir,pattern,Mode.TV_SHOW),true);
			}

			Collections.sort(names,new Comparator<TSearchDetails>() {
				@Override
				public int compare(TSearchDetails o1, TSearchDetails o2) {
					return o1.getOriginalFile().getName().compareTo(o2.getOriginalFile().getName());
				}
			});
//			Assert.assertEquals(14,names.size());
			int index = 0;
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("A Show",null,names.get(index++));
			assertSearchDetails("A TV Show .",null,names.get(index++));
			assertSearchDetails("A TV Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			assertSearchDetails("Show",null,names.get(index++));
			index++;
//			assertSearchDetails("A TV Show",null,names.get(index++));
			index++;
//			assertSearchDetails("A TV Show",null,names.get(index++));
		}
		finally {
			FileHelper.delete(filmsDir);
		}
	}

	private void assertSearchDetails(String expectedTerm,String expectedYear,TSearchDetails actual) {
		Assert.assertEquals("Did not extract the correct term from: " + actual.getOriginalFile().getAbsolutePath(),expectedTerm,actual.getTerm());
		if (expectedYear == null) {
			Assert.assertNull("Got year when did not expect one: " + actual.getOriginalFile().getAbsolutePath(),actual.getYear());
		}
		else {
			Assert.assertEquals("Did not extract the correct year from: " + actual.getOriginalFile().getAbsolutePath(),expectedYear,actual.getYear());
		}
	}


	private File createTVShows() throws Exception {
		File tmpDir = FileHelper.createTmpDir("tv-shows");
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = TestFilmSearcher.class.getResourceAsStream("show-names.txt");
			br = new BufferedReader(new InputStreamReader(is));

			String strLine = null;
		    while ((strLine = br.readLine()) != null)   {
		    	int pos = strLine.indexOf(",");
		    	if (!strLine.startsWith("#")) {
			    	String fileName = strLine.substring(pos+1);
			    	String pattern =  strLine.substring(0,pos);

			    	File f = new File(tmpDir,fileName);
			    	patterns.put(f,pattern);
			    	if (!f.getParentFile().exists()) {
			    		if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
			    			throw new IOException("Unable to create dir: " + f.getParentFile().getAbsolutePath());
			    		}
			    	}

			    	if (!f.createNewFile() && !f.exists()) {
			    		throw new IOException("Unable to create file: " + f.getAbsolutePath());
			    	}
		    	}
		    }
		}
		finally {
			if (br!=null) {
				br.close();
				br = null;
			}
			if (is!=null) {
				is.close();
				is = null;
			}
		}
		return tmpDir;
	}
}

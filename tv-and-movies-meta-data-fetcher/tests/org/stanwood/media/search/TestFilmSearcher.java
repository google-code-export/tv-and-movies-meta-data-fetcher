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
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.FileHelper;

public class TestFilmSearcher {

	@Test
	public void testSearch() throws Exception {
		File filmsDir = createFilmFiles();
		try {
			final List<TSearchDetails>names = new ArrayList<TSearchDetails>();
			FilmSearcher f = new FilmSearcher() {
				@Override
				protected SearchResult doSearch(File mediaFile,String name,String year) throws MalformedURLException,
						IOException, SourceException {
					names.add(new TSearchDetails(mediaFile,name,year));
					return null;
				}
			};
			for (File mediaFile : FileHelper.listFiles(filmsDir)) {
				f.search(mediaFile, filmsDir, "%t.%x");
			}

			Collections.sort(names,new Comparator<TSearchDetails>() {
				@Override
				public int compare(TSearchDetails o1, TSearchDetails o2) {
					return o1.getOriginalFile().getName().compareTo(o2.getOriginalFile().getName());
				}
			});
			Assert.assertEquals(45,names.size());
			int index = 0;
			assertSearchDetails("A Movie",null,names.get(index++));
			assertSearchDetails("\"Movie\"",null,names.get(index++));
			assertSearchDetails("A movie",null,names.get(index++));
			assertSearchDetails("blah - Movie","1995",names.get(index++));
			assertSearchDetails("blah - Movie","1995",names.get(index++));
			assertSearchDetails("A Movie's",null,names.get(index++));
			assertSearchDetails("A Movie",null,names.get(index++));
			assertSearchDetails("A Movie",null,names.get(index++));
			index++;
//			assertSearchDetails("A Movie",null,names.get(index++));
			assertSearchDetails("A movie 2000",null,names.get(index++));
			assertSearchDetails("A movie",null,names.get(index++));
			assertSearchDetails("A movie",null,names.get(index++));
			assertSearchDetails("A movie",null,names.get(index++));
			assertSearchDetails("A, Movie?",null,names.get(index++));
			assertSearchDetails("A Movie","2007",names.get(index++));
			assertSearchDetails("A Movie","2008",names.get(index++));
			assertSearchDetails("A Movie","2010",names.get(index++));
			assertSearchDetails("A Movie",null,names.get(index++));
			assertSearchDetails("AMOVIE",null,names.get(index++));
			assertSearchDetails("Blah",null,names.get(index++));
			assertSearchDetails("Blah: Movie",null,names.get(index++));
			assertSearchDetails("Blahía, b ôb bbbb",null,names.get(index++));
			assertSearchDetails("Dr. Movie",null,names.get(index++));
			index++;
//			assertSearchDetails("Movie",null,names.get(index++));
			assertSearchDetails("Movie 12",null,names.get(index++));
			assertSearchDetails("Movie 2: Some movie title",null,names.get(index++));
			assertSearchDetails("Movie I",null,names.get(index++));
			assertSearchDetails("Movie III",null,names.get(index++));
			assertSearchDetails("Movie's blah",null,names.get(index++));
			assertSearchDetails("Movie","2007",names.get(index++));
			assertSearchDetails("Movie",null,names.get(index++));
			assertSearchDetails("Movie Vol 2",null,names.get(index++));
			assertSearchDetails("Movie",null,names.get(index++));
			assertSearchDetails("Movie: Three",null,names.get(index++));
			index++;
//			assertSearchDetails("A Movie (Original Render)",null,names.get(index++));
			assertSearchDetails("The Movie: Part II",null,names.get(index++));
			index++;
//			assertSearchDetails("a-movie",null,names.get(index++));
			assertSearchDetails("a movie joined",null,names.get(index++));
			assertSearchDetails("amovie",null,names.get(index++));
			assertSearchDetails("amovie",null,names.get(index++));
			assertSearchDetails("9",null,names.get(index++));
			assertSearchDetails("movie",null,names.get(index++));
			assertSearchDetails("movie","2009",names.get(index++));
			assertSearchDetails("movie","2009",names.get(index++));
			assertSearchDetails("á movié",null,names.get(index++));
		}
		finally {
			FileHelper.delete(filmsDir);
		}
	}

	public void assertSearchDetails(String expectedTerm,String expectedYear,TSearchDetails actual) {
		Assert.assertEquals("Did not extract the correct term from: " + actual.getOriginalFile().getName(),expectedTerm,actual.getTerm());
		if (expectedYear == null) {
			Assert.assertNull("Got year when did not expect one: " + actual.getOriginalFile().getName(),actual.getYear());
		}
		else {
			Assert.assertEquals("Did not extract the correct year from: " + actual.getOriginalFile().getName(),expectedYear,actual.getYear());
		}
	}

	private File createFilmFiles() throws Exception {
		File tmpDir = FileHelper.createTmpDir("films");
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = TestFilmSearcher.class.getResourceAsStream("test-film-names.txt");
			br = new BufferedReader(new InputStreamReader(is));

			String strLine = null;
		    while ((strLine = br.readLine()) != null)   {
		    	File f = new File(tmpDir,strLine);
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

	private static class TSearchDetails {

		private File originalFile;
		private String term;
		private String year;

		public TSearchDetails(File originalFile,String term,String year) {
			this.originalFile = originalFile;
			this.term = term;
			this.year = year;
		}

		public File getOriginalFile() {
			return originalFile;
		}

		public String getTerm() {
			return term;
		}

		public String getYear() {
			return year;
		}


	}
}

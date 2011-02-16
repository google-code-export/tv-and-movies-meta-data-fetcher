package org.stanwood.media.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
			final List<SearchDetails>names = new ArrayList<SearchDetails>();
			FilmSearcher f = new FilmSearcher() {
				@Override
				protected SearchResult doSearch(String name,String year) throws MalformedURLException,
						IOException, SourceException {
					names.add(new SearchDetails(name,year));
					return null;
				}
			};
			for (File mediaFile : FileHelper.listFiles(filmsDir)) {
				f.search(mediaFile, filmsDir, "%t.%x");
			}
			Assert.assertEquals(73,names.size());
			Assert.assertEquals("A Movie.m4v",names.get(0).getTerm());
			Assert.assertEquals("\"Movie\".avi",names.get(1).getTerm());
			Assert.assertEquals("1995",names.get(2).getYear());
			Assert.assertEquals("Movie.avi",names.get(2).getTerm());
			Assert.assertEquals("1111- A movie.avi",names.get(3).getTerm());
			Assert.assertEquals("1111- A movie.avi",names.get(4).getTerm());
			Assert.assertEquals("1111- A movie.avi",names.get(5).getTerm());
		}
		finally {
			FileHelper.deleteDir(filmsDir);
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
}

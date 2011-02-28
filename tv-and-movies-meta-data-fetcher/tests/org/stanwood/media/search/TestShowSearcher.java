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

public class TestShowSearcher {

	@Test
	public void testSearch() throws Exception {
		File filmsDir = createTVFilms();
		try {
			final List<TSearchDetails>names = new ArrayList<TSearchDetails>();
			ShowSearcher searcher = new ShowSearcher() {
				@Override
				protected SearchResult doSearch(File mediaFile,String name,String year) throws MalformedURLException,
						IOException, SourceException {
					names.add(new TSearchDetails(mediaFile,name,year));
					return null;
				}
			};
			for (File mediaFile : FileHelper.listFiles(filmsDir)) {
				searcher.search(mediaFile, filmsDir, "%t.%x");
			}

			Collections.sort(names,new Comparator<TSearchDetails>() {
				@Override
				public int compare(TSearchDetails o1, TSearchDetails o2) {
					return o1.getOriginalFile().getName().compareTo(o2.getOriginalFile().getName());
				}
			});
			Assert.assertEquals(45,names.size());
		}
		finally {
			FileHelper.delete(filmsDir);
		}
	}

	private File createTVFilms() throws Exception {
		File tmpDir = FileHelper.createTmpDir("tv");
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = TestFilmSearcher.class.getResourceAsStream("show-names.txt");
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

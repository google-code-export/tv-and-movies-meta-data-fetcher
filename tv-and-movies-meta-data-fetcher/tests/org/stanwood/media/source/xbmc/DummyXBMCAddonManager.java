package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.updater.XBMCWebUpdater;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.Stream;

/**
 * This is a dummy XBMCAddonManager that fetches data from the test source tree instead of from the web. This
 * allows tests to run agaist a known set of data
 */
public class DummyXBMCAddonManager extends XBMCAddonManager {

	private static final Pattern TVDB_SEARCH_PATTERN = Pattern.compile(".*thetvdb.*GetSeries.*seriesname\\=(.+?)\\&.*");
	private static final Pattern TVDB_SERIES_PATTERN = Pattern.compile(".*thetvdb.*/series/(\\d+)/all.*");
	private static final Pattern THE_MOVIE_DB_SEARCH = Pattern.compile(".*themoviedb.*/Movie.search/.*/(.+)");
	private static final Pattern THE_MOVIE_DB_PATTERN = Pattern.compile(".*themoviedb.*/Movie\\.getInfo/.*/(\\d+)");
	private static final Pattern THE_MOVIE_DB_IMDB_LOOKUP = Pattern.compile(".*themoviedb.*/Movie\\.imdbLookup/.*/(tt\\d+)");
	private static final Pattern THE_MOVIE_DB_IMAGES_PATTERN = Pattern.compile(".*themoviedb.*/Movie\\.getImages/.*/(.*?)");
	private static final Pattern IDBM_PATTERN = Pattern.compile(".*imdb.com/title/(tt\\d+)/");
	private static final Pattern IDBM_COMBINED = Pattern.compile(".*imdb.com/title/(tt\\d+)/combined");
	private static final Pattern IDBM_POSTERS = Pattern.compile(".*imdb.com/title/(tt\\d+)/posters");
	private static final Pattern UPDATE_SIZE = Pattern.compile(".*mirrors.xbmc.org/addons/dharma/(.*)");

	private File updateSite;

	/**
	 * Used to create a instance of the addon manager
	 * @param config The configuration
	 * @param updateSite location of the test update site
	 * @throws XBMCException Thrown if their is a problem creating the addon manager
	 */
	public DummyXBMCAddonManager(ConfigReader config,File updateSite) throws XBMCException {
		super(config,null,false);
		XBMCWebUpdater updater = new XBMCWebUpdater(config);
		this.updateSite = updateSite;
		init(updater);
	}

	@Override
	Stream getSource(URL url) throws IOException {
		String strUrl = url.toExternalForm();
		System.out.println("Fetching URL: " + strUrl);
		Matcher m = TVDB_SEARCH_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("tvdb-search-"+getSearchName(m.group(1))+".html"),"text/xml","UTF-8",url.toExternalForm());
		}
		m = THE_MOVIE_DB_SEARCH.matcher(strUrl);
		if (m.matches()) {
			String term = getSearchName(m.group(1));
			term=term.replaceAll("\\+","-");
			term=term.replaceAll("\\%..","");
			return new Stream(Data.class.getResourceAsStream("themoviedb-search-"+term+".html"),"text/xml","UTF-8",url.toExternalForm());
		}
		m = THE_MOVIE_DB_IMDB_LOOKUP.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("themoviedb-imdbLookup-"+m.group(1)+".html"),"text/xml","UTF-8",url.toExternalForm());
		}
		m = IDBM_COMBINED.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("imdb-combined-"+m.group(1)+".html"),"text/html","UTF-8",url.toExternalForm());
		}
		m = IDBM_POSTERS.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("imdb-posters-"+m.group(1)+".html"),"text/html","UTF-8",url.toExternalForm());
		}
		m = THE_MOVIE_DB_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("themoviedb-film-"+m.group(1)+".html"),"text/xml","UTF-8",url.toExternalForm());
		}
		m = THE_MOVIE_DB_IMAGES_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("themoviedb-images-"+m.group(1)+".html"),"text/xml","UTF-8",url.toExternalForm());
		}
		m = IDBM_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return new Stream(Data.class.getResourceAsStream("imdb-"+m.group(1)+".html"),"text/xml","UTF-8",url.toExternalForm());
		}
		m = TVDB_SERIES_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return new Stream(new ZipInputStream(Data.class.getResourceAsStream("tvdb-series-"+m.group(1)+".zip")),"zip","UTF-8",url.toExternalForm());
		}
		throw new IOException("Unable to find test data for url: " + url);
	}

	/** {@inheritDoc} */
	@Override
	public String downloadFile(URL url, File newAddon) throws IOException {
		Matcher m = UPDATE_SIZE.matcher(url.toExternalForm());
		if (m.matches()) {
			File f = new File(updateSite,m.group(1));
			if (!f.exists()) {
				throw new IOException("Unable to find file: " +f);
			}
			System.out.println("Fetching URL: " + url + " from " + f.getAbsolutePath());
			FileHelper.copy(f,newAddon);
			return FileHelper.getMD5Checksum(newAddon);
		}
		throw new IOException("Unable to find test data for url: " + url);
	}

	private String getSearchName(String value) {
		value = value.toLowerCase();
		value = value.replaceAll("[ |+]", "-");
		return value;
	}





}

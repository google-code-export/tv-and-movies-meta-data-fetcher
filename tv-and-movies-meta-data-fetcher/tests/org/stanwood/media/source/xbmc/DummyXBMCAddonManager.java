package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

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
	private static final Pattern THE_MOVIE_DB_IMAGES_PATTERN = Pattern.compile(".*themoviedb.*/Movie\\.getImages/.*/(\\d+)");
	private static final Pattern IDBM_PATTERN = Pattern.compile(".*imdb.com/title/(tt\\d+)/");
	private static final Pattern UPDATE_SIZE = Pattern.compile(".*mirrors.xbmc.org/addons/dharma/(.*)");

	private File updateSite;

	/**
	 * Used to create a instance of the class
	 * @param addonDir The directory the addon's data is stored in
	 * @param locale The language been used
	 * @throws XBMCException Thrown if their are any problems
	 */
	public DummyXBMCAddonManager(File addonDir, Locale locale)
			throws XBMCException {
		super(new XBMCWebUpdater(),addonDir, locale);
	}

	public void setUpdateSite(File updateSite) {
		this.updateSite = updateSite;
	}

	@Override
	InputStream getSource(URL url) throws IOException {
		String strUrl = url.toExternalForm();
		System.out.println("Fetching URL: " + strUrl);
		Matcher m = TVDB_SEARCH_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("tvdb-search-"+getSearchName(m.group(1))+".html");
		}
		m = THE_MOVIE_DB_SEARCH.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("themoviedb-search-"+getSearchName(m.group(1))+".html");
		}
		m = THE_MOVIE_DB_IMDB_LOOKUP.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("themoviedb-imdbLookup-"+m.group(1)+".html");
		}
		m = THE_MOVIE_DB_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("themoviedb-film-"+m.group(1)+".html");
		}
		m = THE_MOVIE_DB_IMAGES_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("themoviedb-images-"+m.group(1)+".html");
		}
		m = IDBM_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("imdb-"+m.group(1)+".html");
		}
		m = TVDB_SERIES_PATTERN.matcher(strUrl);
		if (m.matches()) {

			return new ZipInputStream(Data.class.getResourceAsStream("tvdb-series-"+m.group(1)+".zip"));
		}
		throw new IOException("Unable to find test data for url: " + url);
	}

	@Override
	String downloadFile(URL url, File newAddon) throws IOException {
		Matcher m = UPDATE_SIZE.matcher(url.toExternalForm());
		if (m.matches()) {
			File f = new File(updateSite,m.group(1));
			System.out.println("Fetching URL: " + url + " from " + f.getAbsolutePath());
			FileHelper.copy(f,newAddon);
			return FileHelper.getMD5Checksum(newAddon);
		}
		return super.downloadFile(url, newAddon);
	}

	private String getSearchName(String value) {
		value = value.toLowerCase();
		value = value.replaceAll("[ |+]", "-");
		return value;
	}

}

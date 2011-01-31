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

public class DummyXBMCAddonManager extends XBMCAddonManager {

	// http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man

	private static final Pattern TVDB_SEARCH_PATTERN = Pattern.compile(".*thetvdb.*GetSeries.*seriesname\\=(.+?)\\&.*");
	private static final Pattern TVDB_SERIES_PATTERN = Pattern.compile(".*thetvdb.*/series/(\\d+)/all.*");
	private static final Pattern THE_MOVIE_DB_SEARCH = Pattern.compile(".*themoviedb.*/Movie.search/.*/(.+)");
	private static final Pattern THE_MOVIE_DB_PATTERN = Pattern.compile(".*themoviedb.*/Movie\\.getInfo/.*/(\\d+)");
	private static final Pattern THE_MOVIE_DB_IMAGES_PATTERN = Pattern.compile(".*themoviedb.*/Movie\\.getImages/.*/(\\d+)");
	private static final Pattern IDBM_PATTERN = Pattern.compile(".*imdb.com/title/(tt\\d+)/");

	public DummyXBMCAddonManager(File addonDir, Locale locale)
			throws XBMCException {
		super(addonDir, locale);
	}

	@Override
	InputStream getSource(URL url) throws IOException {
		String strUrl = url.toExternalForm();
		Matcher m = TVDB_SEARCH_PATTERN.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("tvdb-search-"+getSearchName(m.group(1))+".html");
		}
		m = THE_MOVIE_DB_SEARCH.matcher(strUrl);
		if (m.matches()) {
			return Data.class.getResourceAsStream("themoviedb-search-"+getSearchName(m.group(1))+".html");
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

	private String getSearchName(String value) {
		value = value.toLowerCase();
		value = value.replaceAll("[ |+]", "-");
		return value;
	}

}

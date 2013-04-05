package org.stanwood.media.test.sources;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ParameterType;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;

public class TestSource implements ISource {
	
	private static List<String>events = new ArrayList<String>();

	@Override
	public IEpisode getEpisode(ISeason season, int episodeNum, File file)
			throws SourceException, MalformedURLException, IOException {
		events.add("getEpisode()");
		return null;
	}

	@Override
	public ISeason getSeason(IShow show, int seasonNum) throws SourceException,
			IOException {
		events.add("getSeason()");
		return null;
	}

	@Override
	public IShow getShow(String showId, URL url, File file)
			throws SourceException, MalformedURLException, IOException {
		events.add("getShow()");
		return null;
	}

	@Override
	public IFilm getFilm(String filmId, URL url, File filmFile)
			throws SourceException, MalformedURLException, IOException {
		events.add("getFilm()");
		return null;
	}

	@Override
	public IEpisode getSpecial(ISeason season, int specialNumber, File file)
			throws SourceException, MalformedURLException, IOException {
		events.add("getSpecial()");
		return null;
	}	

	@Override
	public void setParameter(String key, String value) throws SourceException {
		events.add("setParameter()");
	}

	@Override
	public String getParameter(String key) throws SourceException {
		events.add("getParameter()");
		return null;
	}

	@Override
	public void setMediaDirConfig(MediaDirectory dir) throws SourceException {
		events.add("setMediaDirConfig()");
	}

	@Override
	public SearchResult searchMedia(String name,String year, Mode mode, Integer part)
			throws SourceException {
		events.add("searchMedia()");
		return null;
	}
	
	public static List<String>getEvents() {
		return events;
	}

	@Override
	public ExtensionInfo<? extends ISource> getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

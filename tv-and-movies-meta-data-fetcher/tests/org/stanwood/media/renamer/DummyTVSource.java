package org.stanwood.media.renamer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;

public class DummyTVSource implements ISource{

	@Override
	public Episode getEpisode(Season season, int episodeNum)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException,
			IOException {
		return null;
	}

	@Override
	public Show getShow(String showId, URL url) throws SourceException,
			MalformedURLException, IOException {
		return null;
	}

	@Override
	public Film getFilm(String filmId, URL url) throws SourceException,
			MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws SourceException, MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchResult searchForVideoId(File rootMediaDir, Mode mode,
			File episodeFile, String renamePattern) throws SourceException,
			MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}

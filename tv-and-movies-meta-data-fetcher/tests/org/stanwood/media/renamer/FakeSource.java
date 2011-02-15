package org.stanwood.media.renamer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCSource;

public class FakeSource extends XBMCSource {

	public FakeSource(XBMCAddonManager mgr, String addonId)
			throws XBMCException {
		super(mgr, addonId);
	}

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
		return null;
	}

	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public String getParameter(String key) throws SourceException {
		return null;
	}


}
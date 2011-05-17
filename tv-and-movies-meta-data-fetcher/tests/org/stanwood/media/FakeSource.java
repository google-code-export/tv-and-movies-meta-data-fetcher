package org.stanwood.media;

import java.io.File;
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

/**
 * This is a fake source that does not actually do anything. Used for tests
 */
public class FakeSource extends XBMCSource {

	/**
	 * The constructor
	 * @param mgr The addon manager
	 * @param addonId The ID of the sources XBMC addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public FakeSource(XBMCAddonManager mgr, String addonId)
			throws XBMCException {
		super(mgr, addonId);
	}

	/** {@inheritDoc} */
	@Override
	public Episode getEpisode(Season season, int episodeNum,File file)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException,
			IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Show getShow(String showId, URL url,File file) throws SourceException,
			MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Film getFilm(String filmId, URL url,File file) throws SourceException,
			MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Episode getSpecial(Season season, int specialNumber,File file)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws SourceException {
		return null;
	}


}

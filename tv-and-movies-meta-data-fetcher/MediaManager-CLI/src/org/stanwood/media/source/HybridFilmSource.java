/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.ConfigException;

/**
 * This class is a source used to retrieve the best film information it can. It
 * does this by calling other sources and picking the best information from them.
 * <p>This source supports the following parameters:
 * <ul>
 * <li>xbmcSourceId - Id of XBMC source to use, if parameter is not specified, then the default is used.</li>
 * </ul>
 * </p>
 */
public class HybridFilmSource implements ISource {

	private ISource imdbSource;
	private ISource tagChimpSource = new TagChimpSource(new TagChimpSourceInfo());
	private Map<String,URL> urls = new HashMap<String,URL>();

	/** The ID of the the source */
	public static final String OLD_SOURCE_ID = "hybridFilm"; //$NON-NLS-1$

	private String sourceId = null;
	private HybridFilmSourceInfo sourceInfo;

	/**
	 * Used to create a instance of the source
	 * @param sourceInfo The description of the the source
	 * @throws SourceException Thrown if their are any problems
	 */
	public HybridFilmSource(HybridFilmSourceInfo sourceInfo) throws SourceException {
		this.sourceInfo = sourceInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void setMediaDirConfig(MediaDirectory dir) throws SourceException {
		try {
			ExtensionInfo<? extends ISource> info;
			if (sourceId!=null) {
				info = dir.getController().getSourceInfo(sourceId);
			}
			else {
				info = dir.getController().getDefaultSource(Mode.FILM);
			}
			imdbSource = info.getAnyExtension(dir.getMediaDirConfig());
			if (imdbSource==null) {
				throw new SourceException(Messages.getString("HybridFilmSource.UNABLE_CREATE_SOURCE1")); //$NON-NLS-1$
			}
		} catch (ExtensionException e) {
			throw new SourceException(Messages.getString("HybridFilmSource.UNABLE_CREATE_SOURCE1"),e); //$NON-NLS-1$
		} catch (ConfigException e) {
			throw new SourceException(Messages.getString("HybridFilmSource.UNABLE_CREATE_SOURCE1"),e); //$NON-NLS-1$
		}
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 * @param file The film file if looking up a files details, or NULL
	 */
	@Override
	public IEpisode getEpisode(ISeason season, int episodeNum,File file) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param show The show the season belongs to.
	 * @param seasonNum The number of the season to read
	 */
	@Override
	public ISeason getSeason(IShow show, int seasonNum) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param file The media file if looking up a files details, or NULL
	 * @param url String url of the show
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(String showId,URL url,File file) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param specialNumber The number of the special episode to read
	 * @param file The film file if looking up a files details, or NULL
	 */
	@Override
	public IEpisode getSpecial(ISeason season, int specialNumber,File file) {
		return null;
	}

	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 *
	 * @param filmId The id of the film
	 * @param url The URL to use when looking up film details
	 * @param file The film file if looking up a files details, or NULL
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public IFilm getFilm(String filmId,URL url,File file) throws SourceException, MalformedURLException, IOException {
		IFilm tagChimpFilm = null;
		IFilm imdbFilm = null;
		StringTokenizer tok = new StringTokenizer(filmId,"|"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String key = tok.nextToken();
			String value = tok.nextToken();
			if (key.equals(imdbSource.getInfo().getId())) {
				imdbFilm = imdbSource.getFilm(value,urls.get(key+"|"+value),file); //$NON-NLS-1$
			}
			else if (key.equals(tagChimpSource.getInfo().getId())) {
				tagChimpFilm = tagChimpSource.getFilm(value,urls.get(key+"|"+value),file); //$NON-NLS-1$
			}
		}

		if (tagChimpFilm!=null && imdbFilm!=null) {
			IFilm film = new Film(filmId,imdbFilm.getFilmUrl());
			film.setTitle(imdbFilm.getTitle());
			film.setCertifications(imdbFilm.getCertifications());
			film.setChapters(tagChimpFilm.getChapters());
			film.setDate(imdbFilm.getDate());
			film.setDescription(tagChimpFilm.getDescription());
			film.setDirectors(imdbFilm.getDirectors());
//			film.setFilmUrl(imdbFilm.getFilmUrl());
			film.setActors(imdbFilm.getActors());
			film.setCountry(imdbFilm.getCountry());
			if (tagChimpFilm.getImageURL()!=null){
				film.setImageURL(tagChimpFilm.getImageURL());
			}
			else if (imdbFilm.getImageURL()!=null) {
				film.setImageURL(imdbFilm.getImageURL());
			}

			film.setPreferredGenre(tagChimpFilm.getPreferredGenre());
			List<String> genres = imdbFilm.getGenres();
			if (tagChimpFilm.getPreferredGenre()!=null && !genres.contains(tagChimpFilm.getPreferredGenre())) {
				genres.add(tagChimpFilm.getPreferredGenre());
			}
			film.setGenres(genres);
			film.setRating(imdbFilm.getRating());
			film.setSourceId(sourceInfo.getId());
			film.setSummary(imdbFilm.getSummary());
			film.setTitle(imdbFilm.getTitle());
			film.setWriters(imdbFilm.getWriters());
			return film;
		}
		else {
			if (tagChimpFilm!=null) {
				return tagChimpFilm;
			}
			else if (imdbFilm!=null )  {
				return imdbFilm;
			}
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name,String year, Mode mode, Integer part)
			throws SourceException {

		if (mode != Mode.FILM) {
			return null;
		}

		StringBuilder id = new StringBuilder();
		String newUrl = null;

		ISource sources[] = new ISource[] {imdbSource,tagChimpSource};
		for (ISource source : sources) {
			if (source!=null) {
				SearchResult result = source.searchMedia(name,year, mode, part);
				if (result!=null) {
					if (id.length()>0) {
						id.append("|"); //$NON-NLS-1$
					}
					id.append(result.getSourceId());
					id.append("|"); //$NON-NLS-1$
					id.append(result.getId());
					try {
						urls.put(result.getSourceId()+"|"+result.getId(),new URL(result.getUrl())); //$NON-NLS-1$
					} catch (MalformedURLException e) {
						throw new SourceException(MessageFormat.format(Messages.getString("HybridFilmSource.INVALID_URL"),result.getUrl()),e); //$NON-NLS-1$
					}
					newUrl = result.getUrl();

				}
				else {
					return null;
				}
			}

		}
		if (id!=null && id.length()>0) {
			SearchResult result = new SearchResult(id.toString(),sourceInfo.getId(),newUrl,part,Mode.FILM);
			return result;
		}

		return null;
	}

	/**
	 * <p>Used to set source parameters. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * <p>This source supports the following parameters:
	 * <ul>
	 * <li>xbmcSourceId - Id of XBMC source to use, if parameter is not specified, then the default is used.</li>
	 * </ul>
	 * </p>
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public void setParameter(String key, String value) throws SourceException {
		if (key.equals(HybridFilmSourceInfo.PARAM_KEY_SOURCE_ID.getName())) {
			sourceId = value;
		}
		else {
			throw new SourceException(MessageFormat.format(Messages.getString("HybridFilmSource.UNSUPPORTED_PARAM"),key,getClass().getName())); //$NON-NLS-1$
		}
	}



	/**
	 * <p>Used to get the value of a source parameter. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * <p>This source supports the following parameters:
	 * <ul>
	 * <li>xbmcSourceId - Id of XBMC source to use, if parameter is not specified, then the default is used.</li>
	 * </ul>
	 * </p>
	 * @param key The key of the parameter
	 * @return The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public String getParameter(String key) throws SourceException {
		if (key.equals(HybridFilmSourceInfo.PARAM_KEY_SOURCE_ID.getName())) {
			return sourceId;
		}
		else {
			throw new SourceException(MessageFormat.format(Messages.getString("HybridFilmSource.UNSUPPORTED_PARAM"),key,getClass().getName())); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public ExtensionInfo<? extends ISource> getInfo() {
		return sourceInfo;
	}


}

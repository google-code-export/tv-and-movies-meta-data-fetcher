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
import java.util.List;
import java.util.StringTokenizer;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.Controller;

/**
 * This class is a source used to retrieve the best film information it can. It
 * does this by calling other sources and picking the best information from them.
 */
public class HybridFilmSource implements ISource {

	private ISource imdbSource;
	private ISource tagChimpSource = new TagChimpSource();

	/** The ID of the the source */
	public static final String SOURCE_ID = "hybridFilm";

	/**
	 * Used to create a instance of the source
	 * @throws SourceException Thrown if their are any problems
	 */
	public HybridFilmSource() throws SourceException {
		imdbSource =Controller.getSource(Controller.getDefaultSourceID(Mode.TV_SHOW));
	}


	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 * @param file The film file if looking up a files details, or NULL
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum,File file) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param show The show the season belongs to.
	 * @param seasonNum The number of the season to read
	 */
	@Override
	public Season getSeason(Show show, int seasonNum) {
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
	public Episode getSpecial(Season season, int specialNumber,File file) {
		return null;
	}

	/**
	 * Get the id of the source.
	 *
	 * @return The id of the source
	 */
	@Override
	public String getSourceId() {
		return SOURCE_ID;
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
	public Film getFilm(String filmId,URL url,File file) throws SourceException, MalformedURLException, IOException {
		Film tagChimpFilm = null;
		Film imdbFilm = null;
		StringTokenizer tok = new StringTokenizer(filmId,"|");
		while (tok.hasMoreTokens()) {
			String key = tok.nextToken();
			String value = tok.nextToken();
			if (key.equals(imdbSource.getSourceId())) {
				imdbFilm = imdbSource.getFilm(value,url,file);
			}
			else if (key.equals(tagChimpSource.getSourceId())) {
				tagChimpFilm = tagChimpSource.getFilm(value,url,file);
			}
		}

		if (tagChimpFilm!=null && imdbFilm!=null) {
			Film film = new Film(filmId);
			film.setCertifications(imdbFilm.getCertifications());
			film.setChapters(tagChimpFilm.getChapters());
			film.setDate(imdbFilm.getDate());
			film.setDescription(tagChimpFilm.getDescription());
			film.setDirectors(imdbFilm.getDirectors());
			film.setFilmUrl(imdbFilm.getFilmUrl());
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
			film.setSourceId(SOURCE_ID);
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

	/**
	 * This will search the www.imdb.com and www.tagchimp.com site for the film. It uses the
	 * last segment of the file name, converts it to lower case, tidies up the name and performs
	 * the search.
	 *
	 * @param filmFile The file the film is located in
	 * @param mode The mode that the search operation should be performed in
	 * @return Always returns null
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public SearchResult searchForVideoId(File rootMediaDir,Mode mode,File filmFile,String renamePattern) throws SourceException, MalformedURLException,
			IOException {
		if (mode != Mode.FILM) {
			return null;
		}
		StringBuilder id = new StringBuilder();
		String url = null;

		ISource sources[] = new ISource[] {imdbSource,tagChimpSource};
		for (ISource source : sources) {
			SearchResult result = source.searchForVideoId(rootMediaDir,mode, filmFile,renamePattern);
			if (result!=null) {
				if (id.length()>0) {
					id.append("|");
				}
				id.append(result.getSourceId());
				id.append("|");
				id.append(result.getId());
				url = result.getUrl();
			}
		}

		if (id!=null && id.length()>0) {
			SearchResult result = new SearchResult(id.toString(),SOURCE_ID,url);
			return result;
		}

		return null;
	}

	/**
	 * <p>Used to set source parameters. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * <p>This source does not support any parameters.</p>
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public void setParameter(String key, String value) throws SourceException {
		throw new SourceException("Unsupported parameter '" +key+"' on source '"+getClass().getName()+"'");
	}



	/**
	 * <p>Used to get the value of a source parameter. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * <p>This source does not support any parameters.</p>
	 * @param key The key of the parameter
	 * @return The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public String getParameter(String key) throws SourceException {
		throw new SourceException("Unsupported parameter '" +key+"' on source '"+getClass().getName()+"'");
	}



}

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
import java.util.Iterator;
import java.util.List;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;

public class IMDBSource implements ISource {

	private static final String IMDB_BASE_URL = "http://www.imdb.com";
	private static final String URL_SUMMARY = "/title/tt$filmId$/";
	public static final String SOURCE_ID = "imdb";

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
	public Show getShow(File showDirectory, long showId)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public String getSourceId() {
		return null;
	}

	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public SearchResult searchForShowId(File showDirectory)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}
	
	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 * @param filmDirectory The directory the film is located in.
	 * @param filmId The id of the film
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Film getFilm(File showDirectory, long filmId)
			throws SourceException, MalformedURLException, IOException {
		URL url = new URL(getFilmURL(filmId));
		Film film = new Film(filmId);
		film.setFilmUrl(url);		
		film.setSourceId(SOURCE_ID);
		Source source = getSource(film.getFilmUrl());
		if (source==null) {
			throw new SourceException("Unable to find film with id: " + filmId);
		}
		parseFilm(source, film);
		return film;
	}
	
	@SuppressWarnings("unchecked")
	private void parseFilm(Source source, Film film) {
		List<Element> divs = source.findAllElements(HTMLElementName.DIV);
		for (Element div : divs) {			
			if (div.getAttributeValue("id") !=null) {						
				if (div.getAttributeValue("id").equals("tn15title")) {
					Element h1 = findFirstChild(div,HTMLElementName.H1);
					Iterator it = h1.getNodeIterator(); 
					it.next();										
					film.setTitle(it.next().toString());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Element findFirstChild(Element parent, String tagName) {
		for (Element child : (List<Element>)parent.getChildElements()) {
			if (child.getName().equals(tagName)){
				return child;
			}
		}
		return null;
	}

	/* package for test */Source getSource(URL url) throws IOException {
		return new Source(url);
	}
	
	private final static String getFilmURL(long filmId) {
		String strFilmId = String.valueOf(filmId);
		while (strFilmId.length()<7) {
			strFilmId="0"+strFilmId;
		}
		
		return IMDB_BASE_URL+ URL_SUMMARY.replaceAll("\\$filmId\\$",strFilmId );
	}
	
}

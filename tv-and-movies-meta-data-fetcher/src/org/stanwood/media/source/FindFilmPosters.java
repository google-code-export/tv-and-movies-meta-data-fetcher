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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.Film;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;

/**
 * Used to find film posters from different web sites
 */
public class FindFilmPosters {

	private final static Pattern URL_PATTERN = Pattern.compile("http://.*movieposter.com/poster/(MPW-\\d+)/.*");
	private final static Pattern IMG_PATTERN = Pattern.compile("/posters/archive/tiny/(.*)/(.*)");

	/**
	 * Used to find a film poster via movieposter.com
	 * @param film The film we want a poster for
	 * @return The URL which points to the film poster
	 * @throws IOException Thrown if their is a problem getting the film poster
	 */
	@SuppressWarnings("unchecked")
	public URL findViaMoviePoster(Film film) throws IOException {
		URL searchUrl = new URL(getMoviePosterUrl(film));
		Source source = getSource(searchUrl);
		List<Element> links = source.findAllElements(HTMLElementName.A);
		for (Element link : links) {
			String href = link.getAttributeValue("href");
			String title = link.getAttributeValue("title");
			Matcher m = URL_PATTERN.matcher(href);
			if (m.matches()) {
				String part1 = m.group(1);
				String filmTitle = film.getTitle().toLowerCase();
				filmTitle = filmTitle.replaceAll("the", "");
				filmTitle = filmTitle.trim();
				if (title != null && title.toLowerCase().contains(filmTitle)) {
					List<Element> imgs = source.findAllElements(HTMLElementName.IMG);
					for (Element img : imgs) {
						String src = img.getAttributeValue("src");
						Matcher m2 = IMG_PATTERN.matcher(src);
						if (m2.matches() && m2.group(2).equals(part1)) {
							String part2 = m2.group(1);
							String sURL = "http://uk.movieposter.com/posters/archive/main/" + part2 + "/" + part1;
							return new URL(sURL);
						}
					}
				}
			}
		}
		return null;
	}

	/* package for test */ Source getSource(URL searchUrl) throws IOException {
		Source source = new Source(searchUrl);
		return source;
	}

	/**
	 * Used to get the URL needed to lookup a films poster via eu.movieposter.com
	 * @param film The film we are looking for a poster for
	 * @return The string version of the URL
	 */
	private static String getMoviePosterUrl(Film film) {
		return "http://eu.movieposter.com/cgi-bin/mpw8/search.pl?ti=" + film.getTitle().replaceAll(" ", "+")
				+ "&pl=action&th=y&rs=12&size=any";
	}
}

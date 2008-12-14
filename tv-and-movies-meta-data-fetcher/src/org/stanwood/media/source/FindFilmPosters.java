package org.stanwood.media.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.Film;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;

public class FindFilmPosters {

	private final static Pattern URL_PATTERN = Pattern.compile("http://eu.movieposter.com/poster/(MPW-\\d+)");
	private final static Pattern IMG_PATTERN = Pattern.compile("/posters/archive/tiny/(.*)/(.*)");

	@SuppressWarnings("unchecked")
	public URL findFilmPosterUrl(Film film) throws IOException {
		URL searchUrl = new URL(getUrl(film));
		Source source = new Source(searchUrl);
		List<Element> links = source.findAllElements(HTMLElementName.A);
		for (Element link : links) {
			String href = link.getAttributeValue("href");
			String title = link.getAttributeValue("title");

			Matcher m = URL_PATTERN.matcher(href);
			if (m.matches()) {
				String part1 = m.group(1);
				if (title != null && title.toLowerCase().contains(film.getTitle().toLowerCase())) {
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

	public static String getUrl(Film film) {
		return "http://eu.movieposter.com/cgi-bin/mpw8/search.pl?ti=" + film.getTitle().replaceAll(" ", "+")
				+ "&pl=action&th=y&rs=12&size=any";
	}

}

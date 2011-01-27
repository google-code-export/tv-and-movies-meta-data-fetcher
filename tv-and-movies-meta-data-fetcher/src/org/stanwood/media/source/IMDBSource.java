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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.search.SearchHelper;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Segment;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.Tag;

/**
 * This class is a source used to retrieve information about films from {@link "www.imdb.com"}.
 * This source has the option parameter "regexpToReplace". This is used when searching for a film
 * via the film's filename. The parameter is a regular expression, that when found in the filename,
 * is removed. Use the method <code>setRegexpToReplace</code> to set the regular expression.
 */
public class IMDBSource implements ISource {

	private final static Log log = LogFactory.getLog(IMDBSource.class);

	/** The ID of the the source */
	public static final String SOURCE_ID = "imdb";

	private static final String IMDB_BASE_URL = "http://www.imdb.com";
	private static final String URL_SUMMARY = "/title/tt$filmId$/";
	private static final SimpleDateFormat RELEASE_DATE_FORMAT_1 = new SimpleDateFormat("dd MMMM yyyy");
	private static final SimpleDateFormat RELEASE_DATE_FORMAT_2 = new SimpleDateFormat("MMMM yyyy");
	private static final Pattern FILM_TITLE_PATTERN = Pattern.compile(".*\\d+\\. (.*) \\((\\d+)\\).*");
	private static final Pattern EXTRACT_ID_PATTERN = Pattern.compile(".*tt(\\d+)/");
	private static final Pattern EXTRACT_ID2_PATTERN = Pattern.compile(".*title/tt(\\d+).*");
	private static final Pattern IMAGE_PATTERN = Pattern.compile("(.*)SX(\\d+)_SY(\\d+)(.*)");

	private String regexpToReplace = null;

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum) {
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
	 * @param url String url of the show
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(String showId,URL url) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param specialNumber The number of the special episode to read
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber) {
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
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Film getFilm(String filmId) throws SourceException, MalformedURLException, IOException {
		URL url = new URL(getFilmURL(filmId));
		Film film = new Film(filmId);
		film.setFilmUrl(url);
		film.setSourceId(SOURCE_ID);
		String html = getSource(film.getFilmUrl());
		if (html == null) {
			throw new SourceException("Unable to find film with id: " + filmId);
		}
		parseFilm(html, film);

		return film;
	}

	@SuppressWarnings("unchecked")
	private void parseFilm(String html, Film film) throws MalformedURLException {
		Source source = new Source(html);
		List<Element> divs = source.findAllElements(HTMLElementName.DIV);
		for (Element div : divs) {
			if (div.getAttributeValue("id") != null && div.getAttributeValue("id").equals("main")) {
				Element h1 = ParseHelper.findFirstChild(div, HTMLElementName.H1,null);
				if (h1 != null) {
					film.setTitle(SearchHelper.decodeHtmlEntities(getContents(h1)));
				}
			}
			else if (div.getAttributeValue("class")!=null && div.getAttributeValue("class").equals("photo")) {
				List<Element> imgs = div.findAllElements(HTMLElementName.IMG);
				if (imgs.size()>0) {
					Element img = imgs.get(0);
					String src = img.getAttributeValue("src");
					Matcher m = IMAGE_PATTERN.matcher(src);
					if (m.matches()) {
						String url = m.group(1)+"SX284_SY400"+m.group(4);
						film.setImageURL(new URL(url));
					}
				}
			}
			else if (div.getAttributeValue("id")!=null && div.getAttributeValue("id").equals("tn15rating")) {
				Element element = ParseHelper.findFirstChild(div, HTMLElementName.B, true,new IFilterElement() {							
					@Override
					public boolean accept(Element element) {
						return true;
					}
				});						
				String ratingStr = element.getTextExtractor().toString();
				try {
					float rating = Float.parseFloat(ratingStr.substring(0, ratingStr.indexOf('/')));
					film.setRating(rating);
				} catch (NumberFormatException e) {
					log.error("Unable to parse rating from string: " + ratingStr);
				}
			}
			else if (div.getAttributeValue("class") != null && div.getAttributeValue("class").equals("info")) {
				Element h5 = ParseHelper.findFirstChild(div, HTMLElementName.H5,null);
				if (h5 != null) {
					if (getContents(h5).equals("Plot:")) {
						String str = getInfoContent(div);
						if (str!=null) {
							film.setSummary(str);
						}
					} else if (getContents(h5).equals("Director:")) {
						List<Link> links = getLinks(div, "/name");
						film.setDirectors(links);
					} else if (getContents(h5).startsWith("Writers")) {
						List<Link> links = getLinks(div, "/name");
						film.setWriters(links);
					} else if (getContents(h5).equals("Genre:")) {
						List<Link> links = getLinks(div, "/Sections/Genres");
						film.setGenres(new ArrayList<String>());
						for (Link link : links) {
							film.addGenre(link.getTitle());
						}
					} else if (getContents(h5).equals("Certification:")) {
						Element element = ParseHelper.findFirstChild(div, HTMLElementName.DIV, new IFilterElement() {							
							@Override
							public boolean accept(Element element) {
								return (element.getAttributeValue("class")!=null && element.getAttributeValue("class").equals("info-content"));
							}
						});
						List<Certification> certs = new ArrayList<Certification>();

						List<Link> links = getLinks(element, "/search/title?certificates=");
						for (Link link : links) {
							int pos = link.getTitle().indexOf(':');
							Certification cert = new Certification(SearchHelper.decodeHtmlEntities(link.getTitle()).substring(0, pos), link.getTitle()
									.substring(pos + 1));
							certs.add(cert);
						}
						film.setCertifications(certs);
					} else if (getContents(h5).equals("Release Date:")) {						
						String str = getInfoContent(div);												
						if (str != null) {
							int pos = str.lastIndexOf(' ');
							if (pos != -1) {
								str = str.substring(0, pos);
							}
							try {
								Date date = RELEASE_DATE_FORMAT_1.parse(str);
								film.setDate(date);
							} catch (ParseException e) {
								try {
									Date date = RELEASE_DATE_FORMAT_2.parse(str);
									film.setDate(date);
								} catch (ParseException e1) {
									log.error("Unable to parse date '" + str +"' of film with id '"+film.getId()+"'");
								}
							}
						}
					}
					else if (getContents(h5).equals("Country:")) {
						List<Link> countries = getLinks(div, "/Sections/Countries");
						if (countries!=null && countries.size()==1) {
							film.setCountry(countries.get(0));
						}
					}
				}
			}
		}

		if (film.getDate() == null) {
			log.error("Unable to find a date of film with the id '" + film.getId()+"' and the title '"+film.getTitle()+"'");
		}
	}

	@SuppressWarnings("unchecked")
	private String getInfoContent(Element div) {
		String str = null;
		Element infoContentDiv = ParseHelper.findFirstChild(div,HTMLElementName.DIV,false,new IFilterElement() {							
			@Override
			public boolean accept(Element element) {
				return (element.getAttributeValue("class") != null && element.getAttributeValue("class").equals("info-content"));								
			}
		});						
		if (infoContentDiv!=null) {
			Iterator it = infoContentDiv.getNodeIterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o.getClass().equals(Segment.class)) {
					str = SearchHelper.decodeHtmlEntities(((Segment)o).getTextExtractor().toString().trim());									
					break;
				}
			}
		}
		return str;
	}

	private List<Link> getLinks(Element div, final String linkStart) {
		List<Link> links =ParseHelper.getLinks(IMDB_BASE_URL, div, new IFilterLink() {
			@Override
			public boolean accept(Link link) {
				return link.getURL().startsWith(IMDB_BASE_URL+linkStart);
			}
		});
		return links;
	}

	@SuppressWarnings("unchecked")
	private String getSectionText(Element div) {
		String result = null;
		Iterator it = div.getNodeIterator();
		Object node = it.next();
		while (it.hasNext() && !(node instanceof EndTag)) {
			node = it.next();
		}
		while (it.hasNext() && node instanceof Tag) {
			node = it.next();
		}
		result = node.toString().trim();
		return result;
	}

	@SuppressWarnings("unchecked")
	private String getContents(Element e) {
		Iterator it = e.getNodeIterator();
		it.next();
		return it.next().toString().trim();
	}

	/* package for test */String getSource(URL url) throws IOException {
		String html = getHTMLFromURL(url);
		return html;
	}

	private final static String getFilmURL(String filmId) {
		String strFilmId = filmId;
		while (strFilmId.length() < 7) {
			strFilmId = "0" + strFilmId;
		}

		return IMDB_BASE_URL + URL_SUMMARY.replaceAll("\\$filmId\\$", strFilmId);
	}

	/**
	 * This will search the IMDB site for the film. It uses the last segment of the file name, converts it to lower
	 * case, tidies up the name and performs the search.
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
		String query = SearchHelper.getQuery(filmFile,regexpToReplace).toLowerCase();
		Source source = new Source(getSource(new URL(getSearchUrl(query.replaceAll(" ", "+")))));
		SearchResult result = searchTitles(query,"Popular Titles", source);
		if (result == null) {
			result = searchTitles(query,"Exact Matches", source);
		}
		if (result == null) {
			result = searchFilmsPage(query,source);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private SearchResult searchFilmsPage(String query, Source source) {
		List<Element> aEls = source.findAllElements(HTMLElementName.A);
		for (Element a : aEls) {
			if (a.getAttributeValue("class") != null && a.getAttributeValue("class").equals("linkasbutton-secondary")) {
				String url = a.getAttributeValue("href");
				Matcher m = EXTRACT_ID2_PATTERN.matcher(url);
				if (m.matches()) {
					SearchResult result = new SearchResult(m.group(1), SOURCE_ID,getFilmURL(m.group(1)));
					return result;
				}

			}
		}
		return null;
	}



	@SuppressWarnings("unchecked")
	private SearchResult searchTitles(String query,String searchType, Source source) {
		List<Element> elements = source.findAllElements(HTMLElementName.B);
		for (Element elB : elements) {
			if (elB.getTextExtractor().toString().contains(searchType)) {
				Tag table = elB.getEndTag().findNextTag();
				List<Element> trs = table.getElement().findAllElements(HTMLElementName.TR);
				String url = null;
				int year = 0;

				for (Element tr : trs) {
					String filmTilteText = tr.getTextExtractor().toString();
					Matcher m = FILM_TITLE_PATTERN.matcher( filmTilteText);
					if (m.matches()) {
						String realTitle =SearchHelper.normalizeQuery(m.group(1));
						if (realTitle.contains(SearchHelper.normalizeQuery(query))) {
							if (Integer.parseInt(m.group(2)) > year) {
								year = Integer.parseInt(m.group(2));
								url = getFirstUrl(tr).getAttributeValue("href");
								if (url != null) {
									Matcher m2 = EXTRACT_ID_PATTERN.matcher(url);
									if (m2.matches()) {
										SearchResult result = new SearchResult(m2.group(1), SOURCE_ID,IMDB_BASE_URL+url);
										return result;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Element getFirstUrl(Element parent) {
		List<Element> elAs = parent.findAllElements(HTMLElementName.A);
		return elAs.get(0);
	}

	private String getHTMLFromURL(URL url) throws IOException {
		WebFile page = new WebFile(url);
		String MIME = page.getMIMEType();
		byte[] content = (byte[]) page.getContent();
		String html = null;
		if (MIME.equals("text/html")) {
			html = new String(content, "iso-8859-1");
		}
		return html;
	}

	private String getSearchUrl(String query) {
		return IMDB_BASE_URL + "/find?q=" + query + "";
	}

	/**
	 * Get the "RegexpToReplace" parameter value.
	 * @return The "RegexpToReplace" parameter value.
	 */
	public String getRegexpToReplace() {
		return regexpToReplace;
	}

	/**
	 * Used to set the "RegexpToReplace" parameter value.
	 * @param regexpToReplace The value of the parameter been set.
	 */
	public void setRegexpToReplace(String regexpToReplace) {
		this.regexpToReplace = regexpToReplace;
	}
}

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

import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;

/**
 * This class is a source used to retrieve information about films from {@link "www.imdb.com"}.
 * This source has the option parameter "regexpToReplace". This is used when searching for a film
 * via the film's filename. The parameter is a regular expression, that when found in the filename,
 * is removed. Use the method <code>setRegexpToReplace</code> to set the regular expression.  
 */
public class IMDBSource implements ISource {

	/** The ID of the the source */
	public static final String SOURCE_ID = "imdb";

	private static final String IMDB_BASE_URL = "http://www.imdb.com";
	private static final String URL_SUMMARY = "/title/tt$filmId$/";
	private static final SimpleDateFormat RELEASE_DATE_FORMAT_1 = new SimpleDateFormat("dd MMMMM yyyy");
	private static final SimpleDateFormat RELEASE_DATE_FORMAT_2 = new SimpleDateFormat("MMMMM yyyy");
	private static final Pattern FILM_TITLE_PATTERN = Pattern.compile(".*\\d+\\. (.*) \\((\\d+)\\).*");
	private static final Pattern EXTRACT_ID_PATTERN = Pattern.compile(".*tt(\\d+)/");
	private static final Pattern EXTRACT_ID2_PATTERN = Pattern.compile(".*title/tt(\\d+).*");
	
	
	
	/** Used to disable fetching of posters at test time */
	/* package protected */ static boolean fetchPosters = true;
	
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
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(String showId) {
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
		if (fetchPosters) {
			FindFilmPosters posterFinder = new FindFilmPosters();
			film.setImageURL(posterFinder.findViaMoviePoster(film));
		}		
		
		return film;
	}

	@SuppressWarnings("unchecked")
	private void parseFilm(String html, Film film) {
		Source source = new Source(html);
		List<Element> divs = source.findAllElements(HTMLElementName.DIV);
		for (Element div : divs) {
			if (div.getAttributeValue("id") != null && div.getAttributeValue("id").equals("tn15title")) {
				Element h1 = findFirstChild(div, HTMLElementName.H1);
				if (h1 != null) {
					film.setTitle(decodeHtmlEntities(getContents(h1)));
				}
			} else if (div.getAttributeValue("class") != null && div.getAttributeValue("class").equals("info")) {
				Element h5 = findFirstChild(div, HTMLElementName.H5);
				if (h5 != null) {
					if (getContents(h5).equals("Plot:")) {
						film.setSummary(decodeHtmlEntities(getSectionText(div)));
					} else if (getContents(h5).equals("Director:")) {
						List<Link> links = getLinks(div, "/name");
						film.setDirectors(links);
					} else if (getContents(h5).equals("Writers")) {
						List<Link> links = getLinks(div, "/name");
						film.setWriters(links);
					} else if (getContents(h5).equals("Genre:")) {
						List<Link> links = getLinks(div, "/Sections/Genres");
						film.setGenres(new ArrayList<String>());
						for (Link link : links) {
							film.addGenre(link.getTitle());
						}
					} else if (getContents(h5).equals("User Rating:")) {
						List<StartTag> tags = div.findAllStartTags(HTMLElementName.B);
						if (tags != null && tags.size() == 1) {
							String ratingStr = tags.get(0).getElement().getTextExtractor().toString();
							try {
								float rating = Float.parseFloat(ratingStr.substring(0, ratingStr.indexOf('/')));
								film.setRating(rating);
							} catch (NumberFormatException e) {
								System.err.println("Unable to parse rating from string: " + ratingStr);
							}
						}
					} else if (getContents(h5).equals("Certification:")) {
						List<Certification> certs = new ArrayList<Certification>();

						List<Link> links = getLinks(div, "/List?certificates=");
						for (Link link : links) {
							int pos = link.getTitle().indexOf(':');
							Certification cert = new Certification(decodeHtmlEntities(link.getTitle()).substring(0, pos), link.getTitle()
									.substring(pos + 1));
							certs.add(cert);
						}
						film.setCertifications(certs);
					} else if (getContents(h5).equals("Release Date:")) {
						String str = getSectionText(div);
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
									System.err.println("Unable to parse date '" + str +"' of film with id '"+film.getId()+"'"); 
								}								
							}
						}
					}
				}
			}
		}

		if (film.getDate() == null) {
			System.err.println("Unable to find a date of film with the id '" + film.getId()+"' and the title '"+film.getTitle()+"'");
		}
	}

	/* package private */String decodeHtmlEntities(String s) {
//		int i = 0, j = 0, pos = 0;
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		
		while (pos< s.length()) {
			if (s.length()>pos+2 && s.substring(pos,pos+2).equals("&#")) {			
				int n = -1;
				int j = s.indexOf(';', pos);
				pos+=2;
				while (pos < j) {
					char c = s.charAt(pos);
					if ('0' <= c && c <= '9')
						n = (n == -1 ? 0 : n * 10) + c - '0';
					else
						break;
					pos++;					
				}
				if (n!=-1) {
					sb.append((char) n);
				}
			}
			else {
				sb.append(s.charAt(pos));
			}
			
			pos++;
		}
		
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private List<Link> getLinks(Element div, String linkStart) {
		List<Link> links = new ArrayList<Link>();
		for (Element a : (List<Element>) div.findAllElements(HTMLElementName.A)) {
			String href = a.getAttributeValue("href");
			String title = a.getTextExtractor().toString();
			if (href.startsWith(linkStart)) {
				Link link = new Link(IMDB_BASE_URL + href, decodeHtmlEntities(title));
				links.add(link);
			}
		}
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

	@SuppressWarnings("unchecked")
	private Element findFirstChild(Element parent, String tagName) {
		for (Element child : (List<Element>) parent.getChildElements()) {
			if (child.getName().equals(tagName)) {
				return child;
			}
		}
		return null;
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
	public SearchResult searchForVideoId(Mode mode, File filmFile) throws SourceException, MalformedURLException,
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
				Matcher m = EXTRACT_ID2_PATTERN.matcher(a.getAttributeValue("href"));
				if (m.matches()) {
					SearchResult result = new SearchResult(m.group(1), SOURCE_ID);
					return result;
				}
				
			}
		}
		return null;
	}

	private String normalizeQuery(String s) {
		s =s.toLowerCase();
		
		s = s.replaceAll("ä|á","a"); 
		s = s.replaceAll("ñ","n");
		s = s.replaceAll("ö","o");
		s = s.replaceAll("ü","u");
		s = s.replaceAll("ÿ","y");
		s = s.replaceAll("é","e");
		
		s = s.replaceAll("ß","ss");  //  German beta “ß” -> “ss”
		s = s.replaceAll("Æ","AE");  //  Æ
		s = s.replaceAll("æ","ae");  //  æ
		s = s.replaceAll("Ĳ","IJ");  //  Ĳ
		s = s.replaceAll("ĳ","ij");  //  ĳ
		s = s.replaceAll("Œ","Oe");  //  Œ
		s = s.replaceAll("œ","oe");  //  œ
//
//		s = s.replaceAll("\\x{00d0}\\x{0110}\\x{00f0}\\x{0111}\\x{0126}\\x{0127}","DDddHh"); 
//		s = s.replaceAll("\\x{0131}\\x{0138}\\x{013f}\\x{0141}\\x{0140}\\x{0142}","ikLLll"); 
//		s = s.replaceAll("\\x{014a}\\x{0149}\\x{014b}\\x{00d8}\\x{00f8}\\x{017f}","NnnOos"); 
//		s = s.replaceAll("\\x{00de}\\x{0166}\\x{00fe}\\x{0167}","TTtt");                     
		
		s =s.replaceAll(":|-|,|'", "");
		return s;
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
						String realTitle =normalizeQuery(m.group(1)); 
						if (realTitle.contains(normalizeQuery(query))) {
							if (Integer.parseInt(m.group(2)) > year) {
								year = Integer.parseInt(m.group(2));
								url = getFirstUrl(tr).getAttributeValue("href");
								if (url != null) {
									Matcher m2 = EXTRACT_ID_PATTERN.matcher(url);
									if (m2.matches()) {
										SearchResult result = new SearchResult(m2.group(1), SOURCE_ID);
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

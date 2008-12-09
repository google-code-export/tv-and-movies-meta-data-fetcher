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

import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;

/**
 * This class is a soure used to retrive information about films from {@link "www.imdb.com"}
 */
public class IMDBSource implements ISource {

	/** The ID of the the source */
	public static final String SOURCE_ID = "imdb";
	
	private static final String IMDB_BASE_URL = "http://www.imdb.com";
	private static final String URL_SUMMARY = "/title/tt$filmId$/";	
	private SimpleDateFormat RELEASE_DATE_FORMAT= new SimpleDateFormat("dd MMMMM yyyy");

	/**
	 * This always returns null as this source does not support reading episodes.	
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 * @param show The show the season belongs to.
	 * @param seasonNum The number of the season to read
	 */
	@Override
	public Season getSeason(Show show, int seasonNum)  {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(long showId)  {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 * @param season The season the episode belongs to.
	 * @param specialNumber The number of the special episode to read
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber) {
		return null;
	}

	/**
	 * Get the id of the source. 
	 * @return The id of the source
	 */
	@Override
	public String getSourceId() {
		return SOURCE_ID;
	}	

	/**
	 * This will always return null as this store does not support searching
	 * @param episodeFile The file the episode is located in
	 * @return Always returns null
	 */
	@Override
	public SearchResult searchForShowId(File episodeFile){
		return null;
	}
	
	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 * @param filmId The id of the film
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Film getFilm( long filmId)
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
			if (div.getAttributeValue("id") !=null && div.getAttributeValue("id").equals("tn15title")) {										
				Element h1 = findFirstChild(div,HTMLElementName.H1);
				if (h1!=null) {
					film.setTitle(getContents(h1));
				}
			}
			else if (div.getAttributeValue("class") !=null && div.getAttributeValue("class").equals("info")) {
				Element h5 = findFirstChild(div,HTMLElementName.H5);
				if (h5!=null) {
					if (getContents(h5).equals("Plot:")) {																	
						film.setSummary(getSectionText(div));
					}
					else if (getContents(h5).equals("Director:")){
						List<Link> links = getLinks(div,"/name");
						film.setDirectors(links);
					}
					else if (getContents(h5).equals("Writers")){
						List<Link> links = getLinks(div,"/name");
						film.setWriters(links);
					}
					else if (getContents(h5).equals("Genre:")){
						List<Link> links = getLinks(div,"/Sections/Genres");
						film.setGenres(new ArrayList<String>());
						for (Link link : links) {
							film.addGenre(link.getTitle());
						}
					}
					else if (getContents(h5).equals("User Rating:")){
						List<StartTag> tags = div.findAllStartTags(HTMLElementName.B);
						if (tags!=null && tags.size()==1) {		
							String ratingStr = tags.get(0).getElement().getTextExtractor().toString();
							try {
								float rating = Float.parseFloat(ratingStr.substring(0,ratingStr.indexOf('/')));								
								film.setRating(rating);							
							}
							catch (NumberFormatException e) {
								System.err.println("Unable to parse rating from string: " + ratingStr);
							}
						}
					}
					else if (getContents(h5).equals("Certification:")){
						List<Certification> certs = new ArrayList<Certification>();
						
						List<Link> links = getLinks(div,"/List?certificates=");
						for (Link link : links) {
							int pos = link.getTitle().indexOf(':');
							Certification cert = new Certification(link.getTitle().substring(0,pos),link.getTitle().substring(pos+1));
							certs.add(cert);
						}					
						film.setCertifications(certs);
					}
					else if (getContents(h5).equals("Release Date:")){
						String str = getSectionText(div);
						if (str!=null) {
							int pos = str.lastIndexOf(' ');
							if (pos!=-1) {
								str = str.substring(0,pos);
							}
							try {
								Date date = RELEASE_DATE_FORMAT.parse(str);
								film.setDate(date);
							} catch (ParseException e) {
								System.err.println("Unable to parse date: " +str);
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Link> getLinks(Element div,String linkStart) {
		List<Link> links = new ArrayList<Link>();
		for (Element a : (List<Element>)div.findAllElements(HTMLElementName.A)) {
			String href =a.getAttributeValue("href");
			String title = a.getTextExtractor().toString(); 
			if (href.startsWith(linkStart)) {
				Link link = new Link(IMDB_BASE_URL+href,title);
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

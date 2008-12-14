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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.Mode;
import org.stanwood.media.renamer.SearchResult;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;

/**
 * This source will pull TV show information from www.tv.com
 */
public class TVCOMSource implements ISource {

	private final static Pattern EPISODE_ID_PATTERN = Pattern
			.compile(".*episode\\/(.*)\\/summary.*");
	
	private final static Pattern SHOW_ID_PATTERN = Pattern
	.compile("\\/(.*)\\/show\\/(.*)\\/summary\\.html.*");

	private final static String TVCOM_BASE_URL = "http://www.tv.com/";
	private final static String URL_SUMMARY = "show/$showId$/summary.html";

	private final static String URL_EPISODES = "show/$showId$/episode_guide.html?printable=1";
	private final static String URL_EPISODE_LISTING = "show/$showId$/episode_listings.html?season=$seasonNum$";

	private final static String URL_SHOW_SEARCH = "search.php?type=Search&stype=ajax_search&search_type=program&qs=";
	
	/** The ID used to identify the www.tv.com TV source. */
	public static final String SOURCE_ID = "tvcom";

	/**
	 * This gets a special episode from the source. If it can't be found, then it will
	 * return null. It does this by accessing two different URL that are needed to 
	 * get all of the information.
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws MalformedURLException, IOException {
		Episode special = season.getSpecial(specialNumber);
		if (special == null) {
			prarseSeasonEpisodes(season);
			special = season.getSpecial(specialNumber);
		}
		return special;
	}

	/**
	 * This gets a episode from the source. If it can't be found, then it will
	 * return null. It does this by accessing two different URL that are needed to 
	 * get all of the information.
	 * @param season The season the special episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return The episode, or null if it can't be found
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum)
			throws MalformedURLException, IOException {
		Episode episode = season.getEpisode(episodeNum);
		if (episode == null) {
			prarseSeasonEpisodes(season);
			episode = season.getEpisode(episodeNum);
		}
		return episode;
	}

	/**
	 * This will get a season from the source. If the season can't be found,
	 * then it will return null. This also gets all the episode data as well.
	 * Ths is because the episodes and seasons are linked in this source.
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException,
			IOException {
		Season season = new Season(show, seasonNum);		
		season.setListingUrl(new URL(getSeasonEposideListing(show.getShowId(),
				seasonNum)));
		season.setDetailedUrl(new URL(getSeasonEposideDetailed(
				show.getShowId(), seasonNum)));

		prarseSeasonEpisodes(season);

		if (season.getEpisodeCount() == 0) {
			return null;
		}

		return season;
	}

	private void prarseSeasonEpisodes(Season season) throws IOException,
			MalformedURLException {
		Source source = getSource(season.getListingUrl());
		parse(season, source);
		source = getSource(season.getDetailedUrl());
		defailedParse(season, source);
	}

	/* package for test */Source getSource(URL detailedUrl) throws IOException {
		return new Source(detailedUrl);
	}

	private void defailedParse(Season season, Source source)
			throws MalformedURLException {
		for (Episode episode : season.getEpisodes()) {
			populateEpisodeWithDetail(source, episode);
		}

		for (Episode special : season.getSpecials()) {
			populateEpisodeWithDetail(source, special);
		}
	}

	@SuppressWarnings("unchecked")
	private void populateEpisodeWithDetail(Source source, Episode episode) {
		List<Element> divs = source.findAllElements(HTMLElementName.DIV);		
		
		for (Element div : divs) {
			String classAttr = div.getAttributeValue("class");
			if (classAttr != null && classAttr.equals("pl-5 pr-5")) {
				List<Element> h1s = div.getChildElements();
				Element h1 = h1s.get(0);
				if (h1.getTextExtractor().toString().trim().startsWith(
						episode.getEpisodeSiteId() + ".")) {
					// Element innerDiv = h1s.get(1);
					Element summary = h1s.get(2);
					episode.setSummary(summary.getTextExtractor().toString());
				}
				Element div1 = ((List<Element>) div
						.findAllElements(HTMLElementName.DIV)).get(0);
				Iterator it = div1.getNodeIterator();
				String span = "";							
				List<Link>guestStars = new ArrayList<Link>();
				List<Link>directors = new ArrayList<Link>();
				List<Link>writers = new ArrayList<Link>();	
				while (it.hasNext()) {
					Tag o = getNextStartTag(it);
						
					if (o!=null) {
						Tag el = (Tag) o;
						if (el.getName().equals("span")) {							
							Element spanEl = el.getElement();							
							span =  spanEl.getTextExtractor().toString().toLowerCase().trim();							
							Tag tag = getNextEndTag(it);
							while ((tag = getNextTag(it)).getName().equals(HTMLElementName.A)) {
								Element link = tag.getElement();
								String href = link.getAttributeValue("href");
								String title = link.getTextExtractor().toString().trim();
								if (span.equals("global rating:")) {									
									episode.setRating(Float.parseFloat(title));
								}
								else if (span.equals("guest star:")) {
									guestStars.add(new Link(href,title));
								}
								else if (span.equals("director:")) {
									directors.add(new Link(href,title));
								}
								else if (span.equals("writer:")) {
									writers.add(new Link(href,title));
								}
								else if (span.equals("first aired date:")) {
									
								}
								else {
									System.out.println("Unhandled span: " + span);
								}
									
								tag = getNextTag(it);
							}
							span = "";
						}							
					}
				}
				episode.setWriters(writers);		
				episode.setGuestStars(guestStars);
				episode.setDirectors(directors);
			}
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void parse(Season season, Source source)
			throws MalformedURLException {
		int specialCount = 0;
		List<Element> elements = source.findAllElements("tbody");
		Element tbody = elements.get(0);
		List<Element> trs = tbody.findAllElements(HTMLElementName.TR);
		int epCount = 0;
		for (Element tr : trs) {
			List<Element> tds = tr.findAllElements(HTMLElementName.TD);
			int totalNum = -1;
			String title = null;
			Date airDate = null;
			int episodeNumber = -1;
			boolean special = false;
			String specialName = null;
			String episodeSiteId = null;
			String prodCode = null;
			String strUrl = null;

			for (Element td : tds) {
				if (td.getAttributeValue("class").equals("num")) {
					episodeSiteId = td.getTextExtractor().toString();
					try {
						totalNum = Integer.parseInt(episodeSiteId);
						epCount++;
						episodeNumber = epCount;
					} catch (NumberFormatException e) {
						special = true;
						specialName = td.getTextExtractor().toString();
					}
				} else if (td.getAttributeValue("class").equals("ep_title")) {
					for (Element child : (List<Element>) td
							.findAllElements(HTMLElementName.A)) {
						strUrl = child.getAttributeValue("href");
						int queryPos = strUrl.lastIndexOf('?');
						if (queryPos != -1) {
							strUrl = strUrl.substring(0, queryPos);
						}
					}
					title = td.getTextExtractor().toString();
				} else if (td.getAttributeValue("class").equals("ep_air_date")) {
					String sAirDate = td.getTextExtractor().toString();
					DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
					try {
						airDate = (Date) formatter.parse(sAirDate);
					} catch (ParseException e) {
						airDate = null;
					}
				} else if (td.getAttributeValue("class").equals("ep_prod_code")) {
					prodCode = td.getTextExtractor().toString();
				}
			}
			long episodeId = -1;
			URL url = null;
			if (strUrl != null) {
				url = new URL(strUrl);
				Matcher m = EPISODE_ID_PATTERN.matcher(strUrl);
				if (m.find()) {
					episodeId = Long.parseLong(m.group(1));
				}
			}
			if (!special) {
				createEpisode(totalNum, title, airDate, season, episodeNumber,
						special, specialName, episodeSiteId, prodCode, url,
						episodeId);
			} else {
				createEpisode(totalNum, title, airDate, season, specialCount++,
						special, specialName, episodeSiteId, prodCode, url,
						episodeId);
			}
		}
	}

	private void createEpisode(int totalNum, String title, Date airDate,
			Season season, int episodeNumber, boolean special,
			String specialName, String episodeSiteId, String prodCode, URL url,
			long episodeId) {

		Episode episode = createEpisode(totalNum, title, airDate,
				episodeNumber, special, specialName, episodeSiteId, prodCode,
				url, season, episodeId);

		if (special) {
			season.addSepcial(episode);
		} else {
			season.addEpisode(episode);
		}
	}

	private Episode createEpisode(int totalNum, String title, Date airDate,
			int episodeNumber, boolean special, String specialName,
			String episodeSiteId, String prodCode, URL url, Season season,
			long episodeId) {
		Episode episode = new Episode(episodeNumber, season);
		episode.setTitle(title);		
		episode.setDate(airDate);
		episode.setSpecial(special);
		episode.setSpecialName(specialName);
		episode.setSiteId(episodeSiteId);
		episode.setProductionCode(prodCode);
		episode.setSummaryUrl(url);
		episode.setEpisodeId(episodeId);
		return episode;
	}

	/**
	 * This will get a show from the source. If the season can't be found, then it 
	 * will return null. 	
	 * @param showId The id of the show to get.
	 * @return The show if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Show getShow( long showId)
			throws SourceException, MalformedURLException, IOException {
		Show show = new Show( showId);
		show.setShowURL(new URL(getSummaryURL(show.getShowId())));
		show.setSourceId(SOURCE_ID);
		Source source = getSource(show.getShowURL());
		parseShow(source, show);
		return show;
	}

	

	@SuppressWarnings("unchecked")
	private void parseShow(Source source, Show show)
			throws MalformedURLException {

		List<Element> elements = source.findAllElements(HTMLElementName.SPAN);
		List<String> genres = new ArrayList<String>();
		for (Element element : elements) {
			String classValue = element.getAttributeValue("class");
			if (classValue != null) {
				if (classValue.equals("long")) {
					show.setLongSummary(element.getTextExtractor().toString());
				} else if (classValue.equals("short")) {
					show.setShortSummary(element.getTextExtractor().toString());
				} else if (classValue.equals("genres")) {
					StringTokenizer tok = new StringTokenizer(element
							.getTextExtractor().toString(), ",");
					while (tok.hasMoreTokens()) {
						genres.add(tok.nextToken().trim());
					}
				}
			}
		}

		if (show.getShortSummary() == null && show.getLongSummary() != null) {
			String shortSummary = show.getLongSummary();
			if (shortSummary.length() > 296) {
				shortSummary = shortSummary.substring(0, 296);
				if (!show.getLongSummary().equals(shortSummary)) {
					shortSummary += "...";
				}
			}
			show.setShortSummary(shortSummary);
		}
		show.setGenres(genres);

		elements = source.findAllElements(HTMLElementName.DIV);
		for (Element element : elements) {
			String classValue = element.getAttributeValue("class");
			String id = element.getAttributeValue("id");
			if (classValue != null) {
				if (classValue.equals("content_title")) {
					String text = ((Element) element.getChildElements().get(0))
							.getTextExtractor().toString();
					show.setName(text.replaceAll(":.*$", ""));
				}
			}
			if (id != null) {
				if (id.equals("topslot")) {
					for (Element e : (List<Element>) element.findAllElements()) {
						if (e.getName().equals(HTMLElementName.IMG)) {							
							String url = e.getAttributeValue("src");
							if (url!=null) {
								show.setImageURL(new URL(url));
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private EndTag getNextEndTag(Iterator it) {
		Object o;
		do {					 
			o = it.next();
		} while ((!(o instanceof EndTag)) && it.hasNext()  );
		if (o instanceof StartTag) {
			return (EndTag)o;
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Tag getNextTag(Iterator it) {
		Object o;
		do {					 
			o = it.next();
		} while ((!(o instanceof Tag)) && it.hasNext()  );
		if (o instanceof Tag) {
			return (Tag)o;
		}
		else {
			return null;
		}	
	}

	@SuppressWarnings("unchecked")
	private StartTag getNextStartTag(Iterator it) {
		Object o;
		do {					 
			o = it.next();
		} while ((!(o instanceof StartTag)) && it.hasNext()  );
		if (o instanceof StartTag) {
			return (StartTag)o;
		}
		else {
			return null;
		}
	}	
	
	private final static String getSummaryURL(long showId) {
		return TVCOM_BASE_URL
				+ URL_SUMMARY
						.replaceAll("\\$showId\\$", String.valueOf(showId));
	}

	private final static String getSeasonEposideListing(long showId,
			int seasonNumber) {
		String url = TVCOM_BASE_URL + URL_EPISODE_LISTING;
		url = url.replaceAll("\\$showId\\$", String.valueOf(showId));
		url = url.replaceAll("\\$seasonNum\\$", String.valueOf(seasonNumber));
		return url;
	}

	private String getSeasonEposideDetailed(long showId, int seasonNumber) {
		String url = TVCOM_BASE_URL + URL_EPISODES;
		url = url.replaceAll("\\$showId\\$", String.valueOf(showId));
		url = url.replaceAll("\\$seasonNum\\$", String.valueOf(seasonNumber));
		return url;
	}
	
	private String getShowSearchUrl(String query) {
		String url = TVCOM_BASE_URL + URL_SHOW_SEARCH + query.replaceAll(" ", "+");
		return url;
	}

	/**
	 * This will return the ID of the source. @see org.stanwood.media.source.TVCOMSource#SOURCE_ID
	 * @returns The source ID
	 */
	@Override
	public String getSourceId() {
		return SOURCE_ID;
	}

	/**
	 * This will search for a show ID from the source. It uses the name of the show directory as the show name
	 * when it does a search, and uses the first result it finds.
	 * @param episodeFile The file the episode is located in
	 * @param mode The mode that the search operation should be performed in
	 * @return The results of the search, or null if the show could not be found
	 * @throws SourceException Thrown if their is a problem retrieving the data 
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SearchResult searchForVideoId(Mode mode,File episodeFile) throws SourceException, MalformedURLException, IOException {
		if (mode!=Mode.TV_SHOW) {
			return null;
		}
		List<SearchResult> results = new ArrayList<SearchResult>();
		Source source = getSource(new URL(getShowSearchUrl(episodeFile.getParentFile().getName())));		
		List<Element> elements = source.findAllElements(HTMLElementName.LI);		
		for (Element element : elements) {
			if (element.getAttributeValue("class")!=null && element.getAttributeValue("class").equals("result")) {
				List<Element> elements2 = source.findAllElements(HTMLElementName.A);
				for (Element element2 : elements2) {
					String href =element2.getAttributeValue("href");
					Matcher m = SHOW_ID_PATTERN.matcher(href);
					if (m.find()) {
						SearchResult result = new SearchResult(Long.parseLong(m.group(2)),SOURCE_ID);
						results.add(result);
					}					
				}
			}
		}
		
		if (results.size()>=1) {			
			return results.get(0);
		}
		return null;
	}

	/**
	 * Films are not supported, so this will always return null.
	 * @param filmId The id of the film
	 * @return Always returns null
	 */
	@Override
	public Film getFilm(long filmId) {
		return null;
	}	
}

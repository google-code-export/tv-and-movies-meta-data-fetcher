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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.search.ShowSearcher;

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

	private final static Pattern EPISODE_ID_PATTERN = Pattern.compile(".*episode\\/(.*)\\/summary.*");

	private final static Pattern SHOW_ID_PATTERN = Pattern.compile(".*\\.com\\/(.*)\\/show\\/(.*)\\/summary\\.html.*");

	private final static Pattern PRINT_PAGE_TITLE_PATTERN = Pattern.compile(".*?(\\d+)\\..*");

	private final static Pattern EPISODE_EXTRACT_PATTERN = Pattern
			.compile("Season (\\d+), Episode (\\d+).*Aired: (.*)");
	private final static Pattern SPECIAL_EXTRACT_PATTERN = Pattern.compile("Special\\. Season (\\d+).*Aired: (.*)");

	private final static String TVCOM_BASE_URL = "http://www.tv.com/";

	private final static String URL_EPISODE_LISTING_FULL = "$textId$/show/$showId$/episode.html?season=$seasonNum$";
	private final static String URL_EPISODES = "$textId$/show/$showId$/episode_guide.html?printable=1";
	// private final static String URL_EPISODE_LISTING = "$textId$/show/$showId$/episode_listings.html?season=$seasonNum$";

	private final static String URL_SHOW_SEARCH = "search.php?type=Search&stype=ajax_search&search_type=program&qs=";

	/** The ID used to identify the www.tv.com TV source. */
	public static final String SOURCE_ID = "tvcom";

	/**
	 * This gets a special episode from the source. If it can't be found, then it will return null. It does this by
	 * accessing two different URL that are needed to get all of the information.
	 *
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber) throws MalformedURLException, IOException, SourceException {
		Episode special = season.getSpecial(specialNumber);
		if (special == null) {
			prarseSeasonEpisodes(season);
			special = season.getSpecial(specialNumber);
		}
		return special;
	}

	/**
	 * This gets a episode from the source. If it can't be found, then it will return null. It does this by accessing
	 * two different URL that are needed to get all of the information.
	 *
	 * @param season The season the special episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return The episode, or null if it can't be found
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum) throws MalformedURLException, IOException, SourceException {
		Episode episode = season.getEpisode(episodeNum);
		if (episode == null) {
			prarseSeasonEpisodes(season);
			episode = season.getEpisode(episodeNum);
		}
		return episode;
	}

	/**
	 * This will get a season from the source. If the season can't be found, then it will return null. This also gets
	 * all the episode data as well. Ths is because the episodes and seasons are linked in this source.
	 *
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException, IOException {
		Season season = new Season(show, seasonNum);
		String textId = getShowTextId(show);
		season.setListingUrl(new URL(getSeasonEposideListing(textId,show.getShowId(), seasonNum)));
		season.setDetailedUrl(new URL(getSeasonEposideDetailed(textId,show.getShowId(), seasonNum)));

		prarseSeasonEpisodes(season);

		if (season.getEpisodeCount() == 0) {
			return null;
		}

		return season;
	}

	private String getShowTextId(Show show) throws SourceException, MalformedURLException, IOException {
		Matcher m = SHOW_ID_PATTERN.matcher(show.getShowURL().toExternalForm());
		if (m.matches()) {
			return m.group(1);
		}
		else {
			SearchResult result = searchForTvShow(show.getName());
			if (result!=null && result.getId().equals(show.getShowId())) {
				m = SHOW_ID_PATTERN.matcher(result.getUrl());
				if (m.matches()) {
					return m.group(1);
				}
			}
		}
		throw new SourceException("Unable to work out the text id of the show: " + show.getShowId());
	}

	private void prarseSeasonEpisodes(Season season) throws IOException, MalformedURLException, SourceException {
		String textId = getShowTextId(season.getShow());
		Source source = getSource(new URL(getSeasonEposideListing(textId,season.getShow().getShowId(), season
				.getSeasonNumber())));
		parse(season, source);
		source = getSource(new URL(getSeasonEposideDetailed(textId,season.getShow().getShowId(), season.getSeasonNumber())));
		defailedParse(season, source);
	}

	/* package for test */Source getSource(URL detailedUrl) throws IOException {
		return new Source(detailedUrl);
	}

	private void defailedParse(Season season, Source source) throws MalformedURLException {
		for (Episode episode : season.getEpisodes()) {
			populateEpisodeWithDetail(source, episode);
		}

		for (Episode special : season.getSpecials()) {
			populateEpisodeWithDetail(source, special);
		}
	}

	private Link getDetailedPageEpisodeLink(Element div) {
		Element h1 = ParseHelper.findFirstChild(div, HTMLElementName.H1, null);
		if (h1 != null) {
			List<Link> links = ParseHelper.getLinks(TVCOM_BASE_URL, h1, null);
			if (links != null && links.size() > 0) {
				return links.get(0);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void populateEpisodeWithDetail(Source source, final Episode episode) {
		Element div = ParseHelper.findFirstElement(source, HTMLElementName.DIV, new IFilterElement() {
			@Override
			public boolean accept(Element div) {
				String classAttr = div.getAttributeValue("class");
				if (classAttr != null && classAttr.equals("pl-5 pr-5")) {
					Link link = getDetailedPageEpisodeLink(div);
					Matcher m = EPISODE_ID_PATTERN.matcher(link.getURL());
					if (m.matches()) {
						if (Long.parseLong(m.group(1)) == episode.getEpisodeId()) {
							return true;
						}
					}
				}
				return false;
			}
		});

		if (div != null) {
			Link epLink = getDetailedPageEpisodeLink(div);
			Matcher m = PRINT_PAGE_TITLE_PATTERN.matcher(epLink.getTitle());
			if (m.matches()) {
				episode.setShowEpisodeNumber(Long.parseLong(m.group(1)));
			} else {
				episode.setShowEpisodeNumber(-1);
			}
			Element div1 = ParseHelper.findFirstElement(div, HTMLElementName.DIV, null);

			Iterator it = div1.getNodeIterator();
			String span = "";
			List<Link> guestStars = new ArrayList<Link>();
			List<Link> directors = new ArrayList<Link>();
			List<Link> writers = new ArrayList<Link>();
			while (it.hasNext()) {
				Tag o = getNextStartTag(it);

				if (o != null) {
					Tag el = o;
					if (el.getName().equals("span")) {
						Element spanEl = el.getElement();
						span = spanEl.getTextExtractor().toString().toLowerCase().trim();
						Tag tag = getNextEndTag(it);
						while ((tag = getNextTag(it)).getName().equals(HTMLElementName.A)) {
							Element link = tag.getElement();
							String href = link.getAttributeValue("href");
							String title = link.getTextExtractor().toString().trim();
							if (span.equals("global rating:")) {
								episode.setRating(Float.parseFloat(title));
							} else if (span.equals("guest star:")) {
								guestStars.add(new Link(href, title));
							} else if (span.equals("director:")) {
								directors.add(new Link(href, title));
							} else if (span.equals("writer:")) {
								writers.add(new Link(href, title));
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

	private void parse(Season season, Source source) throws MalformedURLException {
		List<Element> elements = ParseHelper.findAllElements(source, HTMLElementName.LI, new IFilterElement() {
			@Override
			public boolean accept(Element element) {
				return element.getAttributeValue("class") != null
						&& element.getAttributeValue("class").startsWith("episode");
			}
		});

		for (Element eposiodeLi : elements) {
			String title = null;
			Date airDate = null;
			boolean special = false;
			long episodeSiteId = -1;
			int episodeNumber = -1;
			URL url = null;
			String description = "";

			Element div = ParseHelper.findFirstChild(eposiodeLi, HTMLElementName.DIV, true, new IFilterElement() {
				@Override
				public boolean accept(Element element) {
					return element.getAttributeValue("class").equals("meta");
				}
			});

			// Get the air date, episode number and findout if it's a special
			if (div != null) {
				String text = div.getTextExtractor().toString();
				Matcher m = EPISODE_EXTRACT_PATTERN.matcher(text);
				int seasonNumber = -1;
				if (m.matches()) {
					special = false;
					airDate = parseAirDate(m.group(3));
					episodeNumber = Integer.parseInt(m.group(2));
					seasonNumber = Integer.parseInt(m.group(1));
				} else {
					m = SPECIAL_EXTRACT_PATTERN.matcher(text);
					if (m.matches()) {
						special = true;
						airDate = parseAirDate(m.group(2));
						seasonNumber = Integer.parseInt(m.group(1));
					}
				}
				if (seasonNumber != season.getSeasonNumber()) {
					continue;
				}
			}

			// Get the title, url and site id of the episode
			Element h3 = ParseHelper.findFirstChild(eposiodeLi, HTMLElementName.H3, true, null);
			if (h3 != null) {
				List<Link> links = ParseHelper.getLinks("", h3, new IFilterLink() {
					@Override
					public boolean accept(Link link) {
						Matcher m = EPISODE_ID_PATTERN.matcher(link.getURL());
						return (m.matches());
					}
				});

				if (links != null && !links.isEmpty()) {
					Link link = links.get(0);
					title = link.getTitle();
					url = new URL(link.getURL());
					Matcher m = EPISODE_ID_PATTERN.matcher(link.getURL());
					if (m.find()) {
						episodeSiteId = Long.parseLong(m.group(1));
					}
				}
			}

			Element p = ParseHelper.findFirstChild(eposiodeLi, HTMLElementName.P, true, new IFilterElement() {
				@Override
				public boolean accept(Element element) {
					return element.getAttributeValue("class") != null
							&& element.getAttributeValue("class").equals("synopsis");
				}
			});

			if (p != null) {
				description = p.getTextExtractor().toString();
			}

			if (!special) {
				Episode episode1 = createEpisode(title, airDate, episodeNumber, special, url, season, episodeSiteId);
				episode1.setSummary(description);
				season.addEpisode(episode1);
			} else {
				Episode special1 = createEpisode(title, airDate, episodeNumber, special, url, season, episodeSiteId);
				special1.setSummary(description);
				season.addSepcial(special1);
			}

		}
	}

	private Date parseAirDate(String dateString) {
		Date airDate;
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		try {
			airDate = formatter.parse(dateString);
		} catch (ParseException e) {
			airDate = null;
		}
		return airDate;
	}

	private Episode createEpisode(String title, Date airDate, int episodeNumber, boolean special, URL url,
			Season season, long episodeSiteId) {
		Episode episode = new Episode(episodeNumber, season);
		episode.setTitle(title);
		episode.setDate(airDate);
		episode.setSpecial(special);
		episode.setSummaryUrl(url);
		episode.setEpisodeId(episodeSiteId);
		return episode;
	}

	/**
	 * This will get a show from the source. If the season can't be found, then it will return null.
	 *
	 * @param showId The id of the show to get.
	 * @param url String url of the show
	 * @return The show if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Show getShow(String showId,URL url) throws SourceException, MalformedURLException, IOException {
		Show show = new Show(showId);
		show.setShowURL(url);
		show.setSourceId(SOURCE_ID);
		Source source = getSource(show.getShowURL());
		parseShow(source, show);
		return show;
	}

	private void parseShow(Source source, Show show) throws MalformedURLException {

		List<String> genres = new ArrayList<String>();
		Element pShortSummary = ParseHelper.findFirstChild(source, HTMLElementName.P, true, new IFilterElement() {
			@Override
			public boolean accept(Element element) {
				return (element.getAttributeValue("id") != null && element.getAttributeValue("id").equals("trunc_summ"));
			}
		});
		if (pShortSummary != null) {
			show.setShortSummary(pShortSummary.getTextExtractor().toString());
		}

		Element pLongSummary = ParseHelper.findFirstChild(source, HTMLElementName.P, true, new IFilterElement() {
			@Override
			public boolean accept(Element element) {
				return (element.getAttributeValue("id") != null && element.getAttributeValue("id").equals("whole_summ"));
			}
		});
		if (pLongSummary != null) {
			String summary = pLongSummary.getTextExtractor().toString();
			summary = summary.substring(0, summary.length() - 6);
			show.setLongSummary(summary);
		}

		Element buz = ParseHelper.findFirstChild(source, HTMLElementName.DIV, true, new IFilterElement() {
			@Override
			public boolean accept(Element element) {
				return (element.getAttributeValue("id") != null && element.getAttributeValue("id").equals(
						"show_buzz_info"));
			}
		});
		if (buz != null) {
			List<Link> genreLinks = ParseHelper.getLinks("", buz, new IFilterLink() {
				@Override
				public boolean accept(Link link) {
					return (link.getURL().endsWith("today.html"));
				}
			});

			for (Link link : genreLinks) {
				genres.add(link.getTitle());
			}
		}

		Element h1 = ParseHelper.findFirstChild(source, HTMLElementName.H1, true, new IFilterElement() {
			@Override
			public boolean accept(Element element) {
				return (element.getAttributeValue("class") != null && element.getAttributeValue("class").equals(
						"show_title"));
			}
		});
		if (h1 != null) {
			show.setName(h1.getTextExtractor().toString());
		}

		if (show.getShortSummary() == null && show.getLongSummary() != null) {
			show.setShortSummary(show.getLongSummary());
		}

		if (show.getShortSummary() != null && show.getShortSummary().length() > 296) {
			String shortSummary = show.getShortSummary();
			shortSummary = shortSummary.substring(0, 296);
			shortSummary += "...";
			show.setShortSummary(shortSummary);
		}
		show.setGenres(genres);
		show.setImageURL(new URL("http://image.com.com/tv/images/content_headers/program_new/" + show.getShowId()
				+ ".jpg"));
	}

	@SuppressWarnings("unchecked")
	private EndTag getNextEndTag(Iterator it) {
		Object o;
		do {
			o = it.next();
		} while ((!(o instanceof EndTag)) && it.hasNext());
		if (o instanceof StartTag) {
			return (EndTag) o;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Tag getNextTag(Iterator it) {
		Object o;
		do {
			o = it.next();
		} while ((!(o instanceof Tag)) && it.hasNext());
		if (o instanceof Tag) {
			return (Tag) o;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private StartTag getNextStartTag(Iterator it) {
		Object o;
		do {
			o = it.next();
		} while ((!(o instanceof StartTag)) && it.hasNext());
		if (o instanceof StartTag) {
			return (StartTag) o;
		} else {
			return null;
		}
	}

	private final static String getSeasonEposideListing(String showTextId,String showId, int seasonNumber) {
		String url = TVCOM_BASE_URL + URL_EPISODE_LISTING_FULL;
		url = url.replaceAll("\\$textId\\$",showTextId);
		url = url.replaceAll("\\$showId\\$", showId);
		url = url.replaceAll("\\$seasonNum\\$", String.valueOf(seasonNumber));
		return url;
	}

	private String getSeasonEposideDetailed(String showTextId,String showId, int seasonNumber) {
		String url = TVCOM_BASE_URL + URL_EPISODES;
		url = url.replaceAll("\\$textId\\$",showTextId);
		url = url.replaceAll("\\$showId\\$", showId);
		url = url.replaceAll("\\$seasonNum\\$", String.valueOf(seasonNumber));
		return url;
	}

	private String getShowSearchUrl(String query) {
		String fixedQuery = query;
		fixedQuery = fixedQuery.replaceAll(" ", "+");
		fixedQuery = fixedQuery.replaceAll("&", "%26");
		String url = TVCOM_BASE_URL + URL_SHOW_SEARCH + fixedQuery;
		return url;
	}

	/**
	 * This will return the ID of the source. @see org.stanwood.media.source.TVCOMSource#SOURCE_ID
	 *
	 * @returns The source ID
	 */
	@Override
	public String getSourceId() {
		return SOURCE_ID;
	}

	/**
	 * This will search for a show ID from the source. It uses the name of the show directory as the show name when it
	 * does a search, and uses the first result it finds.
	 *
	 * @param episodeFile The file the episode is located in
	 * @param mode The mode that the search operation should be performed in
	 * @return The results of the search, or null if the show could not be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public SearchResult searchForVideoId(File rootMediaDir,Mode mode,File episodeFile,String renamePattern) throws SourceException, MalformedURLException,
			IOException {
		if (mode != Mode.TV_SHOW) {
			return null;
		}

		ShowSearcher s = new ShowSearcher() {
			@Override
			public SearchResult doSearch(String name) throws MalformedURLException, IOException {
				return searchForTvShow(name);
			}
		};

		return s.search(episodeFile,rootMediaDir,renamePattern);
	}

	@SuppressWarnings("unchecked")
	private SearchResult searchForTvShow(String name) throws MalformedURLException, IOException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		URL url = new URL(getShowSearchUrl(name));
		Source source = getSource(url);
		List<Element> elements = source.findAllElements(HTMLElementName.LI);
		for (Element element : elements) {
			if (element.getAttributeValue("class") != null
					&& element.getAttributeValue("class").equals("result search_spotlight_new")) {
				List<Element> elements2 = source.findAllElements(HTMLElementName.A);
				for (Element element2 : elements2) {
					String href = element2.getAttributeValue("href");
					Matcher m = SHOW_ID_PATTERN.matcher(href);
					if (m.find()) {
						SearchResult result = new SearchResult(m.group(2), SOURCE_ID,href.substring(0,href.indexOf('?')));
						results.add(result);
					}
				}
			}
		}

		if (results.size() >= 1) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * Films are not supported, so this will always return null.
	 *
	 * @param filmId The id of the film
	 * @return Always returns null
	 */
	@Override
	public Film getFilm(String filmId) {
		return null;
	}
}

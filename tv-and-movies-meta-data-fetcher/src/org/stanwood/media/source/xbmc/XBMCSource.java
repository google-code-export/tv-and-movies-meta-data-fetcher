package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.IVideoActors;
import org.stanwood.media.model.IVideoGenre;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.xml.IterableNodeList;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This is a source that reads the details using XBMC addons. It is capable of reading both TV Show and Film
 * information if the XBMC Scraper supports it. Any parameters set on this source are passed through to the XBMC
 * scrapers.
 */
public class XBMCSource extends XMLParser implements ISource {

	private static final SimpleDateFormat FILM_YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy");
	private static final SimpleDateFormat EPISODE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final static Log log = LogFactory.getLog(XBMCSource.class);

	private XBMCAddon addon;
	private String id;
	private XBMCAddonManager mgr;

	/**
	 * Used to create a instance of this class
	 * @param mgr The addon manager
	 * @param addonId The ID of the sources XBMC addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public XBMCSource(XBMCAddonManager mgr,String addonId) throws XBMCException {
		this.id = addonId;
		this.mgr = mgr;
		addon = mgr.getAddon(addonId);
	}

	/**
	 * Called to retrieve the information on a episode
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode
	 * @param file The film file if looking up a files details, or NULL
	 * @return The episode
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Throw if their is a IO related problem
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum,File file)
			throws SourceException, MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return parseEpisode(season,episodeNum);
	}

	private Episode parseEpisode(Season season, int episodeNum) throws SourceException, IOException {
		List<XBMCEpisode> episodes = getEpisodeList(season.getShow(), season);
		for (final XBMCEpisode episode : episodes) {
			if (episode.getEpisodeNumber() == episodeNum) {
				StreamProcessor processor = new StreamProcessor(mgr.getStreamToURL(episode.getUrl())) {
					@Override
					public void processContents(String contents) throws SourceException {
						Document doc = addon.getScraper(Mode.TV_SHOW).getGetEpisodeDetails(contents,String.valueOf(episode.getEpisodeId()));
						parseEpisode(episode, doc);
					}
				};
				processor.handleStream();
				return episode;
			}
		}
		return null;
	}

	/**
	 * This will get a season from the source. If the season can't be found,
	 * then it will return null.
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException,
			IOException {
		checkMode(Mode.TV_SHOW);
		return parseSeason(show,seasonNum);
	}

	private Season parseSeason(final Show show, final int seasonNum) throws SourceException, IOException {
		Season season = new Season(show,seasonNum);
		try {
			URL url = new URL(show.getExtraInfo().get("episodeGuideURL"));
			season.setURL(url);
			if (getEpisodeList(show, season).size()>0) {
				return season;
			}
			return null;
		} catch (MalformedURLException e) {
			throw new SourceException("Unable to parse season",e);
		}
	}

	private List<XBMCEpisode>getEpisodeList(final Show show,final Season season) throws SourceException, IOException {
		final List<XBMCEpisode>episodes = new ArrayList<XBMCEpisode>();

		StreamProcessor processor = new StreamProcessor(mgr.getStreamToURL(season.getURL())) {
			@Override
			public void processContents(String contents) throws SourceException {
				Document doc = addon.getScraper(Mode.TV_SHOW).getGetEpisodeList(contents,show.getShowURL());
				try {
    				IterableNodeList episodesList = selectNodeList(doc,"/episodeguide/episode");
    				if (episodesList!=null) {
    					for (Node episodeNode : episodesList) {
    						try {
	    						if (getIntegerFromXML(episodeNode, "season/text()")==season.getSeasonNumber()) {
	    							XBMCEpisode ep = new XBMCEpisode(getIntegerFromXML(episodeNode, "epnum/text()"), season);
		    						ep.setTitle(getStringFromXML(episodeNode, "title/text()"));
		    						ep.setUrl(new URL(getStringFromXML(episodeNode, "url/text()")));
		    						ep.setEpisodeId(getStringFromXML(episodeNode, "id/text()"));
		    						try {
		    							ep.setDate(EPISODE_DATE_FORMAT.parse(getStringFromXML(episodeNode, "aired/text()")));
		    						}
		    						catch (XMLParserNotFoundException e) {
		    							if (log.isDebugEnabled()) {
		    								log.debug("No date for episode: " + ep.getEpisodeId());
		    							}
		    						}
		    						episodes.add(ep);
	    						}
    						}
    						catch (XMLParserNotFoundException e) {
    							// Ignore
    						}
    					}
    				}
				} catch (XMLParserException e) {
					throw new SourceException("Unable to parse season",e);
				} catch (MalformedURLException e) {
					throw new SourceException("Unable to parse season",e);
				} catch (ParseException e) {
					throw new SourceException("Unable to parse season",e);
				}
			}
		};

		processor.handleStream();
		return episodes;

	}

	/**
	 * This will get a show from the source. If the show can't be found, then it
	 * will return null.
	 * @param showId The id of the show to get.
	 * @param url String url of the show
	 * @param file The media file if looking up a files details, or NULL
	 * @return The show if it can be found, otherwise null.
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Show getShow(final String showId, URL url,File file) throws SourceException,
			MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return parseShow(showId, url,file);
	}

	private Show parseShow(final String showId, URL url,final File file) throws IOException,
			SourceException {
		final Show show = new Show(showId);
		show.setShowURL(url);
		show.setSourceId(getSourceId());
		StreamProcessor processor = new StreamProcessor(mgr.getStreamToURL(url)) {
			@Override
			public void processContents(String contents) throws SourceException {
				try {
	    			Document doc = addon.getScraper(Mode.TV_SHOW).getGetDetails(file,contents,showId);
	    			try {
	    				String longSummary = getStringFromXML(doc, "details/plot/text()");
						show.setLongSummary(longSummary);
						if (longSummary.length()>100) {
							show.setShortSummary(longSummary.substring(0,99)+"...");
						}
						else {
							show.setShortSummary(longSummary);
						}
					} catch (XMLParserNotFoundException e) {
						// Ignore
					}

					try {
						show.setName(getStringFromXML(doc, "details/title/text()"));
					} catch (XMLParserNotFoundException e) {
						// Ignore
					}

					parseGenres(show, doc);

					if (show.getImageURL()==null) {
						try {
							show.setImageURL(new URL(getStringFromXML(doc, "details/thumb/text()")));
						} catch (XMLParserNotFoundException e) {
							// Ignore
						}
					}

					show.getExtraInfo().put("episodeGuideURL", getStringFromXML(doc, "details/episodeguide/url/text()"));

				}
				catch (XMLParserException e) {
					throw new SourceException("Unable to parse show details",e);
				}
				catch (MalformedURLException e) {
					throw new SourceException("Unable to parse show details",e);
				}
			}


		};
		processor.handleStream();

		if (show.getName() == null || show.getLongSummary()==null) {
			return null; // Show details not complete
		}
		return show;
	}

	private void parseGenres(final IVideoGenre video, Document doc)
	throws XMLParserException {
		for (Node node : selectNodeList(doc, "details/genre/text()")) {
			video.addGenre(node.getTextContent());
		}
	}

	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 * @param filmId The id of the film
	 * @param url The URL used to lookup the film
	 * @param file The film file if looking up a files details, or NULL
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Film getFilm(String filmId,URL url,File file) throws SourceException,
			MalformedURLException, IOException {
		checkMode(Mode.FILM);

		Film film =  parseFilm(filmId,url,file);
		return film;
	}

	private Film parseFilm(final String filmId,final URL url,final File file) throws IOException, SourceException {
		final Film film = new Film(filmId);
		film.setFilmUrl(url);
		film.setSourceId(getSourceId());

		StreamProcessor processor = new StreamProcessor(mgr.getStreamToURL(url)) {
			@Override
			public void processContents(String contents) throws SourceException {
				try {
	    			Document doc = addon.getScraper(Mode.FILM).getGetDetails(file,contents,filmId);
	    			try {
	    				film.setDate(FILM_YEAR_DATE_FORMAT.parse(getStringFromXML(doc, "details/year/text()")));
	    			}
	    			catch (XMLParserNotFoundException e) {
	    				// Ignore if not found
	    			}
	    			String plot = getStringFromXML(doc, "details/plot/text()");
	    			film.setDescription(plot);
	    			film.setId(filmId);
	    			try {
	    				film.setImageURL(new URL(getStringFromXML(doc, "details/thumb/text()")));
	    			}
	    			catch (XMLParserNotFoundException e) {
	    				// Ignore no image URL
	    			}
	    			String title= getStringFromXMLOrNull(doc, "details/title/text()");
	    			if (title==null) {
	    				title= getStringFromXMLOrNull(doc, "details/originaltitle/text()");
	    			}
	    			if (title ==null) {
	    				throw new XBMCException("Unable to find title");
	    			}
//	    			title = SearchHelper.decodeHtmlEntities(title);
	    			film.setTitle(title);
	    			String overview = getStringFromXMLOrNull(doc, "details/overview/text()");
	    			if (overview==null) {
	    				overview = "";
	    			}
	    			film.setSummary(overview);
	    			String rawVote = getStringFromXML(doc, "details/votes/text()").replaceAll(",","");
	    			Integer vote = Integer.parseInt(rawVote);
	    			film.setRating(new Rating(getFloatFromXML(doc, "details/rating/text()"),vote));
	    			try {
	    				film.setCountry(getStringFromXML(doc, "details/country/text()"));
	    			}
	    			catch (XMLParserNotFoundException e) {
	    				// Ignore, their was no country
	    			}

	    			parseDirectors(film, doc);
	    			parseActors(film, doc);
	    			parseWriters(film, doc);

	    			parseGenres(film,doc);
	    			parseCertification(film,doc);

//	    			film.setChapters(chapters);
				}
				catch (XMLParserException e) {
					throw new SourceException("Unable to parse show details",e);
				}
				catch (MalformedURLException e) {
					throw new SourceException("Unable to parse show details",e);
				} catch (ParseException e) {
					throw new SourceException("Unable to parse show details",e);
				}
			}

		};
		processor.handleStream();


		return film;
	}

	private void parseWriters(final IVideo viode, Document doc)
			throws XMLParserException {
		List<String> writers = new ArrayList<String>();
		for (Node writer : selectNodeList(doc, "details/credits/text()")) {
			writers.add(writer.getTextContent());
		}
		viode.setWriters(writers);
	}

	private void parseDirectors(final IVideo video, Document doc)
			throws XMLParserException {
		List<String> directors = new ArrayList<String>();
		for (Node director : selectNodeList(doc, "details/director/text()")) {
			directors.add(director.getTextContent());
		}
		video.setDirectors(directors);
	}

	private void parseActors(final IVideoActors video, Document doc)
			throws XMLParserException {
		List<Actor> actors = new ArrayList<Actor>();
		try {
			for (Node actor : selectNodeList(doc, "details/actor")) {
				String role = "";
				try {
					 role = getStringFromXML(actor, "role/text()");
				}
				catch (XMLParserNotFoundException e) {
					// Ignore
				}
				actors.add(new Actor(getStringFromXML(actor, "name/text()"),role));
			}
		}
		catch (XMLParserNotFoundException e) {
			// Ignore, no actors
		}
		video.setActors(actors);
	}

	protected void parseCertification(Film film, Document doc) throws XMLParserException {
		try {
			String type = "mpaa";
			String cert = getStringFromXML(doc, "details/mpaa/text()");
			if (cert.startsWith("Rated ")) {
				cert = cert.substring(6);
			}
			List<Certification>certs = new ArrayList<Certification>();
			certs.add(new Certification(cert, type));
			film.setCertifications(certs);
		}
		catch (XMLParserNotFoundException e) {
			// Ignore, was no rating
		}
	}

	/**
	 * This gets a special episode from the source. If it can't be found, then it will
	 * return null;
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @param file The film file if looking up a files details, or NULL
	 * @return The special episode, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber,File file)
			throws SourceException, MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return parseSpecial(season,specialNumber);
	}

	private List<XBMCEpisode> getSpecialList(Show show) throws SourceException, IOException {
		Season specialSeason = getSeason(show, 0);
		List<XBMCEpisode> episodes = getEpisodeList(show, specialSeason);
		for (final XBMCEpisode episode : episodes) {
			StreamProcessor processor = new StreamProcessor(mgr.getStreamToURL(episode.getUrl())) {
				@Override
				public void processContents(String contents) throws SourceException {
					Document doc = addon.getScraper(Mode.TV_SHOW).getGetEpisodeDetails(contents,String.valueOf(episode.getEpisodeId()));
					parseEpisode(episode, doc);
				}
			};

			processor.handleStream();
		}
		return episodes;
	}

	private Episode parseSpecial(Season season, int specialNumber) throws SourceException, IOException {
		List<XBMCEpisode> episodes = getSpecialList(season.getShow());
		for (final XBMCEpisode episode : episodes) {
			if (episode.getDisplayEpisode() == specialNumber && episode.getDisplaySeason() == season.getSeasonNumber()) {
				episode.setSeason(season);
				return episode;
			}
		}
		return null;
	}

	private void parseEpisode(final XBMCEpisode episode,
			Document doc) throws SourceException {
		try {
			episode.setSummary(getStringFromXML(doc, "details/plot/text()"));
			parseWriters(episode, doc);
			parseDirectors(episode, doc);
			parseActors(episode, doc);
			episode.setImageURL(new URL(getStringFromXML(doc, "details/thumb/text()")));
			try {
				episode.setDate(EPISODE_DATE_FORMAT.parse(getStringFromXML(doc, "details/aired/text()")));
			}
			catch (XMLParserNotFoundException e) {
				// Ignore as their is no date for this episode. Probally never shown
			}
			episode.setRating(new Rating(getFloatFromXML(doc, "details/rating/text()"),1));

			try {
				int displaySeason = getIntegerFromXML(doc, "details/displayseason/text()");
				int displayEpisode = getIntegerFromXML(doc, "details/displayepisode/text()");
				// This is a special
				episode.setSpecial(true);
				episode.setDisplaySeason(displaySeason);
				episode.setDisplayEpisode(displayEpisode);
				episode.setEpisodeNumber(displayEpisode);
			}
			catch (XMLParserNotFoundException e) {
				// This is not a special
			}
		}
		catch (XMLParserNotFoundException e) {
			// Ignore
		} catch (XMLParserException e) {
			throw new SourceException("Unable to parse episode details",e);
		} catch (ParseException e) {
			throw new SourceException("Unable to parse episode details",e);
		} catch (MalformedURLException e) {
			throw new SourceException("Unable to parse episode details",e);
		}
	}



	/**
	 * Get the id of the source
	 * @return The id of the source
	 */
	@Override
	public String getSourceId() {
		return "xbmc-"+id;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(final String name,final Mode mode,final Integer part) throws SourceException {
		if (!addon.supportsMode(mode)) {
			return null;
		}

		final List<SearchResult>results = new ArrayList<SearchResult>();
		try {
			URL url = new URL(getURLFromScraper(addon.getScraper(mode),name, ""));
			StreamProcessor processor = new StreamProcessor(mgr.getStreamToURL(url)) {
				@Override
				public void processContents(String contents) throws SourceException {
					try {
		    			Document doc = addon.getScraper(mode).getGetSearchResults(contents, name);
//		    			System.out.println(XMLParser.domToStr(doc));
						NodeList entities = XPathAPI.selectNodeList(doc, "*/entity");

						for (int i=0;i<entities.getLength();i++) {
							Node node = entities.item(i);
							String id = getStringFromXMLOrNull(node, "id/text()");
							String url =  getStringFromXMLOrNull(node, "url/text()");
							if (id!=null && url!=null) {
								SearchResult result = new SearchResult(id, getSourceId(),url,part);
								results.add(result);
							}
						}
					}
					catch (TransformerException e) {
						throw new SourceException("Unale to get show results",e);
					}
					catch (XMLParserException e) {
						throw new SourceException("Unale to get show results",e);
					}
				}
			};
			processor.handleStream();
		} catch (MalformedURLException e) {
			throw new SourceException("Unable to search for show",e);
		}
		if (results.size()>0) {
			return results.get(0);
		}
		else {
			return null;
		}
	}

	private String getURLFromScraper(XBMCScraper scraper,String name, String year)
			throws SourceException {
		try {
			Document doc = scraper.getCreateSearchUrl(name, year);
			String url = getStringFromXML(doc,"url/text()");
			return url;
		}
		catch (Exception e) {
			throw new SourceException("Unable to parse search url",e);
		}
	}

	private void checkMode(Mode mode) throws SourceException  {
		if (!addon.supportsMode(mode)) {
			throw new SourceException("Scraper '"+addon.getId()+"' is not of type '"+mode.getDisplayName()+"'");
		}
	}

	/**
	 * <p>Used to set source parameters. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */

	@Override
	public void setParameter(String key, String value) throws SourceException {
		addon.setSetting(key,value);
	}

	/**
	 * <p>Used to get the value of a source parameter. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * @param key The key of the parameter
	 * @return The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public String getParameter(String key) throws SourceException {
		return addon.getSetting(key).toString();
	}

	/** {@inheritDoc} */
	@Override
	public void setMediaDirConfig(MediaDirectory dir) throws SourceException {

	}

	@Override
	public String toString() {
		return "XBMCSource: "+id;
	}


}

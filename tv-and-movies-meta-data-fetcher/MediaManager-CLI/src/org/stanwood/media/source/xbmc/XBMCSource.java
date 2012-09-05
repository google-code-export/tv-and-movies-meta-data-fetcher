package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.IVideoActors;
import org.stanwood.media.model.IVideoCertification;
import org.stanwood.media.model.IVideoGenre;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.Stream;
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

	private static final SimpleDateFormat FILM_YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
	private static final SimpleDateFormat EPISODE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private final static Log log = LogFactory.getLog(XBMCSource.class);

	private XBMCAddon addon;
	private String id;
	private XBMCAddonManager mgr;
	private ExtensionInfo<? extends ISource> sourceInfo;

	/**
	 * Used to create a instance of this class
	 * @param sourceInfo The source information
	 * @param mgr The addon manager
	 * @param addonId The ID of the sources XBMC addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public XBMCSource(ExtensionInfo<? extends ISource> sourceInfo,XBMCAddonManager mgr,String addonId) throws XBMCException {
		this.id = addonId;
		this.mgr = mgr;
		addon = mgr.getAddon(addonId);
		this.sourceInfo = sourceInfo;
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
	public IEpisode getEpisode(ISeason season, int episodeNum,File file)
			throws SourceException, MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return parseEpisode(season,episodeNum);
	}

	private IEpisode parseEpisode(ISeason season, int episodeNum) throws SourceException, IOException {
		List<XBMCEpisode> episodes = getEpisodeList(season.getShow(), season);
		for (final XBMCEpisode episode : episodes) {
			if (episode.getEpisodeNumber() == episodeNum) {
				StreamProcessor processor = new StreamProcessor(episode.getUrl().toExternalForm()) {
					@Override
					protected Stream getStream() throws ExtensionException, IOException {
						return mgr.getStreamToURL(episode.getUrl());
					}

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
	public ISeason getSeason(IShow show, int seasonNum) throws SourceException,
			IOException {
		checkMode(Mode.TV_SHOW);
		return parseSeason(show,seasonNum);
	}

	private ISeason parseSeason(final IShow show, final int seasonNum) throws SourceException, IOException {
		ISeason season = new Season(show,seasonNum);
		try {
			URL url = new URL(show.getExtraInfo().get("episodeGuideURL")); //$NON-NLS-1$
			season.setURL(url);
			if (getEpisodeList(show, season).size()>0) {
				return season;
			}
			return null;
		} catch (MalformedURLException e) {
			throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SEASON"),e); //$NON-NLS-1$
		}
	}

	private List<XBMCEpisode>getEpisodeList(final IShow show,final ISeason season) throws SourceException, IOException {
		final List<XBMCEpisode>episodes = new ArrayList<XBMCEpisode>();

		StreamProcessor processor = new StreamProcessor(season.getURL().toExternalForm()) {
			@Override
			protected Stream getStream() throws ExtensionException, IOException {
				return mgr.getStreamToURL(season.getURL());
			}

			@Override
			public void processContents(String contents) throws SourceException {
				Document doc = addon.getScraper(Mode.TV_SHOW).getGetEpisodeList(contents,show.getShowURL());
				try {
    				IterableNodeList episodesList = selectNodeList(doc,"/episodeguide/episode"); //$NON-NLS-1$
    				if (episodesList!=null) {
    					for (Node episodeNode : episodesList) {
    						try {
	    						if (getIntegerFromXML(episodeNode, "season/text()")==season.getSeasonNumber()) { //$NON-NLS-1$
	    							XBMCEpisode ep = new XBMCEpisode(getIntegerFromXML(episodeNode, "epnum/text()"), season,false); //$NON-NLS-1$
		    						ep.setTitle(getStringFromXML(episodeNode, "title/text()")); //$NON-NLS-1$
		    						ep.setUrl(new URL(getStringFromXML(episodeNode, "url/text()"))); //$NON-NLS-1$
		    						ep.setEpisodeId(getStringFromXML(episodeNode, "id/text()")); //$NON-NLS-1$
		    						try {
		    							ep.setDate(EPISODE_DATE_FORMAT.parse(getStringFromXML(episodeNode, "aired/text()"))); //$NON-NLS-1$
		    						}
		    						catch (XMLParserNotFoundException e) {
		    							if (log.isDebugEnabled()) {
		    								log.debug("No date for episode: " + ep.getEpisodeId()); //$NON-NLS-1$
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
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SEASON"),e); //$NON-NLS-1$
				} catch (MalformedURLException e) {
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SEASON"),e); //$NON-NLS-1$
				} catch (ParseException e) {
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SEASON"),e); //$NON-NLS-1$
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

	private Show parseShow(final String showId, final URL url,final File file) throws IOException,
			SourceException {
		final Show show = new Show(showId);
		show.setShowURL(url);
		show.setSourceId(sourceInfo.getId());
		StreamProcessor processor = new StreamProcessor(url.toExternalForm()) {
			@Override
			protected Stream getStream() throws ExtensionException, IOException {
				return mgr.getStreamToURL(url);
			}

			@Override
			public void processContents(String contents) throws SourceException {
				try {
	    			Document doc = addon.getScraper(Mode.TV_SHOW).getGetDetails(file,contents,showId);
	    			try {
	    				String longSummary = getStringFromXML(doc, "details/plot/text()"); //$NON-NLS-1$
						show.setLongSummary(longSummary);
						if (longSummary.length()>100) {
							show.setShortSummary(longSummary.substring(0,99)+"..."); //$NON-NLS-1$
						}
						else {
							show.setShortSummary(longSummary);
						}
					} catch (XMLParserNotFoundException e) {
						// Ignore
					}

	    			parseCertification(show,doc);

	    			try {
	    				String studio = getStringFromXML(doc, "details/studio/text()"); //$NON-NLS-1$
	    				show.setStudio(studio);
	    			} catch (XMLParserNotFoundException e) {
						// Ignore
					}

					try {
						show.setName(getStringFromXML(doc, "details/title/text()")); //$NON-NLS-1$
					} catch (XMLParserNotFoundException e) {
						// Ignore
					}

					parseGenres(show, doc);

					if (show.getImageURL()==null) {
						try {
							show.setImageURL(new URL(getStringFromXML(doc, "details/thumb/text()"))); //$NON-NLS-1$
						} catch (XMLParserNotFoundException e) {
							// Ignore
						}
					}

					show.getExtraInfo().put("episodeGuideURL", getStringFromXML(doc, "details/episodeguide/url/text()"));  //$NON-NLS-1$//$NON-NLS-2$

				}
				catch (XMLParserException e) {
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SHOW"),e); //$NON-NLS-1$
				}
				catch (MalformedURLException e) {
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SHOW"),e); //$NON-NLS-1$
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
		for (Node node : selectNodeList(doc, "details/genre/text()")) { //$NON-NLS-1$
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
		film.setSourceId(sourceInfo.getId());

		StreamProcessor processor = new StreamProcessor(url.toExternalForm()) {
			@Override
			protected Stream getStream() throws ExtensionException, IOException {
				return mgr.getStreamToURL(url);
			}

			@Override
			public void processContents(String contents) throws SourceException {
				if (contents==null) {
					return;
				}
				try {
	    			Document doc = addon.getScraper(Mode.FILM).getGetDetails(file,contents,filmId);
	    			try {
	    				film.setDate(FILM_YEAR_DATE_FORMAT.parse(getStringFromXML(doc, "details/year/text()"))); //$NON-NLS-1$
	    			}
	    			catch (XMLParserNotFoundException e) {
	    				// Ignore if not found
	    			}

	    			String plot = getStringFromXMLOrNull(doc, "details/plot/text()"); //$NON-NLS-1$
	    			if (plot!=null) {
	    				film.setDescription(plot.trim());
	    			}
	    			film.setId(filmId);
	    			try {
	    				film.setImageURL(new URL(getStringFromXML(doc, "details/thumb/text()"))); //$NON-NLS-1$
	    			}
	    			catch (XMLParserNotFoundException e) {
	    				// Ignore no image URL
	    			}
	    			String title= getStringFromXMLOrNull(doc, "details/title/text()"); //$NON-NLS-1$
	    			if (title==null) {
	    				title= getStringFromXMLOrNull(doc, "details/originaltitle/text()"); //$NON-NLS-1$
	    			}
	    			if (title ==null) {
	    				throw new XBMCException("Unable to find title"); //$NON-NLS-1$
	    			}
//	    			title = SearchHelper.decodeHtmlEntities(title);
	    			if (title!=null) {
	    				film.setTitle(title.trim());
	    			}
	    			String overview = getStringFromXMLOrNull(doc, "details/overview/text()"); //$NON-NLS-1$
	    			if (overview==null) {
	    				if (plot!=null) {
		    				if (plot.length()>100) {
		    					overview = plot.substring(0,99)+"..."; //$NON-NLS-1$
							}
							else {
								overview = plot;
							}
	    				}
	    			}
	    			if (overview!=null) {
	    				film.setSummary(overview.trim());
	    			}

	    			Integer vote = null;
	    			try {
	    				String rawVote = getStringFromXML(doc, "details/votes/text()").replaceAll(",",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    				vote = Integer.parseInt(rawVote);
	    			}
	    			catch (XMLParserNotFoundException e) {

	    			}
	    			try {
	    				film.setRating(new Rating(getFloatFromXML(doc, "details/rating/text()"),vote)); //$NON-NLS-1$
	    			}
	    			catch (XMLParserNotFoundException e) {

	    			}
	    			try {
	    				film.setCountry(getStringFromXML(doc, "details/country/text()")); //$NON-NLS-1$
	    			}
	    			catch (XMLParserNotFoundException e) {
	    				// Ignore, their was no country
	    			}
	    			try {
	    				film.setStudio(getStringFromXML(doc, "details/studio/text()")); //$NON-NLS-1$
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
					e.printStackTrace();
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SHOW"),e); //$NON-NLS-1$
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SHOW"),e); //$NON-NLS-1$
				} catch (ParseException e) {
					e.printStackTrace();
					throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SHOW"),e); //$NON-NLS-1$
				}
			}

		};
		processor.handleStream();


		return film;
	}

	private void parseWriters(final IVideo viode, Document doc)
			throws XMLParserException {
		List<String> writers = new ArrayList<String>();
		for (Node writer : selectNodeList(doc, "details/credits/text()")) { //$NON-NLS-1$
			writers.add(writer.getTextContent());
		}
		viode.setWriters(writers);
	}

	private void parseDirectors(final IVideo video, Document doc)
			throws XMLParserException {
		List<String> directors = new ArrayList<String>();
		for (Node director : selectNodeList(doc, "details/director/text()")) { //$NON-NLS-1$
			directors.add(director.getTextContent());
		}
		video.setDirectors(directors);
	}

	private void parseActors(final IVideoActors video, Document doc)
			throws XMLParserException {
		List<Actor> actors = new ArrayList<Actor>();
		try {
			for (Node actor : selectNodeList(doc, "details/actor")) { //$NON-NLS-1$
				String role = ""; //$NON-NLS-1$
				try {
					 role = getStringFromXML(actor, "role/text()"); //$NON-NLS-1$
				}
				catch (XMLParserNotFoundException e) {
					// Ignore
				}
				actors.add(new Actor(getStringFromXML(actor, "name/text()"),role)); //$NON-NLS-1$
			}
		}
		catch (XMLParserNotFoundException e) {
			// Ignore, no actors
		}
		video.setActors(actors);
	}

	protected void parseCertification(IVideoCertification video, Document doc) throws XMLParserException {
		try {
			String type = "mpaa"; //$NON-NLS-1$
			String cert = getStringFromXML(doc, "details/mpaa/text()"); //$NON-NLS-1$
			if (cert.startsWith("Rated ")) { //$NON-NLS-1$
				cert = cert.substring(6);
			}
			List<Certification>certs = new ArrayList<Certification>();
			certs.add(new Certification(cert, type));
			video.setCertifications(certs);
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
	public IEpisode getSpecial(ISeason season, int specialNumber,File file)
			throws SourceException, MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return parseSpecial(season,specialNumber);
	}

	private List<XBMCEpisode> getSpecialList(IShow show) throws SourceException, IOException {
		ISeason specialSeason = getSeason(show, 0);
		List<XBMCEpisode> episodes = getEpisodeList(show, specialSeason);
		for (final XBMCEpisode episode : episodes) {
			StreamProcessor processor = new StreamProcessor(episode.getUrl().toExternalForm()) {
				@Override
				protected Stream getStream() throws ExtensionException, IOException {
					return mgr.getStreamToURL(episode.getUrl());
				}

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

	private IEpisode parseSpecial(ISeason season, int specialNumber) throws SourceException, IOException {
		List<XBMCEpisode> episodes = getSpecialList(season.getShow());
		for (final XBMCEpisode episode : episodes) {
			if (episode.getDisplayEpisode() == specialNumber && episode.getDisplaySeason() == season.getSeasonNumber()) {
				episode.setSeason(season);
				return episode;
			}
		}
		return null;
	}

	private void parseEpisode(final XBMCEpisode episode,Document doc) throws SourceException {
		try {

			episode.setSummary(getStringFromXML(doc, "details/plot/text()")); //$NON-NLS-1$
			parseWriters(episode, doc);
			parseDirectors(episode, doc);
			parseActors(episode, doc);
			episode.setImageURL(new URL(getStringFromXML(doc, "details/thumb/text()"))); //$NON-NLS-1$
			try {
				episode.setDate(EPISODE_DATE_FORMAT.parse(getStringFromXML(doc, "details/aired/text()"))); //$NON-NLS-1$
			}
			catch (XMLParserNotFoundException e) {
				// Ignore as their is no date for this episode. Probally never shown
			}
			episode.setRating(new Rating(getFloatFromXML(doc, "details/rating/text()"),1)); //$NON-NLS-1$

			try {
				int displaySeason = getIntegerFromXML(doc, "details/displayseason/text()"); //$NON-NLS-1$
				int displayEpisode = getIntegerFromXML(doc, "details/displayepisode/text()"); //$NON-NLS-1$
				// This is a special
				episode.setSpecial(true);
				episode.setDisplaySeason(displaySeason);
				episode.setDisplayEpisode(displayEpisode);
				episode.setEpisodeNumber(displayEpisode);
				List<Integer> episodes = new ArrayList<Integer>();
				episodes.add(displayEpisode);
				episode.setEpisodes(episodes);
			}
			catch (XMLParserNotFoundException e) {
				// This is not a special
			}
		}
		catch (XMLParserNotFoundException e) {
			// Ignore
		} catch (XMLParserException e) {
			throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_EPISODE"),e); //$NON-NLS-1$
		} catch (ParseException e) {
			throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_EPISODE"),e); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_EPISODE"),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(final String name,final String year,final Mode mode,final Integer part) throws SourceException {
		if (!addon.supportsMode(mode)) {
			return null;
		}

		final List<SearchResult>results = new ArrayList<SearchResult>();
		try {
			final URL url = new URL(getURLFromScraper(addon.getScraper(mode),name, year));
			if (mgr.getStreamToURL(url)==null || mgr.getStreamToURL(url).getInputStream()==null) {
				throw new SourceException(MessageFormat.format(Messages.getString("XBMCSource.UNABLE_GET_STREAM_URL"), url.toExternalForm())); //$NON-NLS-1$
			}
			StreamProcessor processor = new StreamProcessor(url.toExternalForm()) {
				@Override
				protected Stream getStream() throws ExtensionException, IOException {
					return mgr.getStreamToURL(url);
				}

				@Override
				public void processContents(String contents) throws SourceException {
					try {
		    			Document doc = addon.getScraper(mode).getGetSearchResults(contents, name);
						NodeList entities = XPathAPI.selectNodeList(doc, "*/entity"); //$NON-NLS-1$

						for (int i=0;i<entities.getLength();i++) {
							Node node = entities.item(i);
							String id = getStringFromXMLOrNull(node, "id/text()"); //$NON-NLS-1$
							String url =  getStringFromXMLOrNull(node, "url/text()"); //$NON-NLS-1$
							if (id!=null && url!=null) {
								SearchResult result = new SearchResult(id, sourceInfo.getId(),url,part,mode);
								results.add(result);
							}
						}
					}
					catch (TransformerException e) {
						throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_GET_SHOW_RESULTS"),e); //$NON-NLS-1$
					}
					catch (XMLParserException e) {
						throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_GET_SHOW_RESULTS"),e); //$NON-NLS-1$
					}
				}
			};
			processor.handleStream();
		} catch (MalformedURLException e) {
			throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_SEARCH_FOR_SHOW"),e); //$NON-NLS-1$
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
			String url = getStringFromXML(doc,"url/text()"); //$NON-NLS-1$
			return url;
		}
		catch (Exception e) {
			throw new SourceException(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.UNABLE_PARSE_SEARCH_URL"),e); //$NON-NLS-1$
		}
	}

	private void checkMode(Mode mode) throws SourceException  {
		if (!addon.supportsMode(mode)) {
			throw new SourceException(MessageFormat.format(org.stanwood.media.source.xbmc.Messages.getString("XBMCSource.SCRAPER_WRONG_TYPE"),addon.getId(),mode.getDisplayName())); //$NON-NLS-1$
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "XBMCSource: "+id; //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public ExtensionInfo<? extends ISource> getInfo() {
		return sourceInfo;
	}


}

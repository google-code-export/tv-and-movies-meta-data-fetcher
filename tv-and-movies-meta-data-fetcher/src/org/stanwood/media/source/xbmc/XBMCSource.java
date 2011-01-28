package org.stanwood.media.source.xbmc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideoGenre;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.search.ShowSearcher;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.WebFile;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.stanwood.media.util.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class XBMCSource extends XMLParser implements ISource {

	private XBMCAddon addon;
	private String id;

	public XBMCSource(XMBCAddonManager mgr,String addonId) throws SourceException {
		this.id = addonId;
		addon = mgr.getAddon(addonId);
	}

	@Override
	public Episode getEpisode(Season season, int episodeNum)
			throws SourceException, MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return null;
	}

	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException,
			IOException {
		checkMode(Mode.TV_SHOW);
		return null;
	}



	@Override
	public Show getShow(final String showId, URL url) throws SourceException,
			MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return parseShow(showId, url);
	}

	private Show parseShow(final String showId, URL url) throws IOException,
			SourceException {
		final Show show = new Show(showId);
		show.setShowURL(url);
		show.setSourceId(getSourceId());
		StreamProcessor processor = new StreamProcessor(getStreamToURL(url)) {
			@Override
			public void processContents(String contents) throws SourceException {
				try {
	    			Document doc = addon.getScraper(Mode.TV_SHOW).getGetDetails(contents,showId);
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
			throw new SourceException("Show details parsing was incomplete");
		}
		return show;
	}

	private void parseGenres(final IVideoGenre video, Document doc)
	throws XMLParserException {
		for (Node node : selectNodeList(doc, "details/genre/text()")) {
			video.addGenre(node.getTextContent());
		}
	}

	@Override
	public Film getFilm(String filmId,URL url) throws SourceException,
			MalformedURLException, IOException {
		checkMode(Mode.FILM);

		return parseFilm(filmId,url);
	}

	private Film parseFilm(final String filmId,final URL url) throws IOException, SourceException {
		final Film film = new Film(filmId);

		StreamProcessor processor = new StreamProcessor(getStreamToURL(url)) {
			@Override
			public void processContents(String contents) throws SourceException {
				try {
	    			Document doc = addon.getScraper(Mode.FILM).getGetDetails(contents,filmId);
	    			System.out.println(domToStr(doc));
	    			film.setTitle(getStringFromXML(doc, "details/title/text()"));
	    			film.setSummary(getStringFromXML(doc, "details/overview/text()"));
	    			film.setRating(getFloatFromXML(doc, "details/rating/text()"));
	    			parseGenres(film,doc);

				}
				catch (XMLParserException e) {
					throw new SourceException("Unable to parse show details",e);
				}
//				catch (MalformedURLException e) {
//					throw new SourceException("Unable to parse show details",e);
//				}
			}
		};
		processor.handleStream();

//		if (show.getName() == null || show.getLongSummary()==null) {
//			throw new SourceException("Show details parsing was incomplete");
//		}


		return film;
	}

	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws SourceException, MalformedURLException, IOException {
		checkMode(Mode.TV_SHOW);
		return null;
	}

	@Override
	public String getSourceId() {
		return "xbmc-"+id;
	}

	@Override
	public SearchResult searchForVideoId(File rootMediaDir, final Mode mode,
			File episodeFile, String renamePattern) throws SourceException,
			MalformedURLException, IOException {
		if (!addon.supportsMode(mode)) {
			return null;
		}

		ShowSearcher s = new ShowSearcher() {
			@Override
			public SearchResult doSearch(String name) throws MalformedURLException, IOException, SourceException {
				return searchMedia(name,mode);
			}
		};

		return s.search(episodeFile,rootMediaDir,renamePattern);
	}

	/* package for test */InputStream getSource(URL url) throws IOException {
		WebFile page = new WebFile(url);
		String MIME = page.getMIMEType();
		byte[] content = (byte[]) page.getContent();
		if (MIME.equals("zip")) {
			return new ZipInputStream(new ByteArrayInputStream(content));
		}
		else {
			return new ByteArrayInputStream(content);
		}
	}

	private InputStream getStreamToURL(URL url) throws IOException, SourceException {
		InputStream stream = getSource(url);
		if (stream==null) {
			throw new SourceException("Unable to get resource: " + url);
		}
		return stream;
	}

	protected SearchResult searchMedia(final String name,final Mode mode) throws SourceException {
		final List<SearchResult>results = new ArrayList<SearchResult>();
		try {
			URL url = new URL(getURLFromScraper(addon.getScraper(mode),name, ""));
			StreamProcessor processor = new StreamProcessor(getStreamToURL(url)) {
				@Override
				public void processContents(String contents) throws SourceException {
					try {
		    			Document doc = addon.getScraper(mode).getGetSearchResults(contents, name);
						NodeList entities = XPathAPI.selectNodeList(doc, "*/entity");

						for (int i=0;i<entities.getLength();i++) {
							Node node = entities.item(i);
							SearchResult result = new SearchResult(getStringFromXML(node, "id/text()"), getSourceId(), getStringFromXML(node, "url/text()"));
							results.add(result);
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
		} catch (IOException e) {
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




}

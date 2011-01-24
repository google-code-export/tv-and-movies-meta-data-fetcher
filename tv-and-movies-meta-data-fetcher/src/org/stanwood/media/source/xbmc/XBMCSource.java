package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.search.ShowSearcher;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.WebFile;
import org.stanwood.media.util.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

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
		return null;
	}

	@Override
	public Season getSeason(Show show, int seasonNum) throws SourceException,
			IOException {
		return null;
	}

	@Override
	public Show getShow(String showId, URL url) throws SourceException,
			MalformedURLException, IOException {
		return null;
	}

	@Override
	public Film getFilm(String filmId) throws SourceException,
			MalformedURLException, IOException {
		return null;
	}

	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws SourceException, MalformedURLException, IOException {
		return null;
	}

	@Override
	public String getSourceId() {
		return "xbmc-"+id;
	}

	@Override
	public SearchResult searchForVideoId(File rootMediaDir, Mode mode,
			File episodeFile, String renamePattern) throws SourceException,
			MalformedURLException, IOException {
		
		if (mode != addon.getScraper().getMode()) {
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
	
	private String getHTMLFromURL(URL url) throws IOException {
		WebFile page = new WebFile(url);
		String MIME = page.getMIMEType();
		byte[] content = (byte[]) page.getContent();
		String html = null;
		if (MIME.equals("text/xml")) {
			html = new String(content, "iso-8859-1");
		}
		return html;
	}
	
	/* package for test */String getSource(URL url) throws IOException {
		String html = getHTMLFromURL(url);
		return html;
	}

	protected SearchResult searchForTvShow(String name) {
		try {			
			URL url = new URL(getURLFromScraper(name, ""));			
			String contents = getSource(url);			
			if (contents!=null) {
				Document doc = addon.getScraper().getGetSearchResults(contents, name);
				NodeList entities = XPathAPI.selectNodeList(doc, "*/entity");
				if (entities.getLength()>0) {
					Node node = entities.item(0);					
					SearchResult result = new SearchResult(getStringFromXML(node, "id/text()"), getSourceId(), getStringFromXML(node, "url/text()"));
					return result;				
				}
			}
			return null;
		} catch (SourceException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (NotInStoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getURLFromScraper(String name, String year)
			throws SourceException {
		try {
			Document doc = addon.getScraper().getCreateSearchUrl(name, year);
			String url = getStringFromXML(doc,"url/text()");
			return url;
		}
		catch (Exception e) {
			throw new SourceException("Unable to parse search url",e);
		}		
	}
}

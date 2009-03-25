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
package org.stanwood.media.store.xmlstore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.store.StoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This store is used to store the show information in a XML called .show.xml. 
 * This is located in the directory were the show is located. It can hold all
 * of the information of the Shows, Seasons, Episodes and Specials. This store
 * can be read and written too, and it's also possible too lookup the show id 
 * of the show in the current directory.
 */
public class TVXMLStore extends BaseXMLStore {

	private final static Log log = LogFactory.getLog(TVXMLStore.class);
	
	private final static String FILENAME = ".show.xml";
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * This gets a special episode from the store. If it can't be found, then it will
	 * return null;
	 * @param specialFile The file that contains the special episode file
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return The special episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */	
	public Episode getSpecial(File rootMediaDir,File specialFile,Season season, int specialNumber)
			throws MalformedURLException, IOException, StoreException {
		Document doc = getCache(rootMediaDir, season.getShow().getShowId());
		if (doc==null) {
			return null;
		}
		Episode special = null;
		try {
			special = getSpecialFromCache(specialNumber, season, doc);
			return special;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	/**
	 * This gets a episode from the store. If it can't be found, then it will
	 * return null;
	 * @param episodeFile The file that contains the episode file
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return The episode, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the source
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 */	
	public Episode getEpisode(File rootMediaDir,File episodeFile,Season season, int episodeNum)
			throws StoreException, MalformedURLException {
		Document doc = getCache(rootMediaDir, season.getShow().getShowId());
		if (doc==null) {
			return null;
		};
		Episode episode = null;
		try {
			episode = getEpisodeFromCache(episodeNum, season, doc);
			return episode;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	/**
	 * This will get a season from the store. If the season can't be found,
	 * then it will return null.
	 * @param episodeFile The file that contains the episode file
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season that is to be fetched
	 * @return The season if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 */	
	public Season getSeason(File rootMediaDir,File episodeFile,Show show, int seasonNum) throws StoreException,
			MalformedURLException {
		Document doc = getCache(rootMediaDir, show.getShowId());
		if (doc==null) {
			return null;
		}
		Season season = null;
		try {
			season = getSeasonFromCache(seasonNum, show, doc);
			return season;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	private Episode getSpecialFromCache(int specialNum, Season season,
			Document doc) throws NotInStoreException, StoreException,
			MalformedURLException {
		try {
			Node episodeNode = XPathAPI.selectSingleNode(doc, "show[@id="
					+ season.getShow().getShowId() + "]/season[@number="
					+ season.getSeasonNumber() + "]/special[@number="
					+ specialNum + "]");
			if (episodeNode == null) {
				throw new NotInStoreException();
			}

			Episode episode = new Episode(specialNum, season);

			readCommonEpisodeInfo(episodeNode, episode);

			episode.setSpecial(true);
			String specialName = getStringFromXML(episodeNode, "@specialName");
			episode.setSpecialName(specialName);
			return episode;
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache: "
					+ e.getMessage(), e);
		} catch (ParseException e) {
			throw new StoreException(
					"Unable to parse date: " + e.getMessage(), e);
		}
	}

	private void readCommonEpisodeInfo(Node episodeNode, Episode episode)
			throws TransformerException, NotInStoreException,
			MalformedURLException, ParseException {
		String summary = getStringFromXML(episodeNode, "summary/text()");
		URL url = new URL(getStringFromXML(episodeNode, "@url"));
		String title = getStringFromXML(episodeNode, "@title");
		String airDate = getStringFromXML(episodeNode, "@firstAired");
		String productionCode = getStringFromXML(episodeNode, "@productionCode");

		String episodeSiteId = getStringFromXML(episodeNode, "@siteId");
		long episodeId = getLongFromXML(episodeNode, "@episodeId");
		float rating = getFloatFromXML(episodeNode, "@rating");		
		List<Link> directors = getLinks(episodeNode, "director");
		List<Link> writers = getLinks(episodeNode, "writer");
		List<Link> guestStars = getLinks(episodeNode, "guestStar");

		episode.setSummaryUrl(url);
		episode.setSummary(summary);
		episode.setTitle(title);
		episode.setDate(df.parse(airDate));
		episode.setProductionCode(productionCode);
		episode.setSiteId(episodeSiteId);		
		episode.setEpisodeId(episodeId);
		episode.setGuestStars(guestStars);
		episode.setWriters(writers);
		episode.setRating(rating);
		episode.setDirectors(directors);
	}	

	private Episode getEpisodeFromCache(int episodeNum, Season season,
			Document doc) throws NotInStoreException, StoreException,
			MalformedURLException {
		try {
			Node episodeNode = XPathAPI.selectSingleNode(doc, "show[@id="
					+ season.getShow().getShowId() + "]/season[@number="
					+ season.getSeasonNumber() + "]/episode[@number="
					+ episodeNum + "]");
			if (episodeNode == null) {
				throw new NotInStoreException();
			}

			Episode episode = new Episode(episodeNum, season);
			readCommonEpisodeInfo(episodeNode, episode);
			episode.setSpecial(false);
			episode.setSpecialName(null);		

			return episode;
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache: "
					+ e.getMessage(), e);
		} catch (ParseException e) {
			throw new StoreException(
					"Unable to parse date: " + e.getMessage(), e);
		}
	}

	private Season getSeasonFromCache(int seasonNum, Show show, Document doc)
			throws StoreException, NotInStoreException, MalformedURLException {
		try {
			Node seasonNode = XPathAPI.selectSingleNode(doc, "show[@id="
					+ show.getShowId() + "]/season[@number=" + seasonNum + "]");
			if (seasonNode == null) {
				throw new NotInStoreException();
			}
			Season season = new Season(show, seasonNum);
			season.setDetailedUrl(new URL(getStringFromXML(seasonNode,
					"@detailedListingUrl")));
			season.setListingUrl(new URL(getStringFromXML(seasonNode,
					"@listingUrl")));
			return season;

		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * This will get a show from the store. If the season can't be found, then it 
	 * will return null. 
	 * @param episodeFile The file the episode is located in
	 * @param showId The id of the show to get.
	 * @return The show if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Show getShow(File rootMediaDir,File episodeFile, String showId)
			throws StoreException, MalformedURLException, IOException {		
		Document doc = getCache(rootMediaDir, showId);
		if (doc==null) {
			return null;
		}		
		Show show = null;
		try {
			show = getShowFromCache(episodeFile.getParentFile(), doc);
			return show;
		} catch (NotInStoreException e) {
			return null;
		}

	}

	private Show getShowFromCache(File showDirectory, Document doc)
			throws StoreException, NotInStoreException, MalformedURLException {
		try {
			String showId = getStringFromXML(doc, "show/@id");
			String imageURL = getStringFromXML(doc, "show/@imageUrl");
			String showURL = getStringFromXML(doc, "show/@url");
			String name = getStringFromXML(doc, "show/@name");
			String sourceId = getStringFromXML(doc, "show/@sourceId");
			String longSummary = getStringFromXML(doc,
					"show/description/long/text()");
			String shortSummary = getStringFromXML(doc,
					"show/description/short/text()");
			List<String> genres = readGenresFromXML(doc);

			Show show = new Show(showId);
			show.setName(name);
			try {
				show.setImageURL(new URL(imageURL));
			}
			catch (MalformedURLException e) {
				log.warn("Unable to get show image url " + imageURL);
			}
		
			show.setLongSummary(longSummary);
			show.setShortSummary(shortSummary);
			show.setShowURL(new URL(showURL));
			show.setSourceId(sourceId);
			show.setGenres(genres);

			return show;
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache: "
					+ e.getMessage(), e);
		}
	}

	

	

	private Document getCache(File rootMediaDirectory, String showId)
			throws StoreException {
		File cacheFile = getCacheFile(rootMediaDirectory,FILENAME);				
		if (cacheFile.exists()) {
			Document doc = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			// Create the builder and parse the file
			try {
				doc = factory.newDocumentBuilder().parse(cacheFile);
			} catch (SAXException e) {
				throw new StoreException("Unable to parse cache file: "
						+ e.getMessage(), e);
			} catch (IOException e) {
				throw new StoreException("Unable to read cache file: "
						+ e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new StoreException("Unable to parse cache file: "
						+ e.getMessage(), e);
			}
			return doc;
		} 		
		else {							
			Document doc = createCacheFile(showId, cacheFile);
			return doc;			
		}				
	}

	private Document createCacheFile(String showId, File cacheFile)
			throws StoreException {
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
			Element series = doc.createElement("show");
			series.setAttribute("id", showId);
			doc.appendChild(series);
			writeCache(cacheFile, doc);
		} catch (ParserConfigurationException e) {
			throw new StoreException("Unable to create cache: "
					+ e.getMessage());
		}
		return doc;
	}
	
	/**
	 * This is used to write a show too the store.
	 * @param episodeFile The file that contains the episode
	 * @param show The show too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */	
	public void cacheShow(File rootMediaDir,File episodeFile,Show show) throws StoreException {
		Document doc = getCache(rootMediaDir, show.getShowId());
		
		Element series = (Element) doc.getFirstChild();
		series.setAttribute("id", String.valueOf(show.getShowId()));
		series.setAttribute("url", urlToText(show.getShowURL()));
		series.setAttribute("name", show.getName());
		series.setAttribute("imageUrl", urlToText(show.getImageURL()));
		series.setAttribute("sourceId", show.getSourceId());
		try {
			Node node = XPathAPI.selectSingleNode(series, "description");
			if (node != null) {
				series.removeChild(node);
			}

			Element description = doc.createElement("description");
			Element shortDesc = doc.createElement("short");
			shortDesc.appendChild(doc
					.createCDATASection(show.getShortSummary()));
			description.appendChild(shortDesc);
			Element longDesc = doc.createElement("long");
			longDesc.appendChild(doc.createCDATASection(show.getLongSummary()));
			description.appendChild(longDesc);
			series.appendChild(description);

			NodeList nodeList = XPathAPI.selectNodeList(series, "genre");
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodeList.item(i).getParentNode().removeChild(nodeList.item(i));
			}

			for (String value : show.getGenres()) {
				Element genre = doc.createElement("genre");
				genre.setAttribute("name", value);
				series.appendChild(genre);
			}
			File cacheFile = getCacheFile(episodeFile.getParentFile(),FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache file: "
					+ e.getMessage(), e);
		}

	}

   /**
	 * This is used to write a episode or special too the store
	 * @param episodeFile The file that contains the episode
	 * @param episode The episode or special too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */	
	public void cacheEpisode(File rootMediaDir,File episodeFile,Episode episode) throws StoreException {
		Season season = episode.getSeason();
		Show show = season.getShow();

		Document doc = getCache(rootMediaDir, show.getShowId());		
		if (episode.isSpecial()) {
			cacheSpecial(rootMediaDir,episodeFile,doc, show, season, episode);
		} else {
			cacheEpisode(rootMediaDir,episodeFile,doc, show, season, episode);
		}
	}

	private void cacheEpisode(File rootMediaDir,File episodeFile,Document doc, Show show, Season season,
			Episode episode) throws StoreException {
		try {
			Node node = XPathAPI.selectSingleNode(doc, "series[@id="
					+ show.getShowId() + "]/season[@number="
					+ season.getSeasonNumber() + "]/episode[number="
					+ episode.getEpisodeNumber() + "]");
			if (node == null) {
				node = doc.createElement("episode");
				Node seasonNode = getSeasonNode(season, show, doc);
				((Element) node).setAttribute("number", String.valueOf(episode
						.getEpisodeNumber()));
				seasonNode.appendChild(node);
			}		

			writeEpisodeCommonData(doc, episode, node);

			File cacheFile = getCacheFile(episodeFile.getParentFile(),FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	private void cacheSpecial(File rootMediaDir,File episodeFile,Document doc, Show show, Season season,
			Episode episode) throws StoreException {
		try {
			Node node = XPathAPI.selectSingleNode(doc, "show[@id="
					+ show.getShowId() + "]/season[@number="
					+ season.getSeasonNumber() + "]/special[number="
					+ episode.getEpisodeNumber() + "]");
			if (node == null) {
				node = doc.createElement("special");
				Node seasonNode = getSeasonNode(season, show, doc);
				((Element) node).setAttribute("number", String.valueOf(episode
						.getEpisodeNumber()));
				seasonNode.appendChild(node);
			}

			((Element) node).setAttribute("specialName", episode
					.getSpecialName());

			writeEpisodeCommonData(doc, episode, node);

			File cacheFile = getCacheFile(episodeFile.getParentFile(),FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	private void writeEpisodeCommonData(Document doc, Episode episode, Node node)
			throws TransformerException {
		((Element) node).setAttribute("rating", String.valueOf(episode
				.getRating()));
		((Element) node).setAttribute("siteId", episode.getEpisodeSiteId());
		((Element) node).setAttribute("title", episode.getTitle());
		((Element) node).setAttribute("url", urlToText(episode.getSummaryUrl()));
		((Element) node).setAttribute("firstAired", df.format(episode
				.getDate()));
		((Element) node).setAttribute("productionCode", episode
				.getProductionCode());
		((Element) node).setAttribute("episodeId", String.valueOf(episode
				.getEpisodeId()));

		writeEpsoideExtraInfo(doc, node, "director", episode
				.getDirectors());
		writeEpsoideExtraInfo(doc, node, "writers", episode
				.getWriters());
		writeEpsoideExtraInfo(doc, node, "guestStars", episode
				.getGuestStars());

		Node summaryNode = XPathAPI.selectSingleNode(node, "summary");
		if (summaryNode != null) {
			summaryNode.getParentNode().removeChild(summaryNode);
		}
		summaryNode = doc.createElement("summary");
		node.appendChild(summaryNode);
		summaryNode.appendChild(doc.createTextNode(episode.getSummary()));
	}

	

	/**
	 * This is used to write a season too the store.
	 * @param episodeFile The file that contains the episode
	 * @param season The season too write
	 * @throws StoreException Thrown if their is a problem with the source
	 */	
	public void cacheSeason(File rootMediaDir,File episodeFile,Season season) throws StoreException {
		try {
			Show show = season.getShow();
			Document doc = getCache(rootMediaDir, show.getShowId());

			Node node = getSeasonNode(season, show, doc);
			if (node == null) {
				node = doc.createElement("season");
				Node seriesNode = getSeriesNode(show, doc);
				((Element) node).setAttribute("number", String.valueOf(season
						.getSeasonNumber()));
				seriesNode.appendChild(node);
			}

			((Element) node).setAttribute("detailedListingUrl", urlToText(season
					.getDetailedUrl()));
			((Element) node).setAttribute("listingUrl", urlToText(season.getListingUrl()));
			File cacheFile = getCacheFile(episodeFile.getParentFile(),FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	private Node getSeriesNode(Show show, Document doc)
			throws TransformerException {
		Node seriesNode = XPathAPI.selectSingleNode(doc, "show[@id="
				+ show.getShowId() + "]");
		return seriesNode;
	}

	private Node getSeasonNode(Season season, Show show, Document doc)
			throws TransformerException {
		Node node = XPathAPI.selectSingleNode(doc, "show[@id="
				+ show.getShowId() + "]/season[@number="
				+ season.getSeasonNumber() + "]");
		return node;
	}

	/**
	 * This is called to search the store for a show id. If it can't be found, then
	 * it will return null. The search is done be reading the .show.xml file within
	 * the shows directory and looking to see what show id is stored in it.
	 * @param episodeFile The file the episode is stored in 
	 * @return The results of the search if it was found, otherwise null
	 * @throws StoreException Thrown if their is a problem with the store 
	 */	
	public SearchResult searchForShowId(File rootMediaDir,File episodeFile) throws StoreException {
		SearchResult result = searchInLocalCacheFile(episodeFile);
		if (result == null) {
			
		}
		return result;
	}

	private SearchResult searchInLocalCacheFile(File episodeFile)
			throws StoreException {
		File cacheFile = getCacheFile(episodeFile.getParentFile(),FILENAME);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Document doc = null;
		if (cacheFile.exists()) {
			// Create the builder and parse the file
			try {
				doc = factory.newDocumentBuilder().parse(cacheFile);
				NodeList nodeList = XPathAPI.selectNodeList(doc, "show");				
				if (nodeList.getLength()==1) {
					if (nodeList.item(0) instanceof Element) {
						Element node = (Element) nodeList.item(0);
						if (node.getAttribute("id")!=null && node.getAttribute("sourceId")!=null) {
							log.info("Found show id in XMLStore");
							return new SearchResult(node.getAttribute("id"),node.getAttribute("sourceId"));
							
						}
							
					}
				}
			} catch (TransformerException e) {
				throw new StoreException("Unable to parse cache file: "
						+ e.getMessage(), e);	
			} catch (SAXException e) {
				throw new StoreException("Unable to parse cache file: "
						+ e.getMessage(), e);
			} catch (IOException e) {
				throw new StoreException("Unable to read cache file: "
						+ e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new StoreException("Unable to parse cache file: "
						+ e.getMessage(), e);
			}		
		} 
		return null;
	}
}

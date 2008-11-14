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
package org.stanwood.media.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.util.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This store is used to store the show information in a XML called .show.xml. 
 * This is located in the directory were the show is located. It can hold all
 * of the information of the Shows, Seasons, Episodes and Specials. This store
 * can be read and written too, and it's also possible too lookup the show id 
 * of the show in the current directory.
 */
public class XMLStore extends XMLParser implements IStore {
	private final static String FILENAME = ".show.xml";

	private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Episode getSpecial(Season season, int specialNumber)
			throws MalformedURLException, IOException, StoreException {
		Document doc = getCache(season.getShow().getShowDirectory(), season
				.getShow().getShowId());
		Episode special = null;
		try {
			special = getSpecialFromCache(specialNumber, season, doc);
			return special;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	@Override
	public Episode getEpisode(Season season, int episodeNum)
			throws StoreException, MalformedURLException {
		Document doc = getCache(season.getShow().getShowDirectory(), season
				.getShow().getShowId());
		Episode episode = null;
		try {
			episode = getEpisodeFromCache(episodeNum, season, doc);
			return episode;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	@Override
	public Season getSeason(Show show, int seasonNum) throws StoreException,
			MalformedURLException {
		Document doc = getCache(show.getShowDirectory(), show.getShowId());
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
			episode.setTotalNumber(-1);

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
		episode.setAirDate(df.parse(airDate));
		episode.setProductionCode(productionCode);
		episode.setSiteId(episodeSiteId);		
		episode.setEpisodeId(episodeId);
		episode.setGuestStars(guestStars);
		episode.setWriters(writers);
		episode.setRating(rating);
		episode.setDirectors(directors);
	}

	private List<Link> getLinks(Node episodeNode, String tagLabel)
			throws TransformerException {
		List<Link> result = new ArrayList<Link>();
		NodeList list = XPathAPI.selectNodeList(episodeNode, tagLabel);
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			result.add(new Link(element.getAttribute("name"), element
					.getAttribute("link")));
		}
		return result;
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
			int totalNumber = getIntegerFromXML(episodeNode, "@totalNumber");
			episode.setTotalNumber(totalNumber);

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

	public Show getShow(File showDirectory, long showId)
			throws StoreException, MalformedURLException, IOException {
		Document doc = getCache(showDirectory, showId);
		Show show = null;
		try {
			show = getShowFromCache(showDirectory, doc);
			return show;
		} catch (NotInStoreException e) {
			return null;
		}

	}

	private Show getShowFromCache(File showDirectory, Document doc)
			throws StoreException, NotInStoreException, MalformedURLException {
		try {
			Long showId = getLongFromXML(doc, "show/@id");
			String imageURL = getStringFromXML(doc, "show/@imageUrl");
			String showURL = getStringFromXML(doc, "show/@url");
			String name = getStringFromXML(doc, "show/@name");
			String sourceId = getStringFromXML(doc, "show/@sourceId");
			String longSummary = getStringFromXML(doc,
					"show/description/long/text()");
			String shortSummary = getStringFromXML(doc,
					"show/description/short/text()");
			List<String> genres = getGenresFromXML(doc);

			Show show = new Show(showDirectory, showId);
			show.setName(name);
			show.setImageURL(new URL(imageURL));
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

	private List<String> getGenresFromXML(Document doc)
			throws TransformerException, NotInStoreException {
		List<String> genres = new ArrayList<String>();
		NodeList nodeList = XPathAPI.selectNodeList(doc, "genre");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element node = (Element) nodeList.item(i);
				String genre = node.getAttribute("name");
				if (genre == null) {
					throw new NotInStoreException();
				}
				genres.add(genre);
			}
		}
		return genres;
	}

	

	private Document getCache(File showDirectory, long showId)
			throws StoreException {
		File cacheFile = getCacheFile(showDirectory);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Document doc = null;
		if (cacheFile.exists()) {
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
		} else {
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.newDocument();
				Element series = doc.createElement("show");
				series.setAttribute("id", String.valueOf(showId));
				doc.appendChild(series);
				writeCache(cacheFile, doc);
			} catch (ParserConfigurationException e) {
				throw new StoreException("Unable to create cache: "
						+ e.getMessage());
			}
		}

		return doc;
	}

	private String urlToText(URL url) {
		if (url==null) {
			return "";
		}
		return url.toExternalForm();
	}
	
	public void cacheShow(Show show) throws StoreException {
		Document doc = getCache(show.getShowDirectory(), show.getShowId());
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
			File cacheFile = getCacheFile(show.getShowDirectory());
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache file: "
					+ e.getMessage(), e);
		}

	}

	private void writeCache(File file, Document doc) throws StoreException {
		try {
			OutputFormat format = new OutputFormat(doc);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(
					file), format);
			serializer.serialize(doc);
		} catch (FileNotFoundException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	public File getCacheFile(File showDirectory) {
		File file = new File(showDirectory, FILENAME);
		return file;
	}

	@Override
	public void cacheEpisode(Episode episode) throws StoreException {
		Season season = episode.getSeason();
		Show show = season.getShow();

		Document doc = getCache(show.getShowDirectory(), show.getShowId());
		if (episode.isSpecial()) {
			cacheSpecial(doc, show, season, episode);
		} else {
			cacheEpisode(doc, show, season, episode);
		}
	}

	private void cacheEpisode(Document doc, Show show, Season season,
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

			((Element) node).setAttribute("totalNumber", String.valueOf(episode
					.getTotalNumber()));

			writeEpisodeCommonData(doc, episode, node);

			File cacheFile = getCacheFile(show.getShowDirectory());
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	private void cacheSpecial(Document doc, Show show, Season season,
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

			File cacheFile = getCacheFile(show.getShowDirectory());
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
				.getAirDate()));
		((Element) node).setAttribute("productionCode", episode
				.getProductionCode());
		((Element) node).setAttribute("episodeId", String.valueOf(episode
				.getEpisodeId()));

		writeEpsoideExtraInfo(doc, episode, node, "director", episode
				.getDirectors());
		writeEpsoideExtraInfo(doc, episode, node, "writers", episode
				.getWriters());
		writeEpsoideExtraInfo(doc, episode, node, "guestStars", episode
				.getGuestStars());

		Node summaryNode = XPathAPI.selectSingleNode(node, "summary");
		if (summaryNode != null) {
			summaryNode.getParentNode().removeChild(summaryNode);
		}
		summaryNode = doc.createElement("summary");
		node.appendChild(summaryNode);
		summaryNode.appendChild(doc.createTextNode(episode.getSummary()));
	}

	private void writeEpsoideExtraInfo(Document doc, Episode episode,
			Node node, String tagLabel, List<Link> links)
			throws TransformerException {
		NodeList nodeList = XPathAPI.selectNodeList(node, tagLabel);
		for (int i = 0; i < nodeList.getLength(); i++) {
			nodeList.item(i).getParentNode().removeChild(nodeList.item(i));
		}
		if (links != null) {
			for (Link value : links) {
				Element newNode = doc.createElement(tagLabel);
				newNode.setAttribute("name", value.getTitle());
				newNode.setAttribute("link", value.getLink());
				node.appendChild(newNode);
			}
		}
	}

	@Override
	public void cacheSeason(Season season) throws StoreException {
		try {
			Show show = season.getShow();
			Document doc = getCache(show.getShowDirectory(), show.getShowId());

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
			File cacheFile = getCacheFile(show.getShowDirectory());
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

	@Override
	public SearchResult searchForShowId(File showDirectory) throws StoreException {
		File cacheFile = getCacheFile(showDirectory);
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
							System.out.println("Found show id in XMLStore");
							return new SearchResult(Long.parseLong(node.getAttribute("id")),node.getAttribute("sourceId"),null);
							
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

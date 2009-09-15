package org.stanwood.media.store.xmlstore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This store is used to store the show and film information in a XML called .mediaInfoFetcher-xmlStore.xml.
 * This is located in the root media directory.
 */
public class XMLStore2 extends BaseXMLStore implements IStore {

	private static final String VERSION = "2.0";
	//TODO add correct public DTD location
	private String DTD_LOCATION = null;


	private final static Log log = LogFactory.getLog(XMLStore2.class);

	private final static String FILENAME = ".mediaInfoFetcher-xmlStore.xml";
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");


	/**
	 * This is used to write a episode or special too the store
	 * @param episode The episode or special too write
	 * @param episodeFile the file witch the episode is stored in
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheEpisode(File rootMediaDir, File episodeFile, Episode episode) throws StoreException {
		Season season = episode.getSeason();
		Show show = season.getShow();

		Document doc = getCache(rootMediaDir);
		Element seasonNode = getSeasonNode(rootMediaDir,season,doc);
		if (episode.isSpecial()) {
			cacheSpecial(rootMediaDir,episodeFile,doc, show, seasonNode, episode);
		} else {
			cacheEpisode(rootMediaDir,episodeFile,doc, show, seasonNode, episode);
		}
	}

	private void cacheEpisode(File rootMediaDir,File episodeFile,Document doc, Show show, Element seasonNode,
			Episode episode) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache episode");
		}
		try {
			Node node = XPathAPI.selectSingleNode(seasonNode, "episode[number="+ episode.getEpisodeNumber() + "]");
			if (node == null) {
				node = doc.createElement("episode");
				((Element) node).setAttribute("number", String.valueOf(episode.getEpisodeNumber()));
				seasonNode.appendChild(node);
			}

			writeEpisodeCommonData(doc, episode, node,episodeFile);

			File cacheFile = getCacheFile(rootMediaDir,FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	private void cacheSpecial(File rootMediaDir,File episodeFile,Document doc, Show show,Element seasonNode,
			Episode episode) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache special");
		}

		try {
			Node node = XPathAPI.selectSingleNode(seasonNode, "special[number="+ episode.getEpisodeNumber() + "]");
			if (node == null) {
				node = doc.createElement("special");
				((Element) node).setAttribute("number", String.valueOf(episode
						.getEpisodeNumber()));
				seasonNode.appendChild(node);
			}

			writeEpisodeCommonData(doc, episode, node,episodeFile);

			File cacheFile = getCacheFile(rootMediaDir,FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}

	private void writeEpisodeCommonData(Document doc, Episode episode, Node node,File episodeFile)
	throws TransformerException,StoreException {
		((Element) node).setAttribute("rating", String.valueOf(episode.getRating()));
		((Element) node).setAttribute("showEpisodeNumber", String.valueOf(episode.getShowEpisodeNumber()));
		((Element) node).setAttribute("title", episode.getTitle());
		((Element) node).setAttribute("url", urlToText(episode.getSummaryUrl()));
		((Element) node).setAttribute("firstAired", df.format(episode.getDate()));
		((Element) node).setAttribute("episodeId", String.valueOf(episode.getEpisodeId()));

		Node summaryNode = XPathAPI.selectSingleNode(node, "summary");
		if (summaryNode != null) {
			summaryNode.getParentNode().removeChild(summaryNode);
		}
		summaryNode = doc.createElement("summary");
		node.appendChild(summaryNode);
		summaryNode.appendChild(doc.createTextNode(episode.getSummary()));

		writeEpsoideExtraInfo(doc, node, "director", episode.getDirectors());
		writeEpsoideExtraInfo(doc, node, "writer", episode.getWriters());
		writeEpsoideExtraInfo(doc, node, "guestStar", episode.getGuestStars());

		if (episodeFile!=null &&  episodeFile.exists()) {
			appendFile(doc, node, episodeFile.getAbsolutePath());
		}
	}

	private void appendFile(Document doc, Node parent, String filename) throws StoreException {
		if (filename!=null /*&&  filename.exists()*/) {
			try {
				Node fileNode = XPathAPI.selectSingleNode(parent, "file[@location='"+filename+"']");
				if (fileNode==null) {
					fileNode = doc.createElement("file");
					((Element)fileNode).setAttribute("location", filename);
					parent.appendChild(fileNode);

				}
			} catch (TransformerException e) {
				throw new StoreException("Unable to read xml",e);
			}
		}
	}

	/**
	 * This is used to write a film to the store.
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File rootMediaDir, File filmFile, Film film) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache film " + filmFile.getAbsolutePath());
		}
		Document doc = getCache(rootMediaDir);
		try {
			Node storeNode = getStoreNode(doc);
			Node filmNode = XPathAPI.selectSingleNode(storeNode,"film[@id='" + film.getId()+"']");
			Set<String> filenames = null;
			if (filmNode != null) {
				filenames = getOldFilenames(filmNode);
				removeOldCache(filmNode, film);
			} else {
				filenames = new HashSet<String>();
			}
			filenames.add(filmFile.getAbsolutePath());

			appendFilm(doc, storeNode, film, filenames);

			File cacheFile = getCacheFile(rootMediaDir, FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
		}

	}

	private void appendFilm(Document doc, Node filmsNode, Film film, Set<String> filenames)
	throws TransformerException, StoreException {
		Element filmNode = doc.createElement("film");
		filmNode.setAttribute("id", film.getId());
		filmNode.setAttribute("title", film.getTitle());
		filmNode.setAttribute("sourceId", film.getSourceId());
		filmNode.setAttribute("rating", String.valueOf(film.getRating()));
		filmNode.setAttribute("url", urlToText(film.getFilmUrl()));
		Date date = film.getDate();
		if (date != null) {
			filmNode.setAttribute("releaseDate", df.format(date));
		}

		filmNode.setAttribute("imageUrl", urlToText(film.getImageURL()));

		appendDescription(doc,film.getSummary(),film.getDescription(),filmNode);

		if (film.getCountry()!=null) {
			List<Link> countries = new ArrayList<Link>();
			countries.add(film.getCountry());
			writeEpsoideExtraInfo(doc, filmNode, "country",countries );
		}

		for (String value : film.getGenres()) {
			Element genre = doc.createElement("genre");
			genre.setAttribute("name", value);
			if (value.equals(film.getPreferredGenre())) {
				genre.setAttribute("preferred", "true");
			}
			filmNode.appendChild(genre);
		}

		for (Certification value : film.getCertifications()) {
			Element cert = doc.createElement("certification");
			cert.setAttribute("country", value.getCountry());
			cert.setAttribute("certification", value.getCertification());
			filmNode.appendChild(cert);
		}

		for (Chapter chapter : film.getChapters()) {
			Element chap = doc.createElement("chapter");
			chap.setAttribute("number", String.valueOf(chapter.getNumber()));
			chap.setAttribute("name", chapter.getName());
			filmNode.appendChild(chap);
		}

		writeEpsoideExtraInfo(doc, filmNode, "director", film.getDirectors());
		writeEpsoideExtraInfo(doc, filmNode, "writer", film.getWriters());
		writeEpsoideExtraInfo(doc, filmNode, "guestStar", film.getGuestStars());

		writeFilenames(doc, filmNode, filenames);

		filmsNode.appendChild(filmNode);
	}

	/**
	 * Used to append a set of filenames to the document under the given parent node
	 * @param doc The document to append the filenames to
	 * @param parent The parent node
	 * @param filenames The filenames to append
	 * @throws StoreException Thrown if their is a tore releated problem
	 */
	@Override
	protected void writeFilenames(Document doc, Node parent, Set<String> filenames) throws StoreException {
		List<String> sorted = new ArrayList<String>(filenames);
		Collections.sort(sorted,new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (String filename : sorted) {
			appendFile(doc, parent, filename);
		}
	}

	private void removeOldCache(Node filmNode, Film film) throws TransformerException {
		if (filmNode != null) {
			filmNode.getParentNode().removeChild(filmNode);
		}
	}

	private Element getSeasonNode(File rootMediaDir, Season season,Document doc) throws StoreException {

		Element showNode = getShowNode(doc, season.getShow());
		Node node = getSeasonNode(season, showNode, doc);
		if (node == null) {
			node = createSeasonNode(season, showNode, doc);
		}
		return (Element)node;
	}

	/**
	 * This is used to write a season too the store.
	 * @param season The season too write
	 * @param episodeFile The file the episode is stored in
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheSeason(File rootMediaDir, File episodeFile, Season season) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache season " + season.getSeasonNumber());
		}
		Document doc = getCache(rootMediaDir);
		Element node = getSeasonNode(rootMediaDir,season,doc);
		node.setAttribute("detailedListingUrl", urlToText(season.getDetailedUrl()));
		node.setAttribute("listingUrl", urlToText(season.getListingUrl()));

		File cacheFile = getCacheFile(rootMediaDir,FILENAME);
		writeCache(cacheFile, doc);
	}


	private Element createSeasonNode(Season season, Element showNode, Document doc) {
		Element seasonEl = doc.createElement("season");
		seasonEl.setAttribute("number", String.valueOf(season.getSeasonNumber()));
		showNode.appendChild(seasonEl);
		seasonEl.setAttribute("detailedListingUrl", urlToText(season.getDetailedUrl()));
		seasonEl.setAttribute("listingUrl", urlToText(season.getListingUrl()));
		return seasonEl;
	}

	private Node getSeasonNode(Season season, Element showNode, Document doc) throws StoreException  {

		Node node;
		try {
			node = XPathAPI.selectSingleNode(showNode, "season[@number="+ season.getSeasonNumber() + "]");
		} catch (TransformerException e) {
			throw new StoreException(e.getMessage(),e);
		}
		return node;
	}

	/**
	 * This is used to write a show too the store.
	 * @param show The show too write
	 * @param episodeFile The file the episode is stored in
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheShow(File rootMediaDir, File episodeFile, Show show) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache show " + show.getShowId() + ":" + show.getSourceId());
		}
		Document doc = getCache(rootMediaDir);
		getShowNode(doc,show);

		File cacheFile = getCacheFile(rootMediaDir,FILENAME);
		writeCache(cacheFile, doc);
	}

	private Node getStoreNode(Document doc) throws TransformerException, StoreException {
		Node node = XPathAPI.selectSingleNode(doc,"store");
		if (node==null) {
			throw new StoreException("Unable to find the store node");
		}
		return node;
	}

	private Element getShowNode(Document doc, Show show) throws StoreException {
		try {
			Node storeNode = getStoreNode(doc);
			Node node = XPathAPI.selectSingleNode(storeNode,"show[@id='"+show.getShowId()+"']");
			if (node==null) {
				Element showElement = doc.createElement("show");
				showElement.setAttribute("id", String.valueOf(show.getShowId()));
				showElement.setAttribute("url", urlToText(show.getShowURL()));
				showElement.setAttribute("name", show.getName());
				showElement.setAttribute("imageUrl", urlToText(show.getImageURL()));
				showElement.setAttribute("sourceId", show.getSourceId());

				Node descriptionNode = XPathAPI.selectSingleNode(showElement, "description");
				if (descriptionNode != null) {
					showElement.removeChild(descriptionNode);
				}

				appendDescription(doc, show.getShortSummary(),show.getLongSummary(), showElement);

				NodeList nodeList = XPathAPI.selectNodeList(showElement, "genre");
				for (int i = 0; i < nodeList.getLength(); i++) {
					nodeList.item(i).getParentNode().removeChild(nodeList.item(i));
				}

				for (String value : show.getGenres()) {
					Element genre = doc.createElement("genre");
					genre.setAttribute("name", value);
					showElement.appendChild(genre);
				}

				storeNode.appendChild(showElement);
				node = showElement;
			}
			return (Element) node;
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse the XML",e);
		}

	}

	private void appendDescription(Document doc, String shortSummary,String longSummary, Element parent) {
		Element description = doc.createElement("description");
		if (shortSummary!=null) {
			Element shortDesc = doc.createElement("short");
			shortDesc.appendChild(doc.createCDATASection(shortSummary));
			description.appendChild(shortDesc);
		}
		if (longSummary!=null) {
			Element longDesc = doc.createElement("long");
			longDesc.appendChild(doc.createCDATASection(longSummary));
			description.appendChild(longDesc);
		}
		parent.appendChild(description);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Episode getEpisode(File rootMediaDir, File episodeFile, Season season, int episodeNum)
			throws StoreException, MalformedURLException, IOException {
		Document doc = getCache(rootMediaDir);
		if (doc==null) {
			return null;
		}
		Episode episode = null;
		try {
			episode = getEpisodeFromCache(episodeNum, season, doc);
			return episode;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Film getFilm(File rootMediaDir, File filmFile, String filmId) throws StoreException, MalformedURLException,
			IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Season getSeason(File rootMediaDir, File episodeFile, Show show, int seasonNum) throws StoreException,
			IOException {
		Document doc = getCache(rootMediaDir);
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

	private Episode getEpisodeFromCache(int episodeNum, Season season,
			Document doc) throws NotInStoreException, StoreException,
			MalformedURLException {
		try {
			Show show = season.getShow();
			Node episodeNode = XPathAPI.selectSingleNode(doc, "store/show[@id='"+ show.getShowId() + "' and @sourceId='"+show.getSourceId()+"']/" +
					"season[@number="+ season.getSeasonNumber() + "]/episode[@number="+ episodeNum + "]");
			if (episodeNode == null) {
				throw new NotInStoreException();
			}

			Episode episode = new Episode(episodeNum, season);
			readCommonEpisodeInfo(episodeNode, episode);
			episode.setSpecial(false);

			return episode;
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache: "
					+ e.getMessage(), e);
		} catch (ParseException e) {
			throw new StoreException(
					"Unable to parse date: " + e.getMessage(), e);
		}
	}

	private Episode getSpecialFromCache(int specialNum, Season season,
			Document doc) throws NotInStoreException, StoreException,
			MalformedURLException {
		try {
			Show show = season.getShow();
			Node episodeNode = XPathAPI.selectSingleNode(doc, "store/show[@id='"+ show.getShowId() + "' and @sourceId='"+show.getSourceId()+"']/" +
					"season[@number="+ season.getSeasonNumber() + "]/special[@number="+ specialNum + "]");
			if (episodeNode == null) {
				throw new NotInStoreException();
			}

			Episode episode = new Episode(specialNum, season);

			readCommonEpisodeInfo(episodeNode, episode);

			episode.setSpecial(true);
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


		long episodeSiteId = -1;
		try {
			episodeSiteId = getLongFromXML(episodeNode, "@showEpisodeNumber");
		}
		catch (NotInStoreException e) {
			// Field not found, so try with the old name
			try {
				episodeSiteId = getLongFromXML(episodeNode, "@siteId");
			}
			catch (NotInStoreException e1) {
				// Still not found, so throw original error
				throw e;
			}
			catch (NumberFormatException e1) {
				// Old field is not compatiable with new field, so throw original error
				throw e;
			}
		}
		long episodeId = getLongFromXML(episodeNode, "@episodeId");
		float rating = getFloatFromXML(episodeNode, "@rating");
		List<Link> directors = getLinks(episodeNode, "director");
		List<Link> writers = getLinks(episodeNode, "writer");
		List<Link> guestStars = getLinks(episodeNode, "guestStar");

		episode.setSummaryUrl(url);
		episode.setSummary(summary);
		episode.setTitle(title);
		episode.setDate(df.parse(airDate));
		episode.setShowEpisodeNumber(episodeSiteId);
		episode.setEpisodeId(episodeId);
		episode.setGuestStars(guestStars);
		episode.setWriters(writers);
		episode.setRating(rating);
		episode.setDirectors(directors);
	}

	private Season getSeasonFromCache(int seasonNum, Show show, Document doc)
	throws StoreException, NotInStoreException, MalformedURLException {
		try {
			Node seasonNode = XPathAPI.selectSingleNode(doc, "store/show[@id='"+ show.getShowId() + "' and @sourceId='"+show.getSourceId()+"']/season[@number=" + seasonNum + "]");
			if (seasonNode == null) {
				throw new NotInStoreException();
			}
			Season season = new Season(show, seasonNum);
			season.setDetailedUrl(new URL(getStringFromXML(seasonNode,"@detailedListingUrl")));
			season.setListingUrl(new URL(getStringFromXML(seasonNode,"@listingUrl")));
			return season;

		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache: "+ e.getMessage(), e);
		}
	}

	private Show getShowFromCache( Document doc)
	throws StoreException, NotInStoreException, MalformedURLException {
		try {
			Node storeNode = getStoreNode(doc);

			String showId = getStringFromXML(storeNode, "show/@id");
			String imageURL = getStringFromXML(storeNode, "show/@imageUrl");
			String showURL = getStringFromXML(storeNode, "show/@url");
			String name = getStringFromXML(storeNode, "show/@name");
			String sourceId = getStringFromXML(storeNode, "show/@sourceId");
			String longSummary = getStringFromXML(storeNode,"show/description/long/text()");
			String shortSummary = getStringFromXML(storeNode,"show/description/short/text()");
			List<String> genres = readGenresFromXML(storeNode);

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
			throw new StoreException("Unable to parse cache: "+ e.getMessage(), e);
		}
	}

	/**
	 * This will get a show from the store. If the season can't be found, then it
	 * will return null.
	 * @param episodeFile the file which the episode is stored in
	 * @param showId The id of the show to get.
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The show if it can be found, otherwise null.
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Show getShow(File rootMediaDir, File episodeFile, String showId) throws StoreException,
			MalformedURLException, IOException {

		Document doc = getCache(rootMediaDir);

		Show show = null;
		try {
			show =getShowFromCache(doc);
			return show;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Episode getSpecial(File rootMediaDir, File episodeFile, Season season, int specialNumber)
			throws MalformedURLException, IOException, StoreException {
		Document doc = getCache(rootMediaDir);
		if (doc==null) {
			return null;
		}
		Episode episode = null;
		try {
			episode = getSpecialFromCache(specialNumber, season, doc);
			return episode;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renamedFile(File rootMediaDir, File oldFile, File newFile) throws StoreException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult searchForVideoId(File rootMediaDir, Mode mode, File episodeFile) throws StoreException {
		return null;
	}



	private Document getCache(File rootMediaDirectory) throws StoreException {
		File cacheFile = getCacheFile(rootMediaDirectory, FILENAME);
		if (cacheFile.exists()) {
			Document doc = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);

			// Create the builder and parse the file
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(cacheFile);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(cacheFile);
//				doc.setXmlVersion(VERSION);
				if (errorHandler.hasErrors()) {
					throw new StoreException("Unable to parse xml, errors found in file: " + cacheFile);
				}
			} catch (SAXException e) {
				throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new StoreException("Unable to read cache file: " + e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
			}



			return doc;
		} else {
			Document doc = createCacheFile(cacheFile);
			return doc;
		}
	}

	private Document createCacheFile(File cacheFile)
			throws StoreException {
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			DocumentType docType = builder.getDOMImplementation().createDocumentType("store", DTD_LOCATION, "/home/jp/workspaces/Java/tv-and-movies-meta-data-fetcher/etc/MediaInfoFetcher-XmlStore.dtd");
			doc = builder.getDOMImplementation().createDocument(null, "store", docType);
//			doc.setXmlVersion(VERSION);
			NodeList nodes = doc.getDocumentElement().getParentNode().getChildNodes();
			for (int i=0;i<nodes.getLength();i++) {
				System.out.println(nodes.item(i).getNodeName() + " : " + nodes.item(i).getNodeValue() + " : " + nodes.item(i).getClass());
			}
			writeCache(cacheFile, doc);
		} catch (ParserConfigurationException e) {
			throw new StoreException("Unable to create cache: "
					+ e.getMessage());
		}
		return doc;
	}
}

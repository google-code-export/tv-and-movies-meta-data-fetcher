package org.stanwood.media.store.xmlstore;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.IVideoActors;
import org.stanwood.media.model.IVideoExtra;
import org.stanwood.media.model.IVideoGenre;
import org.stanwood.media.model.IVideoRating;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.model.VideoFileSet;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.xml.IterableNodeList;
import org.stanwood.media.xml.SimpleErrorHandler;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This store is used to store the show and film information in a XML called .mediaManager-xmlStore.xml.
 * This is located in the root media directory.
 */
public class XMLStore2 extends BaseXMLStore implements IStore {

	private final static String DTD_WEB_LOCATION = XMLParser.DTD_WEB_LOCATION+"/MediaManager-XmlStore-2.0.dtd"; //$NON-NLS-1$
	private final static String DTD_LOCATION = "-//STANWOOD//DTD XMLStore 2.0//EN"; //$NON-NLS-1$

	private final static Log log = LogFactory.getLog(XMLStore2.class);

	private final static String FILENAME = ".mediaManager-xmlStore.xml"; //$NON-NLS-1$
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$


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
			cacheEpisode("special",rootMediaDir,episodeFile,doc, show, seasonNode, episode); //$NON-NLS-1$
		} else {
			cacheEpisode("episode",rootMediaDir,episodeFile,doc, show, seasonNode, episode); //$NON-NLS-1$
		}
	}

	private void cacheEpisode(String nodeName,File rootMediaDir,File episodeFile,Document doc, Show show, Element seasonNode,
			Episode episode) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache episode"); //$NON-NLS-1$
		}
		try {
			episode.getFiles().add(new VideoFile(episodeFile,episodeFile,null));
			Node node = selectSingleNode(seasonNode, nodeName+"[number="+ episode.getEpisodeNumber() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			if (node == null) {
				node = doc.createElement(nodeName);
				((Element) node).setAttribute("number", String.valueOf(episode.getEpisodeNumber())); //$NON-NLS-1$
				seasonNode.appendChild(node);
			}

			writeEpisodeCommonData(doc, episode, node,episodeFile,rootMediaDir);

			File cacheFile = getCacheFile(rootMediaDir,FILENAME);
			writeCache(cacheFile, doc);
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_WRITE_CACHE") //$NON-NLS-1$
					+ e.getMessage());
		}
	}

	private void writeEpisodeCommonData(Document doc, Episode episode, Node node,File episodeFile,File rootMediaDir)
	throws StoreException {
		try {
			((Element) node).setAttribute("title", episode.getTitle()); //$NON-NLS-1$
			((Element) node).setAttribute("url", urlToText(episode.getUrl())); //$NON-NLS-1$
			if (episode.getDate()!=null) {
				((Element) node).setAttribute("firstAired", df.format(episode.getDate())); //$NON-NLS-1$
			}
			((Element) node).setAttribute("episodeId", episode.getEpisodeId()); //$NON-NLS-1$
			((Element) node).setAttribute("imageUrl", urlToText(episode.getImageURL())); //$NON-NLS-1$

			Node summaryNode = selectSingleNode(node, "summary"); //$NON-NLS-1$
			if (summaryNode != null) {
				summaryNode.getParentNode().removeChild(summaryNode);
			}
			summaryNode = doc.createElement("summary"); //$NON-NLS-1$
			node.appendChild(summaryNode);
			summaryNode.appendChild(doc.createTextNode(episode.getSummary()));
			writeRating(episode,((Element) node));

			writeDirectors((Element)node,episode);
			writeWriters((Element)node,episode);
			writeActors(node,episode);

			episode.getFiles().add(new VideoFile(episodeFile,episodeFile,null));
			writeFilenames(doc, node, episode,rootMediaDir);
		}
		catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_WRITE_EPISODE_DATA"),e); //$NON-NLS-1$
		}
	}

	private void writeActors(Node node, IVideoActors episode) {
		Document doc =  node.getOwnerDocument();
		Element actors = doc.createElement("actors"); //$NON-NLS-1$
		if (episode.getActors()!=null) {
			for (Actor actor : episode.getActors()) {
				Element actorNode = doc.createElement("actor"); //$NON-NLS-1$
				actorNode.setAttribute("name", actor.getName()); //$NON-NLS-1$
				actorNode.setAttribute("role", actor.getRole()); //$NON-NLS-1$
				actors.appendChild(actorNode);
			}
		}
		node.appendChild(actors);
	}

	private void readActors(Node node,IVideoActors episode) throws XMLParserException {
		List<Actor> actors = new ArrayList<Actor>();
		for (Node n : selectNodeList(node, "actors/actor")) { //$NON-NLS-1$
			Element e  = (Element) n;
			actors.add(new Actor(e.getAttribute("name"),e.getAttribute("role"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		episode.setActors(actors);
	}

	private void appendFile(Document doc, Node parent, VideoFile file,File rootMediaDir) throws StoreException {
		if (file!=null) {
			try {

				Element fileNode = (Element)selectSingleNode(parent, "file[@location="+quoteXPathQuery(makePathRelativeToMediaDir(file.getLocation(), rootMediaDir))+"]"); //$NON-NLS-1$ //$NON-NLS-2$
				if (fileNode==null) {
					fileNode = doc.createElement("file"); //$NON-NLS-1$
					parent.appendChild(fileNode);
				}

				fileNode.setAttribute("location", makePathRelativeToMediaDir(file.getLocation(),rootMediaDir)); //$NON-NLS-1$
				if (file.getPart()!=null) {
					fileNode.setAttribute("part", String.valueOf(file.getPart())); //$NON-NLS-1$
				}
			} catch (XMLParserException e) {
				throw new StoreException(Messages.getString("XMLStore2.UNABLE_READ_XML"),e); //$NON-NLS-1$
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
	public void cacheFilm(File rootMediaDir, File filmFile, Film film,Integer part) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("cache film " + filmFile.getAbsolutePath()); //$NON-NLS-1$
		}
		Document doc = getCache(rootMediaDir);
		try {
			Node storeNode = getStoreNode(doc);
			film.getFiles().add(new VideoFile(filmFile,filmFile,part));
			appendFilm(doc, storeNode, film,rootMediaDir);

			File cacheFile = getCacheFile(rootMediaDir, FILENAME);
			writeCache(cacheFile, doc);
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_CACHE_FILE"), e); //$NON-NLS-1$
		}
	}

	private void appendFilm(Document doc, Node filmsNode, Film film,File rootMediaDir)
	throws XMLParserException, StoreException {
		Element filmNode = (Element) selectSingleNode(filmsNode, "film[@id='"+film.getId()+"']"); //$NON-NLS-1$ //$NON-NLS-2$
		if (filmNode!=null) {
			filmNode.getParentNode().removeChild(filmNode);
		}

		filmNode = doc.createElement("film"); //$NON-NLS-1$
		filmsNode.appendChild(filmNode);

		filmNode.setAttribute("id", film.getId()); //$NON-NLS-1$
		filmNode.setAttribute("title", film.getTitle()); //$NON-NLS-1$
		filmNode.setAttribute("sourceId", film.getSourceId()); //$NON-NLS-1$
		filmNode.setAttribute("url", urlToText(film.getFilmUrl())); //$NON-NLS-1$
		Date date = film.getDate();
		if (date != null) {
			filmNode.setAttribute("releaseDate", df.format(date)); //$NON-NLS-1$
		}

		filmNode.setAttribute("imageUrl", urlToText(film.getImageURL())); //$NON-NLS-1$

		appendDescription(doc,film.getSummary(),film.getDescription(),filmNode);
		writeRating(film,filmNode);

		if (film.getCountry()!=null) {
			Element country = doc.createElement("country"); //$NON-NLS-1$
			country.appendChild(doc.createTextNode(film.getCountry()));
			filmNode.appendChild(country);
		}

		writeGenres(film, filmNode);
		writeCertifications(film,filmNode);
		writeDirectors(filmNode, film);
		writeWriters(filmNode, film);
		writeActors(filmNode,film);
		writeChapters(film, filmNode);
		writeFilenames(doc, filmNode, film,rootMediaDir);


	}

	private void writeChapters( Film film, Element filmNode) {
		Document doc = filmNode.getOwnerDocument();
		Element chaptersNode = doc.createElement("chapters"); //$NON-NLS-1$
		for (Chapter chapter : film.getChapters()) {
			Element chap = doc.createElement("chapter"); //$NON-NLS-1$
			chap.setAttribute("number", String.valueOf(chapter.getNumber())); //$NON-NLS-1$
			chap.setAttribute("name", chapter.getName()); //$NON-NLS-1$
			chaptersNode.appendChild(chap);
		}
		filmNode.appendChild(chaptersNode);
	}

	private void readChapters( Film film, Element filmNode) throws XMLParserException {
		List<Chapter>chapters = new ArrayList<Chapter>();
		for (Node n : selectNodeList(filmNode, "chapters/chapter")) { //$NON-NLS-1$
			Element chapNode = (Element)n;

			Chapter chapter = new Chapter(chapNode.getAttribute("name"),Integer.parseInt(chapNode.getAttribute("number"))); //$NON-NLS-1$ //$NON-NLS-2$
			chapters.add(chapter);
		}
		film.setChapters(chapters);
	}

	protected void readWriters(IVideo video,Node videoNode)
	throws XMLParserException, NotInStoreException {
		List<String> writers = new ArrayList<String>();
		for (Node node : selectNodeList(videoNode, "writers/writer/text()")) { //$NON-NLS-1$
			String writer = node.getTextContent();
			writers.add(writer);
		}
		video.setWriters(writers);
	}

	private void writeWriters(Element node, IVideo video) {
		Document doc = node.getOwnerDocument();
		Element writersNode = doc.createElement("writers"); //$NON-NLS-1$
		if (video.getWriters()!=null) {
			for (String value : video.getWriters()) {
				Element writerNode = doc.createElement("writer"); //$NON-NLS-1$
				writerNode.appendChild(doc.createTextNode(value));
				writersNode.appendChild(writerNode);
			}
		}
		node.appendChild(writersNode);
	}

	protected void readDirectors(IVideo video,Node videoNode)
	throws XMLParserException, NotInStoreException {
		List<String> directors = new ArrayList<String>();
		for (Node node : selectNodeList(videoNode, "directors/director/text()")) { //$NON-NLS-1$
			String director = node.getTextContent();
			directors.add(director);
		}
		video.setDirectors(directors);
	}

	private void writeDirectors(Element node, IVideo video) {
		Document doc = node.getOwnerDocument();
		Element directorsNode = doc.createElement(Messages.getString("XMLStore2.48")); //$NON-NLS-1$
		if (video.getDirectors()!=null) {
			for (String value : video.getDirectors()) {
				Element director = doc.createElement("director"); //$NON-NLS-1$
				director.appendChild(doc.createTextNode(value));
				directorsNode.appendChild(director);
			}
		}
		node.appendChild(directorsNode);
	}

	protected void readCertifications(Film video,Node videoNode)
	throws XMLParserException, NotInStoreException {

		List<Certification>certifications = new ArrayList<Certification>();
		for (Node node : selectNodeList(videoNode, "certifications/certification")) { //$NON-NLS-1$
			Element certificationEl = (Element)node;
			certifications.add(new Certification(certificationEl.getAttribute("certification"), certificationEl.getAttribute("type"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		video.setCertifications(certifications);
	}

	private void writeCertifications(Film film, Element node) {
		Document doc = node.getOwnerDocument();
		Element certificationsNode = doc.createElement("certifications"); //$NON-NLS-1$
		if (film.getCertifications()!=null) {
			for (Certification cert : film.getCertifications()) {
				Element certificationNode = node.getOwnerDocument().createElement("certification"); //$NON-NLS-1$
				certificationNode.setAttribute("type", cert.getType()); //$NON-NLS-1$
				certificationNode.setAttribute("certification", cert.getCertification()); //$NON-NLS-1$
				certificationsNode.appendChild(certificationNode);
			}
		}
		node.appendChild(certificationsNode);
	}

	private void writeGenres(IVideoGenre video, Element node) {
		Document doc = node.getOwnerDocument();
		Element genresNode = doc.createElement("genres"); //$NON-NLS-1$
		if (video.getGenres()!=null) {
			for (String value : video.getGenres()) {
				Element genre = node.getOwnerDocument().createElement("genre"); //$NON-NLS-1$
				genre.setAttribute("name", value); //$NON-NLS-1$
				if (value.equals(video.getPreferredGenre())) {
					genre.setAttribute("preferred", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				genresNode.appendChild(genre);
			}
		}
		node.appendChild(genresNode);
	}

	protected void readGenres(IVideoGenre video,Node videoNode)
			throws XMLParserException, NotInStoreException {

		List<String>genres = new ArrayList<String>();
		for (Node node : selectNodeList(videoNode, "genres/genre")) { //$NON-NLS-1$
			Element genreEl = (Element)node;
			String genre = genreEl.getAttribute("name"); //$NON-NLS-1$
			String preferred = genreEl.getAttribute("preferred"); //$NON-NLS-1$
			if (preferred.equals("true")) { //$NON-NLS-1$
				video.setPreferredGenre(genre);
			}
			genres.add(genre);
		}
		video.setGenres(genres);
	}

	private void writeExtraParams(IVideoExtra video, Element node) {
		Document doc = node.getOwnerDocument();
		Element extraNode = doc.createElement("extra"); //$NON-NLS-1$
		if (video.getExtraInfo()!=null) {
			for (Entry<String, String> e : video.getExtraInfo().entrySet()) {
				Element param = node.getOwnerDocument().createElement("param"); //$NON-NLS-1$
				param.setAttribute("key",e.getKey()); //$NON-NLS-1$
				param.setAttribute("value", e.getValue()); //$NON-NLS-1$
				extraNode.appendChild(param);
			}
		}
		node.appendChild(extraNode);
	}

	protected void readExtraParams(IVideoExtra video,Node videoNode)
	throws XMLParserException, NotInStoreException {
		Map<String,String>params = new HashMap<String,String>();
		for (Node node : selectNodeList(videoNode, "extra/param")) { //$NON-NLS-1$
			Element extraEl = (Element)node;
			String value = extraEl.getAttribute("value"); //$NON-NLS-1$
			String key = extraEl.getAttribute("key"); //$NON-NLS-1$
			params.put(key,value);
		}
		video.setExtraInfo(params);
	}


	/**
	 * Used to append a set of filenames to the document under the given parent node
	 * @param doc The document to append the filenames to
	 * @param parent The parent node
	 * @param filenames The filenames to append
	 * @throws StoreException Thrown if their is a tore releated problem
	 */
	protected void writeFilenames(Document doc, Node parent, IVideo video,File rootMediaDir) throws StoreException {
		for (VideoFile filename : video.getFiles()) {
			appendFile(doc, parent, filename,rootMediaDir);
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
			log.debug("cache season " + season.getSeasonNumber()); //$NON-NLS-1$
		}
		Document doc = getCache(rootMediaDir);
		Element node = getSeasonNode(rootMediaDir,season,doc);
		node.setAttribute("url", urlToText(season.getURL())); //$NON-NLS-1$

		File cacheFile = getCacheFile(rootMediaDir,FILENAME);
		writeCache(cacheFile, doc);
	}


	private Element createSeasonNode(Season season, Element showNode, Document doc) {
		Element seasonEl = doc.createElement("season"); //$NON-NLS-1$
		seasonEl.setAttribute("number", String.valueOf(season.getSeasonNumber())); //$NON-NLS-1$
		showNode.appendChild(seasonEl);
		seasonEl.setAttribute("url", urlToText(season.getURL())); //$NON-NLS-1$
		return seasonEl;
	}

	private Node getSeasonNode(Season season, Element showNode, Document doc) throws StoreException  {

		Node node;
		try {
			node = selectSingleNode(showNode, "season[@number="+ season.getSeasonNumber() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (XMLParserException e) {
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
			log.debug("cache show " + show.getShowId() + ":" + show.getSourceId()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Document doc = getCache(rootMediaDir);
		getShowNode(doc,show);

		File cacheFile = getCacheFile(rootMediaDir,FILENAME);
		writeCache(cacheFile, doc);
	}

	private Node getStoreNode(Document doc) throws XMLParserException, StoreException {
		Node node = selectSingleNode(doc,"store"); //$NON-NLS-1$
		if (node==null) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_FIND_STORE_NODE")); //$NON-NLS-1$
		}
		return node;
	}

	private Element getShowNode(Document doc, Show show) throws StoreException {
		try {
			Node storeNode = getStoreNode(doc);
			Node node = selectSingleNode(storeNode,"show[@id='"+show.getShowId()+"']"); //$NON-NLS-1$ //$NON-NLS-2$
			if (node==null) {
				Element showElement = doc.createElement("show"); //$NON-NLS-1$
				showElement.setAttribute("id", String.valueOf(show.getShowId())); //$NON-NLS-1$
				showElement.setAttribute("url", urlToText(show.getShowURL())); //$NON-NLS-1$
				showElement.setAttribute("name", show.getName()); //$NON-NLS-1$
				showElement.setAttribute("imageUrl", urlToText(show.getImageURL())); //$NON-NLS-1$
				showElement.setAttribute("sourceId", show.getSourceId()); //$NON-NLS-1$

				Node descriptionNode = selectSingleNode(showElement, "description"); //$NON-NLS-1$
				if (descriptionNode != null) {
					showElement.removeChild(descriptionNode);
				}

				appendDescription(doc, show.getShortSummary(),show.getLongSummary(), showElement);

				writeGenres(show, showElement);
				writeExtraParams(show,showElement);

				storeNode.appendChild(showElement);
				node = showElement;
			}
			return (Element) node;
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_XML"),e); //$NON-NLS-1$
		}

	}



	private void appendDescription(Document doc, String shortSummary,String longSummary, Element parent) {
		Element description = doc.createElement("description"); //$NON-NLS-1$
		if (shortSummary!=null) {
			Element shortDesc = doc.createElement("short"); //$NON-NLS-1$
			shortDesc.appendChild(doc.createCDATASection(shortSummary));
			description.appendChild(shortDesc);
		}
		if (longSummary!=null) {
			Element longDesc = doc.createElement("long"); //$NON-NLS-1$
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
			throws StoreException, MalformedURLException {
		Document doc = getCache(rootMediaDir);
		if (doc==null) {
			return null;
		}
		Episode episode = null;
		try {
			episode = getEpisodeFromCache(episodeNum, season, doc,rootMediaDir);
			return episode;
		} catch (NotInStoreException e) {
			return null;
		}
	}

	/**
	 * This will get a film from the store. If the film can't be found, then it will return null.
	 * @param filmFile The file the film is located in.
	 * @param filmId The id of the film
	 * @param rootMediaDir This is the directory which is the root of media, this can be the current directory if
	 *         it was not specified on the command line.
	 * @return The film, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 */
	@Override
	public Film getFilm(File rootMediaDir, File filmFile, String filmId) throws StoreException, MalformedURLException {
		Document doc = getCache(rootMediaDir);
		if (doc==null) {
			return null;
		}

		Film film = new Film(filmId);

		try {
			Element filmNode = (Element) selectSingleNode(doc, "store/film[@id='"+filmId+"']"); //$NON-NLS-1$ //$NON-NLS-2$
			if (filmNode==null) {
				if (log.isDebugEnabled()) {
					log.debug("Film with id '"+filmId+"' is not in store " + XMLStore2.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return null;
			}
			readGenres(film, filmNode);
			film.setCountry(getStringFromXMLOrNull(filmNode, "country/text()")); //$NON-NLS-1$
			try {
				film.setDate(df.parse(getStringFromXML(filmNode, "@releaseDate"))); //$NON-NLS-1$
			}
			catch (XMLParserNotFoundException e) {
				// Ignore does not have a date
			}
			film.setFilmUrl(new URL(getStringFromXML(filmNode,"@url"))); //$NON-NLS-1$
			film.setDescription(getStringFromXML(filmNode,"description/long/text()")); //$NON-NLS-1$
			film.setSummary(getStringFromXML(filmNode,"description/short/text()")); //$NON-NLS-1$
			film.setImageURL(getURLFromXMLOrNull(filmNode, "@imageUrl")); //$NON-NLS-1$
			film.setTitle(getStringFromXML(filmNode, "@title")); //$NON-NLS-1$
			parseRating(film,filmNode);
			readActors(filmNode,film);
			readWriters(film, filmNode);
			readDirectors(film, filmNode);
			film.setSourceId(getStringFromXML(filmNode, "@sourceId")); //$NON-NLS-1$
			readChapters(film, filmNode);
			readCertifications(film, filmNode);
			readFiles(film,filmNode,rootMediaDir);
		}
		catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_READ_FILM_FROM_STORE"),e); //$NON-NLS-1$
		}
		catch (NotInStoreException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_READ_FILM_FROM_STORE"),e); //$NON-NLS-1$
		}
		catch (ParseException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_READ_FILM_FROM_STORE"),e); //$NON-NLS-1$
		}

		return film;
	}

	private void readFiles(IVideo video, Element videoNode,File rootMediaDir) throws XMLParserException {
		SortedSet<VideoFile> files = new VideoFileSet();

		for (Node node : selectNodeList(videoNode, "file")) { //$NON-NLS-1$
			String location = ((Element)node).getAttribute("location"); //$NON-NLS-1$
			String originalLocation = ((Element)node).getAttribute("orginalLocation"); //$NON-NLS-1$
			File orgLocFile = null;
			if (!originalLocation.equals("")) { //$NON-NLS-1$
				orgLocFile = new File(rootMediaDir,originalLocation);
			}
			String strPart = ((Element)node).getAttribute("part"); //$NON-NLS-1$
			Integer part = null;
			if (!strPart.equals("")) { //$NON-NLS-1$
				part = Integer.parseInt(strPart);
			}
			files.add(new VideoFile(new File(rootMediaDir,location),orgLocFile,part));
		}
		video.setFiles(files);
	}

	private void parseRating(IVideoRating film, Element node) throws XMLParserException {
		try {
			int numberOfVotes = getIntegerFromXML(node, "rating/@numberOfVotes"); //$NON-NLS-1$
			float rating = getFloatFromXML(node, "rating/@value"); //$NON-NLS-1$

			film.setRating(new Rating(rating,numberOfVotes));
		}
		catch (XMLParserNotFoundException e) {
			// Ignore, not found
		}
	}

	private void writeRating(IVideoRating video, Element node) {
		if (video.getRating()!=null) {
			Element ratingNode = node.getOwnerDocument().createElement("rating"); //$NON-NLS-1$
			ratingNode.setAttribute("value", String.valueOf(video.getRating().getRating())); //$NON-NLS-1$
			ratingNode.setAttribute("numberOfVotes", String.valueOf(video.getRating().getNumberOfVotes())); //$NON-NLS-1$
			node.appendChild(ratingNode);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Season getSeason(File rootMediaDir, File episodeFile, Show show, int seasonNum) throws StoreException,
	MalformedURLException {
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

	private Episode getEpisodeFromCache(int episodeNum, Season season,Document doc,File rootMediaDir) throws NotInStoreException, StoreException,
			MalformedURLException {
		try {
			Show show = season.getShow();
			Node episodeNode = selectSingleNode(doc, "store/show[@id='"+ show.getShowId() + "' and @sourceId='"+show.getSourceId()+"']/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"season[@number="+ season.getSeasonNumber() + "]/episode[@number="+ episodeNum + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (episodeNode == null) {
				throw new NotInStoreException();
			}

			Episode episode = new Episode(episodeNum, season);
			readCommonEpisodeInfo(episodeNode, episode,rootMediaDir);
			episode.setSpecial(false);

			return episode;

		} catch (ParseException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_DATA"), e); //$NON-NLS-1$
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_CACHE"), e); //$NON-NLS-1$
		}
	}

	private Episode getSpecialFromCache(int specialNum, Season season,Document doc,File rootMediaDir) throws NotInStoreException, StoreException,
			MalformedURLException {
		try {
			Show show = season.getShow();
			Node episodeNode = selectSingleNode(doc, "store/show[@id='"+ show.getShowId() + "' and @sourceId='"+show.getSourceId()+"']/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"season[@number="+ season.getSeasonNumber() + "]/special[@number="+ specialNum + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (episodeNode == null) {
				throw new NotInStoreException();
			}

			Episode episode = new Episode(specialNum, season);

			readCommonEpisodeInfo(episodeNode, episode,rootMediaDir);

			episode.setSpecial(true);
			return episode;

		} catch (ParseException e) {
			throw new StoreException(
					"Unable to parse date: " + e.getMessage(), e); //$NON-NLS-1$
		} catch (XMLParserException e) {
			throw new StoreException("Unable to parse cache: " //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

	private void readCommonEpisodeInfo(Node episodeNode, Episode episode,File rootMediaDir)
	throws XMLParserException, NotInStoreException,MalformedURLException, ParseException {
		try {
			String summary = getStringFromXML(episodeNode, "summary/text()"); //$NON-NLS-1$
			URL url = new URL(getStringFromXML(episodeNode, "@url")); //$NON-NLS-1$
			String title = getStringFromXML(episodeNode, "@title"); //$NON-NLS-1$
			String airDate = getStringFromXML(episodeNode, "@firstAired"); //$NON-NLS-1$
			String episodeId = getStringFromXML(episodeNode, "@episodeId"); //$NON-NLS-1$
			String urlStr = getStringFromXML(episodeNode, "@imageUrl"); //$NON-NLS-1$
			URL imageUrl = null;
			if (urlStr.length()>0) {
				imageUrl = new URL(urlStr);
			}

			episode.setUrl(url);
			episode.setSummary(summary);
			episode.setTitle(title);
			episode.setDate(df.parse(airDate));
			episode.setEpisodeId(episodeId);
			episode.setImageURL(imageUrl);
			readActors(episodeNode,episode);
			readWriters(episode, episodeNode);
			parseRating(episode,(Element)episodeNode);
			readDirectors(episode, episodeNode);
			readFiles(episode,(Element)episodeNode,rootMediaDir);
		}
		catch (XMLParserNotFoundException e) {
			throw new NotInStoreException();
		}

	}

	private Season getSeasonFromCache(int seasonNum, Show show, Document doc)
	throws StoreException, NotInStoreException, MalformedURLException {
		try {
			Node seasonNode = selectSingleNode(doc, "store/show[@id='"+ show.getShowId() + "' and @sourceId='"+show.getSourceId()+"']/season[@number=" + seasonNum + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if (seasonNode == null) {
				throw new NotInStoreException();
			}
			Season season = new Season(show, seasonNum);
			season.setURL(new URL(getStringFromXML(seasonNode,"@url"))); //$NON-NLS-1$
			return season;
		}
		 catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_CACHE"), e); //$NON-NLS-1$
		}
	}

	private Show getShowFromCache( Document doc, String showId)
	throws StoreException, NotInStoreException, MalformedURLException {
		try {
			Node storeNode = getStoreNode(doc);
			Element showNode = (Element) selectSingleNode(storeNode, "show[@id="+quoteXPathQuery(showId)+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			if (showNode==null) {
				throw new NotInStoreException();
			}
			String imageURL = showNode.getAttribute("imageUrl"); //$NON-NLS-1$
			String showURL = showNode.getAttribute("url"); //$NON-NLS-1$
			String name = showNode.getAttribute("name"); //$NON-NLS-1$
			String sourceId = showNode.getAttribute("sourceId"); //$NON-NLS-1$
			String longSummary = getStringFromXML(showNode,"description/long/text()"); //$NON-NLS-1$
			String shortSummary = getStringFromXML(showNode,"description/short/text()"); //$NON-NLS-1$

			Show show = new Show(showId);
			readGenres(show, showNode);
			readExtraParams(show,showNode);
			show.setName(name);
			try {
				show.setImageURL(new URL(imageURL));
			}
			catch (MalformedURLException e) {
				log.warn(MessageFormat.format(Messages.getString("XMLStore2.UNABLE_GET_SHOW_URL"),imageURL)); //$NON-NLS-1$
			}

			show.setLongSummary(longSummary);
			show.setShortSummary(shortSummary);
			show.setShowURL(new URL(showURL));
			show.setSourceId(sourceId);

			return show;
		} catch (XMLParserNotFoundException e) {
			throw new NotInStoreException();
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_CACHE"), e); //$NON-NLS-1$
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
	 */
	@Override
	public Show getShow(File rootMediaDir, File episodeFile, String showId) throws StoreException,
			MalformedURLException {

		Document doc = getCache(rootMediaDir);

		Show show = null;
		try {
			show =getShowFromCache(doc,showId);
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
			episode = getSpecialFromCache(specialNumber, season, doc,rootMediaDir);
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
		Document doc = getCache(rootMediaDir);
		if (doc!=null) {
			try {
				for (Node node : selectNodeList(doc, "//file[@location="+quoteXPathQuery(makePathRelativeToMediaDir(oldFile, rootMediaDir))+"]")) { //$NON-NLS-1$ //$NON-NLS-2$
					Element fileNode = (Element)node;
					fileNode.setAttribute("location", makePathRelativeToMediaDir(newFile,rootMediaDir)); //$NON-NLS-1$
					if (fileNode.getAttribute("orginalLocation").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
						fileNode.setAttribute("orginalLocation",makePathRelativeToMediaDir(oldFile, rootMediaDir)); //$NON-NLS-1$
					}
				}

				File cacheFile = getCacheFile(rootMediaDir,FILENAME);
				writeCache(cacheFile, doc);
			} catch (XMLParserException e) {
				throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_XML2"),e); //$NON-NLS-1$
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		Document doc = getCache(dirConfig.getMediaDir());
		if (doc!=null) {
			Node store;
			try {
				store = getStoreNode(doc);
				if (store!=null) {
					if (dirConfig.getMode()==Mode.TV_SHOW) {
						return searchForTVShow(store,mediaFile,dirConfig.getPattern(),dirConfig.getMediaDir());
					}
					else {
						return searchForFilm(store,mediaFile,dirConfig);
					}
				}
			} catch (XMLParserException e) {
				throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_STORE_XML"),e); //$NON-NLS-1$
			}
		}
		return null;
	}



	private SearchResult searchForFilm(Node store, File episodeFile,MediaDirConfig dirConfig) throws XMLParserException {
		SearchResult result = null;

		// search for film by file name
		for (Node node : selectNodeList(store,"film/file[@location="+quoteXPathQuery(makePathRelativeToMediaDir(episodeFile,dirConfig.getMediaDir()))+"]")) { //$NON-NLS-1$ //$NON-NLS-2$
			Element filmEl = (Element)node.getParentNode();
			Integer part = null;
			if (!((Element)node).getAttribute("part").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				part = Integer.parseInt(((Element)node).getAttribute("part")); //$NON-NLS-1$
			}
			result = new SearchResult(filmEl.getAttribute("id"), filmEl.getAttribute("url"), filmEl.getAttribute("sourceId"),part); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return result;
	}

	private String makePathRelativeToMediaDir(File episodeFile, File rootMediaDir) {
		String path = rootMediaDir.getAbsolutePath();
		int len = path.length();
		if (episodeFile.getAbsolutePath().startsWith(path)) {
			return episodeFile.getAbsolutePath().substring(len+1);
		}
		else {
			return episodeFile.getAbsolutePath();
		}
	}

	private SearchResult searchForTVShow(Node store, File episodeFile, String renamePattern,File rootMediaDir) throws XMLParserException {
		SearchResult result = null;

		String query = quoteXPathQuery(makePathRelativeToMediaDir(episodeFile,rootMediaDir));
		// search for show by file name
		NodeList showNodes = selectNodeList(store,"show/season/episode/file[@location="+query+"]"); //$NON-NLS-1$ //$NON-NLS-2$
		if (showNodes!=null && showNodes.getLength()>0) {
			Element showEl = (Element)showNodes.item(0).getParentNode().getParentNode().getParentNode();
			Integer part = null;
			if (!((Element)showNodes.item(0)).getAttribute("part").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				part = Integer.parseInt(((Element)showNodes.item(0)).getAttribute("part")); //$NON-NLS-1$
			}
			result = new SearchResult(showEl.getAttribute("id"), showEl.getAttribute("url"), showEl.getAttribute("sourceId"),part); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return result;
	}

	private Document getCache(File rootMediaDirectory) throws StoreException {
		File cacheFile = getCacheFile(rootMediaDirectory, FILENAME);
		if (cacheFile.exists()) {
			Document doc = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);

			// Create the builder and parse the file
			try {
				DocumentBuilder builder = XMLParser.createDocBuilder(factory);
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(cacheFile);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(cacheFile);
				if (errorHandler.hasErrors()) {
					throw new StoreException(Messages.getString("XMLStore2.ERRORS_IN_FILE") + cacheFile); //$NON-NLS-1$
				}
			} catch (SAXException e) {
				throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_CACHE_FILE"), e); //$NON-NLS-1$
			} catch (IOException e) {
				throw new StoreException(Messages.getString("XMLStore2.UNABLE_READ_CACHE_FILE"), e); //$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_CACHE_FILE"), e); //$NON-NLS-1$
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
		try {
			DocumentBuilder builder = createDocBuilder(factory);

			DocumentType docType = builder.getDOMImplementation().createDocumentType("store", DTD_LOCATION, DTD_WEB_LOCATION); //$NON-NLS-1$
			doc = builder.getDOMImplementation().createDocument(null, "store", docType); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_CREATE_CACHE"),e); //$NON-NLS-1$
		}
		return doc;
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) {

	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void performedActions(MediaDirectory dir) throws StoreException {
		File rootMediaDir = dir.getMediaDirConfig().getMediaDir();
		Document cache = getCache(rootMediaDir);
		try {
			// Remove file elements when the actual file can't be found
			boolean changed = false;
			for (Node fileNode : selectNodeList(cache, "//file")) { //$NON-NLS-1$
				Element el = (Element)fileNode;
				File location = new File(dir.getMediaDirConfig().getMediaDir(),el.getAttribute("location")); //$NON-NLS-1$
				if (!location.exists()) {
					if (log.isDebugEnabled()) {
						log.debug("Unable to find file '"+location.getAbsolutePath()+"' so removing from store"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					el.getParentNode().removeChild(el);
					changed = true;
				}
			}

			// Remove any nodes which don't have media
			for (Node filmNode : selectNodeList(cache, "store/film")) { //$NON-NLS-1$
				if (!hasFileNodes(filmNode)) {
					filmNode.getParentNode().removeChild(filmNode);
					changed = true;
				}
			}

			for (Node showNode : selectNodeList(cache, "store/show")) { //$NON-NLS-1$
				for (Node seasonNode : selectNodeList(showNode, "season")) { //$NON-NLS-1$
					for (Node episodeNode : selectNodeList(seasonNode, "episode")) { //$NON-NLS-1$
						if (!hasFileNodes(episodeNode)) {
							seasonNode.removeChild(episodeNode);
							changed = true;
						}
					}
					for (Node episodeNode : selectNodeList(seasonNode, Messages.getString("XMLStore2.201"))) { //$NON-NLS-1$
						if (!hasFileNodes(episodeNode)) {
							seasonNode.removeChild(episodeNode);
							changed = true;
						}
					}
					if (!hasFileNodes(seasonNode)) {
						showNode.removeChild(seasonNode);
						changed = true;
					}
				}
				if (!hasFileNodes(showNode)) {
					showNode.getParentNode().removeChild(showNode);
					changed = true;
				}
			}

			if (changed) {
				File cacheFile = getCacheFile(rootMediaDir,FILENAME);
				writeCache(cacheFile, cache);
			}
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_STORE_XML2"),e); //$NON-NLS-1$
		}
	}

	private boolean hasFileNodes(Node parentNode) throws XMLParserException{
		IterableNodeList nodes = selectNodeList(parentNode, ".//file"); //$NON-NLS-1$
		return !(nodes==null || nodes.getLength()==0);
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) throws StoreException {
		File rootMediaDir = dir.getMediaDirConfig().getMediaDir();
		Document cache = getCache(rootMediaDir);
		try {
			boolean changed = false;
			for (Node fileNode : selectNodeList(cache, "//file")) { //$NON-NLS-1$
				Element el = (Element)fileNode;
				File location = new File(dir.getMediaDirConfig().getMediaDir(),el.getAttribute("location")); //$NON-NLS-1$
				if (location.equals(file)) {
					if (log.isDebugEnabled()) {
						log.debug("Unable to find file '"+location.getAbsolutePath()+"' so removing from store"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					el.getParentNode().removeChild(el);
					changed = true;
				}
			}
			if (changed) {
				File cacheFile = getCacheFile(rootMediaDir,FILENAME);
				writeCache(cacheFile, cache);
			}
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_STORE_XML1"),e); //$NON-NLS-1$
		}
	}

	private Element getNodeWithFile(MediaDirectory dir,File file) throws StoreException, XMLParserException {
		File rootMediaDir = dir.getMediaDirConfig().getMediaDir();
		Document cache = getCache(rootMediaDir);
		for (Node fileNode : selectNodeList(cache, "//file")) { //$NON-NLS-1$
			Element el = (Element)fileNode;
			File location = new File(dir.getMediaDirConfig().getMediaDir(),el.getAttribute("location")); //$NON-NLS-1$
			if (location.equals(file)) {
				return (Element)fileNode.getParentNode();
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Episode getEpisode(MediaDirectory dir, File file) throws StoreException {
		try {
			File rootMediaDir = dir.getMediaDirConfig().getMediaDir();
			Element episodeNode = getNodeWithFile(dir,file);
			if (episodeNode==null || !episodeNode.getNodeName().equals("episode")) { //$NON-NLS-1$
				return null;
			}
			Element seasonNode = (Element) episodeNode.getParentNode();
			if (seasonNode==null || !seasonNode.getNodeName().equals("season")) { //$NON-NLS-1$
				return null;
			}
			Element showNode = (Element) seasonNode.getParentNode();
			if (showNode==null || !showNode.getNodeName().equals("show")) { //$NON-NLS-1$
				return null;
			}

			Show show = getShow(rootMediaDir,file,showNode.getAttribute("id")); //$NON-NLS-1$
			if (show==null) {
				return null;
			}
			Season season = getSeason(rootMediaDir,file,show,Integer.parseInt(seasonNode.getAttribute("number"))); //$NON-NLS-1$
			if (season==null) {
				return null;
			}
			Episode episode = getEpisode(rootMediaDir, file, season, Integer.parseInt(episodeNode.getAttribute("number"))); //$NON-NLS-1$

			return episode;
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_STORE_XML1"),e); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_CREATE_URL"),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public Film getFilm(MediaDirectory dir, File file) throws StoreException {
		try {
			File rootMediaDir = dir.getMediaDirConfig().getMediaDir();
			Element filmNode = getNodeWithFile(dir,file);
			if (filmNode==null || !filmNode.getNodeName().equals("film")) { //$NON-NLS-1$
				return null;
			}
			Film film = getFilm(rootMediaDir,file,filmNode.getAttribute("id")); //$NON-NLS-1$
			return film;
		} catch (XMLParserException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_PARSE_STORE_XML1"),e); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			throw new StoreException(Messages.getString("XMLStore2.UNABLE_CREATE_URL"),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void init(File nativeDir) throws StoreException {
	}

}

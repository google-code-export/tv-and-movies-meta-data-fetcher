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
import java.util.ArrayList;
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
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.store.StoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This store is used to store film information in a XML called .film.xml. This is located in the directory were the
 * film is located. It can hold all of the information of multiple films. This store can be read and written too, and
 * it's also possible too lookup the film id's.
 */
public class FilmXMLStore extends BaseXMLStore {

	
	private final static Log log = LogFactory.getLog(FilmXMLStore.class);
	
	private final static String FILENAME = ".films.xml";
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * This is used to write a film to the store.
	 * 
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public void cacheFilm(File rootMediaDir,File filmFile, Film film) throws StoreException {
		try {
			Document doc = getCache(rootMediaDir,true);
			Element filmsNode = (Element) doc.getFirstChild();
			Node filmNode = XPathAPI.selectSingleNode(filmsNode, "film[@id=" + film.getId() + "]");
			Set<String> filenames = null;
			if (filmNode != null) {
				filenames = getOldFilenames(filmNode);
				removeOldCache(filmNode, film);
			} else {
				filenames = new HashSet<String>();
			}
			filenames.add(filmFile.getAbsolutePath());

			appendFilm(doc, filmsNode, film, filenames);

			File cacheFile = getCacheFile(filmFile.getParentFile(), FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
		}
	}

	private void appendFilm(Document doc, Element filmsNode, Film film, Set<String> filenames)
			throws TransformerException {
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

		Element summaryNode = doc.createElement("summary");
		summaryNode.appendChild(doc.createTextNode(film.getSummary()));
		filmNode.appendChild(summaryNode);
		Element descriptionNode = doc.createElement("description");
		if (film.getDescription()!=null) {
			descriptionNode.appendChild(doc.createTextNode(film.getDescription()));
		}
		filmNode.appendChild(descriptionNode);

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
		if (film.getCountry()!=null) {
			List<Link> countries = new ArrayList<Link>();
			countries.add(film.getCountry());
			writeEpsoideExtraInfo(doc, filmNode, "country",countries );
			writeFilenames(doc, filmNode, filenames);
		}

		filmsNode.appendChild(filmNode);
	}

	private void removeOldCache(Node filmNode, Film film) throws TransformerException {
		if (filmNode != null) {
			filmNode.getParentNode().removeChild(filmNode);
		}
	}

	/**
	 * Used to get the details of a film with the given id. If it can't be found, then null is returned.
	 * 
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 * @return The film details, or null if it can't be found
	 * @throws StoreException Thrown if their is a problem with the store
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	public Film getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		try {
			Document doc = getCache(rootMediaDir,true);
			Element filmsNode = (Element) doc.getFirstChild();
			Node filmNode = XPathAPI.selectSingleNode(filmsNode, "film[@id='" + filmId + "']");
			if (filmNode != null) {
				Film film = new Film(filmId);

				String filmURL = getStringFromXML(filmNode, "@url");
				String title = getStringFromXML(filmNode, "@title");
				String sourceId = getStringFromXML(filmNode, "@sourceId");
				String summary = getStringFromXML(filmNode, "summary/text()");
				
				String description = null;
				
				try {
					description = getStringFromXML(filmNode, "description/text()");
				} catch (NotInStoreException e) {
					// No description found, leave as null
				}				
				float rating = getFloatFromXML(filmNode, "@rating");
				Date releaseDate = null;
				try {
					releaseDate = df.parse(getStringFromXML(filmNode, "@releaseDate"));
				} catch (NotInStoreException e) {
					// No date found, leave as null
				}

				List<String> genres = readGenresFromXML(filmNode);
				List<Certification> certifications = readCertificationsFromXML(filmNode);
				List<Chapter> chapters = readChaptersFromXML(filmNode);
				List<Link> directors = getLinks(filmNode, "director");
				List<Link> writers = getLinks(filmNode, "writer");
				List<Link> guestStars = getLinks(filmNode, "guestStar");								
				List<Link> countries = getLinks(filmNode, "country");
				String imageUrl = getStringFromXML(filmNode, "@imageUrl");
				
				Element preferredGenreNode = (Element) XPathAPI.selectSingleNode(filmNode, "genre[@preferred='true']");
				if (preferredGenreNode!=null) {
					film.setPreferredGenre(preferredGenreNode.getAttribute("name"));
				}

				film.setCertifications(certifications);
				film.setChapters(chapters);
				film.setDate(releaseDate);
				film.setDirectors(directors);
				film.setFilmUrl(new URL(filmURL));
				if (imageUrl != null && imageUrl.length() > 0) {
					film.setImageURL(new URL(imageUrl));
				}
				film.setGenres(genres);
				film.setGuestStars(guestStars);
				film.setRating(rating);
				film.setSourceId(sourceId);
				film.setSummary(summary);
				film.setDescription(description);
				film.setTitle(title);
				film.setWriters(writers);
				if (countries!=null && countries.size()>0) {
					film.setCountry(countries.get(0));
				}
				return film;
			}
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
		} catch (ParseException e) {
			throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
		} catch (NotInStoreException e) {
			return null;
		}
		return null;
	}

	private List<Chapter> readChaptersFromXML(Node parent) throws TransformerException, NotInStoreException, StoreException {
		List<Chapter> chapters = new ArrayList<Chapter>();
		NodeList nodeList = XPathAPI.selectNodeList(parent, "chapter");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element node = (Element) nodeList.item(i);
				try {
					Chapter chapter = new Chapter(node.getAttribute("name"), Integer.parseInt(node
							.getAttribute("number")));
					chapters.add(chapter);
				} catch (NumberFormatException e) {
					throw new StoreException("Unable to parse chapter number from text '" + node.getAttribute("number")
							+ "'");
				}

			}
		}
		return chapters;
	}

	/**
	 * Used to read the certification from the XML document
	 * 
	 * @param parent The parent node to read them from
	 * @return A list of certification that were found
	 * @throws TransformerException Thrown if their is a problem parsing the XML
	 * @throws NotInStoreException Thrown if the genres are not in the store
	 */
	private List<Certification> readCertificationsFromXML(Node parent) throws TransformerException, NotInStoreException {
		List<Certification> certs = new ArrayList<Certification>();
		NodeList nodeList = XPathAPI.selectNodeList(parent, "certification");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element node = (Element) nodeList.item(i);
				Certification cert = new Certification(node.getAttribute("certification"), node.getAttribute("country"));
				certs.add(cert);
			}
		}
		return certs;
	}

	private Document getCache(File showDirectory,boolean createIfNotFound) throws StoreException {
		File cacheFile = getCacheFile(showDirectory, FILENAME);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Document doc = null;
		if (cacheFile.exists()) {
			// Create the builder and parse the file
			try {
				doc = factory.newDocumentBuilder().parse(cacheFile);
			} catch (SAXException e) {
				throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new StoreException("Unable to read cache file: " + e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
			}
			return doc;
		} else {
			if (createIfNotFound) {
				DocumentBuilder builder;
				try {
					builder = factory.newDocumentBuilder();
					doc = builder.newDocument();
					Element films = doc.createElement("films");
					doc.appendChild(films);
					writeCache(cacheFile, doc);
				} catch (ParserConfigurationException e) {
					throw new StoreException("Unable to create cache: " + e.getMessage());
				}
			}
		}

		return doc;
	}

	/**
	 * This will update all references of the old file to the new file
	 * 
	 * @param oldFile The old file
	 * @param newFile The new file
	 * @throws StoreException Thrown if their is a problem renaming files
	 */
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) throws StoreException {
		if (!isUnderParent(rootMediaDir,newFile)) {			
			log.error("Unable to update store with new file location due different not been under media directory");
		} else {
			Document doc = getCache(rootMediaDir,false);
			if (doc!=null) {
				try {
					NodeList nodes = XPathAPI.selectNodeList(doc, "/films/film/file[@name=\"" + oldFile.getAbsolutePath()
							+ "\"]/@name");
					for (int i = 0; i < nodes.getLength(); i++) {
						nodes.item(i).setNodeValue(newFile.getAbsolutePath());
					}
	
					File cacheFile = getCacheFile(oldFile.getParentFile(), FILENAME);
					writeCache(cacheFile, doc);
				} catch (TransformerException e) {
					throw new StoreException(e.getMessage(), e);
				}
			}
		}
	}
	
	public boolean isUnderParent(File parent,File dir) {		
		while (dir!=null) {
			if (dir.equals(parent)) {
				return true;
			}
			dir = dir.getParentFile();
		}
		return false;
	}

	private String getQuery(File episodeFile) {
		String file = episodeFile.getName().toLowerCase().trim();
		int pos = file.lastIndexOf(".");
		if (pos == -1) {
			return null;
		}
		file = file.substring(0, pos);
		file = file.replaceAll("\\.|_", " ");
		file = file.replaceAll("(\\[|\\().*(\\]|\\))|:|-", "");
		file = file.replaceAll("dvdrip|dvd|xvid|divx|xv|xvi|full", "");
		file = file.trim();
		return file;
	}

	/**
	 * This is called to search the store for a film Id. If it can't be found, then it will return null. The search is
	 * done be reading the .films.xml file next to the film.
	 * 
	 * @param filmFile The file the film is stored in
	 * @return The results of the search if it was found, otherwise null
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public SearchResult searchForFilmId(File rootMediaDir,File filmFile) throws StoreException {
		Document doc = getCache(rootMediaDir,false);
		String query = getQuery(filmFile);
		try {
			NodeList nodes = XPathAPI.selectNodeList(doc, "/films/film");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element el = (Element) nodes.item(i);
				String title = getTitle(el);
				if (title!=null) {
					while (title.endsWith(".") && title.length() > 0) {
						title = title.substring(0, title.length() - 1);
					}
					if (title.equals(query)) {
						SearchResult result = new SearchResult(el.getAttribute("id"), el.getAttribute("sourceId"));
						log.info("Found film '" + query + "' in XMLStore with id '" + result.getId() + "'");
						return result;
					}
				}
			}
		} catch (TransformerException e) {
			throw new StoreException(e.getMessage(), e);
		}
		return null;
	}

	private String getTitle(Element el) {
		String title = el.getAttribute("title");
		if (title != null) {
			title = title.toLowerCase();
			title = title.replaceAll(":|-", "");
		}
		return title;
	}

}

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
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Film;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This store is used to store film information in a XML called .film.xml. 
 * This is located in the directory were the film is located. It can hold all
 * of the information of multiple films. This store
 * can be read and written too, and it's also possible too lookup the film id's.
 */
public class FilmXMLStore extends BaseXMLStore {

	private final static String FILENAME = ".films.xml";

	/**
	 * This is used to write a film to the store.
	 * 
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	public void cacheFilm(File filmFile, Film film) throws StoreException {		
		try {
			Document doc = getCache(filmFile.getParentFile());
			Element filmsNode = (Element) doc.getFirstChild();
			Node filmNode = XPathAPI.selectSingleNode(filmsNode, "film[@id=" + film.getId() + "]");
			Set<String> filenames = null;
			if (filmNode!=null) {
				filenames = getOldFilenames(filmNode); 
				removeOldCache(filmNode, film);
			}
			else {
				filenames = new HashSet<String>();				
			}
			filenames.add(filmFile.getAbsolutePath());
			
			appendFilm(doc,filmsNode,film,filenames);
			
			File cacheFile = getCacheFile(filmFile.getParentFile(),FILENAME);
			writeCache(cacheFile, doc);
		} catch (TransformerException e) {
			throw new StoreException("Unable to parse cache file: " + e.getMessage(), e);
		}
	}

	private void appendFilm(Document doc, Element filmsNode, Film film, Set<String>filenames) throws TransformerException {		
		Element filmNode = doc.createElement("film");
		filmNode.setAttribute("id", String.valueOf(film.getId()));		
		filmNode.setAttribute("title", film.getTitle());
		filmNode.setAttribute("sourceId", film.getSourceId());		
		filmNode.setAttribute("rating", String.valueOf(film.getRating()));
		filmNode.setAttribute("url", urlToText(film.getFilmUrl()));
		filmNode.setAttribute("releaseDate", df.format(film.getDate()));			

		for (String value : film.getGenres()) {
			Element genre = doc.createElement("genre");
			genre.setAttribute("name", value);
			filmNode.appendChild(genre);
		}
		
		for (Certification value : film.getCertifications()) {
			Element genre = doc.createElement("certification");			
			genre.setAttribute("country", value.getContry());
			genre.setAttribute("certification", value.getCertification());
			filmNode.appendChild(genre);
		}
		
		writeEpsoideExtraInfo(doc, filmNode, "director", film.getDirectors());
		writeEpsoideExtraInfo(doc, filmNode, "writers", film.getWriters());
		writeEpsoideExtraInfo(doc, filmNode, "guestStars", film.getGuestStars());
		writeFilenames(doc,filmNode,filenames);
		
		filmsNode.appendChild(filmNode);
	}	

	private void removeOldCache(Node filmNode, Film film) throws TransformerException {		
		if (filmNode != null) {
			filmNode.getParentNode().removeChild(filmNode);
		}
	}

	/**
	 * Used to get the details of a film with the given id. If it can't be found,
	 * then null is returned.
	 * 
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 * @return The film details, or null if it can't be found
	 */
	public Film getFilm(File filmFile, long filmId) throws SourceException, MalformedURLException, IOException {
		return null;
	}

	private Document getCache(File showDirectory) throws StoreException {
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

		return doc;
	}

}

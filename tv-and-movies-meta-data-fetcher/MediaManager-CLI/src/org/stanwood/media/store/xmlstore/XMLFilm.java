/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.model.VideoFileSet;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.xml.Messages;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a Film object that talks directory to the DOM of the XML store
 */
public class XMLFilm extends XMLParser implements IFilm {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private Element filmNode;
	private Document doc;
	private File rootMediaDir;

	public XMLFilm(Element node,File rootMediaDir) {
		this.filmNode = node;
		this.doc = filmNode.getOwnerDocument();
		this.rootMediaDir = rootMediaDir;
	}
	/** {@inheritDoc} */
	@Override
	public String getTitle() {
		try {
			return getAttribute(filmNode, "title"); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {

			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setTitle(String title) {
		filmNode.setAttribute("title", title); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getDirectors() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setDirectors(List<String> directors) {
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getWriters() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setWriters(List<String> writers) {
	}

	/** {@inheritDoc} */
	@Override
	public String getSummary() {
		try {

			return getStringFromXML(filmNode, "description/short/text()"); //$NON-NLS-1$
		} catch (XMLParserException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setSummary(String summary) {
		Element el = getElement(filmNode, "description"); //$NON-NLS-1$
		el = getElement(filmNode, "short"); //$NON-NLS-1$
		el.setTextContent(summary);
	}

	/** {@inheritDoc} */
	@Override
	public SortedSet<VideoFile> getFiles() {
		try {
			SortedSet<VideoFile> files = new VideoFileSet();

			for (Node node : selectNodeList(filmNode, "file")) { //$NON-NLS-1$
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
			return files;
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	/** {@inheritDoc} */
	@Override
	public void setFiles(SortedSet<VideoFile> videoFiles) {
		try {
			for (VideoFile filename : videoFiles) {
				appendFile(doc, filmNode, filename,rootMediaDir);
			}
			for (Node n : selectNodeList(filmNode, "file")) { //$NON-NLS-1$
				if (new File(rootMediaDir,getAttribute((Element)n, "location")).exists()) { //$NON-NLS-1$
					n.getParentNode().removeChild(n);
				}
			}
		}
		catch (StoreException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (DOMException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
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

	/** {@inheritDoc} */
	@Override
	public List<Actor> getActors() {
		try {
			List<Actor> actors = new ArrayList<Actor>();
			for (Node n : selectNodeList(filmNode, "actors/actor")) { //$NON-NLS-1$
				Element e  = (Element) n;
				actors.add(new Actor(e.getAttribute("name"),e.getAttribute("role"))); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return actors;
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	/** {@inheritDoc} */
	@Override
	public void setActors(List<Actor> actors) {
		try {
			Element actorsEl = getElement(filmNode,"actors"); //$NON-NLS-1$
			for (Node n : selectNodeList(actorsEl, "actor")) { //$NON-NLS-1$
				n.getParentNode().removeChild(n);
			}
			if (actors!=null) {
				for (Actor actor : actors) {
					Element actorNode = doc.createElement("actor"); //$NON-NLS-1$
					actorNode.setAttribute("name", actor.getName()); //$NON-NLS-1$
					actorNode.setAttribute("role", actor.getRole()); //$NON-NLS-1$
					actorsEl.appendChild(actorNode);
				}
			}
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setGenres(List<String> genres) {
		Element genresNode = getElement(filmNode,"genres"); //$NON-NLS-1$
		deleteNode(genresNode, "genre"); //$NON-NLS-1$
		if (genres!=null) {
			for (String value :genres) {
				Element genre = doc.createElement("genre"); //$NON-NLS-1$
				genre.setAttribute("name", value); //$NON-NLS-1$
				genresNode.appendChild(genre);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getGenres() {
		try {
			List<String>genres = new ArrayList<String>();
			for (Node node : selectNodeList(filmNode, "genres/genre")) { //$NON-NLS-1$
				Element genreEl = (Element)node;
				String genre = genreEl.getAttribute("name"); //$NON-NLS-1$
				genres.add(genre);
			}

			return genres;
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addGenre(String genre) {
		Element genresNode = getElement(filmNode,"genres"); //$NON-NLS-1$
		Element genreNode = doc.createElement("genre"); //$NON-NLS-1$
		genreNode.setAttribute("name", genre); //$NON-NLS-1$
		genresNode.appendChild(genreNode);
	}

	/** {@inheritDoc} */
	@Override
	public String getPreferredGenre() {
		try {
			for (Node node : selectNodeList(filmNode, "genres/genre")) { //$NON-NLS-1$
				Element genreEl = (Element)node;
				String genre = genreEl.getAttribute("name"); //$NON-NLS-1$
				String preferred = genreEl.getAttribute("preferred"); //$NON-NLS-1$
				if (preferred.equals("true")) { //$NON-NLS-1$
					return genre;
				}
			}
			return null;
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setPreferredGenre(String preferredGenre) {
		try {
			for (Node node : selectNodeList(filmNode, "genres/genre")) { //$NON-NLS-1$
				Element genreEl = (Element)node;
				String genre = genreEl.getAttribute("name"); //$NON-NLS-1$
				if (genre.equals(preferredGenre)) {
					genreEl.setAttribute("preferred", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					genreEl.removeAttribute("preferred"); //$NON-NLS-1$
				}
			}
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Rating getRating() {
		try {
			int numberOfVotes = getIntegerFromXML(filmNode, "rating/@numberOfVotes"); //$NON-NLS-1$
			float rating = getFloatFromXML(filmNode, "rating/@value"); //$NON-NLS-1$

			return new Rating(rating,numberOfVotes);
		}
		catch (XMLParserNotFoundException e) {
			// Ignore, not found
		} catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setRating(Rating rating) {
		deleteNode(filmNode, "rating"); //$NON-NLS-1$
		if (rating!=null) {
			Element ratingElement = getElement(filmNode,"rating"); //$NON-NLS-1$
			ratingElement.setAttribute("value", String.valueOf(rating.getRating())); //$NON-NLS-1$
			ratingElement.setAttribute("numberOfVotes", String.valueOf(rating.getNumberOfVotes())); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getId() {
		try {

			return getAttribute(filmNode, "id"); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {

			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setId(String id) {
		filmNode.setAttribute("id", id); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public String getSourceId() {
		try {

			return getAttribute(filmNode, "sourceId"); //$NON-NLS-1$;
		} catch (XMLParserNotFoundException e) {

			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setSourceId(String sourceId) {

		filmNode.setAttribute("sourceId", sourceId); //$NON-NLS-1$

	}

	/** {@inheritDoc} */
	@Override
	public void setFilmUrl(URL url) {
		filmNode.setAttribute("url", url.toExternalForm()); //$NON-NLS-1$

	}

	/** {@inheritDoc} */
	@Override
	public URL getFilmUrl() {
		try {

			return new URL(getAttribute(filmNode, "url")); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Certification> getCertifications() {
		try {
			List<Certification>certifications = new ArrayList<Certification>();
			for (Node node : selectNodeList(filmNode, "certifications/certification")) { //$NON-NLS-1$
				Element certificationEl = (Element)node;
				certifications.add(new Certification(certificationEl.getAttribute("certification"), certificationEl.getAttribute("type"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return certifications;
		} catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setCertifications(List<Certification> certifications) {
		Element certificationsNode = getElement(filmNode,"certifications"); //$NON-NLS-1$
		deleteNode(certificationsNode, "certification"); //$NON-NLS-1$
		if (certifications!=null) {
			for (Certification cert : certifications) {
				Element certificationNode = doc.createElement("certification"); //$NON-NLS-1$
				certificationNode.setAttribute("type", cert.getType()); //$NON-NLS-1$
				certificationNode.setAttribute("certification", cert.getCertification()); //$NON-NLS-1$
				certificationsNode.appendChild(certificationNode);
			}
		}
		certificationsNode.appendChild(certificationsNode);
	}

	/** {@inheritDoc} */
	@Override
	public Date getDate() {
		 try {
			return df.parse(getStringFromXML(filmNode, "@releaseDate")); //$NON-NLS-1$
		} catch (XMLParserException e) {
			throw new RuntimeException();
		} catch (ParseException e) {
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setDate(Date date) {
		if (date!=null) {
			filmNode.setAttribute("releaseDate",df.format(date)); //$NON-NLS-1$
		}
		else {
			filmNode.removeAttribute("releaseDate"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setImageURL(URL imageURL) {
		if (imageURL != null) {
			filmNode.setAttribute("imageUrl", imageURL.toExternalForm()); //$NON-NLS-1$
		} else {
			filmNode.removeAttribute("imageUrl"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public URL getImageURL() {
		URL url;
		try {
			url = new URL(getAttribute(filmNode, "imageUrl")); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			url = null;
		} catch (XMLParserNotFoundException e) {

			e.printStackTrace();
			throw new RuntimeException();
		}
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public void addChapter(Chapter chapter) {
		Element chaptersNode = getElement(filmNode,"chapters"); //$NON-NLS-1$
		Element chapNode = doc.createElement("chapter"); //$NON-NLS-1$
		chapNode.setAttribute("name", chapter.getName()); //$NON-NLS-1$
		chapNode.setAttribute("number", String.valueOf(chapter.getNumber())); //$NON-NLS-1$
		chaptersNode.appendChild(chapNode);
	}

	/** {@inheritDoc} */
	@Override
	public List<Chapter> getChapters() {
		try {
			List<Chapter>chapters = new ArrayList<Chapter>();
			for (Node n : selectNodeList(filmNode, "chapters/chapter")) { //$NON-NLS-1$
				Element chapNode = (Element)n;

				Chapter chapter = new Chapter(chapNode.getAttribute("name"),Integer.parseInt(chapNode.getAttribute("number"))); //$NON-NLS-1$ //$NON-NLS-2$
				chapters.add(chapter);
			}
			return chapters;
		} catch (XMLParserException e) {
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setChapters(List<Chapter> chapters) {
		Element chaptersNode = getElement(filmNode,"chapters"); //$NON-NLS-1$
		deleteNode(chaptersNode, "chapters"); //$NON-NLS-1$
		if (chapters!=null) {
			for (Chapter c : chapters) {
				addChapter(c);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setDescription(String description) {
		Element el = getElement(filmNode, "description"); //$NON-NLS-1$
		el = getElement(filmNode, "long"); //$NON-NLS-1$
		if (description != null) {
			el.setTextContent(description);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		try {

			return getStringFromXML(filmNode, "description/long/text()"); //$NON-NLS-1$
		} catch (XMLParserException e) {

			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getCountry() {
		try {

			return getStringFromXMLOrNull(filmNode, "country/text()"); //$NON-NLS-1$
		} catch (XMLParserException e) {

			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setCountry(String country) {
		if (country != null) {
			Element el = getElement(filmNode, "country"); //$NON-NLS-1$
			el.setTextContent(country);
		} else {
			deleteNode(filmNode, "country"); //$NON-NLS-1$
		}
	}

	private String getAttribute(Element node, String attributeName) throws XMLParserNotFoundException {
		if (node.hasAttribute(attributeName)) {
			return node.getAttribute(attributeName);
		} else {
			throw new XMLParserNotFoundException(MessageFormat.format(
					Messages.getString("XMLParser.UNABLE_FIND_PATH"), attributeName)); //$NON-NLS-1$
		}
	}

	private void deleteNode(Element parent, String name) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equals(name)) {
				children.item(i).getParentNode().removeChild(children.item(i));
				return;
			}
		}
	}

	private Element getElement(Element parent, String name) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equals(name)) {
				return (Element) children.item(i);
			}
		}

		Element child = doc.createElement(name);
		parent.appendChild(child);
		return child;
	}


}

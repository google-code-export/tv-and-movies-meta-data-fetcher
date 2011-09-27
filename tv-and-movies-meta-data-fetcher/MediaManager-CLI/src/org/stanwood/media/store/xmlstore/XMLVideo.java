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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.stanwood.media.model.Actor;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.IVideoActors;
import org.stanwood.media.model.IVideoFile;
import org.stanwood.media.model.IVideoRating;
import org.stanwood.media.model.Rating;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XML Video class that read/sets the information into a dom object
 */
public class XMLVideo extends XMLParser implements IVideo,IVideoActors,IVideoRating {

	private Element videoNode;
	private Document doc;
	private File rootMediaDir;

	/**
	 * The constructor
	 * @param node The node with video data
	 * @param rootMediaDir The root media dir
	 */
	public XMLVideo(Element node,File rootMediaDir) {
		this.videoNode = node;
		this.doc = videoNode.getOwnerDocument();
		this.rootMediaDir = rootMediaDir;
	}

	/** {@inheritDoc} */
	@Override
	public List<Actor> getActors() {
		try {
			List<Actor> actors = new ArrayList<Actor>();
			for (Node n : selectNodeList(videoNode, "actors/actor")) { //$NON-NLS-1$
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
			Element actorsEl = getElement(videoNode,"actors"); //$NON-NLS-1$
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
	public List<String> getWriters() {
		try {
			List<String> writers = new ArrayList<String>();
			for (Node node : selectNodeList(videoNode, "writers/writer/text()")) { //$NON-NLS-1$
				String writer = node.getTextContent();
				writers.add(writer);
			}
			return writers;
		} catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setWriters(List<String> writers) {
		Element writrersEl = getElement(videoNode,"writers"); //$NON-NLS-1$
		deleteNode(writrersEl, "writer"); //$NON-NLS-1$
		for (String writer : writers) {
			Element writerNode = doc.createElement("writer"); //$NON-NLS-1$
			writerNode.appendChild(doc.createTextNode(writer));
			writrersEl.appendChild(writerNode);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getTitle() {
		try {
			return getAttribute(videoNode, "title"); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setTitle(String title) {
		videoNode.setAttribute("title", title); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getDirectors() {
		try {
			List<String> directors = new ArrayList<String>();
			for (Node node : selectNodeList(videoNode, "directors/director/text()")) { //$NON-NLS-1$
				String director = node.getTextContent();
				directors.add(director);
			}
			return directors;
		} catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setDirectors(List<String> directors) {
		Element directorsEl = getElement(videoNode,"directors"); //$NON-NLS-1$
		deleteNode(directorsEl, "director"); //$NON-NLS-1$
		for (String director : directors) {
			Element directorNode = doc.createElement("director"); //$NON-NLS-1$
			directorNode.appendChild(doc.createTextNode(director));
			directorsEl.appendChild(directorNode);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getSummary() {
		try {
			return getStringFromXML(videoNode, "description/short/text()"); //$NON-NLS-1$
		}
		catch (XMLParserNotFoundException e) {
			return null;
		} catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setSummary(String summary) {
		Element el = getElement(videoNode, "description"); //$NON-NLS-1$
		if (summary!=null) {
			el = getElement(el, "short"); //$NON-NLS-1$
			el.setTextContent(summary);
		}
		else {
			deleteNode(el, "short"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IVideoFile> getFiles() {
		List<IVideoFile> files = new ArrayList<IVideoFile>();

		for (Element node : selectChildNodes(videoNode, "file")) { //$NON-NLS-1$
			files.add(new XMLVideoFile(rootMediaDir,node));
		}
		return files;
	}

	/** {@inheritDoc} */
	@Override
	public void setFiles(Collection<IVideoFile> videoFiles) {
		try {
			for (IVideoFile filename : videoFiles) {
				appendFile(doc, videoNode, filename,rootMediaDir);
			}
			for (Element n : selectChildNodes(videoNode, "file")) { //$NON-NLS-1$
				if (new File(rootMediaDir,getAttribute(n, "location")).exists()) { //$NON-NLS-1$
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

	/** {@inheritDoc} */
	@Override
	public Rating getRating() {
		try {
			int numberOfVotes = getIntegerFromXML(videoNode, "rating/@numberOfVotes"); //$NON-NLS-1$
			float rating = getFloatFromXML(videoNode, "rating/@value"); //$NON-NLS-1$

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
		deleteNode(videoNode, "rating"); //$NON-NLS-1$
		if (rating!=null) {
			Element ratingElement = getElement(videoNode,"rating"); //$NON-NLS-1$
			ratingElement.setAttribute("value", String.valueOf(rating.getRating())); //$NON-NLS-1$
			ratingElement.setAttribute("numberOfVotes", String.valueOf(rating.getNumberOfVotes())); //$NON-NLS-1$
		}
	}

	private IVideoFile findFile(IVideoFile file) {
		for (IVideoFile f : getFiles()) {
			if (f.getLocation().equals(file.getLocation())) {
				return f;
			}
		}
		return null;
	}

	private void appendFile(Document doc, Node parent, IVideoFile file,File rootMediaDir) throws StoreException {
		if (file!=null) {
			IVideoFile vidFile = findFile(file);
			if (vidFile==null) {
				Element fileNode = doc.createElement("file"); //$NON-NLS-1$
				parent.appendChild(fileNode);
				vidFile = new XMLVideoFile(rootMediaDir, fileNode);
			}
			vidFile.setLocation(file.getLocation());
			vidFile.setPart(file.getPart());
		}
	}

}
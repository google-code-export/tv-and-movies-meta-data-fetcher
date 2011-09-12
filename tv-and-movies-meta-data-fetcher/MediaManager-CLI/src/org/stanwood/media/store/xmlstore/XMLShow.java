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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.stanwood.media.model.IShow;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLShow extends XMLParser implements IShow {

	private Element showNode;
	private Document doc;

	public XMLShow(Element showNode) {
		this.showNode = showNode;
		this.doc = showNode.getOwnerDocument();
	}

	/** {@inheritDoc} */
	@Override
	public void setGenres(List<String> genres) {
		Element genresNode = getElement(showNode,"genres"); //$NON-NLS-1$
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
			for (Node node : selectNodeList(showNode, "genres/genre")) { //$NON-NLS-1$
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
		Element genresNode = getElement(showNode,"genres"); //$NON-NLS-1$
		Element genreNode = doc.createElement("genre"); //$NON-NLS-1$
		genreNode.setAttribute("name", genre); //$NON-NLS-1$
		genresNode.appendChild(genreNode);
	}

	/** {@inheritDoc} */
	@Override
	public String getPreferredGenre() {
		try {
			for (Node node : selectNodeList(showNode, "genres/genre")) { //$NON-NLS-1$
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
	public Map<String, String> getExtraInfo() {
		try {
			Node extraNode = selectSingleNode(showNode,"extra"); //$NON-NLS-1$
			if (extraNode!=null) {
				Map<String,String>result = new HashMap<String,String>();
				for (Node paramNode : selectNodeList(extraNode, "param")) { //$NON-NLS-1$
					result.put(((Element)paramNode).getAttribute("key"),((Element)paramNode).getAttribute("value"));  //$NON-NLS-1$//$NON-NLS-2$
				}
				return result;
			}
			return null;
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setExtraInfo(Map<String, String> params) {
		if (params!=null) {
			Element extraNode = getElement(showNode,"extra"); //$NON-NLS-1$

			for (Entry<String, String> e : params.entrySet()) {
				Element param = doc.createElement("param"); //$NON-NLS-1$
				param.setAttribute("key",e.getKey()); //$NON-NLS-1$
				param.setAttribute("value", e.getValue()); //$NON-NLS-1$
				extraNode.appendChild(param);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getLongSummary() {
		try {
			return getStringFromXML(showNode,"description/long/text()"); //$NON-NLS-1$
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setLongSummary(String longSummary) {
		Element descriptionEl = getElement(showNode,"description"); //$NON-NLS-1$
		if (longSummary!=null) {
			Element longEl = getElement(descriptionEl,"long"); //$NON-NLS-1$
			longEl.appendChild(doc.createCDATASection(longSummary));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setShortSummary(String shortSummary) {
		Element descriptionEl = getElement(showNode,"description"); //$NON-NLS-1$
		if (shortSummary!=null) {
			Element shortEl = getElement(descriptionEl,"short"); //$NON-NLS-1$
			shortEl.appendChild(doc.createCDATASection(shortSummary));
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getShortSummary() {
		try {
			return getStringFromXML(showNode,"description/short/text()"); //$NON-NLS-1$
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setName(String name) {
		if (name!=null) {
			showNode.setAttribute("name", name); //$NON-NLS-1$
		}
		else {
			showNode.removeAttribute("name"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setShowURL(URL showURL) {
		if (showURL!=null) {
			showNode.setAttribute("url", showURL.toExternalForm()); //$NON-NLS-1$
		}
		else {
			showNode.removeAttribute("url"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getShowId() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public URL getImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setImageURL(URL imageURL) {
		if (imageURL!=null) {
			showNode.setAttribute("imageUrl", imageURL.toExternalForm()); //$NON-NLS-1$
		}
		else {
			showNode.removeAttribute("imageUrl"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public URL getShowURL() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setSourceId(String sourceId) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void setPreferredGenre(String preferredGenre) {
		try {
			for (Node node : selectNodeList(showNode, "genres/genre")) { //$NON-NLS-1$
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

}

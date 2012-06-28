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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Episode information for the store {@link XMLStore2}, that access the information from the XML File
 */
public class XMLEpisode extends XMLVideo implements IEpisode {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private Element episodeNode;
	private ISeason season;

	/**
	 * The constructor
	 * @param season The season
	 * @param node The episode XML node
	 * @param rootMediaDir The media directory location
	 */
	public XMLEpisode(ISeason season,Element node, File rootMediaDir) {
		super(node, rootMediaDir);
		this.episodeNode = node;
		this.season = season;
	}

	/** {@inheritDoc} */
	@Override
	public ISeason getSeason() {
		return season;
	}

	/** {@inheritDoc} */
	@Override
	public int getEpisodeNumber() {
		try {
			return Integer.parseInt(getAttribute(episodeNode,"number")); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setEpisodeNumber(int episodeNumner) {
		episodeNode.setAttribute("number",String.valueOf(episodeNumner)); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void setDate(Date airDate) {
		if (airDate!=null) {
			episodeNode.setAttribute("firstAired", df.format(airDate)); //$NON-NLS-1$
		}
		else {
			episodeNode.removeAttribute("firstAired"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public Date getDate() {
		try {
			return df.parse(getAttribute(episodeNode, "firstAired")); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			return null;
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSpecial() {
		return (episodeNode.getNodeName().equals("special")); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void setSpecial(boolean special) {
		if (special) {
			episodeNode.getOwnerDocument().renameNode(episodeNode, null, "special"); //$NON-NLS-1$
		}
		else {
			episodeNode.getOwnerDocument().renameNode(episodeNode, null, "episode"); //$NON-NLS-1$
		}
	}


	/** {@inheritDoc} */
	@Override
	public void setUrl(URL url) {
		episodeNode.setAttribute("url", url.toExternalForm()); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public URL getUrl() {
		try {
			return new URL(getAttribute(episodeNode, "url")); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getEpisodeId() {
		try {
			return getAttribute(episodeNode,"episodeId"); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setEpisodeId(String episodeId) {
		episodeNode.setAttribute("episodeId",episodeId); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void setImageURL(URL imageURL) {
		if (imageURL != null) {
			episodeNode.setAttribute("imageUrl", imageURL.toExternalForm()); //$NON-NLS-1$
		} else {
			episodeNode.removeAttribute("imageUrl"); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public URL getImageURL() {
		URL url;
		try {
			url = new URL(getAttribute(episodeNode, "imageUrl")); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			url = null;
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public List<Integer> getEpisodes() {
		List<Integer> episodes = new ArrayList<Integer>();

		try {
			for (Node node : selectNodeList(episodeNode, "episodeNum")) { //$NON-NLS-1$
				String strNum = ((Element) node).getAttribute("number"); //$NON-NLS-1$
				Integer value = Integer.valueOf(strNum);
				if (!episodes.contains(value)) {
					episodes.add(value);
				}
			}
		} catch (XMLParserNotFoundException e) {
			return null;
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}

		return episodes;
	}

	/** {@inheritDoc} */
	@Override
	public void setEpisodes(List<Integer> epsiodes) {
		try {
			for (Node node : selectNodeList(episodeNode, "episodeNum")) { //$NON-NLS-1$
				node.getParentNode().removeChild(node);
			}

			if (epsiodes!=null) {
				Document doc = episodeNode.getOwnerDocument();
				for (Integer value : epsiodes) {
					Element episodeNumNode = doc.createElement("episodeNum"); //$NON-NLS-1$
					episodeNumNode.setAttribute("number", String.valueOf(value)); //$NON-NLS-1$
					episodeNode.insertBefore(episodeNumNode, episodeNode.getFirstChild());
				}
			}
		}
		catch (XMLParserException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

}

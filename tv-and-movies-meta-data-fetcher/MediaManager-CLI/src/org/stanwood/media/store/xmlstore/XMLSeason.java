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

import java.net.MalformedURLException;
import java.net.URL;

import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Element;

/**
 * Episode information for the store {@link XMLStore2}, that access the information from the XML File
 */
public class XMLSeason extends XMLParser implements ISeason {

	private Element seasonNode;
	private IShow show;

	/**
	 * The constructor
	 * @param show The show
	 * @param seasonNode The season xml node
	 */
	public XMLSeason(IShow show, Element seasonNode) {
		this.show = show;
		this.seasonNode = seasonNode;
	}

	/** {@inheritDoc} */
	@Override
	public URL getURL() {
		try {
			return new URL(getAttribute(seasonNode,"url")); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setURL(URL url) {
		seasonNode.setAttribute("url", url.toExternalForm()); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public int getSeasonNumber() {
		try {
			return Integer.parseInt(getAttribute(seasonNode,"number")); //$NON-NLS-1$
		} catch (XMLParserNotFoundException e) {
			throw new RuntimeException(e.getMessage(),e);
		} catch (NumberFormatException e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public IShow getShow() {
		return show;
	}

	/** {@inheritDoc} */
	@Override
	public void setSeasonNumber(int seasonNumber) {
		seasonNode.setAttribute("number", String.valueOf(seasonNumber)); //$NON-NLS-1$
	}

}

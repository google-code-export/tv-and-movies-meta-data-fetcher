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
package org.stanwood.tv.util;

import javax.xml.transform.TransformerException;

import org.stanwood.tv.source.NotInStoreException;
import org.w3c.dom.Node;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class XMLParser {

	protected Integer getIntegerFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return Integer.parseInt(node.getNodeValue());
		}
		throw new NotInStoreException();
	}

	protected Long getLongFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return Long.parseLong(node.getNodeValue());
		}
		throw new NotInStoreException();
	}

	protected String getStringFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return node.getNodeValue();
		}
		throw new NotInStoreException();
	}

	protected float getFloatFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return Float.parseFloat(node.getNodeValue());
		}
		throw new NotInStoreException();
	}
}

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
package org.stanwood.media.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.source.SourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This is a helper class that should be extend by classes that need to parse XML
 */
public class XMLParser {

	/**
	 * Used to read a integer from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the integer
	 * @return The integer value
	 * @throws TransformerException Thrown if their is a XML problem
	 * @throws NotInStoreException Thrown if the value can't be read
	 */
	protected Integer getIntegerFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return Integer.parseInt(node.getNodeValue());
		}
		throw new NotInStoreException();
	}

	/**
	 * Used to read a long from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the long
	 * @return The long value
	 * @throws TransformerException Thrown if their is a XML problem
	 * @throws NotInStoreException Thrown if the value can't be read
	 */
	protected Long getLongFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return Long.parseLong(node.getNodeValue());
		}
		throw new NotInStoreException();
	}

	/**
	 * Used to read a string from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the string
	 * @return The string value
	 * @throws TransformerException Thrown if their is a XML problem
	 * @throws NotInStoreException Thrown if the value can't be read
	 */
	protected String getStringFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return node.getNodeValue();
		}
		throw new NotInStoreException();
	}

	/**
	 * Used to read a float from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the float
	 * @return The float value
	 * @throws TransformerException Thrown if their is a XML problem
	 * @throws NotInStoreException Thrown if the value can't be read
	 */
	protected float getFloatFromXML(Node parent, String path)
			throws TransformerException, NotInStoreException {
		Node node = XPathAPI.selectSingleNode(parent, path);
		if (node != null) {
			return Float.parseFloat(node.getNodeValue());
		}
		throw new NotInStoreException();
	}
	
	/**
	 * Used to convert a XML string to a DOM document
	 * @param str The string to convert
	 * @return The DOM Document
	 * @throws ParserConfigurationException Thrown if their is a parsing problem
	 * @throws SAXException Thrown if their is a SAX problem
	 * @throws IOException Thrown if their is a I/O releated problem
	 */
	public static Document strToDom(String str) throws ParserConfigurationException, SAXException, IOException {		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource( new StringReader( str ) );
		Document d = builder.parse( is );
		return d;	
	}
	
	protected Element getFirstChildElement(Node parent,String name) {
		NodeList children =  parent.getChildNodes();
		for (int i =0;i<children.getLength();i++) {
			Node node = children.item(i);
			if (node instanceof Element && node.getNodeValue().equals(name)) {
				return (Element) node;
			}
		}
		return null;
	}
	
	protected Element firstChild(Element expressionEl) {
		NodeList children =  expressionEl.getChildNodes();
		for (int i =0;i<children.getLength();i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				return (Element) node;
			}
		}
		return null;
	}
}

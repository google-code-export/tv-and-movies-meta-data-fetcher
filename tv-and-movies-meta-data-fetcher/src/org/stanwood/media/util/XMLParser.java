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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
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
	 * @throws XMLParserException Thrown if their is a XML problem
	 * @throws XMLParserNotFoundException Thrown if the value can't be read
	 */
	protected Integer getIntegerFromXML(Node parent, String path)
			throws XMLParserException {
		try {
			Node node = XPathAPI.selectSingleNode(parent, path);
			if (node != null) {
				return Integer.parseInt(node.getNodeValue());
			}
		}
		catch (TransformerException e) {
			throw new XMLParserException("Unable to parser XML",e);
		}
		throw new XMLParserNotFoundException();
	}

	/**
	 * Used to read a long from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the long
	 * @return The long value
	 * @throws XMLParserException Thrown if their is a XML problem
	 * @throws XMLParserNotFoundException Thrown if the value can't be read
	 */
	protected Long getLongFromXML(Node parent, String path)
			throws XMLParserException {
		try {
			Node node = XPathAPI.selectSingleNode(parent, path);
			if (node != null) {
				return Long.parseLong(node.getNodeValue());
			}
		}
		catch (TransformerException e) {
			throw new XMLParserException("Unable to parser XML",e);
		}
		throw new XMLParserNotFoundException();
	}

	/**
	 * Used to read a string from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the string
	 * @return The string value
	 * @throws XMLParserException Thrown if their is a XML problem
	 * @throws XMLParserNotFoundException Thrown if the value can't be read
	 */
	protected String getStringFromXML(Node parent, String path)
			throws XMLParserException {
		try {
			Node node = XPathAPI.selectSingleNode(parent, path);
			if (node != null) {
				return node.getNodeValue();
			}
		}
		catch (TransformerException e) {
			throw new XMLParserException("Unable to parser XML",e);
		}
		throw new XMLParserNotFoundException();
	}

	/**
	 * Used to read a float from the XML
	 * @param parent The parent node it's the path is to be used from
	 * @param path The xpath location of the float
	 * @return The float value
	 * @throws XMLParserException Thrown if their is a XML problem
	 * @throws XMLParserNotFoundException Thrown if the value can't be read
	 */
	protected float getFloatFromXML(Node parent, String path)
			throws XMLParserException {
		try {
			Node node = XPathAPI.selectSingleNode(parent, path);
			if (node != null) {
				return Float.parseFloat(node.getNodeValue());
			}
		}
		catch (TransformerException e) {
			throw new XMLParserException("Unable to parser XML",e);
		}
		throw new XMLParserNotFoundException();
	}

	/**
	 * Used to convert a XML string to a DOM document
	 * @param str The string to convert
	 * @return The DOM Document
	 * @throws XMLParserException Thrown if their is a parsing problem
	 */
	public static Document strToDom(String str) throws XMLParserException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = XMLParser.createDocBuilder(factory);

			InputSource is = new InputSource( new StringReader( str ) );
			if (str.trim().length()==0) {
				return builder.newDocument();
			}
			else {
				builder.newDocument();
				Document d = builder.parse( is );
				return d;
			}
		}
		catch (Exception e) {
			throw new XMLParserException("Unable to convert string to XML DOM",e);
		}
	}

	/**
	 * Used to convert a DOM document to a string
	 * @param document The DOM document
	 * @return The XML as a string
	 * @throws XMLParserException Thrown if their is a parsing problem
	 */
	public static String domToStr(Document document) throws XMLParserException {
		try {
			OutputFormat format = new OutputFormat(document);
	        format.setLineWidth(65);
	        format.setIndenting(true);
	        format.setIndent(2);

	        Writer out = new StringWriter();
	        XMLSerializer serializer = new XMLSerializer(out, format);
	        serializer.serialize(document);

	        return out.toString();
		}
		catch (Exception e) {
			throw new XMLParserException("Unable to convert XML DOM to string",e);
		}
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

	protected IterableNodeList selectNodeList(Node contextNode,String path) throws XMLParserException {
		try {
			NodeList list = XPathAPI.selectNodeList(contextNode, path);
			return new IterableNodeList(list);
		}
		catch (Exception e) {
			throw new XMLParserException("Unable to parser path " + path + " from XML DOM",e);
		}
	}

	protected Node selectSingleNode(Node contextNode,String path) throws XMLParserException {
		try {
			return XPathAPI.selectSingleNode(contextNode, path);
		}
		catch (Exception e) {
			throw new XMLParserException("Unable to parser path " + path + " from XML DOM",e);
		}
	}

	public static DocumentBuilder createDocBuilder(DocumentBuilderFactory factory)
	throws ParserConfigurationException {
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {
		    @Override
		    public InputSource resolveEntity(String publicId, String systemId)
		            throws SAXException, IOException {
		    	if (publicId!=null) {
			    	if (publicId.equals("-//STANWOOD//DTD XMLStore 2.0//EN")) {
			        	File currentDir = new File(System.getProperty("user.dir"));
			        	File dtd = new File(currentDir,"etc"+File.separator+"MediaInfoFetcher-XmlStore-2.0.dtd");
			        	if (dtd.exists()) {
			        		return new InputSource(new FileInputStream(dtd));
			        	}
			        }
		    	}
		        return null;
		    }
		});
		return builder;
	}

}

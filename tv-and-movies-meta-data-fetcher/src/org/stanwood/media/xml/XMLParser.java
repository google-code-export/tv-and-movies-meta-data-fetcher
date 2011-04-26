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
package org.stanwood.media.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

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

	public static String DTD_WEB_LOCATION = "http://tv-and-movies-meta-data-fetcher.googlecode.com/svn/trunk/tv-and-movies-meta-data-fetcher/src/org/stanwood/media/xml/dtd";
	public static String SCHEMA_WEB_LOCATION = "http://tv-and-movies-meta-data-fetcher.googlecode.com/svn/trunk/tv-and-movies-meta-data-fetcher/src/org/stanwood/media/xml/schema";

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
	 * Used to convert a XML file to a DOM document
	 * @param file the XML file
	 * @return The DOM Document
	 * @throws XMLParserException Thrown if their is a parsing problem
	 * @throws IOException Thrown if their is a problem reading the file
	 */
	public static Document strToDom(File file) throws XMLParserException, IOException {
		FileInputStream fis = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = XMLParser.createDocBuilder(factory);

			fis = new FileInputStream( file );
			InputSource is = new InputSource( fis );
			builder.newDocument();
			Document d = builder.parse( is );
			return d;
		}
		catch (Exception e) {
			throw new XMLParserException("Unable to convert string to XML DOM",e);
		}
		finally {
			if (fis!=null) {
				fis.close();
			}
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
			throw new XMLParserException("Unable to parse path '" + path + "' from XML DOM",e);
		}
	}

	protected Node selectSingleNode(Node contextNode,String path) throws XMLParserException {
		try {
			return XPathAPI.selectSingleNode(contextNode, path);
		}
		catch (Exception e) {
			throw new XMLParserException("Unable to parse path '" + path + "' from XML DOM",e);
		}
	}

	/**
	 * Create a XML document builder. This takes care of fetching DTD's locally instead of downloading
	 * them from the Internet.
	 * @param factory The document builder factory
	 * @return The document builder
	 * @throws ParserConfigurationException Thrown if their are any problems
	 */
	public static DocumentBuilder createDocBuilder(DocumentBuilderFactory factory)
	throws ParserConfigurationException {
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {
		    @Override
		    public InputSource resolveEntity(String publicId, String systemId)
		            throws SAXException, IOException {
		    	if (systemId.endsWith(".xsd")) {
		    		String schemaName= systemId.substring(systemId.lastIndexOf("/")+1);
		    		InputStream stream = XMLParser.class.getResourceAsStream("schema/"+schemaName);
		    		if (stream==null) {
		    			throw new IOException ("Unable to find schema: " + schemaName);
		    		}
		    		return new InputSource(stream);
		    	}
		    	else if (publicId!=null) {
			    	if (publicId.equals("-//STANWOOD//DTD XMLStore 2.0//EN")) {
			    		InputStream stream = XMLParser.class.getResourceAsStream("dtd/MediaInfoFetcher-XmlStore-2.0.dtd");
			    		if (stream==null) {
			    			throw new IOException ("Unable to find dtd");
			    		}
			    		return new InputSource(stream);
			        }
		    	}
		        return null;
		    }
		});
		return builder;
	}

	/**
	 * Used to convert a XML String to a XML document. If the schemaName is not null,
	 * then the XML string is validated against it. It will also attempt to find the
	 * schema locally if possible
	 * @param contents The XML String
	 * @param schemaName The schema name, or null if validation is not required
	 * @return The XML Document
	 * @throws XMLParserException Thrown if their was a problem converting the string to a document
	 */
	public static Document parse(String contents,String schemaName) throws XMLParserException {
		DocumentBuilderFactory factory = createFactory(schemaName);
		try {
			DocumentBuilder builder = createDocBuilder(factory);
			XMLErrorHandler errorHandler = new XMLErrorHandler();
			builder.setErrorHandler(errorHandler);
			 InputSource is = new InputSource( new StringReader( contents ) );
			Document doc = builder.parse(is);
			if (errorHandler.hasErrors()) {
				throw new XMLParserException("Unable to parse XML document as it containted errors");
			}
			return doc;
		} catch (SAXException e) {
			throw new XMLParserException("Unable to parse XML document as it containted errors");
		} catch (IOException e) {
			throw new XMLParserException("Unable to parse XML document as it containted errors");
		} catch (ParserConfigurationException e) {
			throw new XMLParserException("Unable to parse XML document as it containted errors");
		}
	}

	/**
	 * Used to convert a XML InputStream to a XML document. If the schemaName is not null,
	 * then the XML string is validated against it. It will also attempt to find the
	 * schema locally if possible
	 * @param is The XML InputStream
	 * @param schemaName The schema name, or null if validation is not required
	 * @return The XML Document
	 * @throws XMLParserException Thrown if their was a problem converting the string to a document
	 */
	public static Document parse(InputStream is,String schemaName) throws XMLParserException {
		DocumentBuilderFactory factory = createFactory(schemaName);

		try {
			DocumentBuilder builder = createDocBuilder(factory);
			XMLErrorHandler errorHandler = new XMLErrorHandler();
			builder.setErrorHandler(errorHandler);
			Document doc = builder.parse(is);
			if (errorHandler.hasErrors()) {
				throw new XMLParserException("Unable to parse XML document as it containted errors");
			}
			return doc;
		} catch (SAXException e) {
			throw new XMLParserException("Unable to parse XML document as it containted errors");
		} catch (IOException e) {
			throw new XMLParserException("Unable to parse XML document as it containted errors");
		} catch (ParserConfigurationException e) {
			throw new XMLParserException("Unable to parse XML document as it containted errors");
		}
	}

	private static DocumentBuilderFactory createFactory(String schemaName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		if (schemaName!=null) {
			factory.setValidating(true);
			factory.setXIncludeAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", SCHEMA_WEB_LOCATION+"/"+schemaName);
		}
		return factory;
	}

	/**
	 * Used to convert a XML file to a XML document. If the schemaName is not null,
	 * then the XML string is validated against it. It will also attempt to find the
	 * schema locally if possible
	 * @param file The XML file
	 * @param schemaName The schema name, or null if validation is not required
	 * @return The XML Document
	 * @throws XMLParserException Thrown if their was a problem converting the file to a document
	 */
	public static Document parse(File file,String schemaName) throws XMLParserException {
		DocumentBuilderFactory factory = createFactory(schemaName);

		try {
			DocumentBuilder builder = createDocBuilder(factory);
			SimpleErrorHandler errorHandler = new SimpleErrorHandler(file);
			builder.setErrorHandler(errorHandler);
			Document doc = builder.parse(file);
			if (errorHandler.hasErrors()) {
				throw new XMLParserException("Unable to parse XML document as it containted errors: " + file.getAbsolutePath());
			}
			return doc;
		} catch (SAXException e) {
			throw new XMLParserException("Unable to parse XML document: " + file.getAbsolutePath(),e);
		} catch (IOException e) {
			throw new XMLParserException("Unable to parse XML document: " + file.getAbsolutePath(),e);
		} catch (ParserConfigurationException e) {
			throw new XMLParserException("Unable to parse XML document: " + file.getAbsolutePath(),e);
		}
	}

	/**
	 * Used to create a Schema that can be used to validate XML documents
	 * @param name The name of the schema
	 * @return The schema
	 * @throws XMLParserException Thrown if their are any problems
	 */
	public static Schema getSchema(String name) throws XMLParserException {
		try {
			String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
			SchemaFactory schemaFactory = SchemaFactory.newInstance(language);
			return schemaFactory.newSchema(new StreamSource(XMLParser.class.getResourceAsStream("schema/"+name)));
		} catch (SAXException e) {
			throw new XMLParserException("Unable to get schema: " + name,e);
		}
	}

	/**
	 * This is used to make sure a value can be written to a XML document by encoding the characters that should be XML entities.
	 * @param value The value to encode
	 * @return The encoded value
	 */
	public static String encodeAttributeValue(String value) {
		StringBuilder result =new StringBuilder();
		for (int i=0;i<value.length();i++) {
			char c = value.charAt(i);
			switch (c)  {
			case '&' :
				result.append("&amp;");
				break;
			case '\'' :
				result.append("&apos;");
				break;
			case '<' :
				result.append("&lt;");
				break;
			case '>' :
				result.append("&gt;");
				break;
			case '"' :
				result.append("&quot;");
				break;
			default:
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Used to write a XML document to a file
	 * @param file The file to write
	 * @param doc The XML contents
	 * @throws IOException Thrown if thier is aproblem writing the file
	 */
	public static void writeXML(File file, Document doc) throws IOException {
		OutputFormat format = new OutputFormat(doc);
		format.setLineWidth(65);
		format.setIndenting(true);
		format.setIndent(2);
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			XMLSerializer serializer = new XMLSerializer(os, format);
			serializer.serialize(doc);
		}
		finally {
			os.close();
		}

	}

	public String quoteXPathQuery(String s) {
		if (s.contains("'")) {
			return "\""+s+"\"";
		}
		else {
			return "'"+s+"'";
		}
	}
}

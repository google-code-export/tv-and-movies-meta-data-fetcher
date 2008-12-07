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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Link;
import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This is the base class for the XML Store classes. It has the methods which are common between
 * the different store classes.
 */
public abstract class BaseXMLStore extends XMLParser {

	protected final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * Used to write the case document to a file
	 * @param file The file to write it to
	 * @param doc The contents to write
	 * @throws StoreException Thrown if their is a problem writing the cache
	 */
	protected void writeCache(File file, Document doc) throws StoreException {
		try {
			OutputFormat format = new OutputFormat(doc);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(
					file), format);
			serializer.serialize(doc);
		} catch (FileNotFoundException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new StoreException("Unable to write cache: "
					+ e.getMessage());
		}
	}
	
	/**
	 * Used to get a file object which points to a cache file.
	 * @param cacheDirectory The directory the cache is located in
	 * @param filename The filename of the cache
	 * @return The file oject pointing to the cache file.
	 */
	protected File getCacheFile(File cacheDirectory,String filename) {
		File file = new File(cacheDirectory, filename);
		return file;
	}
	
	/**
	 * This is used to add links to the cache document under the given node.
	 * @param doc The document to add the links to
	 * @param node The node in the document that the nodes should be appended to
	 * @param tagLabel The name of the tag which the nodes will be created under
	 * @param links The links to add to the document.
	 * @throws TransformerException Thrown if their is a problem adding the links too the document
	 */
	protected void writeEpsoideExtraInfo(Document doc, 
			Node node, String tagLabel, List<Link> links)
			throws TransformerException {
		NodeList nodeList = XPathAPI.selectNodeList(node, tagLabel);
		for (int i = 0; i < nodeList.getLength(); i++) {
			nodeList.item(i).getParentNode().removeChild(nodeList.item(i));
		}
		if (links != null) {
			for (Link value : links) {
				Element newNode = doc.createElement(tagLabel);
				newNode.setAttribute("name", value.getTitle());
				newNode.setAttribute("link", value.getURL());
				node.appendChild(newNode);
			}
		}
	}

	/**
	 * Used to convert a URL into text. If the URL is null, then a empty string is returned.
	 * This should be used for writing url's too the XML document.
	 * @param url The URL to convert to text.
	 * @return The url as a string, or a empty string if the url was null.
	 */
	protected String urlToText(URL url) {
		if (url==null) {
			return "";
		}
		return url.toExternalForm();
	}
	
	/**
	 * Used to get a list of filenames under the parent node
	 * @param parent The node to look under for filenames
	 * @return The filenames
	 * @throws TransformerException Thrown if their is a problem parsing the XML
	 */
	protected Set<String> getOldFilenames(Node parent) throws TransformerException {
		Set<String> filenames = new HashSet<String>();
		NodeList node = XPathAPI.selectNodeList(parent,"file/@name");
		if (node!=null) {
		for (int i=0;i<node.getLength();i++) {
			filenames.add(node.item(i).getNodeValue());
		}
		}
		return filenames;
	}
	
	/**
	 * Used to append a set of filenames to the document under the given parent node
	 * @param doc The document to append the filenames to
	 * @param parent The parent node
	 * @param filenames The filenames to append
	 */
	protected void writeFilenames(Document doc, Node filmNode, Set<String> filenames) {
		List<String> sorted = new ArrayList<String>(filenames);
		Collections.sort(sorted,new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}		
		});
		for (String filename : sorted) {			
			Element fileNode = doc.createElement("file");
			fileNode.setAttribute("name", filename);
			filmNode.appendChild(fileNode);
		}
	}

	/**
	 * Used to read the genres from the XML document
	 * @param parent The parent node to read them from
	 * @return A list of genres that were found
	 * @throws TransformerException Thrown if their is a problem parsing the XML
	 * @throws NotInStoreException Thrown if the genres are not in the store
	 */
	protected List<String> readGenresFromXML(Node parent)
			throws TransformerException, NotInStoreException {
		List<String> genres = new ArrayList<String>();
		NodeList nodeList = XPathAPI.selectNodeList(parent, "genre");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element node = (Element) nodeList.item(i);
				String genre = node.getAttribute("name");
				if (genre == null) {
					throw new NotInStoreException();
				}
				genres.add(genre);
			}
		}
		return genres;
	}
	
	/**
	 * Used to read links from a node in a DOM document
	 * @param node The node to read the links from
	 * @param tagLabel The parent tag of the links
	 * @return A list of links
	 * @throws TransformerException Thrown if their is a problem parsing the XML
	 */
	protected List<Link> getLinks(Node node, String tagLabel)
			throws TransformerException {
		List<Link> result = new ArrayList<Link>();
		NodeList list = XPathAPI.selectNodeList(node, tagLabel);
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			result.add(new Link(element.getAttribute("name"), element
					.getAttribute("link")));
		}
		return result;
	}
}

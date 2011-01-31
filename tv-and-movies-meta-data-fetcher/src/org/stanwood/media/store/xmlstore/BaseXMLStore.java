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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
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

	private final static Log log = LogFactory.getLog(BaseXMLStore.class);

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
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(file), format);
			serializer.serialize(doc);

			if (log.isDebugEnabled()) {
				log.debug("Written cache to file : " + file.getAbsolutePath());
			}
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
	 * @return The file object pointing to the cache file.
	 */
	protected File getCacheFile(File cacheDirectory,String filename) {
		File file = new File(cacheDirectory, filename);
		return file;
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
	 * @throws XMLParserException Thrown if their is a problem parsing the XML
	 */
	protected Set<String> getOldFilenames(Node parent) throws XMLParserException {
		try {
			Set<String> filenames = new HashSet<String>();
			NodeList node = XPathAPI.selectNodeList(parent,"file/@location");
			if (node!=null) {
				for (int i=0;i<node.getLength();i++) {
					filenames.add(node.item(i).getNodeValue());
				}
			}
			node = XPathAPI.selectNodeList(parent,"file/@name");
			if (node!=null) {
				for (int i=0;i<node.getLength();i++) {
					filenames.add(node.item(i).getNodeValue());
				}
			}

			return filenames;
		}
		catch (TransformerException e) {
			throw new XMLParserException("Unable to parse old filenames from XML",e);
		}
	}

	/**
	 * Used to append a set of filenames to the document under the given parent node
	 * @param doc The document to append the filenames to
	 * @param parent The parent node
	 * @param filenames The filenames to append
	 * @throws StoreException Thrown if their is a tore releated problem
	 */
	protected void writeFilenames(Document doc, Node filmNode, Set<String> filenames) throws StoreException {
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



}

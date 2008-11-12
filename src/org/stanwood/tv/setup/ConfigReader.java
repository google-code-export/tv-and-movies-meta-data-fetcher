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
package org.stanwood.tv.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.tv.util.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This is used to parse the XML configuration files. These are used to tell the 
 * application which stores and sources should be used.
 */
public class ConfigReader extends XMLParser {

	private File file;
	private List<String>stores = new ArrayList<String>();
	private List<String>sources = new ArrayList<String>();

	/**
	 * The constructor used to create a instance of the configuration reader
	 * @param file The configuration file
	 */
	public ConfigReader(File file) {
		this.file = file;
	}
	
	/**
	 * This will parse the configuration in the XML configuration file and store the
	 * results in this class.
	 * @throws ConfigException Thrown if their is a problem parsing the file
	 */
	public void parse() throws ConfigException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);		 
		if (file.exists()) {
			try {
				Document doc = factory.newDocumentBuilder().parse(file);
				
				NodeList stores = XPathAPI.selectNodeList(doc, "config/stores/store/@id");
				for (int i=0;i<stores.getLength();i++) {	
					Node item = stores.item(i);
					if (item!=null) {
						this.stores.add(item.getNodeValue());
					}
				}
				
				
				NodeList sources = XPathAPI.selectNodeList(doc, "config/sources/source/@id");
				for (int i=0;i<sources.getLength();i++) {
					if (sources.item(i)!=null) {
						this.sources.add(sources.item(i).getNodeValue());
					}
				}
				
			} catch (SAXException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			} catch (IOException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			} catch (ParserConfigurationException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			} catch (TransformerException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			}
		}
	}

	/**
	 * Once the data has been parsed, this will returned a list of the stores in the
	 * configuration file.
	 * @return A list of stores from the file
	 */
	public List<String> getStores() {
		return stores;
	}	

	/**
	 * Once the data has been parsed, this will returned a list of the sources in the
	 * configuration file.
	 * @return A list of sources from the file
	 */
	public List<String> getSources() {
		return sources;
	}

	
	
	
}

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
package org.stanwood.media.setup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This is used to parse the XML configuration files. These are used to tell the
 * application which stores and sources should be used.
 */
public class ConfigReader extends XMLParser {

	private final static Log log = LogFactory.getLog(ConfigReader.class);

	private File file;
	private List<StoreConfig>stores = new ArrayList<StoreConfig>();
	private List<SourceConfig>sources = new ArrayList<SourceConfig>();

	private static XBMCAddonManager xbmcMgr;

	static {
		try {
			setManager(new XBMCAddonManager());
		} catch (XBMCException e) {
			log.error(e.getMessage(),e);
		}
	}

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
		if (file.exists()) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(true);
				factory.setSchema(XMLParser.getSchema("MediaInfoFetcher-Config-1.0.xsd"));
				Document doc = createDocBuilder(factory).parse(file);

				for (Node storeElement : selectNodeList(doc, "config/stores/store")) {
					StoreConfig store = new StoreConfig();
					store.setID(((Element)storeElement).getAttribute("id"));

					for (Node paramNode : selectNodeList(storeElement, "param")) {
						String name = ((Element)paramNode).getAttribute("name");
						String value = ((Element)paramNode).getAttribute("value");
						store.addParam(name, value);
					}

					this.stores.add(store);
				}


				for (Node sourceElement : selectNodeList(doc, "config/sources/source")) {
					SourceConfig source = new SourceConfig();
					source.setID(((Element)sourceElement).getAttribute("id"));
					for (Node paramNode : selectNodeList(sourceElement, "param")) {
						String name = ((Element)paramNode).getAttribute("name");
						String value = ((Element)paramNode).getAttribute("value");
						source.addParam(name, value);
					}

					this.sources.add(source);
				}

			} catch (SAXException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			} catch (IOException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			} catch (ParserConfigurationException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			} catch (XMLParserException e) {
				throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
			}
		}
	}

	/**
	 * Once the data has been parsed, this will returned a list of the stores in the
	 * configuration file.
	 * @return A list of stores from the file
	 */
	public List<StoreConfig> getStores() {
		return stores;
	}

	/**
	 * Once the data has been parsed, this will returned a list of the sources in the
	 * configuration file.
	 * @return A list of sources from the file
	 */
	public List<SourceConfig> getSources() {
		return sources;
	}


	/**
	 * Used to read the sources from the configuration file
	 * @return Thrown if their are any problems
	 * @throws ConfigException Thrown if their are any problems
	 */
	public List<ISource> loadSourcesFromConfigFile() throws ConfigException {
		List<ISource>sources = new ArrayList<ISource>();
		for (SourceConfig sourceConfig : getSources()) {
			String sourceClass = sourceConfig.getID();
			try {
				Class<? extends ISource> c = Class.forName(sourceClass).asSubclass(ISource.class);
				if (XBMCSource.class.isAssignableFrom(c)) {
					List<ISource> xbmcSources = xbmcMgr.getSources();
					if (sourceConfig.getParams() != null) {
						for (String key : sourceConfig.getParams().keySet()) {
							String value = sourceConfig.getParams().get(key);
							List<String>addons = null;
							if (key.equals("scrapers")) {
								addons = new ArrayList<String>();
								StringTokenizer tok = new StringTokenizer(value,",");
								while (tok.hasMoreTokens()) {
									addons.add("xbmc-"+tok.nextToken());
								}
							}

							Iterator<ISource> it = xbmcSources.iterator();
							while (it.hasNext()) {
								ISource source = it.next();
								if (addons!=null && !addons.contains(source.getSourceId())) {
									it.remove();
								}
								else {
									setParamOnSource( source, key, value);
								}
							}
						}
					}
					sources.addAll(xbmcSources);
				}
				else {
					ISource source = c.newInstance();
					if (sourceConfig.getParams() != null) {
						for (String key : sourceConfig.getParams().keySet()) {
							String value = sourceConfig.getParams().get(key);
							setParamOnSource( source, key, value);
						}
					}
					sources.add(source);
				}
			} catch (ClassNotFoundException e) {
				throw new ConfigException("Unable to add source '" + sourceClass + "' because " + e.getMessage(),e);
			} catch (InstantiationException e) {
				throw new ConfigException("Unable to add source '" + sourceClass + "' because " + e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new ConfigException("Unable to add source '" + sourceClass + "' because " + e.getMessage(),e);
			} catch (SourceException e) {
				throw new ConfigException("Unable to add source '" + sourceClass + "' because " + e.getMessage(),e);
			}
		}
		return sources;
	}

	/**
	 * Used to read the stores from the configuration file
	 * @return The stores
	 * @throws ConfigException Thrown if their is any problems
	 */
	public List<IStore> loadStoresFromConfigFile() throws ConfigException {
		List<IStore>stores = new ArrayList<IStore>();
		for (StoreConfig storeConfig : getStores()) {
			String storeClass = storeConfig.getID();
			try {
				Class<? extends IStore> c = Class.forName(storeClass).asSubclass(IStore.class);
				IStore store = c.newInstance();
				if (storeConfig.getParams() != null) {
					for (String key : storeConfig.getParams().keySet()) {
						String value = storeConfig.getParams().get(key);
						setParamOnStore(c, store, key, value);
					}
				}
				stores.add(store);
			} catch (ClassNotFoundException e) {
				throw new ConfigException("Unable to add source '" + storeClass + "' because " + e.getMessage(),e);
			} catch (InstantiationException e) {
				throw new ConfigException("Unable to add source '" + storeClass + "' because " + e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new ConfigException("Unable to add source '" + storeClass + "' because " + e.getMessage(),e);
			} catch (IllegalArgumentException e) {
				throw new ConfigException("Unable to add source '" + storeClass + "' because " + e.getMessage(),e);
			} catch (InvocationTargetException e) {
				throw new ConfigException("Unable to add source '" + storeClass + "' because " + e.getMessage(),e);
			}
		}
		return stores;
	}

	private static void setParamOnSource(ISource source, String key, String value)
	throws  SourceException {
		source.setParameter(key, value);
	}

	private static void setParamOnStore(Class<? extends IStore> c, IStore store, String key, String value)
		throws IllegalAccessException, InvocationTargetException {
		for (Method method : c.getMethods()) {
			if (method.getName().toLowerCase().equals("set" + key.toLowerCase())) {
				method.invoke(store, value);
				break;
			}
		}
	}

	public static void setManager(XBMCAddonManager mgr) {
		xbmcMgr = mgr;
	}

	public String getDefaultSourceID(Mode mode) throws XBMCException {
		return xbmcMgr.getDefaultSourceID(mode);
	}

	public ISource getDefaultSource(Mode mode) throws XBMCException {
		String id = xbmcMgr.getDefaultSourceID(mode);
		for (ISource source : xbmcMgr.getSources()) {
			if (source.getSourceId().equals(id)) {
				return source;
			}
		}
		return null;
	}



}

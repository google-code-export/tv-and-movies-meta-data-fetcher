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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.actions.rename.PatternMatcher;
import org.stanwood.media.model.Mode;
import org.stanwood.media.renamer.Controller;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is used to parse the XML configuration files. These are used to tell the
 * application which stores and sources should be used.
 */
public class ConfigReader extends BaseConfigReader {

	private final static Log log = LogFactory.getLog(ConfigReader.class);

	private final static String DEFAULT_TV_FILE_PATTERN = "%s %e - %t.%x";
	private final static String DEFAULT_FILM_FILE_PATTERN = "%t.%x";

	/** The default location to store configuration */
	public static final File MEDIA_CONFIG_DIR = new File(FileHelper.HOME_DIR,".mediaInfo");

	private InputStream is;
	private List<MediaDirConfig>mediaDir;

	private File xbmcAddonDir;

	private Locale xbmcLocale = Locale.ENGLISH;

	private List<Plugin> plugins = new ArrayList<Plugin>();

	/**
	 * The constructor used to create a instance of the configuration reader
	 * @param is The configuration file input stream
	 */
	public ConfigReader(InputStream is) {
		this.is = is;
	}

	/**
	 * This will parse the configuration in the XML configuration file and store the
	 * results in this class.
	 * @throws ConfigException Thrown if their is a problem parsing the file
	 */
	public void parse() throws ConfigException {
		try {
			Document doc = XMLParser.parse(is, "MediaInfoFetcher-Config-2.0.xsd");
			parseXBMCSettings(doc);
			parseMediaDirs(doc);
			parsePlguins(doc);
		} catch (XMLParserException e) {
			throw new ConfigException("Unable to parse config file: " + e.getMessage(),e);
		}
	}

	private void parseMediaDirs(Document doc) throws XMLParserException, ConfigException {
		List<MediaDirConfig>dirConfigs = new ArrayList<MediaDirConfig>();
		for (Node node : selectNodeList(doc,"/mediaManager/mediaDirectory")) {
			Element dirNode = (Element) node;
			MediaDirConfig dirConfig = new MediaDirConfig();
			File dir = new File(dirNode.getAttribute("directory"));
			if (!dir.exists()) {
				throw new ConfigException("Unable to find root media directory: '"+dir.getAbsolutePath()+"'" );
			}
			dirConfig.setMediaDir(dir);

			String strMode = dirNode.getAttribute("mode").toUpperCase();
			Mode mode ;
			try {
				mode = Mode.valueOf(strMode);
			}
			catch (IllegalArgumentException e) {
				throw new ConfigException("Unkown mode '"+strMode+"' for media directory '"+dir.getAbsolutePath()+"'");
			}

			String pattern = dirNode.getAttribute("pattern").trim();
			if (pattern.length()==0) {
				pattern = DEFAULT_TV_FILE_PATTERN;
				if (mode == Mode.FILM) {
					pattern = DEFAULT_FILM_FILE_PATTERN;
				}
				log.warn("No pattern given, using default: " + pattern);
			}
			else {
				if (!PatternMatcher.validPattern(pattern)) {
					throw new ConfigException("Invalid pattern '"+pattern+"' for media directory '"+dir.getAbsolutePath()+"'");
				}
			}

			dirConfig.setPattern(pattern);
			dirConfig.setMode(mode);

			dirConfig.setSources(readSources(node));
			dirConfig.setStores(readStores(node));
			dirConfigs.add(dirConfig);
		}
		this.mediaDir = dirConfigs;
	}

	/**
	 * Used to get the configuration for a root media directory
	 * @param directory the root media directory
	 * @return The configuration
	 * @throws ConfigException Thrown if the configuration can't be found
	 */
	public MediaDirConfig getMediaDirectory(File directory) throws ConfigException {
		for (MediaDirConfig c : mediaDir) {
			if (c.getMediaDir().equals(directory)) {
				return c;
			}
		}
		throw new ConfigException("Unable to find media directory '"+directory+"' in the configuration");
	}

	/**
	 * Used to read the sources from the configuration file
	 * @return Thrown if their are any problems
	 * @throws ConfigException Thrown if their are any problems
	 */
	@Override
	public List<ISource> loadSourcesFromConfigFile(Controller controller,MediaDirConfig dirConfig) throws ConfigException {
		List<ISource>sources = new ArrayList<ISource>();
		for (SourceConfig sourceConfig : dirConfig.getSources()) {
			String sourceClass = sourceConfig.getID();
			try {
				Class<? extends ISource> c = controller.getSourceClass(sourceClass);
				if (XBMCSource.class.isAssignableFrom(c)) {
					List<ISource> xbmcSources = controller.getXBMCAddonManager().getSources();
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
	@Override
	public List<IStore> loadStoresFromConfigFile(Controller controller,MediaDirConfig dirConfig) throws ConfigException {
		List<IStore>stores = new ArrayList<IStore>();
		for (StoreConfig storeConfig : dirConfig.getStores()) {
			String storeClass = storeConfig.getID();
			try {

				Class<? extends IStore> c = controller.getStoreClass(storeClass);
				IStore store = c.newInstance();
				if (storeConfig.getParams() != null) {
					for (String key : storeConfig.getParams().keySet()) {
						String value = storeConfig.getParams().get(key);
						setParamOnStore(c, store, key, value);
					}
				}
				stores.add(store);
			} catch (InstantiationException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			} catch (IllegalArgumentException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			} catch (InvocationTargetException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
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
		store.setParameter(key, value);
	}

	/**
	 * Used to get the directory where XBMC addons are installed. If one has not been specified in the configuration, then
	 * a default on is used instead of $HOME/.mediaInfo/xbmc/addons.
	 * @return The XBMC addon directory
	 * @throws ConfigException Thrown if their is a problem
	 */
	public File getXBMCAddonDir() throws ConfigException {
		if (xbmcAddonDir==null) {
			xbmcAddonDir = getDefaultAddonDir();
		}
		return xbmcAddonDir;
	}

	private static File getDefaultAddonDir() throws ConfigException {

		File addonDir = new File(MEDIA_CONFIG_DIR,"xbmc"+File.separator+"addons");
		if (!addonDir.exists()) {
			if (!addonDir.mkdirs() && !addonDir.exists()) {
				throw new ConfigException("Unable to create xbmc addon directory: " + addonDir);
			}
		}
		return addonDir;
	}

	/**
	 * Used to get the locale that should be used when fetching media information from XBMC Addons.
	 * @return The locale
	 */
	public Locale getXBMCLocale() {
		return xbmcLocale;
	}

	private void parsePlguins(Node doc) throws XMLParserException {
		for(Node n : selectNodeList(doc, "/mediaManager/plugins/plugin")) {
			Element pluginEl = (Element)n;
			String jar = pluginEl.getAttribute("jar");
			String clazz = pluginEl.getAttribute("class");
			plugins.add(new Plugin(jar,clazz));
		}
	}

	private void parseXBMCSettings(Node configNode) throws XMLParserException {
		Element node = (Element) selectSingleNode(configNode, "/mediaManager/XBMCAddons");
		if (node!=null) {
			String dir = node.getAttribute("directory");
			if (dir.trim().length()>0) {
				xbmcAddonDir =new File(dir);
			}
			String locale = node.getAttribute("locale");
			if (locale.trim().length()>0) {
				xbmcLocale = new Locale(locale);
			}
		}
	}

	private List<SourceConfig> readSources(Node configNode) throws XMLParserException {
		List<SourceConfig> sources = new ArrayList<SourceConfig>();
		for (Node sourceElement : selectNodeList(configNode, "sources/source")) {
			SourceConfig source = new SourceConfig();
			source.setID(((Element)sourceElement).getAttribute("id"));
			for (Node paramNode : selectNodeList(sourceElement, "param")) {
				String name = ((Element)paramNode).getAttribute("name");
				String value = ((Element)paramNode).getAttribute("value");
				source.addParam(name, value);
			}

			sources.add(source);
		}
		return sources;
	}

	private List<StoreConfig>readStores(Node configNode) throws XMLParserException {
		List<StoreConfig>stores = new ArrayList<StoreConfig>();
		for (Node storeElement : selectNodeList(configNode, "stores/store")) {
			StoreConfig store = new StoreConfig();
			store.setID(((Element)storeElement).getAttribute("id"));

			for (Node paramNode : selectNodeList(storeElement, "param")) {
				String name = ((Element)paramNode).getAttribute("name");
				String value = ((Element)paramNode).getAttribute("value");
				store.addParam(name, value);
			}

			stores.add(store);
		}
		return stores;
	}

	public List<Plugin>getPlugins() {
		return plugins;
	}
}

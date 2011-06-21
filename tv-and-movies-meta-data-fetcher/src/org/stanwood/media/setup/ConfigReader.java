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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.Controller;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.rename.PatternMatcher;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is used to parse the XML configuration files. These are used to tell the
 * application which stores and sources should be used.
 * Strings in config file can contain variables which get evaulated with the configuration is read.
 */
public class ConfigReader extends BaseConfigReader {

	private final static Log log = LogFactory.getLog(ConfigReader.class);
	private final static String DEFAULT_EXTS[] = new String[] { "avi","mkv","mov","mpg","mpeg","mp4","m4v","srt","sub","divx" };

	private final static String DEFAULT_TV_FILE_PATTERN = "%sx%e - %t.%x";
	private final static String DEFAULT_FILM_FILE_PATTERN = "%t{ (%y)}{ Part %p}.%x";
	private final static String DEFAULT_XBMC_ADDON_DIR = "http://mirrors.xbmc.org/addons/dharma";

	/** The default location to store configuration */
	private static final File DEFAULT_MEDIA_CONFIG_DIR = new File(FileHelper.HOME_DIR,".mediaManager");

	private InputStream is;
	private List<MediaDirConfig>mediaDir;

	private File xbmcAddonDir;

	private Locale xbmcLocale = Locale.ENGLISH;

	private List<Plugin> plugins = new ArrayList<Plugin>();
	private File configDir;
	private File nativeFolder;

	private String xbmcAddonSite = DEFAULT_XBMC_ADDON_DIR;

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
			Document doc = XMLParser.parse(is, "MediaManager-Config-2.0.xsd");
			parseGlobal(doc);
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

				throw new ConfigException("Unknown mode '"+strMode+"' for media directory '"+dir.getAbsolutePath()+"'. Valid modes are "+Mode.modeList());
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
			String ignoreSeenValue = dirNode.getAttribute("ignoreSeen");
			boolean ignoreSeen = false;
			if (ignoreSeenValue!=null && ignoreSeenValue.length()>0) {
				ignoreSeen = Boolean.parseBoolean(ignoreSeenValue);
			}

			dirConfig.setPattern(pattern);
			dirConfig.setMode(mode);
			dirConfig.setIgnoreSeen(ignoreSeen);

			dirConfig.setSources(readSources(node));
			dirConfig.setStores(readStores(node));
			dirConfig.setActions(readActions(node));

			List<String>exts = new ArrayList<String>();
			for (Node extNode : selectNodeList(node,"extensions/extension/text()")) {
				exts.add(extNode.getNodeValue());
			}
			if (exts.size()==0) {
				for (String ext : DEFAULT_EXTS) {
					exts.add(ext);
				}
			}
			dirConfig.setExtensions(exts);

			parseIgnorePatterns(node,dirConfig);
			dirConfigs.add(dirConfig);


		}
		this.mediaDir = dirConfigs;
	}

	private void parseIgnorePatterns(Node dirNode, MediaDirConfig dirConfig) throws XMLParserException, ConfigException {
		List<Pattern>patterns = new ArrayList<Pattern>();
		for (Node node : selectNodeList(dirNode,"ignore/text()")) {
			patterns.add(Pattern.compile(node.getTextContent()));
		}
		if (patterns.size()>0) {
			dirConfig.setIgnorePatterns(patterns);
		}
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
	 * @param controller The media controller
	 * @param dirConfig The media directory configuration
	 * @return Thrown if their are any problems
	 * @throws ConfigException Thrown if their are any problems
	 */
	public List<ISource> loadSourcesFromConfigFile(Controller controller,MediaDirConfig dirConfig) throws ConfigException {
		List<ISource>sources = new ArrayList<ISource>();
		List<String>addons = new ArrayList<String>();
		List<ISource> xbmcSources = null;
		if (controller.getXBMCAddonManager()!=null) {
			xbmcSources = controller.getXBMCAddonManager().getSources();
		}
		for (SourceConfig sourceConfig : dirConfig.getSources()) {
			String sourceClass = sourceConfig.getID();
			try {
				Class<? extends ISource> c = controller.getSourceClass(sourceClass);
				if (XBMCSource.class.isAssignableFrom(c)) {
					if (sourceConfig.getParams() != null) {
						String scraper = null;
						for (String key : sourceConfig.getParams().keySet()) {
							String value = sourceConfig.getParams().get(key);
							if (key.equals("scrapers")) {
								StringTokenizer tok = new StringTokenizer(value,",");
								while (tok.hasMoreTokens()) {
									scraper = "xbmc-"+tok.nextToken();
									addons.add(scraper);
								}
							}
						}

						if (scraper == null) {
							for (ISource source : xbmcSources) {
								addons.add(source.getSourceId());
							}
						}

						for (String key : sourceConfig.getParams().keySet()) {
							if (!key.equals("scrapers")) {
								String value = sourceConfig.getParams().get(key);
								if (xbmcSources!=null) {
									for (ISource source : xbmcSources ) {
										if (scraper == null || scraper.equals(source.getSourceId())) {
											setParamOnSource( source, key, value);
										}
									}
								}
							}
						}
					}
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

		if (xbmcSources!=null) {
			Iterator<ISource>it = xbmcSources.iterator();
			while (it.hasNext()) {
				ISource source = it.next();
				if (!addons.contains(source.getSourceId())) {
					it.remove();
				}
			}
			sources.addAll(xbmcSources);
		}

		return sources;
	}

	/**
	 * Used to read the stores from the configuration file
	 * @param controller The media controller
	 * @param dirConfig The media directory configuration
	 * @return The stores
	 * @throws ConfigException Thrown if their is any problems
	 */
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
						setParamOnStore(store, key, value);
					}
				}
				stores.add(store);
			} catch (InstantiationException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			} catch (IllegalArgumentException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			} catch (StoreException e) {
				throw new ConfigException("Unable to add store '" + storeClass + "' because " + e.getMessage(),e);
			}
		}
		return stores;
	}

	/**
	 * Used to read the actions from the configuration file
	 * @param controller The media controller
	 * @param dirConfig The media directory configuration
	 * @return The actions
	 * @throws ConfigException Thrown if their is any problems
	 */
	public List<IAction> loadActionsFromConfigFile(Controller controller, MediaDirConfig dirConfig) throws ConfigException {
		List<IAction>actions = new ArrayList<IAction>();
		for (ActionConfig actionConfig : dirConfig.getActions()) {
			String actionClass = actionConfig.getID();
			try {

				Class<? extends IAction> c = controller.getActionClass(actionClass);
				IAction action = c.newInstance();
				if (actionConfig.getParams() != null) {
					for (String key : actionConfig.getParams().keySet()) {
						String value = actionConfig.getParams().get(key);
						setParamOnAction(action, key, value);
					}
				}
				actions.add(action);
			} catch (InstantiationException e) {
				throw new ConfigException("Unable to add action '" + actionClass + "' because " + e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new ConfigException("Unable to add action '" + actionClass + "' because " + e.getMessage(),e);
			} catch (IllegalArgumentException e) {
				throw new ConfigException("Unable to add action '" + actionClass + "' because " + e.getMessage(),e);
			} catch (ActionException e) {
				throw new ConfigException("Unable to add action '" + actionClass + "' because " + e.getMessage(),e);
			}
		}
		return actions;
	}



	private static void setParamOnSource(ISource source, String key, String value)
	throws  SourceException {
		source.setParameter(key, value);
	}

	private static void setParamOnStore(IStore store, String key, String value) throws StoreException {
		store.setParameter(key, value);
	}

	private static void setParamOnAction(IAction action, String key, String value)
	throws ActionException {
		action.setParameter(key, value);
	}

	/**
	 * Used to get the directory where XBMC addons are installed. If one has not been specified in the configuration, then
	 * a default on is used instead of $HOME/.mediaManager/xbmc/addons.
	 * @return The XBMC addon directory
	 * @throws ConfigException Thrown if their is a problem
	 */
	public File getXBMCAddonDir() throws ConfigException {
		if (xbmcAddonDir==null) {
			return getDefaultAddonDir();
		}
		return xbmcAddonDir;
	}

	private File getDefaultAddonDir() throws ConfigException {
		File addonDir = new File(getConfigDir(),"xbmc"+File.separator+"addons");
		if (!addonDir.exists()) {
			if (!addonDir.mkdirs() && !addonDir.exists()) {
				throw new ConfigException("Unable to create xbmc addon directory: " + addonDir);
			}
		}
		return addonDir;
	}

	/**
	 * Get the location of the media directory
	 * @return The location of the media directory
	 * @throws ConfigException Thrown if their is a problem
	 */
	public File getConfigDir() throws ConfigException {
		if (configDir == null) {
			configDir = getDefaultConfigDir();
		}
		return configDir;
	}

	/**
	 * Used to get the default location of the media manager configuration directory
	 * @return the default location of the media manager configuration directory
	 * @throws ConfigException Thrown if their is a problem
	 */
	public static File getDefaultConfigDir() throws ConfigException {
		File dir = DEFAULT_MEDIA_CONFIG_DIR;
		if (!dir.exists()) {
			if (!dir.mkdirs() && !dir.exists()) {
				throw new ConfigException ("Unable to create missing configuration directory: " + dir);
			}
		}
		return dir;
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
			String jar = parseString(pluginEl.getAttribute("jar"));
			String clazz = parseString(pluginEl.getAttribute("class"));
			plugins.add(new Plugin(jar,clazz));
		}
	}

	private void parseXBMCSettings(Node configNode) throws XMLParserException {
		Element node = (Element) selectSingleNode(configNode, "/mediaManager/XBMCAddons");
		if (node!=null) {
			String dir = parseString(node.getAttribute("directory"));
			if (dir.trim().length()>0) {
				xbmcAddonDir =FileHelper.resolveRelativePaths(new File(dir));
			}
			String locale = parseString(node.getAttribute("locale"));
			if (locale.trim().length()>0) {
				xbmcLocale = new Locale(locale);
			}
			String addonSite = parseString(node.getAttribute("addonSite"));
			if (addonSite.trim().length()>0) {
				xbmcAddonSite = addonSite;
			}
		}
	}

	private void parseGlobal(Document configNode) throws XMLParserException {
		Element node = (Element) selectSingleNode(configNode, "/mediaManager/global");
		if (node!=null) {
			try {
				String dir = parseString(getStringFromXML(node, "configDirectory/text()"));
				if (dir.trim().length()>0) {
					configDir =FileHelper.resolveRelativePaths(new File(dir));
				}
			}
			catch (XMLParserNotFoundException e) {
				// Ignore
			}
			try {
				String value = parseString(getStringFromXML(node, "native/text()"));
				if (value!=null && !value.equals("")) {
					nativeFolder = FileHelper.resolveRelativePaths(new File(value));
				}
			}
			catch (XMLParserNotFoundException e) {
				// Ignore
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
				String value = parseString(((Element)paramNode).getAttribute("value"));
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
				String value = parseString(((Element)paramNode).getAttribute("value"));
				store.addParam(name, value);
			}

			stores.add(store);
		}
		return stores;
	}

	private List<ActionConfig>readActions(Node configNode) throws XMLParserException {
		List<ActionConfig>actions = new ArrayList<ActionConfig>();
		for (Node storeElement : selectNodeList(configNode, "actions/action")) {
			ActionConfig action = new ActionConfig();
			action.setID(((Element)storeElement).getAttribute("id"));

			for (Node paramNode : selectNodeList(storeElement, "param")) {
				String name = ((Element)paramNode).getAttribute("name");
				String value = parseString(((Element)paramNode).getAttribute("value"));
				action.addParam(name, value);
			}

			actions.add(action);
		}
		return actions;
	}

	/**
	 * Used to get a list of plugins
	 * @return a list of plugins
	 */
	public List<Plugin>getPlugins() {
		return plugins;
	}

	private String parseString(String input) {
		input = input.replaceAll("\\$HOME", FileHelper.HOME_DIR.getAbsolutePath());
		return input;
	}

	/**
	 * Used to the default configuration filename
	 * @return The default configuration filename
	 * @throws ConfigException Thrown if their are any problems
	 */
	public static File getDefaultConfigFile() throws ConfigException {
		File file = new File(ConfigReader.getDefaultConfigDir(),"mediamanager-conf.xml");
		if (!file.exists()) {
			file = new File(File.separator+"etc"+File.separator+"mediamanager-conf.xml");
		}
		if (!file.exists()) {
			file = new File(ConfigReader.getDefaultConfigDir(),"mediamanager-conf.xml");
			try {
				FileHelper.copy(ConfigReader.class.getResourceAsStream("defaultConfig.xml"), file);
			} catch (IOException e) {
				throw new ConfigException("Unable to create default configuration file : " + file,e);
			}
		}
		return file;
	}

	/**
	 * Used to get a list of media directory locations
	 * @return Media directory locations
	 */
	public Collection<File> getMediaDirectiores() {
		List<File> mediaDirs = new ArrayList<File>();
		for (MediaDirConfig c : mediaDir) {
			mediaDirs.add(c.getMediaDir());
		}
		return mediaDirs;
	}

	/**
	 * Used to find the native folder. Null is returend if it could not be found
	 * @return The native folder, or null if not found
	 */
	public File getNativeFolder() {
		if (nativeFolder==null) {
			String nativeDir = System.getenv("MM_NATIVE_DIR");
			if (nativeDir!=null && nativeDir.length()>0) {
				nativeFolder = FileHelper.resolveRelativePaths(new File(nativeDir));
			}
		}
		return nativeFolder;
	}

	/**
	 * Used to get the addon site url
	 * @return the addon site url
	 */
	public String getXBMCAddonSiteUrl() {
		return xbmcAddonSite;
	}
}

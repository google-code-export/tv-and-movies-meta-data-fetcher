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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.Controller;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.rename.PatternMatcher;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.progress.SubMonitor;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.db.FileDatabaseStore;
import org.stanwood.media.store.mp4.MP4ITunesStore;
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

	/** Default file name of config file */
	public static final String CONFIG_NAME = "mediamanager-conf.xml"; //$NON-NLS-1$
	private static final String SCHEMA_NAME = "MediaManager-Config-2.2.xsd"; //$NON-NLS-1$
	private final static Log log = LogFactory.getLog(ConfigReader.class);
	private final static String DEFAULT_EXTS[] = new String[] { "avi","mkv","mov","mpg","mpeg","mp4","m4v","srt","sub","divx" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

	private final static String DEFAULT_TV_FILE_PATTERN = "%sx%e - %t.%x"; //$NON-NLS-1$
	private final static String DEFAULT_FILM_FILE_PATTERN = "%t{ (%y)}{ Part %p}.%x"; //$NON-NLS-1$
	private final static String DEFAULT_XBMC_ADDON_DIR = "http://mirrors.xbmc.org/addons/eden"; //$NON-NLS-1$

	/** The default location to store configuration */
	private static final File DEFAULT_MEDIA_CONFIG_DIR = new File(FileHelper.HOME_DIR,".mediaManager"); //$NON-NLS-1$

	private InputStream is;
	private List<MediaDirConfig>mediaDir;
	private List<WatchDirConfig> watchDirs;
	private Map<String,DBResource> databaseResources = new HashMap<String,DBResource>();

	private File xbmcAddonDir;

	private Locale xbmcLocale = Locale.ENGLISH;

	private List<Plugin> plugins = new ArrayList<Plugin>();
	private File configDir;
	private File nativeFolder;

	private String xbmcAddonSite = DEFAULT_XBMC_ADDON_DIR;
	private SeenDatabaseConfig seenDBConfig;

	/** The default strip tokens */
	public final static List<Pattern> DEFAULT_STRIP_TOKENS;
	static {
		DEFAULT_STRIP_TOKENS = new ArrayList<Pattern>();
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("dvdrip",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("xvid",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("proper",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("ac3",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("1080p",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("720p",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("Blueray",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("x264",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
		DEFAULT_STRIP_TOKENS.add(Pattern.compile("Ntsc",Pattern.CASE_INSENSITIVE));  //$NON-NLS-1$
	}

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
			if (log.isDebugEnabled()) {
				log.debug("Parsing configuration"); //$NON-NLS-1$
			}
			Document doc = XMLParser.parse(is, SCHEMA_NAME);
			parseGlobal(doc);
			parseXBMCSettings(doc);
			parseMediaDirs(doc);
			parseWatchDirs(doc);
			parsePlguins(doc);
			parseResources(doc);
			parseSeenDatabase(doc);
		} catch (XMLParserException e) {
			throw new ConfigException(Messages.getString("UNABLE_PARSE_CONFIG"),e); //$NON-NLS-1$
		}
	}

	private void parseSeenDatabase(Document doc) throws XMLParserException, ConfigException {
		if (selectSingleNode(doc,"/mediaManager/seenDatabase")!=null) { //$NON-NLS-1$
			SeenDatabaseConfig seenDBConfig = new SeenDatabaseConfig();
			for (Node node : selectNodeList(doc,"/mediaManager/seenDatabase")) { //$NON-NLS-1$
				Element seenDBNode = (Element) node;
				String resourceId = seenDBNode.getAttribute("resourceId"); //$NON-NLS-1$
				if (resourceId.length()>0) {
					seenDBConfig.setResourceId(resourceId);
				}
			}
			this.seenDBConfig = seenDBConfig;
		}
		else {
			this.seenDBConfig = null;
		}
	}

	private void writeSeenDatabase(StringBuilder document, IProgressMonitor progress) {
		if (seenDBConfig!=null) {
			document.append("  <seenDatabase"); //$NON-NLS-1$
			if (seenDBConfig.getResourceId()!=null) {
				document.append(" resourceId=\""+seenDBConfig.getResourceId()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			document.append(" />"); //$NON-NLS-1$
		}
	}

	private void writeWatchDirs(StringBuilder document, IProgressMonitor progress) throws XMLParserException {
		for (WatchDirConfig dir : watchDirs) {
			document.append("  <watchDirectory"); //$NON-NLS-1$
			document.append(" directory=\""+dir.getWatchDir().getAbsolutePath()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
			document.append(" />"); //$NON-NLS-1$
		}
	}

	private void writeMediaDirs(StringBuilder document, IProgressMonitor progress) throws XMLParserException {
		for (MediaDirConfig dir : mediaDir) {
			document.append("  <mediaDirectory"); //$NON-NLS-1$
			document.append(" directory=\""+dir.getMediaDir().getAbsolutePath()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
			document.append(" mode=\""+dir.getMode()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
			if (dir.isDefaultForMode()) {
				document.append(" default=\"true\""); //$NON-NLS-1$
			}
			document.append(" pattern=\""+dir.getPattern()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
			document.append(" ignoreSeen=\""+dir.getIgnoreSeen()+"\">"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$

			if (dir.getIgnorePatterns()!=null) {
				for (Pattern p : dir.getIgnorePatterns()) {
					document.append("    <ignore>"+p.pattern()+"</ignore>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (dir.getStripTokens()!=null) {
				if (dir.getStripTokens()!=DEFAULT_STRIP_TOKENS) {
					for (Pattern p : dir.getStripTokens()) {
						document.append("<strip>"+p.pattern()+"</strip>"+FileHelper.LS);  //$NON-NLS-1$//$NON-NLS-2$
					}
				}
			}

			if (dir.getExtensions().size()>0 && !Arrays.equals(dir.getExtensions().toArray(new String[0]),DEFAULT_EXTS)) {
				document.append("    <extensions>"+FileHelper.LS); //$NON-NLS-1$
				for (String ext : dir.getExtensions()) {
					document.append("      <extension>"+ext+"</extension>"+FileHelper.LS);  //$NON-NLS-1$//$NON-NLS-2$
				}
				document.append("    </extensions>"+FileHelper.LS); //$NON-NLS-1$
			}
			if (dir.getSources().size()>0) {
				document.append("    <sources>"+FileHelper.LS); //$NON-NLS-1$
					for (SourceConfig source : dir.getSources()) {
						document.append("      <source"); //$NON-NLS-1$
						witeBaseMediaDirSubItem(document, source);
						document.append("      </source>"); //$NON-NLS-1$
					}
				document.append("    </sources>"+FileHelper.LS); //$NON-NLS-1$
			}
			if (dir.getStores().size()>0) {
				document.append("    <stores>"+FileHelper.LS); //$NON-NLS-1$
					for (StoreConfig store : dir.getStores()) {
						document.append("      <store"); //$NON-NLS-1$
						witeBaseMediaDirSubItem(document, store);
						document.append("      </store>"); //$NON-NLS-1$
					}
				document.append("    </stores>"+FileHelper.LS); //$NON-NLS-1$
			}
			if (dir.getActions().size()>0) {
				document.append("    <actions>"+FileHelper.LS); //$NON-NLS-1$
					for (ActionConfig action : dir.getActions()) {
						document.append("      <action"); //$NON-NLS-1$
						witeBaseMediaDirSubItem(document, action);
						document.append("      </action>"); //$NON-NLS-1$
					}
				document.append("    </actions>"+FileHelper.LS); //$NON-NLS-1$
			}
			document.append("  </mediaDirectory>"+FileHelper.LS); //$NON-NLS-1$
			progress.worked(1);
		}
	}

	protected void witeBaseMediaDirSubItem(StringBuilder document,BaseMediaDirSubItem subItem) {
		document.append(" id=\""+subItem.getID()+"\"");  //$NON-NLS-1$//$NON-NLS-2$
		document.append(">"+FileHelper.LS); //$NON-NLS-1$
		if (subItem.getParams().entrySet().size()>0) {
			for (Entry<String,String>e : subItem.getParams().entrySet()) {
				document.append("        <param"); //$NON-NLS-1$
				document.append(" name=\""+e.getKey()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
				document.append(" value=\""+e.getValue()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
				document.append("/>"+FileHelper.LS); //$NON-NLS-1$
			}
		}
	}

	private void writeDBResources(StringBuilder document, SubMonitor progress) {
		if (databaseResources.size()>0) {
			document.append("  <resources>"+FileHelper.LS); //$NON-NLS-1$
			for (Entry<String,DBResource>e : databaseResources.entrySet() ) {
				DBResource resource = e.getValue();
				document.append("    <databaseResource id=\""+e.getKey()+"\">"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$

				document.append("      <url>"+resource.getUrl()+"</url>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				if (resource.getUsername()!=null) {
					document.append("      <username>"+resource.getUsername()+"</username>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (resource.getPassword()!=null) {
					document.append("      <password>"+resource.getPassword()+"</password>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				}
				document.append("      <dialect>"+resource.getDialect()+"</dialect>"+FileHelper.LS);  //$NON-NLS-1$//$NON-NLS-2$
				if (resource.getSchemaCheck()!=null) {
					document.append("      <schemaCheck>"+resource.getSchemaCheck().getValue()+"</schemaCheck>"+FileHelper.LS);  //$NON-NLS-1$//$NON-NLS-2$
				}
				document.append("    </databaseResource>"+FileHelper.LS); //$NON-NLS-1$

			}
			document.append("  </resources>"+FileHelper.LS); //$NON-NLS-1$
		}
	}

	private void parseResources(Document doc) throws XMLParserException, ConfigException {
		if (log.isDebugEnabled()) {
			log.debug("Parsing resources"); //$NON-NLS-1$
		}
		for (Node node : selectNodeList(doc,"/mediaManager/resources/databaseResource")) { //$NON-NLS-1$

			Element dbRsourceNode = (Element) node;
			DBResource dbResource = new DBResource();
			String id = dbRsourceNode.getAttribute("id"); //$NON-NLS-1$
			if (log.isDebugEnabled()) {
				log.debug("Parsing database resource resources: " + id); //$NON-NLS-1$
			}
			if (id.length()==0) {
				throw new ConfigException(Messages.getString("ConfigReader.DATABASE_ID_EMPTY")); //$NON-NLS-1$
			}
			if (databaseResources.containsKey(id)){
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.DATABASE_ID_NOT_UNIQUE"), id)); //$NON-NLS-1$
			}
			dbResource.setDialect(getStringFromXML(dbRsourceNode, "dialect/text()")); //$NON-NLS-1$
			dbResource.setUsername(getStringFromXMLOrNull(dbRsourceNode, "username/text()")); //$NON-NLS-1$
			dbResource.setPassword(getStringFromXMLOrNull(dbRsourceNode, "password/text()")); //$NON-NLS-1$
			dbResource.setUrl(getStringFromXML(dbRsourceNode, "url/text()")); //$NON-NLS-1$
			dbResource.setResourceId(id);
			String schemaCheck = getStringFromXMLOrNull(dbRsourceNode, "schemaCheck/text()"); //$NON-NLS-1$
			if (schemaCheck!=null) {
				SchemaCheck sc = SchemaCheck.fromValue(schemaCheck);
				if (sc==null) {
					throw new ConfigException(MessageFormat.format("Invalid schemaCheck value {0}, possible values are validate and none",schemaCheck));
				}
				dbResource.setSchemaCheck(sc);
			}
			databaseResources.put(id,dbResource);
		}
	}

	private void parseMediaDirs(Document doc) throws XMLParserException, ConfigException {
		List<MediaDirConfig>dirConfigs = new ArrayList<MediaDirConfig>();
		for (Node node : selectNodeList(doc,"/mediaManager/mediaDirectory")) { //$NON-NLS-1$
			Element dirNode = (Element) node;
			MediaDirConfig dirConfig = new MediaDirConfig();
			File dir = new File(parseString(dirNode.getAttribute("directory"))); //$NON-NLS-1$
			if (!dir.exists()) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_FIND_ROOT_MEDIA_DIR"),dir.getAbsolutePath())); //$NON-NLS-1$
			}
			dirConfig.setMediaDir(dir);
			boolean defaultForMode = false;
			if (dirNode.getAttribute("default").equals("true")) {  //$NON-NLS-1$//$NON-NLS-2$
				defaultForMode = true;
			}
			dirConfig.setDefaultForMode(defaultForMode);

			String strMode = dirNode.getAttribute("mode").toUpperCase(); //$NON-NLS-1$
			Mode mode ;
			try {
				mode = Mode.valueOf(strMode);
			}
			catch (IllegalArgumentException e) {

				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.KNOWN_MODE"),strMode,dir.getAbsolutePath(),Mode.modeList())); //$NON-NLS-1$
			}

			String pattern = dirNode.getAttribute("pattern").trim(); //$NON-NLS-1$
			if (pattern.length()==0) {
				pattern = DEFAULT_TV_FILE_PATTERN;
				if (mode == Mode.FILM) {
					pattern = DEFAULT_FILM_FILE_PATTERN;
				}
				log.warn(MessageFormat.format(Messages.getString("ConfigReader.NO_PATTERN"),pattern)); //$NON-NLS-1$
			}
			else {
				if (!PatternMatcher.validPattern(pattern)) {
					throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.INVALID_PATTERN"),pattern,dir.getAbsolutePath())); //$NON-NLS-1$
				}
			}
			String ignoreSeenValue = dirNode.getAttribute("ignoreSeen"); //$NON-NLS-1$
			boolean ignoreSeen = false;
			if (ignoreSeenValue!=null && ignoreSeenValue.length()>0) {
				ignoreSeen = Boolean.parseBoolean(ignoreSeenValue);
			}

			String name = dirNode.getAttribute("name").trim(); //$NON-NLS-1$
			if (name.equals("")) { //$NON-NLS-1$
				name = null;
			}

			dirConfig.setName(name);
			dirConfig.setPattern(pattern);
			dirConfig.setMode(mode);
			dirConfig.setIgnoreSeen(ignoreSeen);

			dirConfig.setSources(readSources(node,mode));
			dirConfig.setStores(readStores(node,mode));
			dirConfig.setActions(readActions(node,mode));

			List<String>exts = new ArrayList<String>();
			for (Node extNode : selectNodeList(node,"extensions/extension/text()")) { //$NON-NLS-1$
				exts.add(extNode.getNodeValue());
			}
			if (exts.size()==0) {
				for (String ext : DEFAULT_EXTS) {
					exts.add(ext);
				}
			}
			dirConfig.setExtensions(exts);

			parseIgnorePatterns(node,dirConfig);
			parseStripPatterns(node,dirConfig);
			dirConfigs.add(dirConfig);


		}
		this.mediaDir = dirConfigs;
	}

	private void parseWatchDirs(Document doc) throws XMLParserException, ConfigException {
		List<WatchDirConfig>watchDirs = new ArrayList<WatchDirConfig>();
		for (Node node : selectNodeList(doc,"/mediaManager/watchDirectory")) { //$NON-NLS-1$
			Element dirNode = (Element) node;
			WatchDirConfig dirConfig = new WatchDirConfig();
			File dir = new File(parseString(dirNode.getAttribute("directory"))); //$NON-NLS-1$
			if (!dir.exists()) {
				throw new ConfigException(MessageFormat.format("Unable to find watch directory ''{0}''",dir.getAbsolutePath())); //$NON-NLS-1$
			}
			dirConfig.setWatchDir(dir);
			watchDirs.add(dirConfig);
		}
		this.watchDirs = watchDirs;
	}

	private void parseStripPatterns(Node dirNode, MediaDirConfig dirConfig) throws XMLParserException {
		List<Pattern>stripTokens = new ArrayList<Pattern>();
		if (selectSingleNode(dirNode,"strip/text()")!=null) { //$NON-NLS-1$
			for (Node node : selectNodeList(dirNode,"strip/text()")) { //$NON-NLS-1$
				stripTokens.add(Pattern.compile(node.getTextContent()));
			}
			dirConfig.setStripTokens(stripTokens);
		}
		else {
			dirConfig.setStripTokens(DEFAULT_STRIP_TOKENS);
		}
	}

	private void parseIgnorePatterns(Node dirNode, MediaDirConfig dirConfig) throws XMLParserException, ConfigException {
		List<Pattern>patterns = new ArrayList<Pattern>();
		for (Node node : selectNodeList(dirNode,"ignore/text()")) { //$NON-NLS-1$
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
		throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_FIND_MEDIA_DIR"),directory)); //$NON-NLS-1$
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
		int num = 0;
		for (SourceConfig sourceConfig : dirConfig.getSources()) {
			String id = sourceConfig.getID();
			try {
				ExtensionInfo<? extends ISource> sourceInfo = controller.getSourceInfo(id);
				if (sourceInfo==null) {
					throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_FIND_SOURCE"),id)); //$NON-NLS-1$
				}
				ISource source = sourceInfo.getExtension(dirConfig,num++);
				if (sourceConfig.getParams() != null) {
					for (String key : sourceConfig.getParams().keySet()) {
						String value = sourceConfig.getParams().get(key);
						setParamOnSource( source, key, value);

					}
				}
				sources.add(source);
			} catch (ExtensionException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_ADD_SOURCE"),id),e); //$NON-NLS-1$
			}
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
		int num = 0;
		List<IStore>stores = new ArrayList<IStore>();
		for (StoreConfig storeConfig : dirConfig.getStores()) {
			String id = storeConfig.getID();
			try {
				ExtensionInfo<? extends IStore> storeInfo = controller.getStoreInfo(id);
				if (storeInfo==null) {
					throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_FIND_STORE"),id)); //$NON-NLS-1$
				}
				IStore store = storeInfo.getExtension(dirConfig,num++);
				if (storeConfig.getParams() != null) {
					for (String key : storeConfig.getParams().keySet()) {
						String value = storeConfig.getParams().get(key);
						setParamOnStore(store, key, value);
					}
				}
				stores.add(store);
			} catch (IllegalArgumentException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_ADD_STORE"),id),e); //$NON-NLS-1$
			} catch (ExtensionException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_ADD_STORE"),id),e); //$NON-NLS-1$
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
		int num = 0;
		for (ActionConfig actionConfig : dirConfig.getActions()) {
			String id = actionConfig.getID();
			try {
				ExtensionInfo<? extends IAction> actionInfo = controller.getActionInfo(id);
				if (actionInfo==null) {
					throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_FIND_ACTION"),id)); //$NON-NLS-1$
				}
				IAction action = actionInfo.getExtension(dirConfig,num++);
				if (actionConfig.getParams() != null) {
					for (String key : actionConfig.getParams().keySet()) {
						String value = actionConfig.getParams().get(key);
						setParamOnAction(action, key, value);
					}
				}
				actions.add(action);
			} catch (IllegalArgumentException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_ADD_ACTION"),id),e); //$NON-NLS-1$
			} catch (ExtensionException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_ADD_ACTION"),id),e); //$NON-NLS-1$
			}
		}
		return actions;
	}



	private void setParamOnSource(ISource source, String key, String value)
	throws  SourceException {
		source.setParameter(key, value);
	}

	private void setParamOnStore(IStore store, String key, String value) throws StoreException {
		store.setParameter(key, value);
	}

	private void setParamOnAction(IAction action, String key, String value)
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
		File addonDir = new File(getConfigDir(),"xbmc"+File.separator+"addons"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!addonDir.exists()) {
			if (!addonDir.mkdirs() && !addonDir.exists()) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNABLE_CREATE_XBMC_ADDON_DIR") ,addonDir)); //$NON-NLS-1$
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
				throw new ConfigException (MessageFormat.format(Messages.getString("ConfigReader.UNABLE_CREATE_CONFIG_DIR"),dir)); //$NON-NLS-1$
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

	private void writePlugins(StringBuilder document) {
		if (plugins.size()>0) {
			document.append("  <plugins>"+FileHelper.LS); //$NON-NLS-1$
			for (Plugin plugin : plugins) {
				if (plugin.getJar()!=null) {
					document.append("    <plugin jar=\""+plugin.getJar()+"\" class=\""+plugin.getPluginClass()+"\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				else {
					document.append("    <plugin class=\""+plugin.getPluginClass()+"\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			document.append("  </plugins>"+FileHelper.LS); //$NON-NLS-1$
		}
	}

	private void parsePlguins(Node doc) throws XMLParserException {
		for(Node n : selectNodeList(doc, "/mediaManager/plugins/plugin")) { //$NON-NLS-1$
			Element pluginEl = (Element)n;
			String strJar = pluginEl.getAttribute("jar"); //$NON-NLS-1$
			String jar = null;
			if (strJar.length()>0) {
				jar = parseString(strJar);
			}
			String clazz = parseString(pluginEl.getAttribute("class")); //$NON-NLS-1$
			plugins.add(new Plugin(jar,clazz));
		}
	}

	private void parseXBMCSettings(Node configNode) throws XMLParserException {
		Element node = (Element) selectSingleNode(configNode, "/mediaManager/XBMCAddons"); //$NON-NLS-1$
		if (node!=null) {
			String dir = parseString(node.getAttribute("directory")); //$NON-NLS-1$
			if (dir.trim().length()>0) {
				xbmcAddonDir =FileHelper.resolveRelativePaths(new File(dir));
			}
			String locale = parseString(node.getAttribute("locale")); //$NON-NLS-1$
			if (locale.trim().length()>0) {
				xbmcLocale = new Locale(locale);
			}
			String addonSite = parseString(node.getAttribute("addonSite")); //$NON-NLS-1$
			if (addonSite.trim().length()>0) {
				xbmcAddonSite = addonSite;
			}
		}
	}

	private void writeXBMCSettings(StringBuilder document) throws ConfigException {
		StringBuilder subDoc = new StringBuilder();
		if (xbmcAddonDir!=null) {
			subDoc.append(" directory=\""+xbmcAddonDir+"\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!xbmcLocale.equals(Locale.ENGLISH)) {
			subDoc.append(" locale=\""+xbmcLocale.getLanguage()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!xbmcAddonSite.equals(DEFAULT_XBMC_ADDON_DIR)) {
			subDoc.append(" addonSite=\""+xbmcAddonSite+"\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (subDoc.length()>0) {
			document.append("  <XBMCAddons"); //$NON-NLS-1$
			document.append(subDoc);
			document.append("/>"+FileHelper.LS); //$NON-NLS-1$
		}
	}


	private void parseGlobal(Document configNode) throws XMLParserException {
		Element node = (Element) selectSingleNode(configNode, "/mediaManager/global"); //$NON-NLS-1$
		if (node!=null) {
			try {
				String dir = parseString(getStringFromXML(node, "configDirectory/text()")); //$NON-NLS-1$
				if (dir.trim().length()>0) {
					configDir =FileHelper.resolveRelativePaths(new File(dir));
				}
			}
			catch (XMLParserNotFoundException e) {
				// Ignore
			}
			try {
				String value = parseString(getStringFromXML(node, "native/text()")); //$NON-NLS-1$
				if (value!=null && !value.equals("")) { //$NON-NLS-1$
					nativeFolder = FileHelper.resolveRelativePaths(new File(value));
				}
			}
			catch (XMLParserNotFoundException e) {
				// Ignore
			}
		}
	}

	private void writeGlobalSettings(StringBuilder document)
	throws ConfigException {
		StringBuilder subDoc = new StringBuilder();
		if (configDir!=null && !configDir.equals(getDefaultConfigDir())) {
			subDoc.append("    <configDirectory>"+configDir.getAbsolutePath()+"</configDirectory>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (nativeFolder!=null) {
			subDoc.append("    <native>"+nativeFolder.getAbsolutePath()+"</native>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (subDoc.length()>0) {
			document.append("  <global>"+FileHelper.LS); //$NON-NLS-1$
			document.append(subDoc);
			document.append("  </global>"+FileHelper.LS); //$NON-NLS-1$
		}
	}

	private List<SourceConfig> readSources(Node configNode,Mode mode) throws XMLParserException {
		int num = 0;
		List<SourceConfig> sources = new ArrayList<SourceConfig>();
		if (selectSingleNode(configNode, "sources") == null) { //$NON-NLS-1$
			if (mode==Mode.FILM) {
				SourceConfig config = new SourceConfig();
				config.setNumber(num++);
				config.setID(XBMCSource.class.getName()+"#metadata.themoviedb.org"); //$NON-NLS-1$
				sources.add(config);
				config = new SourceConfig();
				config.setNumber(num++);
				config.setID(XBMCSource.class.getName()+"#metadata.imdb.com"); //$NON-NLS-1$
				sources.add(config);
			}
			else {
				SourceConfig config = new SourceConfig();
				config.setNumber(num++);
				config.setID(XBMCSource.class.getName()+"#metadata.tvdb.com"); //$NON-NLS-1$
				sources.add(config);
			}
		}
		else {
			for (Node sourceElement : selectNodeList(configNode, "sources/source")) { //$NON-NLS-1$
				SourceConfig source = new SourceConfig();
				source.setNumber(num);
				source.setID(((Element)sourceElement).getAttribute("id")); //$NON-NLS-1$
				for (Node paramNode : selectNodeList(sourceElement, "param")) { //$NON-NLS-1$
					String name = ((Element)paramNode).getAttribute("name"); //$NON-NLS-1$
					String value = parseString(((Element)paramNode).getAttribute("value")); //$NON-NLS-1$
					source.addParam(name, value);
				}

				sources.add(source);
				num++;
			}
		}
		return sources;
	}

	private List<StoreConfig>readStores(Node configNode,Mode mode) throws XMLParserException {
		int num = 0;
		List<StoreConfig>stores = new ArrayList<StoreConfig>();
		if (selectSingleNode(configNode, "stores") == null) { //$NON-NLS-1$
			StoreConfig config = new StoreConfig();
			config.setNumber(num++);
			config.setID(MP4ITunesStore.class.getName());
			stores.add(config);

			config = new StoreConfig();
			config.setNumber(num++);
			config.setID(FileDatabaseStore.class.getName());
			stores.add(config);
		}
		else {
			for (Node storeElement : selectNodeList(configNode, "stores/store")) { //$NON-NLS-1$
				StoreConfig store = new StoreConfig();
				store.setNumber(num);
				store.setID(((Element)storeElement).getAttribute("id")); //$NON-NLS-1$

				for (Node paramNode : selectNodeList(storeElement, "param")) { //$NON-NLS-1$
					String name = ((Element)paramNode).getAttribute("name"); //$NON-NLS-1$
					String value = parseString(((Element)paramNode).getAttribute("value")); //$NON-NLS-1$
					store.addParam(name, value);
				}

				stores.add(store);
				num++;
			}
		}
		return stores;
	}

	private List<ActionConfig>readActions(Node configNode,Mode mode) throws XMLParserException {
		int num = 0;
		List<ActionConfig>actions = new ArrayList<ActionConfig>();
		if (selectSingleNode(configNode, "actions") == null) { //$NON-NLS-1$
			ActionConfig config = new ActionConfig();
			config.setNumber(num++);
			config.setID(RenameAction.class.getName());
			actions.add(config);
		}
		else {
			for (Node storeElement : selectNodeList(configNode, "actions/action")) { //$NON-NLS-1$
				ActionConfig action = new ActionConfig();
				action.setNumber(num);
				action.setID(((Element)storeElement).getAttribute("id")); //$NON-NLS-1$

				for (Node paramNode : selectNodeList(storeElement, "param")) { //$NON-NLS-1$
					String name = ((Element)paramNode).getAttribute("name"); //$NON-NLS-1$
					String value = parseString(((Element)paramNode).getAttribute("value")); //$NON-NLS-1$
					action.addParam(name, value);
				}

				actions.add(action);
				num++;
			}
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
		input = input.replaceAll("\\$HOME", FileHelper.HOME_DIR.getAbsolutePath()); //$NON-NLS-1$
		return input;
	}

	/**
	 * Used to the default configuration filename
	 * @return The default configuration filename
	 * @throws ConfigException Thrown if their are any problems
	 */
	public static File getDefaultConfigFile() throws ConfigException {
		File file = new File(ConfigReader.getDefaultConfigDir(),CONFIG_NAME);
		if (!file.exists()) {
			file = new File(File.separator+"etc"+File.separator+CONFIG_NAME); //$NON-NLS-1$
		}
		if (!file.exists()) {
			file = new File(ConfigReader.getDefaultConfigDir(),CONFIG_NAME);
			try {
				FileHelper.copy(ConfigReader.class.getResourceAsStream("defaultConfig.xml"), file); //$NON-NLS-1$
			} catch (IOException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("ConfigReader.UNALBE_CREATE_CONFIG_FILE"),file),e); //$NON-NLS-1$
			}
		}
		return file;
	}

	/**
	 * Used to get a list of media directory locations
	 * @return Media directory locations
	 */
	public Collection<File> getMediaDirectories() {
		List<File> mediaDirs = new ArrayList<File>();
		for (MediaDirConfig c : mediaDir) {
			mediaDirs.add(c.getMediaDir());
		}
		return mediaDirs;
	}

	/**
	 * Used to get the watched directory configuration information
	 * @return The watched directories
	 */
	public Collection<WatchDirConfig> getWatchDirectories() {
		List<WatchDirConfig> mediaDirs = new ArrayList<WatchDirConfig>();
		for (WatchDirConfig c : watchDirs) {
			mediaDirs.add(c);
		}
		return mediaDirs;
	}

	/**
	 * Used to find the native folder. Null is returend if it could not be found
	 * @return The native folder, or null if not found
	 */
	public File getNativeFolder() {
		if (nativeFolder==null) {
			String nativeDir = System.getenv("MM_NATIVE_DIR"); //$NON-NLS-1$
			if (nativeDir!=null && nativeDir.length()>0) {
				return FileHelper.resolveRelativePaths(new File(nativeDir));
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

	/**
	 * Write the configuration to a file
	 * @param monitor The progress monitor
	 * @param file File to save the configuration to
	 * @throws ConfigException Thrown if their is a problem
	 */
	public void writeConfig(IProgressMonitor monitor,File file) throws  ConfigException {
		SubMonitor progress = SubMonitor.convert(monitor, mediaDir.size()+7);

		try {
			StringBuilder document = new StringBuilder();
			document.append("<mediaManager>"+FileHelper.LS); //$NON-NLS-1$
			writePlugins(document);
			progress.worked(1);
			writeXBMCSettings(document);
			progress.worked(1);
			writeGlobalSettings(document);
			progress.worked(1);
			writeMediaDirs(document,progress);
			progress.worked(1);
			writeWatchDirs(document,progress);
			progress.worked(1);
			writeDBResources(document,progress);
			progress.worked(1);
			writeSeenDatabase(document,progress);
			document.append("</mediaManager>"+FileHelper.LS); //$NON-NLS-1$
			Document doc = XMLParser.strToDom(document.toString(),SCHEMA_NAME);
			XMLParser.writeXML(file, doc);
			progress.worked(1);
		}
		catch (Exception e) {
			throw new ConfigException(Messages.getString("ConfigReader.UNABLE_WRITE_CONFIG"),e); //$NON-NLS-1$
		}
		finally {
			progress.done();
		}

	}

	/**
	 * Used to get the database resources
	 * @return the database resources
	 */
	public Map<String,DBResource> getDatabaseResources() {
		return databaseResources;
	}

	/**
	 * Used to get the seen database configuration. If none configured, then this returns NULL
	 * @return The seen database configuration
	 */
	public SeenDatabaseConfig getSeenDatabase() {
		return seenDBConfig;
	}
}

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
package org.stanwood.media;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.command.ExecuteSystemCommandActionInfo;
import org.stanwood.media.actions.podcast.PodCastActionInfo;
import org.stanwood.media.actions.rename.RenameActionInfo;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.setup.Plugin;
import org.stanwood.media.setup.WatchDirConfig;
import org.stanwood.media.source.HybridFilmSourceInfo;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.TagChimpSourceInfo;
import org.stanwood.media.source.xbmc.XBMCAddon;
import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.source.xbmc.XBMCSourceInfo;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.SapphireStoreInfo;
import org.stanwood.media.store.memory.MemoryStoreInfo;
import org.stanwood.media.store.mp4.MP4ITunesStoreInfo;
import org.stanwood.media.store.xmlstore.XMLStore2Info;

/**
 * The controller is used to control access to the stores and and sources. This
 * is a singleton class, and just first be setup using the @see
 * initWithDefaults() or @see initFromConfigFile() methods. From then on,
 * getInstance() can be called to a access the methods used to control stores
 * and sources.
 */

//TODO make all the extensions be added as though they were plugins
//TODO don't pass out the Classes, instead use the info classes of extensions
public class Controller {

	private final static Log log = LogFactory.getLog(Controller.class);

	private ConfigReader configReader = null;

	private Map<File, MediaDirectory> mediaDirs = new HashMap<File, MediaDirectory>();

	private List<ExtensionInfo<? extends ISource>> pluginSources = new ArrayList<ExtensionInfo<? extends ISource>>();
	private List<ExtensionInfo<? extends IStore>> pluginStores = new ArrayList<ExtensionInfo<? extends IStore>>();
	private List<ExtensionInfo<? extends IAction>> pluginActions = new ArrayList<ExtensionInfo<? extends IAction>>();

	private boolean testMode;

	private static XBMCAddonManager xbmcMgr;

	/**
	 * The constructor
	 *
	 * @param config The parsed configuration
	 */
	public Controller(ConfigReader config) {
		this.configReader = config;
	}

	/**
	 * Used to setup the controller ready for use
	 * @param testMode If true then test mode is active and no changes are to be written to disk
	 * @throws ConfigException Thrown if their is a problem reading the configuration
	 */
	public void init(boolean testMode) throws ConfigException {
		if (xbmcMgr == null) {
			try {
				setXBMCAddonManager(new XBMCAddonManager(configReader));
				if (getXBMCAddonManager().isFirstTime()) {
					getXBMCAddonManager().getUpdater().update( new IConsole() {
						@Override
						public void error(String error) {
							log.error(error);
						}

						@Override
						public void info(String info) {
							log.info(info);
						}
					});
				}
				// xbmcMgr.getUpdater().update();
			} catch (XBMCException e) {
				log.error(e.getMessage(), e);
			}
		}
		if (xbmcMgr == null) {
			log.fatal(Messages.getString("Controller.UNABLE_TO_READ_XBMC_ADDONS")); //$NON-NLS-1$
			System.exit(2);
		}
		this.testMode = testMode;
		registerInbuild();
		registerPlugins();
	}

	private void registerInbuild() throws ConfigException {
		pluginSources.add(new TagChimpSourceInfo());
		pluginSources.add(new HybridFilmSourceInfo());

		try {
			XBMCAddonManager mgr = getXBMCAddonManager();
			for (String addonId : getXBMCAddonManager().listAddons()) {
				XBMCAddon addon = mgr.getAddon(addonId);
				if (addon.hasScrapers()) {
					pluginSources.add(new XBMCSourceInfo(mgr,addon));
				}
			}
		}
		catch (XBMCException e) {
			throw new ConfigException(Messages.getString("Controller.UNABLE_REGISTER_ADDONS"),e); //$NON-NLS-1$
		}

		pluginStores.add(new SapphireStoreInfo());
		pluginStores.add(new MemoryStoreInfo());
		pluginStores.add(new MP4ITunesStoreInfo());
		pluginStores.add(new XMLStore2Info());

		pluginActions.add(new ExecuteSystemCommandActionInfo());
		pluginActions.add(new PodCastActionInfo());
		pluginActions.add(new RenameActionInfo());
	}

	@SuppressWarnings("unchecked")
	private void registerPlugins() throws ConfigException {
		for (Plugin plugin : configReader.getPlugins()) {
			try {
				Class<?> clazz;
				if (plugin.getJar()!=null) {
					URL url = new URL("jar:file:"+plugin.getJar()+"!/");  //$NON-NLS-1$//$NON-NLS-2$
					URLClassLoader clazzLoader = new URLClassLoader(new URL[]{url});
					clazz=clazzLoader.loadClass(plugin.getPluginClass());
				}
				else {
					clazz = Class.forName(plugin.getPluginClass());
				}
				Class<? extends ExtensionInfo<?>> targetClass = (Class<? extends ExtensionInfo<?>>) clazz.asSubclass(ExtensionInfo.class);
				ExtensionInfo<?> info = targetClass.newInstance();
				registerExtension(info);
			}
			catch (MalformedURLException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_REGISTER_PLUGIN"),plugin.toString()),e); //$NON-NLS-1$
			} catch (ClassNotFoundException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_REGISTER_PLUGIN"),plugin.toString()),e); //$NON-NLS-1$
			} catch (InstantiationException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_REGISTER_PLUGIN"),plugin.toString()),e); //$NON-NLS-1$
			} catch (IllegalAccessException e) {
				throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_REGISTER_PLUGIN"),plugin.toString()),e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Used to register a extension. Extensions can be sources, stores or actions.
	 * @param info The extension information
	 */
	@SuppressWarnings("unchecked")
	public void registerExtension(ExtensionInfo<?> info) {
		if (info.getType()== ExtensionType.SOURCE) {
			pluginSources.add((ExtensionInfo<ISource>) info);
		}
		if (info.getType()== ExtensionType.STORE ) {
			pluginStores.add((ExtensionInfo<IStore>) info);
		}
		if (info.getType()== ExtensionType.ACTION ) {
			pluginActions.add((ExtensionInfo<IAction>) info);
		}
	}

	/**
	 * Used to set the addon manager. Mostly used by tests
	 *
	 * @param xbmcAddonManager The addon manager
	 */
	public static void setXBMCAddonManager(XBMCAddonManager xbmcAddonManager) {
		xbmcMgr = xbmcAddonManager;
	}

	/**
	 * Used to get the addon manager
	 *
	 * @return The addon manager
	 */
	public XBMCAddonManager getXBMCAddonManager() {
		return xbmcMgr;
	}

	/**
	 * Used to convert a media directory location into the media directory object
	 * @param mediaDir The location of a media directory
	 * @return The media directory
	 * @throws ConfigException Thrown if their is a problem reading the configuration
	 */
	public MediaDirectory getMediaDirectory(File mediaDir) throws ConfigException {
		MediaDirectory dir = mediaDirs.get(mediaDir);
		if (dir == null) {
			dir = new MediaDirectory(this, configReader, mediaDir);
			mediaDirs.put(mediaDir, dir);
		}
		return dir;
	}

	/**
	 * Used to get a list of media directory locations
	 * @return Media directory locations
	 */
	public Collection<File> getMediaDirectories() {
		return configReader.getMediaDirectories();
	}

	/**
	 * Used to get a list of watch directory information
	 * @return list of watch directory information
	 */
	public Collection<WatchDirConfig> getWatchDirectories() {
		return configReader.getWatchDirectories();
	}

	/**
	 * Used to get information about a source
	 * @param id The source id
	 * @return The extension information
	 */
	public ExtensionInfo<? extends ISource> getSourceInfo(String id) {
		for (ExtensionInfo<? extends ISource> sourceInfo : pluginSources) {
			if (sourceInfo.getId().equals(id)) {
				return sourceInfo;
			}
		}
		return null;
	}

	/**
	 * Used to get a list of possible sources that can be used with a media directory. This
	 * includes any that have been registered via plugins.
	 * @return The list of sources.
	 */
	public List<ExtensionInfo<? extends ISource>> getAvalibaleSources() {
		List<ExtensionInfo<? extends ISource>> result = new ArrayList<ExtensionInfo<? extends ISource>>();

		for (ExtensionInfo<? extends ISource> info : pluginSources) {
			result.add(info);
		}

		return result;
	}

	/**
	 * Used to get a list of possible stores that can be used with a media directory. This
	 * includes any that have been registered via plugins.
	 * @return The list of sources.
	 */
	public List<ExtensionInfo<? extends IStore>> getAvalibaleStores() {
		List<ExtensionInfo<? extends IStore>> result = new ArrayList<ExtensionInfo<? extends IStore>>();

		for (ExtensionInfo<? extends IStore> info : pluginStores) {
			result.add(info);
		}

		return result;
	}

	/**
	 * Used to get a list of possible actions that can be used with a media directory. This
	 * includes any that have been registered via plugins.
	 * @return The list of sources.
	 */
	public List<ExtensionInfo<? extends IAction>> getAvalibaleActions() {
		List<ExtensionInfo<? extends IAction>> result = new ArrayList<ExtensionInfo<? extends IAction>>();

		for (ExtensionInfo<? extends IAction> info : pluginActions) {
			result.add(info);
		}

		return result;
	}

	/**
	 * Used to get information about a store
	 * @param id The store id
	 * @return The extension information
	 */
	public ExtensionInfo<? extends IStore> getStoreInfo(String id) {
		for (ExtensionInfo<? extends IStore> storeInfo : pluginStores) {
			if (storeInfo.getId().equals(id)) {
				return storeInfo;
			}
		}
		return null;
	}

	/**
	 * Used to get information about a action
	 * @param id The action id
	 * @return The extension information
	 */
	public ExtensionInfo<? extends IAction> getActionInfo(String id) {
		for (ExtensionInfo<? extends IAction> actionInfo : pluginActions) {
			if (actionInfo.getId().equals(id)) {
				return actionInfo;
			}
		}
		return null;
	}

	/**
	 * Used to find out if test mode is been used. Test mode means that changes are not
	 * to be written to disk
	 * @return True if test mode, otherwise false
	 */
	public boolean isTestRun() {
		return testMode;
	}

	/**
	 * Used to find the native folder. Null is returend if it could not be found
	 * @return The native folder, or null if not found
	 */
	public File getNativeFolder() {
		return configReader.getNativeFolder();
	}

	/**
	 * Get the location of the media directory
	 * @return The location of the media directory
	 * @throws ConfigException Thrown if their is a problem
	 */
	public File getConfigDir() throws ConfigException {
		return configReader.getConfigDir();
	}

	/**
	 * Used to get the default source information
	 * @param mode The mode to look for the source in
	 * @return The default source
	 * @throws ConfigException Thrown if unable to find a default source
	 */
	public ExtensionInfo<? extends ISource> getDefaultSource(Mode mode) throws ConfigException {
		try {
			ExtensionInfo<? extends ISource> info = getSourceInfo(XBMCSource.class.getName()+"#"+xbmcMgr.getDefaultAddonID(mode)); //$NON-NLS-1$
			if (info==null){
				throw new ConfigException(Messages.getString("Controller.UNABLE_FIND_DEFAULT_SOURCE")); //$NON-NLS-1$
			}
			return info;
		} catch (XBMCException e) {
			throw new ConfigException(Messages.getString("Controller.UNABLE_FIND_DEFAULT_SOURCE"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get a list of media directories of a given type
	 * @param type The type
	 * @return The list of media Directories
	 * @throws ConfigException Thrown if their is a problem reading the config
	 */
	public List<MediaDirectory>getMediaDirectories(Mode type) throws ConfigException {
		List<MediaDirectory>mediaDirs = new ArrayList<MediaDirectory>();
		for (File mediaDirLoc :  getMediaDirectories()) {
			MediaDirectory mediaDir = getMediaDirectory(mediaDirLoc);
			if (mediaDir.getMediaDirConfig().getMode()==type) {
				mediaDirs.add(mediaDir);
			}
		}
		return mediaDirs;
	}

}

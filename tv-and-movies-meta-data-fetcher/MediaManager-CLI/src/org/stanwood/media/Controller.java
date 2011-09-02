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
import org.stanwood.media.actions.command.ExecuteSystemCommandAction;
import org.stanwood.media.actions.podcast.PodCastAction;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.setup.Plugin;
import org.stanwood.media.source.HybridFilmSource;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.TagChimpSource;
import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.SapphireStore;
import org.stanwood.media.store.memory.MemoryStore;
import org.stanwood.media.store.mp4.MP4ITunesStore;
import org.stanwood.media.store.xmlstore.XMLStore2;

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

	private List<ExtensionInfo<ISource>> pluginSources = new ArrayList<ExtensionInfo<ISource>>();
	private List<ExtensionInfo<IStore>> pluginStores = new ArrayList<ExtensionInfo<IStore>>();
	private List<ExtensionInfo<IAction>> pluginActions = new ArrayList<ExtensionInfo<IAction>>();

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
		registerPlugins();
	}

	@SuppressWarnings("unchecked")
	private void registerPlugins() throws ConfigException {
		for (Plugin plugin : configReader.getPlugins()) {
			try {
				URL url = new URL("jar:file:"+plugin.getJar()+"!/");  //$NON-NLS-1$//$NON-NLS-2$
				URLClassLoader clazzLoader = new URLClassLoader(new URL[]{url});
				Class<?> clazz = clazzLoader.loadClass(plugin.getPluginClass());
				Class<? extends ExtensionInfo<?>> targetClass = (Class<? extends ExtensionInfo<?>>) clazz.asSubclass(ExtensionInfo.class);
				ExtensionInfo<?> info = targetClass.newInstance();
				if (ISource.class.isAssignableFrom(info.getExtension())) {
					pluginSources.add((ExtensionInfo<ISource>) info);
				}
				if (IStore.class.isAssignableFrom(info.getExtension()) ) {
					pluginStores.add((ExtensionInfo<IStore>) info);
				}
				if (IAction.class.isAssignableFrom(info.getExtension()) ) {
					pluginActions.add((ExtensionInfo<IAction>) info);
				}

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
	public Collection<File> getMediaDirectiores() {
		return configReader.getMediaDirectiores();
	}

	/**
	 * Used to get the class of a source. This can handle getting the class if
	 * the source is in a plugin
	 * @param className The name of the class
	 * @return The class object
	 * @throws ConfigException Thrown if their are any problems
	 */
	public Class<? extends ISource> getSourceClass(String className) throws ConfigException {
		for (ExtensionInfo<ISource> info : pluginSources) {
			if (info.getExtension().getName().equals(className)) {
				return info.getExtension();
			}
		}
		try {
			Class<? extends ISource> c = Class.forName(className).asSubclass(ISource.class);
			return c;
		} catch (ClassNotFoundException e) {
			throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_ADD_SOURCE_NOT_FOUND"),className), e); //$NON-NLS-1$
		}

	}

	/**
	 * Used to get a list of possible sources that can be used with a media directory. This
	 * includes any that have been registered via plugins.
	 * @return The list of sources.
	 */
	public List<Class<? extends ISource>> getAvalibaleSources() {
		List<Class<? extends ISource>> result = new ArrayList<Class<? extends ISource>>();
		result.add(TagChimpSource.class);
		result.add(HybridFilmSource.class);
		result.add(XBMCSource.class);
		for (ExtensionInfo<ISource> info : pluginSources) {
			result.add(info.getExtension());
		}

		return result;
	}

	/**
	 * Used to get a list of possible stores that can be used with a media directory. This
	 * includes any that have been registered via plugins.
	 * @return The list of sources.
	 */
	public List<Class<? extends IStore>> getAvalibaleStores() {
		List<Class<? extends IStore>> result = new ArrayList<Class<? extends IStore>>();
		result.add(SapphireStore.class);
		result.add(MemoryStore.class);
		result.add(MP4ITunesStore.class);
		result.add(XMLStore2.class);
		for (ExtensionInfo<IStore> info : pluginStores) {
			result.add(info.getExtension());
		}

		return result;
	}

	/**
	 * Used to get a list of possible actions that can be used with a media directory. This
	 * includes any that have been registered via plugins.
	 * @return The list of sources.
	 */
	public List<Class<? extends IAction>> getAvalibaleActions() {
		List<Class<? extends IAction>> result = new ArrayList<Class<? extends IAction>>();
		result.add(ExecuteSystemCommandAction.class);
		result.add(PodCastAction.class);
		result.add(RenameAction.class);
		for (ExtensionInfo<IAction> info : pluginActions) {
			result.add(info.getExtension());
		}

		return result;
	}

	/**
	 * Used to get the class of a store. This can handle getting the class if
	 * the store is in a plugin
	 * @param className The name of the class
	 * @return The class object
	 * @throws ConfigException Thrown if their are any problems
	 */
	public Class<? extends IStore> getStoreClass(String className) throws  ConfigException {
		for (ExtensionInfo<IStore> info : pluginStores) {
			if (info.getExtension().getName().equals(className)) {
				return info.getExtension();
			}
		}
		try {
			return Class.forName(className).asSubclass(IStore.class);
		} catch (ClassNotFoundException e) {
			throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_ADD_STORE_NOT_FOUND"),className), e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the class of a action. This can handle getting the class if
	 * the action is in a plugin
	 * @param className The name of the class
	 * @return The class object
	 * @throws ConfigException Thrown if their are any problems
	 */
	public Class<? extends IAction> getActionClass(String className) throws  ConfigException {
		for (ExtensionInfo<IAction> info : pluginActions) {
			if (info.getExtension().getName().equals(className)) {
				return info.getExtension();
			}
		}
		try {
			return Class.forName(className).asSubclass(IAction.class);
		} catch (ClassNotFoundException e) {
			throw new ConfigException(MessageFormat.format(Messages.getString("Controller.UNABLE_TO_ADD_ACTION_NOT_FOUND"),className), e); //$NON-NLS-1$
		}
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
}

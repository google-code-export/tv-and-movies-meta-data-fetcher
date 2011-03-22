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
package org.stanwood.media.renamer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;

/**
 * The controller is used to control access to the stores and and sources. This is a singleton class, and just first be
 * setup using the @see initWithDefaults() or @see initFromConfigFile() methods. From then on, getInstance() can be
 * called to a access the methods used to control stores and sources.
 */
public class Controller {

	private final static Log log = LogFactory.getLog(Controller.class);

	private ConfigReader configReader = null;

	private Map<File,MediaDirectory> mediaDirs = new HashMap<File,MediaDirectory>();

	private static XBMCAddonManager xbmcMgr;

	/**
	 * The constructor
	 * @param config The parsed configuration
	 */
	public Controller(ConfigReader config) {
		this.configReader = config;
	}

	public void init() throws ConfigException {
		if (xbmcMgr == null) {
			try {
				setXBMCAddonManager(new XBMCAddonManager(configReader));
//				xbmcMgr.getUpdater().update();
			} catch (XBMCException e) {
				log.error(e.getMessage(),e);
			}
		}
	}

	/**
	 * Used to set the addon manager. Mostly used by tests
	 * @param xbmcAddonManager The addon manager
	 */
	public static void setXBMCAddonManager(XBMCAddonManager xbmcAddonManager) {
		xbmcMgr = xbmcAddonManager;
	}

	/**
	 * Used to get the addon manager
	 * @return The addon manager
	 */
	public XBMCAddonManager getXBMCAddonManager() {
		return xbmcMgr;
	}

	public MediaDirectory getMediaDirectory(File mediaDir) throws ConfigException {
		MediaDirectory dir = mediaDirs.get(mediaDir);
		if (dir == null) {
			dir = new MediaDirectory(this,configReader,mediaDir);
			mediaDirs.put(mediaDir,dir);
		}
		return dir;
	}

}

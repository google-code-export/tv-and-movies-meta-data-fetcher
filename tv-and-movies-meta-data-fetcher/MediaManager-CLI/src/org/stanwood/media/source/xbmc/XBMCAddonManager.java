package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.updater.IXBMCUpdater;
import org.stanwood.media.source.xbmc.updater.XBMCWebUpdater;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.Stream;

/**
 * The manager for XBMC addons
 */
public class XBMCAddonManager implements IContentFetcher {

	private final static Log log = LogFactory.getLog(XBMCAddonManager.class);

	private Map<String,XBMCAddon> addons = null;
	private IXBMCUpdater updater;
	private ConfigReader config;

	protected XBMCAddonManager(ConfigReader config,IXBMCUpdater updater,boolean doInit) throws XBMCException {
		this.config = config;
		if (doInit) {
			init(updater);
		}
	}

	/**
	 * Used to create a instance of the addon manager
	 * @param config The configuration
	 * @throws XBMCException Thrown if their is a problem creating the addon manager
	 */
	public XBMCAddonManager(ConfigReader config) throws XBMCException {
		this(config,new XBMCWebUpdater(config),true);
	}

	protected void init(IXBMCUpdater updater) throws XBMCException {
		updater.setAddonManager(this);
		this.updater = updater;
		registerAddons();
	}

	/**
	 * Used to get the XBMC updater
	 * @return the XBMC updater
	 */
	public IXBMCUpdater getUpdater() {
		return updater;
	}

	/**
	 * Used to unregister all XBMC addons with the manager
	 */
	public void unregisterAddons() {
		addons = new HashMap<String,XBMCAddon>();
	}

	/**
	 * Used to get a addon
	 * @param id The ID of the addon to get
	 * @return The addon
	 * @throws XBMCException Thrown if the addon could not be found
	 */
	public XBMCAddon getAddon(String id) throws XBMCException {
		XBMCAddon addon = addons.get(id);
		if (addon==null) {
			throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddonManager.UNABLE_FIND_XBMC_ADDON") ,id)); //$NON-NLS-1$
		}
		return addon;
	}

	/**
	 * Used to register all the XBMC addons that are found in the addon directory
	 * @throws XBMCException Thrown if their is a problem
	 */
	public void registerAddons() throws XBMCException {
		addons = new HashMap<String,XBMCAddon>();
		try {
			for (File f : config.getXBMCAddonDir().listFiles()) {
				if (f.isDirectory() && !f.getName().equals("newplugins")) { //$NON-NLS-1$
					XBMCAddon addon = new XBMCAddon(this,f,config.getXBMCLocale());
					if (log.isDebugEnabled()) {
						log.debug("Registered addon " + addon.getId()); //$NON-NLS-1$
					}
					addons.put(addon.getId(),addon);
				}
			}
		} catch (ConfigException e) {
			throw new XBMCException(Messages.getString("XBMCAddonManager.UNABLE_TO_GET_ADDON_DIR"),e); //$NON-NLS-1$
		}
	}

	/* package for test */Stream getSource(URL url) throws IOException {
		return FileHelper.getInputStream(url);
	}

	/**
	 * This will get a input stream to the contents pointed at by the URL
	 * @param url The URL
	 * @return The input stream
	 * @exception SourceException thrown if their is a problem getting the stream
	 */
	@Override
	public Stream getStreamToURL(URL url) throws SourceException {
		try {
			Stream stream = getSource(url);
			if (stream==null) {
				throw new SourceException(MessageFormat.format(Messages.getString("XBMCAddonManager.UNABLE_GET_RESOURCE"), url)); //$NON-NLS-1$
			}
			return stream;
		} catch (IOException e) {
			throw new SourceException(MessageFormat.format(Messages.getString("XBMCAddonManager.UNABLE_GET_RESOURCE"), url),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the default source ID
	 * @param mode The mode that were looking for a source id in
	 * @return The default source ID for a given mode
	 * @throws XBMCException Thrown if their is a problem getting the default source ID
	 */
	public String getDefaultAddonID(Mode mode) throws XBMCException {
		for (Entry<String,XBMCAddon> e : addons.entrySet()) {
			if (e.getValue().supportsMode(mode)) {
				return e.getKey();
			}
		}
		return null;
	}

	/**
	 * This will copy a file from the web to a destination file on the local system
	 * @param url The URL to read from the file from
	 * @param newAddon The file to be created on the location system
	 * @return A MD5 sum of the file
	 * @throws IOException Thrown if their is a problem reading or wring the file
	 */
	public String downloadFile(URL url, File newAddon) throws IOException {
		return FileHelper.copy(url,newAddon);
	}

	/**
	 * Used to get a list of addon ID's
	 * @return The list of addon ID's
	 */
	public Set<String> listAddons() {
		return addons.keySet();
	}

	/**
	 * Checks if this is the first time the application has been run.
	 * @return True if first time , otherwise false
	 * @throws XBMCException Thrown if their are any problems
	 */
	public boolean isFirstTime() throws XBMCException {
		try {
			return config.getXBMCAddonDir().list().length==0;
		}
		catch (ConfigException e) {
			throw new XBMCException(Messages.getString("XBMCAddonManager.UNABLE_TO_FIND_ADDON_DIR"),e); //$NON-NLS-1$
		}
	}
}

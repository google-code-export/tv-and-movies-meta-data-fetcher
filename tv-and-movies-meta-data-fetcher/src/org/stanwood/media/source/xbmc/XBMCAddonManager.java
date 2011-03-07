package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.WebFileInputStream;

public class XBMCAddonManager implements IContentFetcher {

	private final static Log log = LogFactory.getLog(XBMCAddonManager.class);

	private Map<String,XBMCAddon> addons = null;
	private List<ISource>sources = new ArrayList<ISource>();
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
		updatePlugins();
	}

	/**
	 * Used to update the installed plugins
	 * @return the count of plugins that were updated
	 * @throws XBMCException Thrown if their are any problems
	 */
	public int updatePlugins() throws XBMCException {
		addons = new HashMap<String,XBMCAddon>();
		try {
			int count = updater.update(config.getXBMCAddonDir());
			registerAddons();
			return count;
		} catch (ConfigException e) {
			throw new XBMCException("Unable to get the addon directory",e);
		}
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
			throw new XBMCException("Unable to find XBMC addon: " + id);
		}
		return addon;
	}

	private void registerAddons() throws XBMCException {
		try {
			for (File f : config.getXBMCAddonDir().listFiles()) {
				if (f.isDirectory()) {
					XBMCAddon addon = new XBMCAddon(this,f,config.getXBMCLocale());
					addons.put(addon.getId(),addon);
					if (addon.hasScrapers()) {
						sources.add(new XBMCSource(this, addon.getId()));
					}
				}
			}
		} catch (ConfigException e) {
			throw new XBMCException("Unable to get the addon directory",e);
		}
	}

	/* package for test */InputStream getSource(URL url) throws IOException {
		WebFileInputStream is = new WebFileInputStream(url);
		String MIME = is.getMIMEType();
		if (MIME.equals("zip")) {
			return new ZipInputStream(is);
		}
		else {
			return is;
		}
	}

	@Override
	public InputStream getStreamToURL(URL url) throws IOException, SourceException {
		InputStream stream = getSource(url);
		if (stream==null) {
			throw new SourceException("Unable to get resource: " + url);
		}
		return stream;
	}

	public List<ISource> getSources() {
		return sources;
	}

	public String getDefaultSourceID(Mode mode) throws XBMCException {
		for (Entry<String,XBMCAddon> e : addons.entrySet()) {
			if (e.getValue().supportsMode(mode)) {
				return "xbmc-" + e.getKey();
			}
		}
		return null;
	}

	String downloadFile(URL url, File newAddon) throws IOException {
		return FileHelper.copy(url,newAddon);
	}

	public Set<String> listAddons() {
		return addons.keySet();
	}
}

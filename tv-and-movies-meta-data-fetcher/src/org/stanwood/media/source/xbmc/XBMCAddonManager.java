package org.stanwood.media.source.xbmc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.WebFile;

public class XBMCAddonManager implements IContentFetcher {

	private final static Log log = LogFactory.getLog(XBMCAddonManager.class);

	private File addonDir;
	private Map<String,XBMCAddon> addons = new HashMap<String,XBMCAddon>();
	private List<ISource>sources = new ArrayList<ISource>();
	private Locale locale;

	XBMCAddonManager(IXBMCUpdater updater,File addonDir,Locale locale) throws XBMCException {
		this.addonDir = addonDir;
		this.locale = locale;
		checkUptoDate(updater);
		registerAddons();
	}

	private void checkUptoDate(IXBMCUpdater updater) {

	}

	/**
	 * Used to create a instance of the addon manager
	 * @throws XBMCException Thrown if thier is a problem creating the addon manager
	 */
	public XBMCAddonManager() throws XBMCException {
		this(new XBMCWebUpdater(),getDefaultAddonDir(),getDefaultLocale());
	}

	private static File getDefaultAddonDir() throws XBMCException {
		File homeDir = new File(System.getProperty("user.home"));
		File mediaConfigDir = new File(homeDir,".mediaInfo");
		File addonDir = new File(mediaConfigDir,"xbmc"+File.separator+"addons");
		if (!addonDir.exists()) {
			if (!addonDir.mkdirs() && !addonDir.exists()) {
				throw new XBMCException("Unable to create xbmc addon directory: " + addonDir);
			}
		}
		return addonDir;
	}

	private static Locale getDefaultLocale() {
		//TODO read from config file
		return Locale.ENGLISH;
	}

	public XBMCAddon getAddon(String id) {
		return addons.get(id);
	}

	private void registerAddons() throws XBMCException {
		for (File f : addonDir.listFiles()) {
			XBMCAddon addon = new XBMCAddon(this,f,locale);
			addons.put(addon.getId(),addon);
			if (addon.hasScrapers()) {
				sources.add(new XBMCSource(this, addon.getId()));
			}
		}
	}

	/* package for test */InputStream getSource(URL url) throws IOException {
		WebFile page = new WebFile(url);
		String MIME = page.getMIMEType();
		byte[] content = (byte[]) page.getContent();
		if (MIME.equals("zip")) {
			return new ZipInputStream(new ByteArrayInputStream(content));
		}
		else {
			return new ByteArrayInputStream(content);
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
}

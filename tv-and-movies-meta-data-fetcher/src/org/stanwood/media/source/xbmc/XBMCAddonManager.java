package org.stanwood.media.source.xbmc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.WebFile;

public class XBMCAddonManager implements IContentFetcher {

	private File addonDir;
	private Map<String,XBMCAddon> addons = new HashMap<String,XBMCAddon>();
	private Locale locale;

	public XBMCAddonManager(File addonDir,Locale locale) throws XBMCException {
		this.addonDir = addonDir;
		this.locale = locale;
		registerAddons();
	}

	public XBMCAddon getAddon(String id) {
		return addons.get(id);
	}

	private void registerAddons() throws XBMCException {
		for (File f : addonDir.listFiles()) {
//			if (f.getName().startsWith("metadata")) {
			XBMCAddon addon = new XBMCAddon(this,f,locale);
			addons.put(addon.getId(),addon);
//			}
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

	public InputStream getStreamToURL(URL url) throws IOException, SourceException {
		InputStream stream = getSource(url);
		if (stream==null) {
			throw new SourceException("Unable to get resource: " + url);
		}
		return stream;
	}
}

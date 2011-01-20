package org.stanwood.media.source.xbmc;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class XMBCAddonManager {

	private File addonDir;
	private Map<String,XBMCAddon> addons = new HashMap<String,XBMCAddon>();
	private Locale locale;

	public XMBCAddonManager(File addonDir,Locale locale) throws XBMCException {
		this.addonDir = addonDir;
		this.locale = locale;
		registerAddons();
	}	
	
	public XBMCAddon getAddon(String id) {
		return addons.get(id);
	}
	
	private void registerAddons() throws XBMCException {
		for (File f : addonDir.listFiles()) {
			if (f.getName().startsWith("metadata")) {
				XBMCAddon addon = new XBMCAddon(f,locale);
				addons.put(addon.getId(),addon);
			}
		}
	}
}

package org.stanwood.media.source.xbmc;

import java.io.File;

public class XBMCWebUpdater implements IXBMCUpdater {

	private final static String UPDATE_SITE_URL = "http://mirrors.xbmc.org/addons/dharma/addons.xml";
	private final static String UPDATE_SITE_MD5 = "http://mirrors.xbmc.org/addons/dharma/addons.xml.md5";
	private final static String UPDATE_SITE_DATA_DIR = "http://mirrors.xbmc.org/addons/dharma";

	@Override
	public void update(File addonsDir) {

	}

}

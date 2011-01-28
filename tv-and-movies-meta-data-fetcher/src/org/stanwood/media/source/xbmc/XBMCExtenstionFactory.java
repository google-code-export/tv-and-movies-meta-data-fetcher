package org.stanwood.media.source.xbmc;

import org.stanwood.media.model.Mode;
import org.w3c.dom.Element;

public class XBMCExtenstionFactory {

	public static XBMCExtension createExtension(XBMCAddon addon,Element extensionNode) {
		String point = extensionNode.getAttribute("point");
		if (point.equals("xbmc.metadata.scraper.tvshows")) {
			return new XBMCScraper(addon, addon.getFile(extensionNode.getAttribute("library")), point,Mode.TV_SHOW);
		}
		else if (point.equals("xbmc.metadata.scraper.movies")) {
			return new XBMCScraper(addon, addon.getFile(extensionNode.getAttribute("library")), point,Mode.FILM);
		}
		return null;
	}
}

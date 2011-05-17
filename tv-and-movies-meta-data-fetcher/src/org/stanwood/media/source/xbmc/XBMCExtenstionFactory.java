package org.stanwood.media.source.xbmc;

import org.stanwood.media.model.Mode;
import org.w3c.dom.Element;

/**
 * A factory class used to create XBMC extensions
 */
public class XBMCExtenstionFactory {

	/**
	 * Create the extension
	 * @param addon The addon the extension belongs to
	 * @param extensionNode The extension node
	 * @return The extension or null if unsupported
	 */
	public static XBMCExtension createExtension(XBMCAddon addon,Element extensionNode) {
		String point = extensionNode.getAttribute("point");
		if (point.equals("xbmc.metadata.scraper.tvshows")) {
			return new XBMCScraper(addon, addon.getFile(extensionNode.getAttribute("library")), point,Mode.TV_SHOW);
		}
		else if (point.equals("xbmc.metadata.scraper.movies")) {
			return new XBMCScraper(addon, addon.getFile(extensionNode.getAttribute("library")), point,Mode.FILM);
		}
		else if (point.equals("xbmc.metadata.scraper.library")) {
			return new XBMCLibrary(addon, addon.getFile(extensionNode.getAttribute("library")), point);
		}
		return null;
	}
}

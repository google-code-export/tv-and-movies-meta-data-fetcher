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
		String point = extensionNode.getAttribute("point"); //$NON-NLS-1$
		if (point.equals("xbmc.metadata.scraper.tvshows")) { //$NON-NLS-1$
			return new XBMCScraper(addon, addon.getFile(extensionNode.getAttribute("library")), point,Mode.TV_SHOW); //$NON-NLS-1$
		}
		else if (point.equals("xbmc.metadata.scraper.movies")) { //$NON-NLS-1$
			return new XBMCScraper(addon, addon.getFile(extensionNode.getAttribute("library")), point,Mode.FILM); //$NON-NLS-1$
		}
		else if (point.equals("xbmc.metadata.scraper.library")) { //$NON-NLS-1$
			return new XBMCLibrary(addon, addon.getFile(extensionNode.getAttribute("library")), point); //$NON-NLS-1$
		}
		return null;
	}
}

package org.stanwood.media.source.xbmc;

import org.w3c.dom.Element;

public class XBMCSettingsFactory {

	public static XBMCSetting createSetting(Element node) {
		String type = node.getAttribute("type");
		String defaultValue = node.getAttribute("default");
		String id = node.getAttribute("id");

		if (type.equals("bool")) {

		}
		else if (type.equals("labelenum")) {

		}
		return null;
	}

}

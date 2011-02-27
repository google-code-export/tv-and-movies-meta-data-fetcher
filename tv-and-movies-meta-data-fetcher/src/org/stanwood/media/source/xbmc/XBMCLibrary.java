package org.stanwood.media.source.xbmc;

import java.io.File;
import java.util.Map;

import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Element;

public class XBMCLibrary extends XBMCExtension {

	private static final String ROOT_NODE_NAME = "scraperfunctions";

	public XBMCLibrary(XBMCAddon addon, File scraperFile, String point) {
		super(addon, scraperFile, point);
	}

	@Override
	public String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws  XBMCException, XMLParserException {
		Element functionNode = (Element) selectSingleNode(getDocument(), ROOT_NODE_NAME+"/"+functionName);
		if (functionNode==null) {
			throw new XBMCException("Unable to find scraper function '" + functionName+"'");
		}
		return executeXBMCFunction(functionNode,params);
	}

}

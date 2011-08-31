package org.stanwood.media.source.xbmc;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Element;

/**
 * A library type of XBMC extensions
 */
public class XBMCLibrary extends XBMCExtension {

	private static final String ROOT_NODE_NAME = "scraperfunctions"; //$NON-NLS-1$

	/**
	 * The constructor
	 * @param addon the addon
	 * @param scraperFile The scraper file the extension is been read from
	 * @param point The extension point been used for the extension
	 */
	public XBMCLibrary(XBMCAddon addon, File scraperFile, String point) {
		super(addon, scraperFile, point);
	}

	/** {@inheritDoc} */
	@Override
	public String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws  XBMCException, XMLParserException {
		Element functionNode = (Element) selectSingleNode(getDocument(), ROOT_NODE_NAME+"/"+functionName); //$NON-NLS-1$
		if (functionNode==null) {
			throw new XBMCFunctionNotFoundException(MessageFormat.format(Messages.getString("XBMCLibrary.UNABLE_PARSE_SCRAPER_FUNC"),functionName)); //$NON-NLS-1$
		}
		return executeXBMCFunction(functionNode,params);
	}

}
package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.stanwood.media.model.Mode;
import org.stanwood.media.source.xbmc.expression.ExpressionEval;
import org.stanwood.media.source.xbmc.expression.Value;
import org.stanwood.media.source.xbmc.expression.ValueType;
import org.stanwood.media.store.xmlstore.SimpleErrorHandler;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class is used to manage XBMC addon's
 */
public class XBMCAddon extends XMLParser {

	private File addonDir;
	private Locale locale;
	private List<XBMCExtension> extensions;
	private XBMCAddonManager addonMgr;
	private List<XBMCAddon> requiredAddons;
	private Map<String,Value> settings = new HashMap<String,Value>();
	private File addonFile;
	private Map<File, Document> docs = new HashMap<File,Document>();

	/**
	 * Used to create a instance of the addon class
	 * @param addonDir The directory contain the XBMC addons
	 * @param locale The locale to use with the scrapers
	 * @throws XBMCException Thrown if anable to parse the settings
	 */
	public XBMCAddon(XBMCAddonManager addonMgr,File addonDir,Locale locale) throws XBMCException {
		this.addonDir = addonDir;
		this.locale = locale;
		this.addonMgr = addonMgr;
		this.addonFile = new File(addonDir,"addon.xml");
		parseSettings();
	}

	private void parseSettings() throws XBMCException {
		File settingsFile = new File(addonDir,"resources"+File.separator+"settings.xml");
		try {
			Document doc = getDocument(settingsFile);
			for (Node node : selectNodeList(doc, "settings/setting")) {
				addSetting((Element)node);
			}
		}
		catch (XMLParserException e) {
			throw new XBMCException("Unable to parse the settigs file: " + settingsFile,e);
		}
	}

	private void addSetting(Element node) throws XBMCException {
		String type = node.getAttribute("type");
		String defaultValue = node.getAttribute("default");
		String id = node.getAttribute("id");

		if (type.equals("bool")) {
			ExpressionEval eval = new ExpressionEval();
			Value value = eval.eval(defaultValue);
			if (value.getType()!=ValueType.BOOLEAN) {
				throw new XBMCException("Unable to get the default value for setting '"+id+"'");
			}
			settings.put(id,value);
		}
		else if (type.equals("labelenum")) {

		}
		throw new XBMCException("Unkown setting type '"+type+"' of setting '"+id+"'");
	}

	public Value getSetting(String id) {
		return settings.get(id);
	}

	private Document getDocument(File file) throws XBMCException {
		Document doc = docs.get(file);
		if (doc==null) {
//			File addonFile = new File(addonDir,"addon.xml");
			if (!file.exists()) {
				throw new XBMCException("Unable to find XMBC addon file: " + file);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(file);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(file);
				docs.put(file,doc);
				if (errorHandler.hasErrors()) {
					throw new XBMCException("Unable to parse  XMBC addon, errors found in file: " + file);
				}
			} catch (SAXException e) {
				throw new XBMCException("Unable to parse XMBC addon: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new XBMCException("Unable to read XMBC addon: " + e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new XBMCException("Unable to parse  XMBC addon: " + e.getMessage(), e);
			}
		}
		return doc;
	}

	/**
	 * Used to get the id of the addon
	 * @return the id of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getId() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/@id");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	public List<XBMCAddon>getRquiredAddons() throws XBMCException {
		if (requiredAddons==null) {
			requiredAddons = new ArrayList<XBMCAddon>();
			try {
				for (Node node : selectNodeList(getDocument(addonFile), "addon/requires/import")) {
					String id = ((Element)node).getAttribute("addon");

					XBMCAddon addon = addonMgr.getAddon(id);
					if (addon==null) {
						throw new XBMCException("Unable to find required addon '" + id);
					}
					//TODO check the version
//					String version = ((Element)node).getAttribute("version");
//					if (!addon.getVersion().equals(version)) {
//						throw new XBMCException("Unable to find required addon '" + id+"' of version '"+version+"'");
//					}

					requiredAddons.add(addon);
				}
			}
			catch (XMLParserException e1) {
				throw new XBMCException("Unable to parse required addons",e1);
			}
		}
		return requiredAddons;
	}

	/**
	 * Used to get the version of the addon
	 * @return the version of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getVersion() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/@version");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	/**
	 * Used to get the provider name of the addon
	 * @return the provider name of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getProviderName() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/@provider-name");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	/**
	 * Used o find out if the scraper supports a given mode
	 * @param mode The mode to check
	 * @return True if the mode is supported.
	 * @throws XBMCException Thrown if their is any problems while checking
	 */
	public boolean supportsMode(Mode mode) throws XBMCException {
		for (XBMCExtension extension : getExtensions()) {
			if (extension instanceof XBMCScraper && ((XBMCScraper)extension).getMode().equals(mode)) {
				return true;
			}
		}
		return false;
	}

	private List<XBMCExtension> getExtensions() throws XBMCException {
		if (extensions == null ) {
			extensions = new ArrayList<XBMCExtension>();
			try {
				for (Node node : selectNodeList(getDocument(addonFile), "addon/extension")) {
					XBMCExtension ext = XBMCExtenstionFactory.createExtension(this,(Element)node);
					if (ext!=null) {
						extensions.add(ext);
					}
				}
			}
			catch (XMLParserException e1) {
				throw new XBMCException("Unable to find scraper filename",e1);
			}
//			if (extensions.size()==0) {
//				throw new XBMCException("Unable to find the scrapper filename of addon '" + getId()+"'");
//			}
		}

		return extensions;
	}

	/**
	 * Used to get the scraper class that will read data using the XBMC XML scraper files
	 * @return The scraper
	 * @throws XBMCException Thrown if their are any problems
	 */
	public XBMCScraper getScraper(Mode mode) throws XBMCException {
		for (XBMCExtension ext : getExtensions()) {
			if (ext instanceof XBMCScraper) {
				return (XBMCScraper) ext;
			}
		}
		return null;
	}

	/**
	 * Used to get the summary of the addon
	 * @return the summary of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getSummary() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/extension[@point='xbmc.addon.metadata']/summary[@lang='"+locale.getLanguage()+"']/text()");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	/**
	 * Used to get the description of the addon
	 * @return the description of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getDescription() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/extension[@point='xbmc.addon.metadata']/description[@lang='"+locale.getLanguage()+"']/text()");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	/**
	 * Used to get the name of the addon
	 * @return the name of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getName() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/@name");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	public String getInfoSetting(String key) throws XBMCException {
		if (key.equals("language")) {
			return locale.getLanguage();
		}
		throw new XBMCException("Unknown info key '"+key+"'");
	}

	public File getFile(String path) {
		return new File(addonDir,path);
	}

	public String executeFunction(String functionName,Map<Integer, String> params) throws XBMCException, XMLParserException {
		String result = null;
		for (XBMCExtension addon : getExtensions()) {
			try {
				result = addon.executeXBMCScraperFunction(functionName, params);
				break;
			}
			catch (XBMCException e) {
				// Ignore
			}
		}

		if (result==null) {
			for (XBMCAddon addon : getRquiredAddons()) {
				try {
					result = addon.executeFunction(functionName, params);
					break;
				}
				catch (XBMCException e) {
					// Ignore
				}
			}
		}

		if (result == null)
		{
			throw new XBMCException("Unable to find scraper function '" + functionName+"'");
		}

		return result;
	}

	public XBMCAddonManager getManager() {
		return addonMgr;
	}

	public boolean hasScrapers() throws XBMCException {
		for (XBMCExtension ext : getExtensions()) {
			if (ext instanceof XBMCScraper) {
				return true;
			}
		}
		return false;
	}
}
package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.xbmc.expression.BooleanValue;
import org.stanwood.media.source.xbmc.expression.ExpressionEval;
import org.stanwood.media.source.xbmc.expression.ExpressionParserException;
import org.stanwood.media.source.xbmc.expression.StringValue;
import org.stanwood.media.source.xbmc.expression.Value;
import org.stanwood.media.source.xbmc.expression.ValueType;
import org.stanwood.media.util.Version;
import org.stanwood.media.xml.IterableNodeList;
import org.stanwood.media.xml.SimpleErrorHandler;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class is used to manage XBMC addon's
 */
public class XBMCAddon extends XMLParser {

	private final static Log log = LogFactory.getLog(XMLParser.class);

	private File addonDir;
	private Locale locale;
	private List<XBMCExtension> extensions;
	private XBMCAddonManager addonMgr;
	private List<XBMCAddon> requiredAddons;
	private File addonFile;
	private Map<File, Document> docs = new HashMap<File,Document>();
	private ExpressionEval eval = new ExpressionEval();

	/**
	 * Used to create a instance of the addon class
	 * @param addonMgr The XBMC addon manager
	 * @param addonDir The directory contain the XBMC addons
	 * @param locale The locale to use with the scrapers
	 * @throws XBMCException Thrown if anable to parse the settings
	 */
	public XBMCAddon(XBMCAddonManager addonMgr,File addonDir,Locale locale) throws XBMCException {
		this.addonDir = addonDir;
		this.locale = locale;
		this.addonMgr = addonMgr;
		this.addonFile = new File(addonDir,"addon.xml"); //$NON-NLS-1$

		parseSettings();
	}

	private void parseSettings() throws XBMCException {
		if (log.isDebugEnabled()) {
			log.debug("Checking for addon " + addonDir.getName() + " settings");  //$NON-NLS-1$//$NON-NLS-2$
		}
		File settingsFile = getFile("resources"+File.separator+"settings.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!settingsFile.getAbsolutePath().contains("visualization.projectm") && settingsFile.exists()) { //$NON-NLS-1$
			try {
				if (log.isDebugEnabled()) {
					log.debug("Loading settings file: " + settingsFile); //$NON-NLS-1$
				}
				Document doc = getDocument(settingsFile);

				Element settingsNode = getFirstChildElement(doc, "settings"); //$NON-NLS-1$
				IterableNodeList settings = selectNodeList(settingsNode, "**/setting"); //$NON-NLS-1$
				for (Node node : settings) {
					addSetting((Element)node);
				}
			}
			catch (XMLParserException e) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_PARSE_SETTINGS_FILE"), settingsFile),e); //$NON-NLS-1$
			}
		}
	}

	private void addSetting(Element node) throws XBMCException {
		String type = node.getAttribute("type"); //$NON-NLS-1$
		String defaultValue = node.getAttribute("default"); //$NON-NLS-1$
		String id = node.getAttribute("id"); //$NON-NLS-1$
		if (log.isDebugEnabled()) {
			log.debug("Found setting: "+id+" of type " + type); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Fix a broken scraper so the trailer part is ignored (not needed anyhow)
		if (id.equals("trailer")) { //$NON-NLS-1$
			defaultValue="false"; //$NON-NLS-1$
		}

		if (type.equals("sep")) { //$NON-NLS-1$
			// do nothing
		}
		else if (type.equals("bool")) { //$NON-NLS-1$
			Value value = eval.eval(defaultValue);
			if (value.getType()!=ValueType.BOOLEAN) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_GET_DEFAULT_VALUE_FOR_SETTING"),id)); //$NON-NLS-1$
			}

			if (eval.getVariables().get(id)==null) {
				if (log.isDebugEnabled()) {
					log.debug("Adding setting: " + id + " : " + type + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				eval.getVariables().put(id,value);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Setting already exists: " + id + " : " + type + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		else if(type.equals("labelenum")) { //$NON-NLS-1$
			Value value = new StringValue(ValueType.STRING,defaultValue);

			if (eval.getVariables().get(id)==null) {
				if (log.isDebugEnabled()) {
					log.debug("Adding setting: " + id + " : " + type + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

				eval.getVariables().put(id,value);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Setting already exists: " + id + " : " + type + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Unable to add setting type: " + id + " : " + type); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Used to get the value of a addon setting
	 * @param id The id of the addon setting
	 * @return The value of the addon setting
	 * @throws XBMCException Thrown if their is a problem getting the setting
	 */
	public Value getSetting(String id) throws XBMCException {
		try {
			return eval.getVariables().get(id);
		}
		catch (ExpressionParserException e) {
			throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_GET_SETTING"),id,getId()),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the addon settings
	 * @return The addon settings
	 */
	public Map<String,Value>getSettings() {
		return eval.getVariables();
	}

	private Document getDocument(File file) throws XBMCException {
		Document doc = docs.get(file);
		if (doc==null) {
//			File addonFile = new File(addonDir,"addon.xml");
			if (!file.exists()) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_FILE"),file)); //$NON-NLS-1$
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
					throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_PARSE_ADDON"),file)); //$NON-NLS-1$
				}
			} catch (SAXException e) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_PARSE_FILE"),file), e); //$NON-NLS-1$
			} catch (IOException e) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_READ_FILE"),file), e); //$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_PARSE_FILE"),file), e); //$NON-NLS-1$
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
			return getStringFromXML(getDocument(addonFile), "addon/@id"); //$NON-NLS-1$
		} catch (Exception e) {
			throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_ID"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get a list of required addons by this addon
	 * @return gets a list of required addons by this addon
	 * @throws XBMCException Thrown if their is a problem
	 */
	public List<XBMCAddon>getRquiredAddons() throws XBMCException {
		if (requiredAddons==null) {
			requiredAddons = new ArrayList<XBMCAddon>();
			try {
				for (Node node : selectNodeList(getDocument(addonFile), "addon/requires/import")) { //$NON-NLS-1$
					String id = ((Element)node).getAttribute("addon"); //$NON-NLS-1$

					// The XBMC addons are not need by this app
					if (!id.startsWith("xbmc.")) { //$NON-NLS-1$
						XBMCAddon addon = addonMgr.getAddon(id);
						//TODO check the version
	//					String version = ((Element)node).getAttribute("version");
	//					if (!addon.getVersion().equals(version)) {
	//						throw new XBMCException("Unable to find required addon '" + id+"' of version '"+version+"'");
	//					}

						requiredAddons.add(addon);
					}
				}
			}
			catch (XMLParserException e1) {
				throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_PARSE_REQUIRED_ADDONS"),e1); //$NON-NLS-1$
			}
		}
		return requiredAddons;
	}

	/**
	 * Used to get the version of the addon
	 * @return the version of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public Version getVersion() throws XBMCException {
		try {
			return new Version(getStringFromXML(getDocument(addonFile), "addon/@version")); //$NON-NLS-1$
		} catch (Exception e) {
			throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_ID"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the provider name of the addon
	 * @return the provider name of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getProviderName() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/@provider-name"); //$NON-NLS-1$
		} catch (Exception e) {
			throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_ID"),e); //$NON-NLS-1$
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
				for (Node node : selectNodeList(getDocument(addonFile), "addon/extension")) { //$NON-NLS-1$
					XBMCExtension ext = XBMCExtenstionFactory.createExtension(this,(Element)node);
					if (ext!=null) {
						extensions.add(ext);
					}
				}
			}
			catch (XMLParserException e1) {
				throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FUND_SCRAPER_FILENAME"),e1); //$NON-NLS-1$
			}
//			if (extensions.size()==0) {
//				throw new XBMCException("Unable to find the scrapper filename of addon '" + getId()+"'");
//			}
		}

		return extensions;
	}

	/**
	 * Used to get the scraper class that will read data using the XBMC XML scraper files
	 * @param mode The mode that the scraper is been used for
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
			return getStringFromXML(getDocument(addonFile), "addon/extension[@point='xbmc.addon.metadata']/summary[@lang='"+locale.getLanguage()+"']/text()"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_ID"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the description of the addon
	 * @return the description of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getDescription() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/extension[@point='xbmc.addon.metadata']/description[@lang='"+locale.getLanguage()+"']/text()"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_ID"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the name of the addon
	 * @return the name of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getName() throws XBMCException {
		try {
			return getStringFromXML(getDocument(addonFile), "addon/@name"); //$NON-NLS-1$
		} catch (Exception e) {
			throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_FIND_ADDON_ID"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get the value of a info setting
	 * @param key The info setting name
	 * @return The info setting value
	 * @throws XBMCException Thrown if not able to find the setting
	 */
	public String getInfoSetting(String key) throws XBMCException {
		if (key.equals("language")) { //$NON-NLS-1$
			return locale.getLanguage();
		}

		try {
			Value value = eval.getVariables().get(key);
			if (value==null) {
				throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNKNOWN_XBMC_SETTING"),key)); //$NON-NLS-1$
			}
			return value.toString();
		}
		catch (ExpressionParserException e) {
			throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNKNOWN_XBMC_SETTING"),key),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to get a reference to one of the addons files
	 * @param path The Path relative to the addon directory
	 * @return The file object referencing the file
	 */
	public File getFile(String path) {
		return new File(addonDir,path);
	}

	/**
	 * Used to execute a scraper function
	 * @param functionName The function name
	 * @param params The parameters given to the function
	 * @return The results from the function
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String executeFunction(String functionName,Map<Integer, String> params) throws XBMCException {
		// Check within this Addon for the function
		for (XBMCExtension addon : getExtensions()) {
			try {
				String result = addon.executeXBMCScraperFunction(functionName, params);
				return result;
			}
			catch (XBMCFunctionNotFoundException e) {
				// Ignore
			}
			catch (XMLParserException e) {
				throw new XBMCException(Messages.getString("XBMCAddon.UNABLE_EXEXC_SCRAPER_FUNCTION"),e); //$NON-NLS-1$
			}
		}

		// Check within the addons that this addon depends on
		for (XBMCAddon addon : getRquiredAddons()) {
			try {
				String result = addon.executeFunction(functionName, params);
				return result;
			}
			catch (XBMCFunctionNotFoundException e) {
				// Ignore
			}
		}

		throw new XBMCFunctionNotFoundException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_FIND_SCRAPER_FUNCTION"),functionName)); //$NON-NLS-1$
	}

	/**
	 * Used to get the addon manager
	 * @return The addon manager
	 */
	public XBMCAddonManager getManager() {
		return addonMgr;
	}

	/**
	 * Used to find out if the extension has scrappers
	 * @return True if the extension has scrapers
	 * @throws XBMCException Thrown if their are any problems
	 */
	public boolean hasScrapers() throws XBMCException {
		for (XBMCExtension ext : getExtensions()) {
			if (ext instanceof XBMCScraper) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Used to evaluate a expression making use of the addon settings. The expression
	 * must evaluate to a boolean type.
	 * @param expression The expression
	 * @return The value the expression evaluates to
	 * @throws XBMCException Thrown if their are any problems
	 */
	public boolean checkCondition(String expression) throws XBMCException {

		try {
			Value value = eval.eval(expression);
			if (value.getType() == ValueType.BOOLEAN) {
				boolean result = ((BooleanValue)value).booleanValue();
				return result;
			}
			throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.EXPRESSION_DID_NOT_EVAL_TO_BOOL"),expression)); //$NON-NLS-1$
		}
		catch (ExpressionParserException e) {
			throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_EVAL_EXPR") ,expression,getId()),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to assign a value to a variable which are used as settings. The expression is evaluated and stored
	 * in the setting variable.
	 * @param key The name of the setting
	 * @param expression The expression to evaluate and store in the setting
	 * @throws XBMCException Thrown if their are any problems.
	 */
	public void setSetting(String key, String expression) throws XBMCException {
		try {
			if (!eval.getVariables().containsKey(key)) {
				return;

//				throw new XBMCException("Unkown setting '"+key+"' in addon '"+getId()+"'");
			}
			Value value = eval.eval(expression);
			eval.getVariables().put(key,value );
		}
		catch (ExpressionParserException e) {
			throw new XBMCException(MessageFormat.format(Messages.getString("XBMCAddon.UNABLE_EVAL_EXPR"),expression ,getId()),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "XBMCAddon: " + addonDir.getAbsolutePath(); //$NON-NLS-1$
	}
}
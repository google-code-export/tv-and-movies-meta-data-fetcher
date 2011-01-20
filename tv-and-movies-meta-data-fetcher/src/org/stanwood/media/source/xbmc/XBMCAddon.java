package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.media.source.NotInStoreException;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.xmlstore.SimpleErrorHandler;
import org.stanwood.media.util.XMLParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class is used to manage XBMC addon's
 */
public class XBMCAddon extends XMLParser {

	private File addonDir;
	private Document doc;
	private Locale locale;

	/**
	 * Used to create a instance of the addon class
	 * @param scraperFile The file that contains the addon details
	 */
	public XBMCAddon(File addonDir,Locale locale) {
		this.addonDir = addonDir; 
		this.locale = locale;
	}
	
	private Document getDocument() throws SourceException {
		if (doc==null) {
			File addonFile = new File(addonDir,"addon.xml");
			if (!addonFile.exists()) {
				throw new SourceException("Unable to find XMBC addon file: " + addonFile);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(addonFile);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(addonFile);
				if (errorHandler.hasErrors()) {
					throw new SourceException("Unable to parse  XMBC addon, errors found in file: " + addonFile);
				}
			} catch (SAXException e) {
				throw new SourceException("Unable to parse XMBC addon: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new SourceException("Unable to read XMBC addon: " + e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new SourceException("Unable to parse  XMBC addon: " + e.getMessage(), e);
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
			return getStringFromXML(getDocument(), "addon/@id");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
			throw new XBMCException("Unable to find addon id",e);
		}			
	}
	
	/**
	 * Used to get the version of the addon
	 * @return the version of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getVersion() throws XBMCException {
		try {
			return getStringFromXML(getDocument(), "addon/@version");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
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
			return getStringFromXML(getDocument(), "addon/@provider-name");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}
	
	private String getScraperLibName() throws XBMCException {
		try {
			String type ="tvshows";
			return getStringFromXML(getDocument(), "addon/extension[@point='xbmc.metadata.scraper."+type+"']/@library");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
			throw new XBMCException("Unable to find addon id",e);
		}	
	}
	
	public XBMCScraper getScraper() throws XBMCException {
		String libName = getScraperLibName();
		return new XBMCScraper(this,new File(addonDir,libName));
	}
	
	/**
	 * Used to get the summary of the addon
	 * @return the summary of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getSummary() throws XBMCException {
		try {
			return getStringFromXML(getDocument(), "addon/extension[@point='xbmc.addon.metadata']/summary[@lang='"+locale.getLanguage()+"']/text()");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
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
			return getStringFromXML(getDocument(), "addon/extension[@point='xbmc.addon.metadata']/description[@lang='"+locale.getLanguage()+"']/text()");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
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
			return getStringFromXML(getDocument(), "addon/@name");
		} catch (TransformerException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (NotInStoreException e) {
			throw new XBMCException("Unable to find addon id",e);
		} catch (SourceException e) {
			throw new XBMCException("Unable to find addon id",e);
		}
	}

	public String getInfoSetting(String key) throws XBMCException {
		if (key.equals("language")) {
			return locale.getLanguage();
		}
		throw new XBMCException("Unknown info key '"+key+"'");
	}
}
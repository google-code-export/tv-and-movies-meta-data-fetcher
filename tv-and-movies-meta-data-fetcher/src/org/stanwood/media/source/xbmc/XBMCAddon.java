package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Mode;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.xmlstore.SimpleErrorHandler;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.stanwood.media.util.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This class is used to manage XBMC addon's
 */
public class XBMCAddon extends XMLParser {

	private String LIB_TYPES[] = {"xbmc.metadata.scraper.tvshows","xbmc.metadata.scraper.movies"};
	private Mode MODE_TYPES[] = {Mode.TV_SHOW,Mode.FILM};
	private File addonDir;
	private Document doc;
	private Locale locale;
	private XBMCScraper scraper;
	private String libScraperName;
	private List<Mode> modes;

	/**
	 * Used to create a instance of the addon class
	 * @param addonDir The directory contain the XBMC addons
	 * @param locale The locale to use with the scrapers
	 */
	public XBMCAddon(File addonDir,Locale locale) {
		this.addonDir = addonDir; 
		this.locale = locale;
	}
	
	private Document getDocument() throws XBMCException {
		if (doc==null) {
			File addonFile = new File(addonDir,"addon.xml");
			if (!addonFile.exists()) {
				throw new XBMCException("Unable to find XMBC addon file: " + addonFile);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(addonFile);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(addonFile);
				if (errorHandler.hasErrors()) {
					throw new XBMCException("Unable to parse  XMBC addon, errors found in file: " + addonFile);
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
			return getStringFromXML(getDocument(), "addon/@id");
		} catch (Exception e) {
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
			return getStringFromXML(getDocument(), "addon/@provider-name");
		} catch (Exception e) {
			throw new XBMCException("Unable to find addon id",e);
		} 
	}
	
	private List<Mode> getModes() throws XBMCException  {
		if (modes==null) {
			modes = new ArrayList<Mode>();
			try {
				for (int i=0;i<LIB_TYPES.length;i++) {
					String type = LIB_TYPES[i];
					try {		
						System.out.println("Checking type: " + type);
						getStringFromXML(getDocument(), "addon/extension[@point='"+type+"']/@library");
						
						// If we get here , then mode was found
						modes.add(MODE_TYPES[i]);												
					} catch (XMLParserNotFoundException e) {
						// Do nothing
					}
				}
			} 	
			catch (XMLParserException e1) {
				throw new XBMCException("Unable to find addon mode",e1);	
			}			
		}
		return modes;
	}
	
	/**
	 * Used o find out if the scraper supports a given mode
	 * @param mode The mode to check
	 * @return True if the mode is supported.
	 * @throws XBMCException Thrown if their is any problems while checking
	 */
	public boolean supportsMode(Mode mode) throws XBMCException {
		return getModes().contains(mode);
	}
	
	private String getScraperLibName() throws XBMCException {
		if (libScraperName==null) {
			try {
				for (String type : LIB_TYPES) {
					try {		
						libScraperName = getStringFromXML(getDocument(), "addon/extension[@point='"+type+"']/@library");
						break;
					} catch (XMLParserNotFoundException e) {
						// Do nothing
					}	
				}
			} 	
			catch (XMLParserException e1) {
				throw new XBMCException("Unable to find scraper filename",e1);	
			}
			if (libScraperName==null) {
				throw new XBMCException("Unable to find the scrapper filename");
			}
		}
		return libScraperName;
	}
	
	/**
	 * Used to get the scraper class that will read data using the XBMC XML scraper files
	 * @return The scraper
	 * @throws XBMCException Thrown if their are any problems
	 */
	public XBMCScraper getScraper() throws XBMCException {
		if (scraper ==null) {
			String libName = getScraperLibName();			
			scraper = new XBMCScraper(this,new File(addonDir,libName));; 
		}
		return scraper;
	}
	
	/**
	 * Used to get the summary of the addon
	 * @return the summary of the addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public String getSummary() throws XBMCException {
		try {
			return getStringFromXML(getDocument(), "addon/extension[@point='xbmc.addon.metadata']/summary[@lang='"+locale.getLanguage()+"']/text()");
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
			return getStringFromXML(getDocument(), "addon/extension[@point='xbmc.addon.metadata']/description[@lang='"+locale.getLanguage()+"']/text()");
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
			return getStringFromXML(getDocument(), "addon/@name");
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
}
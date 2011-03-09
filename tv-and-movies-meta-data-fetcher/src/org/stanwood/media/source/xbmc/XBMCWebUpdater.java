package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.Version;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XBMCWebUpdater extends XMLParser implements IXBMCUpdater {

	private final static Log log = LogFactory.getLog(XBMCWebUpdater.class);

	private final static String UPDATE_SITE_URL = "http://mirrors.xbmc.org/addons/dharma/addons.xml";
	private final static String UPDATE_SITE_MD5 = "http://mirrors.xbmc.org/addons/dharma/addons.xml.md5";
	private final static String UPDATE_SITE_DATA_DIR = "http://mirrors.xbmc.org/addons/dharma";

	private XBMCAddonManager mgr;

	private File addonsDir;

	public XBMCWebUpdater(ConfigReader config) throws XBMCException {
		try {
			addonsDir = config.getXBMCAddonDir();
		}
		catch (ConfigException e) {
			throw new XBMCException("Unable to get XBMC addon directory",e);
		}
	}

	@Override
	public void setAddonManager(XBMCAddonManager mgr) {
		this.mgr =mgr;
	}

	@Test
	public void listPlugins() throws XBMCUpdaterException {
		try {
			File newAddon = null;
			try {
				newAddon = downloadLatestAddonXML();
				Document newAddonDoc = XMLParser.strToDom(newAddon);
				List<AddonDetails>uninstalledAddons = getAddonDetails(newAddonDoc,AddonStatus.NOT_INSTALLED);
			}
			finally {
				if (newAddon!=null) {
					FileHelper.delete(newAddon);
				}
			}


		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to list XBMC scrapers",e);
		}
		catch (XMLParserException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		}
	}

	private List<AddonDetails> getAddonDetails(Document newAddonDoc, AddonStatus status) throws XMLParserException {
		List<AddonDetails>addonDetails = new ArrayList<AddonDetails>();

		for (Node addon : selectNodeList(newAddonDoc, "/addons/addon")) {
			String id = ((Element)addon).getAttribute("id");
			String version = ((Element)addon).getAttribute("version");
			if (version.length()==0) {
				throw new XMLParserNotFoundException("Unable to find version attribute of plugin '"+id+"'");
			}

			Version newVersion = new Version(version);
			addonDetails.add(new AddonDetails(id, newVersion, status));
		}

		return addonDetails;
	}

	@Override
	public int update() throws XBMCUpdaterException {
		try {
			File oldAddon = new File(addonsDir,"addon.xml");
			File newAddon = downloadLatestAddonXML();

			List<String> pluginList = getListOfPluginsToUpdate(addonsDir,newAddon, oldAddon);

			Document newAddonDoc = XMLParser.strToDom(newAddon);
			Document oldAddonDoc = null;
			if (oldAddon.exists()) {
				oldAddonDoc = XMLParser.strToDom(oldAddon);
			}

			File newPluginsDir = new File(addonsDir,"newplugins");
			if (!newPluginsDir.mkdir() && !newPluginsDir.exists()) {
				throw new XBMCUpdaterException("Unable to create working directory: " +newPluginsDir);
			}

			updatePlugins(newAddonDoc,oldAddonDoc,pluginList,addonsDir,newPluginsDir);

			int count = 0;
			for (File f : newPluginsDir.listFiles()) {
				if (f.isDirectory()) {
					File oldPluginDir = new File(addonsDir,f.getName());
					if (oldPluginDir.exists()) {
						FileHelper.delete(oldPluginDir);
					}
					FileHelper.move(f, oldPluginDir);
					count++;
				}
			}

			FileHelper.delete(newPluginsDir);

			if (oldAddon.exists()) {
				FileHelper.delete(oldAddon);
			}
			FileHelper.move(newAddon, oldAddon);
			return count;
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		}
	}

	protected File downloadLatestAddonXML() throws IOException,
			XBMCUpdaterException {
		File newAddon = new File(addonsDir,"addon.xml.new");

		String actualMD5;
		try {
			actualMD5 = mgr.downloadFile(new URL(UPDATE_SITE_URL),newAddon);

		}
		catch (MalformedURLException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers, bad URL",e);
		}

		String expectedMD5 = readMD5(addonsDir);
		if (!expectedMD5.equals(actualMD5)) {
			if (log.isDebugEnabled()) {
				log.debug("MD5 mismatch ["+expectedMD5+"] != ["+actualMD5+"]");
			}
			throw new XBMCUpdaterException("Unable to check for XBMC Scraper updates, MD5 checksum failed.");
		}
		return newAddon;
	}

	private List<String> getListOfPluginsToUpdate(File addonsDir,
			File newAddon, File oldAddon) throws IOException, XBMCUpdaterException {
		List<String>pluginList;
		if (!oldAddon.exists()) {
			pluginList = getDefaultPlugins();
		}
		else {
			if (checkFilesSame(oldAddon,newAddon)) {
				log.info("No XBMC Scraper updates found.");
				pluginList = new ArrayList<String>();
				return pluginList;
			}
			pluginList = new ArrayList<String>();
			for (AddonDetails addon : getInstalledAddons(addonsDir)) {
				pluginList.add(addon.getId());
			}
		}
		return pluginList;
	}

	private String readMD5(File addonsDir) throws XBMCUpdaterException,
			IOException {
		File newAddonMD5 = new File(addonsDir,"addon.md5.new");
		String expectedMD5;
		try {
			mgr.downloadFile(new URL(UPDATE_SITE_MD5),newAddonMD5);
			expectedMD5 = FileHelper.readFileContents(newAddonMD5).trim();
			FileHelper.delete(newAddonMD5);
		}
		catch (MalformedURLException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers, bad URL",e);
		}
		return expectedMD5;
	}

	private void updatePlugins(Document newAddonDoc,Document oldAddonDoc,
			List<String> pluginList, File addonsDir, File newPluginsDir) throws XMLParserException, XBMCUpdaterException {
		for (String plugin : pluginList) {
			for (AddonDetails addonDetails : getAddonDetails(newAddonDoc,AddonStatus.NOT_INSTALLED)) {

				 Version oldVersion = null;
				 if (oldAddonDoc!=null) {
					 oldVersion = new Version(getStringFromXML(oldAddonDoc, "/addons/addon[@id='"+plugin+"']/@version"));
				 }
				 if (oldVersion == null || addonDetails.getVersion().compareTo(oldVersion)>0) {
					 downloadNewPlugin(plugin,newPluginsDir,addonDetails.getVersion());
				 }
			}

			List<String> requiredPlugins = getRequiredPlugins(newAddonDoc, plugin);
			updatePlugins(newAddonDoc, oldAddonDoc, requiredPlugins, addonsDir, newPluginsDir);
		}
	}

	private List<String> getRequiredPlugins(Document newAddonDoc, String plugin)
			throws XMLParserException {
		List<String>requiredPlugins = new ArrayList<String>();
		for (Node addon : selectNodeList(newAddonDoc, "/addons/addon[@id='"+plugin+"']/requires/import/@addon")) {
			requiredPlugins.add(addon.getNodeValue());
		}
		return requiredPlugins;
	}

	private void downloadNewPlugin(String plugin,File newPluginsDir,Version version) throws XBMCUpdaterException {
		try {
			if (new File(newPluginsDir,plugin).exists()) {
				return ;
			}

			String filename = plugin+"-"+version.toString()+".zip";
			URL url = new URL(UPDATE_SITE_DATA_DIR+"/"+plugin+"/"+filename);
			File zipFile = new File(newPluginsDir,filename);
			mgr.downloadFile(url,zipFile);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(zipFile);
				FileHelper.unzip(fis, newPluginsDir);
				if (!new File(newPluginsDir,plugin).exists()) {
					throw new XBMCUpdaterException("Failed to unzip plugin '"+zipFile+"'");
				}
				FileHelper.delete(zipFile);
			}
			finally {
				if (fis!=null) {
					fis.close();
				}
			}
			log.info("Downloaded new plugin '"+plugin+"' version="+version.toString());
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to download new pluign : " + plugin,e);
		}
	}

	private List<AddonDetails> getInstalledAddons(File addonsDir) throws XBMCUpdaterException {
		File[] dirs = addonsDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		List<AddonDetails>plugins = new ArrayList<AddonDetails>();
		for (File dir : dirs) {
			File pluginXml = new File(dir,"addon.xml");
			if (pluginXml.exists()) {
				try {
					Document d = XMLParser.strToDom(pluginXml);
					String strVersion = getStringFromXML(d,"/addon/@version");
					plugins.add(new AddonDetails(dir.getName(),new Version(strVersion),AddonStatus.INSTALLED));
				}
				catch (XMLParserException e) {
					throw new XBMCUpdaterException("Unable to reader plugin version",e);
				}
				catch (IOException e) {
					throw new XBMCUpdaterException("Unable to reader plugin version",e);
				}

			}
		}

		return plugins;
	}

	private List<String> getDefaultPlugins() {
		List<String>defaultPlugins = new ArrayList<String>();
		defaultPlugins.add("metadata.themoviedb.org");
		defaultPlugins.add("metadata.tvdb.com");
		return defaultPlugins;
	}

	private boolean checkFilesSame(File file1, File file2) throws IOException {
		InputStream is1 = null;
		InputStream is2 = null;
		if(file1.length() != file2.length()) {
			return false;
		}

		try {
			is1 = new FileInputStream(file1);
			is2 = new FileInputStream(file2);

			return inputStreamEquals(is1, is2);

		} catch (Exception ei) {
			return false;
		} finally {
			if(is1 != null) {
				is1.close();
			}
			if(is2 != null) {
				is2.close();
			}
		}

	}

	private static boolean inputStreamEquals(InputStream is1, InputStream is2) {
		int bufsize = 1024;
		byte buff1[] = new byte[bufsize];
		byte buff2[] = new byte[bufsize];


		if(is1 == is2) {
			return true;
		}
		if(is1 == null && is2 == null) {
			return true;
		}
		if(is1 == null || is2 == null) {
			return false;
		}
		try {
			int read1 = -1;
			int read2 = -1;

			do {
				int offset1 = 0;
				while (offset1 < bufsize && (read1 = is1.read(buff1, offset1, bufsize-offset1)) >= 0) {
            		offset1 += read1;
        		}

				int offset2 = 0;
				while (offset2 < bufsize && (read2 = is2.read(buff2, offset2, bufsize-offset2)) >= 0) {
					offset2 += read2;
        		}
				if(offset1 != offset2) {
					return false;
				}
				if(offset1 != bufsize) {
					Arrays.fill(buff1, offset1, bufsize, (byte)0);
					Arrays.fill(buff2, offset2, bufsize, (byte)0);
				}
				if(!Arrays.equals(buff1, buff2)) {
					return false;
				}
			} while(read1 >= 0 && read2 >= 0);
			if(read1 < 0 && read2 < 0)
			 {
				return true;	// both at EOF
			}
			return false;

		} catch (IOException ei) {
			return false;
		}
	}


}

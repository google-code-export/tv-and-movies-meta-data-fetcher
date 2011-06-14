package org.stanwood.media.source.xbmc.updater;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.Version;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to install, remove or update XBMC addons
 */
public class XBMCWebUpdater extends XMLParser implements IXBMCUpdater {

	private final static Log log = LogFactory.getLog(XBMCWebUpdater.class);

	private XBMCAddonManager mgr;

	private File addonsDir;

	private String updateSiteDataDir;
	private String updateSiteAddonsURL;
	private String updateSiteAddonsMD5URL;

	/**
	 * The constructor
	 * @param config The Media directory configuration
	 * @throws XBMCException Thrown if their is a problem reading from the configuration
	 */
	public XBMCWebUpdater(ConfigReader config) throws XBMCException {
		try {
			updateSiteDataDir = config.getXBMCAddonSiteUrl();
			updateSiteAddonsURL = updateSiteDataDir+"/addons.xml";
			updateSiteAddonsMD5URL = updateSiteDataDir+"/addons.xml.md5";
			addonsDir = config.getXBMCAddonDir();
		}
		catch (ConfigException e) {
			throw new XBMCException("Unable to get XBMC addon directory",e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setAddonManager(XBMCAddonManager mgr) {
		this.mgr =mgr;
	}

	/** {@inheritDoc} */
	@Override
	public Set<AddonDetails> listAddons(IConsole console) throws XBMCUpdaterException {
		try {
			File newAddon = null;
			try {
				newAddon = downloadLatestAddonXML();
				Document newAddonDoc = XMLParser.strToDom(newAddon);
				List<AddonDetails>uninstalledAddons = getAddonDetails(newAddonDoc,AddonStatus.NOT_INSTALLED);
				SortedSet<AddonDetails>addons = getInstalledAddons(addonsDir);
				for (AddonDetails uninstalledAddon : uninstalledAddons) {
					AddonDetails installedAddon = findAddon(addons, uninstalledAddon.getId());
					if (installedAddon==null){
						uninstalledAddon.setInstalledVersion(null);
						addons.add(uninstalledAddon);
					}
					else {
						if (uninstalledAddon.getAvaliableVersion().compareTo(installedAddon.getInstalledVersion()) > 0) {
							installedAddon.setStatus(AddonStatus.OUT_OF_DATE);
							installedAddon.setAvaliableVersion(uninstalledAddon.getAvaliableVersion());
						}
					}
				}

				return addons;
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

	/** {@inheritDoc} */
	@Override
	public int installAddons(IConsole console,Set<String>addonIds) throws XBMCException {
		try {
			File newAddon = null;
			try {
				newAddon = downloadLatestAddonXML();
				Set<AddonDetails> installedPlugins = getInstalledAddons(addonsDir);
				Document newAddonDoc = XMLParser.strToDom(newAddon);

				Set<String>requiredPlugins = new HashSet<String>();

				for (String pluginId : addonIds) {
					Version version =  new Version(getStringFromXML(newAddonDoc, "/addons/addon[@id='"+pluginId+"']/@version"));
					findUninstalledRequiredPlugins(pluginId,version, newAddonDoc, requiredPlugins,installedPlugins);
				}

				return installPlugins(console,requiredPlugins,newAddon);
			}
			finally {
				if (newAddon!=null) {
					FileHelper.delete(newAddon);
				}
			}
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to install XBMC scrapers",e);
		}
		catch (XMLParserException e) {
			throw new XBMCUpdaterException("Unable to install XBMC scrapers",e);
		}


	}

	private void findUninstalledRequiredPlugins(String pluginId,Version version,Document newAddonDoc,Set<String>requiredPlugins,Set<AddonDetails>installedAddons) throws XMLParserException {
		AddonDetails installedAddon = findAddon(installedAddons, pluginId);
		if (installedAddon==null || installedAddon.getInstalledVersion().compareTo(version) < 0) {
			requiredPlugins.add(pluginId);
		}

		List<AddonDetails> required = getRequiredPlugins(newAddonDoc, pluginId);
		for (AddonDetails r : required) {
			findUninstalledRequiredPlugins(r.getId(),r.getAvaliableVersion(),newAddonDoc,requiredPlugins,installedAddons);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int uninstallAddons(IConsole console,Set<String>addonIds) throws XBMCUpdaterException {
		Set<AddonDetails> installedPlugins = getInstalledAddons(addonsDir);

		Set<String>toUnistall = new HashSet<String>();
		for (String pluginId : addonIds) {
			findDependantAddons(pluginId, installedPlugins , toUnistall);
		}

		for (String id : toUnistall) {
			removeAddon(console,id);
		}
		return toUnistall.size();
	}

	private void removeAddon(IConsole console,String id) throws XBMCUpdaterException {
		console.info("Uninstalling addon " + id);
		File addonDir = new File(addonsDir,id);
		if (addonDir.isDirectory()) {
			try {
				FileHelper.delete(addonDir);
			} catch (IOException e) {
				throw new XBMCUpdaterException("Unable to unistall addon directory:" + addonDir,e);
			}
		}
		else {
			throw new XBMCUpdaterException("Unable to find addon directory:" + addonDir);
		}

	}

	private void findDependantAddons(String pluginId,Set<AddonDetails> installedPlugins, Set<String> toUnistall) {
		toUnistall.add(pluginId);

		for (AddonDetails ad : installedPlugins) {
			if (ad.getRequiredAddons()!=null) {
				if (ad.getRequiredAddons().contains(pluginId)) {
					findDependantAddons(ad.getId(),installedPlugins,toUnistall);
				}
			}
		}
	}

	private AddonDetails findAddon(Set<AddonDetails> addons,String id) {
		for (AddonDetails a : addons) {
			if (a.getId().equals(id)) {
				return a;
			}
		}
		return null;
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

			List<AddonDetails> requiredPlugins = getRequiredPlugins(newAddonDoc, id);
			AddonDetails ad = new AddonDetails(id, newVersion,newVersion, status);
			ad.setRequiredAddons(getAddonDetailsIds(requiredPlugins));
			addonDetails.add(ad);
		}

		return addonDetails;
	}

	private Set<String>getAddonDetailsIds(List<AddonDetails>addonDetails) {
		Set<String>ids = new HashSet<String>();
		for (AddonDetails ad : addonDetails) {
			ids.add(ad.getId());
		}
		return ids;
	}

	private int installPlugins(IConsole console,Set<String> plugins,File newAddon) throws XBMCException {
		mgr.unregisterAddons();
		try {
			File oldAddon = new File(addonsDir,"addon.xml");
			return updatePlugins(console,oldAddon, newAddon, plugins);
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int update(IConsole console,Set<String> pluginList) throws XBMCException {
		mgr.unregisterAddons();
		try {
			File oldAddon = new File(addonsDir,"addon.xml");
			File newAddon = downloadLatestAddonXML();

			return updatePlugins(console,oldAddon, newAddon, pluginList);
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int update(IConsole console) throws XBMCException {
		mgr.unregisterAddons();
		try {
			File oldAddon = new File(addonsDir,"addon.xml");
			File newAddon = downloadLatestAddonXML();

			Set<String> pluginList = getListOfPluginsToUpdate(console,addonsDir,newAddon, oldAddon);

			return updatePlugins(console,oldAddon, newAddon, pluginList);
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		}
	}

	protected int updatePlugins(IConsole console,File oldAddon, File newAddon,
			Set<String> plugins) throws XMLParserException, IOException,
			XBMCUpdaterException, XBMCException {

		Document newAddonDoc = XMLParser.strToDom(newAddon);
		Document oldAddonDoc = null;
		if (oldAddon.exists()) {
			oldAddonDoc = XMLParser.strToDom(oldAddon);
		}

		File newPluginsDir = new File(addonsDir,"newplugins");
		if (!newPluginsDir.mkdir() && !newPluginsDir.exists()) {
			throw new XBMCUpdaterException("Unable to create working directory: " +newPluginsDir);
		}
		try {
			List<AddonDetails> uninstalledAddons = getAddonDetails(newAddonDoc,AddonStatus.NOT_INSTALLED);
			updatePlugins(console,uninstalledAddons,newAddonDoc,oldAddonDoc,plugins,addonsDir,newPluginsDir);

			int count = 0;
			for (File f : newPluginsDir.listFiles()) {
				if (f.isDirectory()) {
					File oldPluginDir = new File(addonsDir,f.getName());
					if (oldPluginDir.exists()) {
						FileHelper.delete(oldPluginDir);
					}
					FileHelper.move(f, oldPluginDir);
					console.info("Installed plugin '"+f.getName()+"'");
					count++;
				}
			}

			FileHelper.delete(newPluginsDir);

			if (oldAddon.exists()) {
				FileHelper.delete(oldAddon);
			}
			FileHelper.move(newAddon, oldAddon);
			mgr.registerAddons();
			return count;
		}
		catch (XBMCUpdaterException e) {
			// Something went wrong, so tidy up
			try {
				if (newPluginsDir.exists()) {
					FileHelper.delete(newPluginsDir);
				}
			}
			catch (IOException e1) {
				log.error("Unable to delete file",e);
			}
			try {
				if (newAddon.exists()) {
					FileHelper.delete(newAddon);
				}
			}
			catch (IOException e1) {
				log.error("Unable to delete file",e);
			}
			throw e;
		}
	}

	protected File downloadLatestAddonXML() throws IOException,
			XBMCUpdaterException {
		File newAddon = new File(addonsDir,"addon.xml.new");

		String actualMD5;
		try {
			actualMD5 = mgr.downloadFile(new URL(updateSiteAddonsURL),newAddon);
		}
		catch (MalformedURLException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers, bad URL: "+updateSiteAddonsURL,e);
		}

		String expectedMD5 = readMD5(addonsDir);
		if (expectedMD5!=null && !expectedMD5.equals(actualMD5)) {
			if (log.isDebugEnabled()) {
				log.debug("MD5 mismatch ["+expectedMD5+"] != ["+actualMD5+"]");
			}
			throw new XBMCUpdaterException("Unable to check for XBMC Scraper updates, MD5 checksum failed.");
		}
		return newAddon;
	}

	private Set<String> getListOfPluginsToUpdate(IConsole console,File addonsDir,File newAddon, File oldAddon) throws IOException, XBMCUpdaterException {
		Set<String>pluginList;
		if (!oldAddon.exists()) {
			pluginList = getDefaultPlugins();
		}
		else {
			if (checkFilesSame(oldAddon,newAddon)) {
				console.info("No XBMC Scraper updates found.");
				pluginList = new HashSet<String>();
				return pluginList;
			}
			pluginList = new HashSet<String>();
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
			mgr.downloadFile(new URL(updateSiteAddonsMD5URL),newAddonMD5);
			expectedMD5 = FileHelper.readFileContents(newAddonMD5).trim();
			FileHelper.delete(newAddonMD5);
		}
		catch (MalformedURLException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers, bad URL: " +updateSiteAddonsMD5URL,e);
		}
		catch (IOException e) {
			log.error("Unable to download md5, so can't check downloaded file is correct: " +updateSiteAddonsMD5URL ,e);
			expectedMD5 = null;
		}
		return expectedMD5;
	}

	private void updatePlugins(IConsole console,List<AddonDetails> uninstalledAddons,Document newAddonDoc,Document oldAddonDoc,
			Set<String> pluginList, File addonsDir, File newPluginsDir) throws XMLParserException, XBMCUpdaterException {
		for (String plugin : pluginList) {
			Set<String> requiredPlugins = null;
			for (AddonDetails addonDetails : uninstalledAddons) {
				if (addonDetails.getId().equals(plugin)) {
					requiredPlugins = addonDetails.getRequiredAddons();
					Version oldVersion = null;
					if (oldAddonDoc!=null) {
						oldVersion = new Version(getStringFromXML(oldAddonDoc, "/addons/addon[@id='"+plugin+"']/@version"));
					}
					if (oldVersion == null || addonDetails.getStatus()==AddonStatus.NOT_INSTALLED || addonDetails.getAvaliableVersion().compareTo(oldVersion)>0) {
						downloadNewPlugin(console,plugin,newPluginsDir,addonDetails.getAvaliableVersion());
					}
				}
			}

			if (requiredPlugins!=null) {
				updatePlugins(console,uninstalledAddons,newAddonDoc, oldAddonDoc, requiredPlugins, addonsDir, newPluginsDir);
			}
		}
	}

	private List<AddonDetails> getRequiredPlugins(Document newAddonDoc, String plugin) throws XMLParserException {

		List<AddonDetails>requiredPlugins = new ArrayList<AddonDetails>();
		for (Node addon : selectNodeList(newAddonDoc, "/addons/addon[@id='"+plugin+"']/requires/import")) {
			String strVersion = ((Element)addon).getAttribute("version");
			String rId = ((Element)addon).getAttribute("addon");
			AddonDetails ad = new AddonDetails(rId,new Version(strVersion), new Version(strVersion),AddonStatus.NOT_INSTALLED);
			requiredPlugins.add(ad);
		}
		return requiredPlugins;
	}

	private void downloadNewPlugin(IConsole console,String plugin,File newPluginsDir,Version version) throws XBMCUpdaterException {
		String filename = plugin+"-"+version.toString()+".zip";
		File zipFile = new File(newPluginsDir,filename);
		try {
			if (new File(newPluginsDir,plugin).exists()) {
				return ;
			}

			URL url = new URL(updateSiteDataDir+"/"+plugin+"/"+filename);

			mgr.downloadFile(url,zipFile);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(zipFile);
				FileHelper.unzip(fis, newPluginsDir);
				if (!new File(newPluginsDir,plugin).exists()) {
					throw new XBMCUpdaterException("Failed to unzip plugin '"+zipFile+"'");
				}
				FileHelper.delete(zipFile);
				console.info("Downloaded plugin '"+plugin+"' version="+version.toString());
			}
			finally {
				if (fis!=null) {
					fis.close();
				}
			}
		}
		catch (IOException e) {
			if (zipFile.exists()) {
				try {
					FileHelper.delete(zipFile);
				} catch (IOException e1) {
					log.error("Unable to delete file:" + zipFile);
				}
			}
			throw new XBMCUpdaterException("Unable to download new pluign : " + plugin,e);
		}
	}

	private SortedSet<AddonDetails> getInstalledAddons(File addonsDir) throws XBMCUpdaterException {
		File[] dirs = addonsDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		SortedSet<AddonDetails>plugins = new TreeSet<AddonDetails>(new Comparator<AddonDetails>() {
			@Override
			public int compare(AddonDetails o1, AddonDetails o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		for (File dir : dirs) {
			File pluginXml = new File(dir,"addon.xml");
			if (pluginXml.exists()) {
				try {
					Document d = XMLParser.strToDom(pluginXml);
					String strVersion = getStringFromXML(d,"/addon/@version");
					AddonDetails ad = new AddonDetails(dir.getName(),new Version(strVersion),new Version(strVersion),AddonStatus.INSTALLED);
					Set<String>required = new HashSet<String>();
					for (Node node : selectNodeList(d, "/addon/requires/import")) {
						required.add(((Element)node).getAttribute("addon"));
//						((Element)node).getAttribute("version");
					}
					ad.setRequiredAddons(required);
					plugins.add(ad);
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

	private Set<String> getDefaultPlugins() {
		Set<String>defaultPlugins = new HashSet<String>();
		defaultPlugins.add("metadata.themoviedb.org");
		defaultPlugins.add("metadata.tvdb.com");
		defaultPlugins.add("metadata.imdb.com");
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

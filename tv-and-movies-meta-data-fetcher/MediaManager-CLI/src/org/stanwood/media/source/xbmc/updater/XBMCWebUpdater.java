package org.stanwood.media.source.xbmc.updater;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
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
			updateSiteAddonsURL = updateSiteDataDir+"/addons.xml"; //$NON-NLS-1$
			updateSiteAddonsMD5URL = updateSiteDataDir+"/addons.xml.md5"; //$NON-NLS-1$
			addonsDir = config.getXBMCAddonDir();
		}
		catch (ConfigException e) {
			throw new XBMCException(Messages.getString("XBMCWebUpdater.UNABLE_GET_ADDON_DIR"),e); //$NON-NLS-1$
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
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_LIST_SCRAPPERS"),e); //$NON-NLS-1$
		}
		catch (XMLParserException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPERS"),e); //$NON-NLS-1$
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
					Version version =  new Version(getStringFromXML(newAddonDoc, "/addons/addon[@id='"+pluginId+"']/@version")); //$NON-NLS-1$ //$NON-NLS-2$
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
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.7"),e); //$NON-NLS-1$
		}
		catch (XMLParserException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.8"),e); //$NON-NLS-1$
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
		console.info(Messages.getString("XBMCWebUpdater.9") + id); //$NON-NLS-1$
		File addonDir = new File(addonsDir,id);
		if (addonDir.isDirectory()) {
			try {
				FileHelper.delete(addonDir);
			} catch (IOException e) {
				throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.10") + addonDir,e); //$NON-NLS-1$
			}
		}
		else {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.11") + addonDir); //$NON-NLS-1$
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

		for (Node addon : selectNodeList(newAddonDoc, "/addons/addon")) { //$NON-NLS-1$
			String id = ((Element)addon).getAttribute("id"); //$NON-NLS-1$
			String version = ((Element)addon).getAttribute("version"); //$NON-NLS-1$
			if (version.length()==0) {
				throw new XMLParserNotFoundException(MessageFormat.format(Messages.getString("XBMCWebUpdater.UNABLE_FIND_VERSION_ATTRIB"),id)); //$NON-NLS-1$
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
			return updatePlugins(console, newAddon, plugins);
		}
		catch (IOException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPE"),e); //$NON-NLS-1$
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPE"),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public int update(IConsole console,Set<String> pluginList) throws XBMCException {
		mgr.unregisterAddons();
		try {
			File newAddon = downloadLatestAddonXML();
			return updatePlugins(console, newAddon, pluginList);
		}
		catch (IOException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPE"),e); //$NON-NLS-1$
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPE"),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public int update(IConsole console) throws XBMCException {
		mgr.unregisterAddons();
		try {
			File newAddon = downloadLatestAddonXML();

			Set<String> pluginList = getListOfPluginsToUpdate(console,addonsDir,newAddon);

			return updatePlugins(console, newAddon, pluginList);
		}
		catch (IOException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPE"),e); //$NON-NLS-1$
		} catch (XMLParserException e) {
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_UPDATE_SCRAPPE"),e); //$NON-NLS-1$
		}
	}

	protected int updatePlugins(IConsole console, File newAddon,Set<String> plugins) throws XMLParserException, IOException,
			XBMCUpdaterException, XBMCException {

		Document newAddonDoc = XMLParser.strToDom(newAddon);

		File newPluginsDir = new File(addonsDir,"newplugins"); //$NON-NLS-1$
		if (!newPluginsDir.mkdir() && !newPluginsDir.exists()) {
			throw new XBMCUpdaterException(MessageFormat.format(Messages.getString("XBMCWebUpdater.UNABLE_CREATE_WORKING_DIR"),newPluginsDir)); //$NON-NLS-1$
		}
		try {
			List<AddonDetails> uninstalledAddons = getAddonDetails(newAddonDoc,AddonStatus.NOT_INSTALLED);
			updatePlugins(console,uninstalledAddons,newAddonDoc,plugins,addonsDir,newPluginsDir);

			int count = 0;
			for (File f : newPluginsDir.listFiles()) {
				if (f.isDirectory()) {
					File oldPluginDir = new File(addonsDir,f.getName());
					if (oldPluginDir.exists()) {
						FileHelper.delete(oldPluginDir);
					}
					FileHelper.move(f, oldPluginDir);
					console.info(MessageFormat.format(Messages.getString("XBMCWebUpdater.INSTALL_PLUGIN"),f.getName())); //$NON-NLS-1$
					count++;
				}
			}

			FileHelper.delete(newPluginsDir);

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
				log.error(MessageFormat.format(Messages.getString("XBMCWebUpdater.UNABLE_DELTE_DIR"),newPluginsDir),e); //$NON-NLS-1$
			}

			throw e;
		}
		finally {
			try {
				if (newAddon.exists()) {
					FileHelper.delete(newAddon);
				}
			}
			catch (IOException e1) {
				log.error(MessageFormat.format(Messages.getString("XBMCWebUpdater.UNABLE_DELETE_FILE"),newAddon),e1); //$NON-NLS-1$
			}
		}
	}

	protected File downloadLatestAddonXML() throws IOException,
			XBMCUpdaterException {
		File newAddon = new File(addonsDir,"addon.xml.new"); //$NON-NLS-1$

		String actualMD5;
		try {
			actualMD5 = mgr.downloadFile(new URL(updateSiteAddonsURL),newAddon);
		}
		catch (MalformedURLException e) {
			throw new XBMCUpdaterException(MessageFormat.format(Messages.getString("XBMCWebUpdater.BAD_URL"),updateSiteAddonsURL),e); //$NON-NLS-1$
		}

		String expectedMD5 = readMD5(addonsDir);
		if (expectedMD5!=null && !expectedMD5.equals(actualMD5)) {
			if (log.isDebugEnabled()) {
				log.debug("MD5 mismatch ["+expectedMD5+"] != ["+actualMD5+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_CHECK_UPDATES")); //$NON-NLS-1$
		}
		return newAddon;
	}

	private Set<String> getListOfPluginsToUpdate(IConsole console,File addonsDir,File newAddon) throws IOException, XBMCUpdaterException {
		Set<String>pluginList;
		if ((!addonsDir.exists()) || getAddonCount(addonsDir)==0) {
			pluginList = getDefaultPlugins();
		}
		else {
//			if (checkFilesSame(oldAddon,newAddon)) {
//				console.info("No XBMC Scraper updates found.");
//				pluginList = new HashSet<String>();
//				return pluginList;
//			}
			pluginList = new HashSet<String>();
			for (AddonDetails addon : getInstalledAddons(addonsDir)) {
				pluginList.add(addon.getId());
			}
		}
		return pluginList;
	}

	private String readMD5(File addonsDir) throws XBMCUpdaterException,
			IOException {
		File newAddonMD5 = new File(addonsDir,"addon.md5.new"); //$NON-NLS-1$
		String expectedMD5;
		try {
			mgr.downloadFile(new URL(updateSiteAddonsMD5URL),newAddonMD5);
			expectedMD5 = FileHelper.readFileContents(newAddonMD5).trim();
			FileHelper.delete(newAddonMD5);
		}
		catch (MalformedURLException e) {
			throw new XBMCUpdaterException(MessageFormat.format(Messages.getString("XBMCWebUpdater.BAD_URL"),updateSiteAddonsMD5URL),e); //$NON-NLS-1$
		}
		catch (IOException e) {
			log.error(MessageFormat.format(Messages.getString("XBMCWebUpdater.UNABLE_DOWNLOAD_MD5"),updateSiteAddonsMD5URL) ,e); //$NON-NLS-1$
			expectedMD5 = null;
		}
		return expectedMD5;
	}

	private void updatePlugins(IConsole console,List<AddonDetails> uninstalledAddons,Document newAddonDoc,
			Set<String> pluginList, File addonsDir, File newPluginsDir) throws XMLParserException, XBMCUpdaterException {
		for (String plugin : pluginList) {
			Set<String> requiredPlugins = null;
			for (AddonDetails addonDetails : uninstalledAddons) {
				if (addonDetails.getId().equals(plugin)) {
					requiredPlugins = addonDetails.getRequiredAddons();
					Version oldVersion = null;
					AddonDetails ad = readPluginDetails(addonsDir,plugin);
					if (ad!=null) {
						oldVersion = ad.getInstalledVersion();
					}
					if (oldVersion == null || addonDetails.getAvaliableVersion().compareTo(oldVersion)>0) {
						downloadNewPlugin(console,plugin,newPluginsDir,addonDetails.getAvaliableVersion());
					}
				}
			}

			if (requiredPlugins!=null) {
				updatePlugins(console,uninstalledAddons,newAddonDoc, requiredPlugins, addonsDir, newPluginsDir);
			}
		}
	}

	private AddonDetails readPluginDetails(File addonsDir, String pluginId) throws XBMCUpdaterException {
		for (File dir : addonsDir.listFiles()) {
			if (dir.isDirectory() && dir.getName().startsWith(pluginId)) {
				return readPluginDetails(dir);
			}
		}
		return null;
	}

	private List<AddonDetails> getRequiredPlugins(Document newAddonDoc, String plugin) throws XMLParserException {

		List<AddonDetails>requiredPlugins = new ArrayList<AddonDetails>();
		for (Node addon : selectNodeList(newAddonDoc, "/addons/addon[@id='"+plugin+"']/requires/import")) { //$NON-NLS-1$ //$NON-NLS-2$
			String strVersion = ((Element)addon).getAttribute("version"); //$NON-NLS-1$
			String rId = ((Element)addon).getAttribute("addon"); //$NON-NLS-1$
			AddonDetails ad = new AddonDetails(rId,new Version(strVersion), new Version(strVersion),AddonStatus.NOT_INSTALLED);
			requiredPlugins.add(ad);
		}
		return requiredPlugins;
	}

	private void downloadNewPlugin(IConsole console,String plugin,File newPluginsDir,Version version) throws XBMCUpdaterException {
		String filename = plugin+"-"+version.toString()+".zip"; //$NON-NLS-1$ //$NON-NLS-2$
		File zipFile = new File(newPluginsDir,filename);
		try {
			if (new File(newPluginsDir,plugin).exists()) {
				return ;
			}

			URL url = new URL(updateSiteDataDir+"/"+plugin+"/"+filename); //$NON-NLS-1$ //$NON-NLS-2$

			mgr.downloadFile(url,zipFile);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(zipFile);
				FileHelper.unzip(fis, newPluginsDir);
				if (!new File(newPluginsDir,plugin).exists()) {
					throw new XBMCUpdaterException(MessageFormat.format(Messages.getString("XBMCWebUpdater.FAILED_TO_UNZIP"),zipFile)); //$NON-NLS-1$
				}
				FileHelper.delete(zipFile);
				console.info(MessageFormat.format(Messages.getString("XBMCWebUpdater.DOWNLOAD_PLUGIN"),plugin,version.toString())); //$NON-NLS-1$
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
					log.error(Messages.getString("XBMCWebUpdater.UNABLE_DELETE_FILE1") + zipFile); //$NON-NLS-1$
				}
			}
			throw new XBMCUpdaterException(MessageFormat.format(Messages.getString("XBMCWebUpdater.UNABLE_DOWNLOAD_NEW_PLUGIN"),plugin),e); //$NON-NLS-1$
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
			AddonDetails ad = readPluginDetails(dir);
			if (ad!=null) {
				plugins.add(ad);
			}
		}

		return plugins;
	}

	private int getAddonCount(File addonDir) {
		int count = 0;
		for (File f : addonDir.listFiles()) {
			if (f.isDirectory() && !f.getName().equals("newplugins")) { //$NON-NLS-1$
				count++;
			}
		}
		return count;
	}

	private AddonDetails readPluginDetails(File dir) throws XBMCUpdaterException {
		AddonDetails ad = null;
		File pluginXml = new File(dir,"addon.xml"); //$NON-NLS-1$
		if (pluginXml.exists()) {

			try {
				Document d = XMLParser.strToDom(pluginXml);
				String strVersion = getStringFromXML(d,"/addon/@version"); //$NON-NLS-1$
				ad = new AddonDetails(dir.getName(),new Version(strVersion),new Version(strVersion),AddonStatus.INSTALLED);
				Set<String>required = new HashSet<String>();
				for (Node node : selectNodeList(d, "/addon/requires/import")) { //$NON-NLS-1$
					required.add(((Element)node).getAttribute("addon")); //$NON-NLS-1$
				}
				ad.setRequiredAddons(required);

			}
			catch (XMLParserException e) {
				throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_READ_PLUGIN_VERSION"),e); //$NON-NLS-1$
			}
			catch (IOException e) {
				throw new XBMCUpdaterException(Messages.getString("XBMCWebUpdater.UNABLE_READ_PLUGIN_VERSION"),e); //$NON-NLS-1$
			}

		}
		return ad;
	}

	private Set<String> getDefaultPlugins() {
		Set<String>defaultPlugins = new HashSet<String>();
		defaultPlugins.add("metadata.themoviedb.org"); //$NON-NLS-1$
		defaultPlugins.add("metadata.tvdb.com"); //$NON-NLS-1$
		defaultPlugins.add("metadata.imdb.com"); //$NON-NLS-1$
		return defaultPlugins;
	}
}

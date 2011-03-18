package org.stanwood.media.source.xbmc.updater;

import java.util.List;
import java.util.Set;

import org.stanwood.media.source.xbmc.XBMCAddonManager;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;


/**
 * This interface should be implemented by classes that can update the instance XBMC addons
 */
public interface IXBMCUpdater {

	/**
	 * Used to get a list of installed and uninstalled addons
	 * @return The list of addons
	 * @throws XBMCUpdaterException thrown if their are any problems
	 */
	public Set<AddonDetails> listAddons(IConsole console) throws XBMCUpdaterException;

	public int update(IConsole console) throws XBMCUpdaterException, XBMCException;

	public int update(IConsole console,Set<String> pluginList) throws XBMCException;

	public void setAddonManager(XBMCAddonManager xbmcAddonManager);

	public int installAddons(IConsole console,List<String>addonIds) throws XBMCException;

	public int uninstallAddons(IConsole console,List<String>addonIds) throws XBMCUpdaterException;


}

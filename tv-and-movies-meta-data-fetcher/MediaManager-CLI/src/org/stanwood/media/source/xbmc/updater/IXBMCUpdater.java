package org.stanwood.media.source.xbmc.updater;

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
	 * @param console The console used to send messages to
	 * @return The list of addons
	 * @throws XBMCUpdaterException thrown if their are any problems
	 */
	public Set<AddonDetails> listAddons(IConsole console) throws XBMCUpdaterException;

	/**
	 * Update all addons to the latest version
	 * @param console The console used to send messages to
	 * @return The number of addones updated
	 * @throws XBMCUpdaterException thrown if their are any problems with the updater
	 * @throws XBMCException Thrown if their are any other problems
	 */
	public int update(IConsole console) throws XBMCUpdaterException, XBMCException;

	/**
	 * Update a list of addons to the latest version and the addons they depend on
	 * @param console The console used to send messages to
	 * @param addonList The list of addons to update
	 * @return The number of addones updated
	 * @throws XBMCUpdaterException thrown if their are any problems with the updater
	 * @throws XBMCException Thrown if their are any other problems
	 */
	public int update(IConsole console,Set<String> addonList) throws XBMCException;

	/**
	 * Register the XBMC manager with the updater
	 * @param xbmcAddonManager the XBMC manager
	 */
	public void setAddonManager(XBMCAddonManager xbmcAddonManager);

	/**
	 * Used to install a list of addons
	 * @param console The console used to send messages to
	 * @param addonIds a list of addon ID's to install
	 * @return The number of installed addons
	 * @throws XBMCException Thrown if their are any problems
	 */
	public int installAddons(IConsole console,Set<String>addonIds) throws XBMCException;

	/**
	 * Used to uninstall a list of addons and any that depend on these addons
	 * @param console The console used to send messages to
	 * @param addonIds a list of addon ID's to uninstall
	 * @return The number of uninstalled addons
	 * @throws XBMCUpdaterException Thrown if their are any problems
	 */
	public int uninstallAddons(IConsole console,Set<String>addonIds) throws XBMCUpdaterException;


}

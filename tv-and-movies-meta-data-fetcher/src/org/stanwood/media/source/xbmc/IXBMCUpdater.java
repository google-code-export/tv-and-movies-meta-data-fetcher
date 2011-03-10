package org.stanwood.media.source.xbmc;

import java.util.List;


/**
 * This interface should be implemented by classes that can update the instance XBMC addons
 */
public interface IXBMCUpdater {

	/**
	 * Used to get a list of installed and uninstalled addons
	 * @return The list of addons
	 * @throws XBMCUpdaterException thrown if their are any problems
	 */
	public List<AddonDetails> listAddons() throws XBMCUpdaterException;

	public int update() throws XBMCUpdaterException, XBMCException;

	public void setAddonManager(XBMCAddonManager xbmcAddonManager);
}

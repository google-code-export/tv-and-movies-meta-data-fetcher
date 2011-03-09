package org.stanwood.media.source.xbmc;


/**
 * This interface should be implemented by classes that can update the instance XBMC addons
 */
public interface IXBMCUpdater {

	public int update() throws XBMCUpdaterException;

	public void setAddonManager(XBMCAddonManager xbmcAddonManager);
}

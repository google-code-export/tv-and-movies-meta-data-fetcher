package org.stanwood.media.source.xbmc;

import java.io.File;

/**
 * This interface should be implemented by classes that can update the instance XBMC addons
 */
public interface IXBMCUpdater {

	public int update(File addonsDir) throws XBMCUpdaterException;

	public void setAddonManager(XBMCAddonManager xbmcAddonManager);
}

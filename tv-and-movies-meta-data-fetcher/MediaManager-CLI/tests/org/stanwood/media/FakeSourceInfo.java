/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media;

import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;
import org.stanwood.media.source.xbmc.XBMCAddonManager;

/**
 * Source information for the fake source
 */
public class FakeSourceInfo extends ExtensionInfo<FakeSource> {

	private String addonId;
	private XBMCAddonManager mgr;

	/**
	 * The constructor
	 */
	public FakeSourceInfo() {
		super(FakeSource.class.getName(),ExtensionType.SOURCE,new ParameterType[0]);

	}

	/**
	 * Used to set the addon id
	 * @param addonId The addon id
	 */
	public void setAddonId(String addonId) {
		this.addonId = addonId;
	}

	/**
	 * Used to set the XBMC addon manager
	 * @param mgr the XBMC addon manager
	 */
	public void setMgr(XBMCAddonManager mgr) {
		this.mgr = mgr;
	}

	@Override
	protected FakeSource createExtension() throws ExtensionException {
		return new FakeSource(mgr,addonId);
	}

}

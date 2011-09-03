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

public class FakeSourceInfo extends ExtensionInfo<FakeSource> {

	private String addonId;
	private XBMCAddonManager mgr;

	public FakeSourceInfo(XBMCAddonManager mgr, String addonId) {
		super(FakeSource.class.getName(),ExtensionType.SOURCE,new ParameterType[0]);
		this.addonId = addonId;
		this.mgr = mgr;
	}

	@Override
	protected FakeSource createExtension() throws ExtensionException {
		return new FakeSource(mgr,addonId);
	}

}

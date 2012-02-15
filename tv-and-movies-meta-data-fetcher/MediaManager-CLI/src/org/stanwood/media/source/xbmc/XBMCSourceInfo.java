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
package org.stanwood.media.source.xbmc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;
import org.stanwood.media.source.xbmc.expression.Value;
import org.stanwood.media.source.xbmc.expression.ValueType;

/**
 * Extension information about the source {@link XBMCSource}
 */
public class XBMCSourceInfo extends ExtensionInfo<XBMCSource>{

	private XBMCAddon addon;

	/**
	 * The constructor
	 * @param mgr The XBMC addon manager
	 * @param addon The addon
	 * @throws XBMCException Thrown if their are any problems
	 */
	public XBMCSourceInfo(XBMCAddonManager mgr,XBMCAddon addon) throws XBMCException {
		super(ExtensionType.SOURCE);
		this.addon = addon;
		setId(XBMCSource.class.getName()+"#"+addon.getId()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParameterType[] getParameterInfos() {
		Map<String, Value> settings = addon.getSettings();
		List<ParameterType>types = new ArrayList<ParameterType>();
		for (Entry<String,Value> e : settings.entrySet()) {
			if (e.getValue().getType().equals(ValueType.INTEGER)) {
				types.add(new ParameterType(e.getKey(),Integer.class,false));
			}
			else if (e.getValue().getType().equals(ValueType.BOOLEAN)) {
				types.add(new ParameterType(e.getKey(),Boolean.class,false));
			} else {
				types.add(new ParameterType(e.getKey(),String.class,false));
			}
		}
		return types.toArray(new ParameterType[types.size()]);
	}

	@Override
	protected XBMCSource createExtension() throws ExtensionException {
		return new XBMCSource(this,addon.getManager(), addon.getId());
	}


}

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

import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ParameterType;

public class XBMCSourceInfo extends ExtensionInfo<XBMCSource>{

	public XBMCSourceInfo(Class<? extends XBMCSource> extension,
			ParameterType[] parameterInfos) {
		super(extension, parameterInfos);
	}

//	public ParameterType[] getParameters() {
//		Map<String, Value> settings = addon.getSettings();
//		List<ParameterType>types = new ArrayList<ParameterType>();
//		for (Entry<String,Value> e : settings.entrySet()) {
//			if (e.getValue().getType().equals(ValueType.INTEGER)) {
//				types.add(new ParameterType(e.getKey(),Integer.class,false));
//			}
//			else if (e.getValue().getType().equals(ValueType.BOOLEAN)) {
//				types.add(new ParameterType(e.getKey(),Boolean.class,false));
//			} else {
//				types.add(new ParameterType(e.getKey(),String.class,false));
//			}
//		}
//		return types.toArray(new ParameterType[types.size()]);
//	}

}

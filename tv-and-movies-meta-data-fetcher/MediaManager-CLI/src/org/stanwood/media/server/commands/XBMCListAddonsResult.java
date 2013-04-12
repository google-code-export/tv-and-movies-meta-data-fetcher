/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.server.commands;

import java.util.Set;

import org.stanwood.media.source.xbmc.updater.AddonDetails;

public class XBMCListAddonsResult implements ICommandResult {

	private Set<AddonDetails> addons;

	public XBMCListAddonsResult(Set<AddonDetails> addons) {
		this.addons = addons;
	}

	public Set<AddonDetails>getAddons() {
		return addons;
	}
}

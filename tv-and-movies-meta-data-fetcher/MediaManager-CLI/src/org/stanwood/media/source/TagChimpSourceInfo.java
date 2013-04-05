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
package org.stanwood.media.source;

import org.stanwood.media.Controller;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

/**
 * The {@link TagChimpSource} source information
 */
public class TagChimpSourceInfo extends ExtensionInfo<TagChimpSource> {

	private final static ParameterType PARAM_KEY_XBMC_SOURCE_ID = new ParameterType("xbmcSourceId",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_TYPES[] = new ParameterType[]{PARAM_KEY_XBMC_SOURCE_ID};

	/**
	 * The constructor
	 */
	public TagChimpSourceInfo() {
		super(TagChimpSource.class.getName(),ExtensionType.SOURCE, PARAM_TYPES);
	}

	/** {@inheritDoc} */
	@Override
	protected TagChimpSource createExtension(Controller controller) {
		return new TagChimpSource(this);
	}
}

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

import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

/**
 * Extension information about the source {@link HybridFilmSource}
 */
public class HybridFilmSourceInfo extends ExtensionInfo<HybridFilmSource> {

	final static ParameterType PARAM_KEY_SOURCE_ID = new ParameterType("sourceId",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_TYPES[] = new ParameterType[]{PARAM_KEY_SOURCE_ID};

	/**
	 * The constructor
	 */
	public HybridFilmSourceInfo() {
		super(HybridFilmSource.class.getName(),ExtensionType.SOURCE, PARAM_TYPES);
	}

	@Override
	protected HybridFilmSource createExtension() throws ExtensionException {
		return new HybridFilmSource(this);
	}

}

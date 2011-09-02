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
package org.stanwood.media.store.mp4;

import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ParameterType;

public class MP4ITunesStoreInfo extends ExtensionInfo<MP4ITunesStore> {

	public static final ParameterType PARAM_MP4FILE_KEY = new ParameterType("mp4file",String.class,false); //$NON-NLS-1$
	public static final ParameterType PARAM_MP4TAGS_KEY = new ParameterType("mp4tags",String.class,false); //$NON-NLS-1$
	public static final ParameterType PARAM_MP4INFO_KEY = new ParameterType("mp4info",String.class,false); //$NON-NLS-1$
	public static final ParameterType PARAM_MP4ART_KEY = new ParameterType("mp4art",String.class,false); //$NON-NLS-1$
	public static final ParameterType PARAM_MANAGER_KEY = new ParameterType("manager",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_TYPES[] = new ParameterType[]{PARAM_MP4FILE_KEY,PARAM_MP4TAGS_KEY,PARAM_MP4INFO_KEY,PARAM_MP4ART_KEY,PARAM_MANAGER_KEY};

	public MP4ITunesStoreInfo() {
		super(MP4ITunesStore.class, PARAM_TYPES);
	}

}

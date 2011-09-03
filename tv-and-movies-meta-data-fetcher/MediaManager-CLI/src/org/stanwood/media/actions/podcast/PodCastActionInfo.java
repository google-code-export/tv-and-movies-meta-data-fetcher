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
package org.stanwood.media.actions.podcast;

import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

public class PodCastActionInfo extends ExtensionInfo<PodCastAction>{

	public final static ParameterType PARAM_MEDIA_DIR_URL = new ParameterType("mediaDirURL",String.class,true); //$NON-NLS-1$
	public final static ParameterType PARAM_NUMBER_ENTRIES = new ParameterType("numberEntries",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_FILE_LOCATION = new ParameterType("fileLocation",String.class,true); //$NON-NLS-1$
	public final static ParameterType PARAM_RESTRICT_PATTERN = new ParameterType("restrictPattern",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_EXTENSIONS_KEY = new ParameterType("extensions",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_FEED_TITLE_KEY = new ParameterType("feedTitle",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_FEED_DESCRIPTION_KEY = new ParameterType("feedDescription",String.class,false); //$NON-NLS-1$

	private final static ParameterType PARAM_TYPES[] = {PARAM_MEDIA_DIR_URL,PARAM_NUMBER_ENTRIES,PARAM_FILE_LOCATION,
		                                                PARAM_RESTRICT_PATTERN,PARAM_EXTENSIONS_KEY,PARAM_FEED_TITLE_KEY,
		                                                PARAM_FEED_DESCRIPTION_KEY};

	public PodCastActionInfo() {
		super(PodCastAction.class.getName(),ExtensionType.ACTION, PARAM_TYPES);
	}

	@Override
	protected PodCastAction createExtension() {
		return new PodCastAction();
	}
}

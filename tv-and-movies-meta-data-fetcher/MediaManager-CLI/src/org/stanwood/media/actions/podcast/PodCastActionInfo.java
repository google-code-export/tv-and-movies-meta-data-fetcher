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

/**
 * Extension information about the action {@link PodCastAction}
 */
public class PodCastActionInfo extends ExtensionInfo<PodCastAction>{

	/** Parameter type information for the parameter that specifies the the URL used to find the root media directory */
	public final static ParameterType PARAM_MEDIA_DIR_URL = new ParameterType("mediaDirURL",String.class,true); //$NON-NLS-1$
	/** Parameter type information for the parameter that specifies the maximum number of entries in the feed*/
	public final static ParameterType PARAM_NUMBER_ENTRIES = new ParameterType("numberEntries",String.class,false); //$NON-NLS-1$
	/** Parameter type information for the parameter that specifies the location of the RSS feed relative to the root of the media directory. It can contain standard rename patterns with the value*/
	public final static ParameterType PARAM_FILE_LOCATION = new ParameterType("fileLocation",String.class,true); //$NON-NLS-1$
	/** Parameter type information for the parameter that can be used to restrict the media files */
	public final static ParameterType PARAM_RESTRICT_PATTERN = new ParameterType("restrictPattern",String.class,false); //$NON-NLS-1$
	/** Parameter type information for the parameter that specifies a comma separated list of media file extensions to accept */
	public final static ParameterType PARAM_EXTENSIONS_KEY = new ParameterType("extensions",String.class,false); //$NON-NLS-1$
	/** Parameter type information for the parameter that specifies a title to the RSS feed */
	public final static ParameterType PARAM_FEED_TITLE_KEY = new ParameterType("feedTitle",String.class,false); //$NON-NLS-1$
	/** Parameter type information for the parameter that specifies a description to the RSS feed */
	public final static ParameterType PARAM_FEED_DESCRIPTION_KEY = new ParameterType("feedDescription",String.class,false); //$NON-NLS-1$

	private final static ParameterType PARAM_TYPES[] = {PARAM_MEDIA_DIR_URL,PARAM_NUMBER_ENTRIES,PARAM_FILE_LOCATION,
		                                                PARAM_RESTRICT_PATTERN,PARAM_EXTENSIONS_KEY,PARAM_FEED_TITLE_KEY,
		                                                PARAM_FEED_DESCRIPTION_KEY};

	/**
	 * The constructor
	 */
	public PodCastActionInfo() {
		super(PodCastAction.class.getName(),ExtensionType.ACTION, PARAM_TYPES);
	}

	@Override
	protected PodCastAction createExtension() {
		return new PodCastAction();
	}
}

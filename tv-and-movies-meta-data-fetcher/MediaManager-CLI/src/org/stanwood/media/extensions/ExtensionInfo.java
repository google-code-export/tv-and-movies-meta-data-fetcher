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
package org.stanwood.media.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.stanwood.media.setup.MediaDirConfig;


/**
 * Used to describe extensions to the media manager
 * @param <T> The type of the extension
 */
public abstract class ExtensionInfo<T extends IExtension> {

	private ParameterType parameterInfos[];
	private String id;
	private Map<CacheKey,T> extension = new HashMap<CacheKey,T>();
	private ExtensionType type;

	/**
	 * The constructor
	 * @param type The type of the extension
	 */
	public ExtensionInfo(ExtensionType type)
	{
		this.type = type;
	}

	/**
	 * The constructor
	 * @param id The id of the extension
	 * @param parameterInfos The parameter info
	 * @param type The type of the extension
	 */
	public ExtensionInfo(String id,ExtensionType type,ParameterType[] parameterInfos) {
		super();
		if (parameterInfos==null) {
			throw new NullPointerException("Parameter types cannot be null"); //$NON-NLS-1$
		}
		this.parameterInfos = parameterInfos.clone();
		this.id = id;
		this.type = type;
	}

	/**
	 * Used to get the extension class type
	 * @param config The media directory configuration
	 * @param number The index number of the extension
	 * @return The extension class type
	 * @throws ExtensionException  Thrown if their is a problem creating the extension
	 */
	public T getExtension(MediaDirConfig config,int number) throws ExtensionException {
		CacheKey cacheKey = new CacheKey(config,number);
		if (extension.get(new CacheKey(config,number))==null) {
			extension.put(cacheKey,createExtension());
		}
		return extension.get(cacheKey);
	}

	/**
	 * Used to create the extension
	 * @return The extension
	 * @throws ExtensionException Thrown if their is a problem
	 */
	protected abstract T createExtension() throws ExtensionException;

	/**
	 * Used to get any extension from the media directory configuration. So if their are
	 * multiple XBMCSources, then it will just pick one of them.
	 * @param config The media directory configuration
	 * @return The extension
	 * @throws ExtensionException Thrown if their is a problem getting the extension
	 */
	public T getAnyExtension(MediaDirConfig config) throws ExtensionException {
		for (Entry<CacheKey,T>ext : extension.entrySet()) {
			if (ext.getKey().getConfig().equals(config)) {
				return ext.getValue();
			}
		}
		return createExtension();
	}

	/**
	 * Used to get information on the parameters
	 * @return The parameter information
	 */
	public ParameterType[] getParameterInfos() {
		return parameterInfos.clone();
	}

	/**
	 * Used to set the parameter information
	 * @param parameterInfos The parameter information
	 */
	protected void setParameterInfos(ParameterType[] parameterInfos) {
		this.parameterInfos = parameterInfos;
	}

	/**
	 * Used to get the id of the extension
	 * @return The id of the extension
	 */
	public String getId() {
		return id;
	}

	/**
	 * Used to set the id of the extension
	 * @param id The id of the extension
	 */
	protected void setId(String id) {
		this.id = id;
	}

	/**
	 * Used to get the type of the extension
	 * @return The type of the extension
	 */
	public ExtensionType getType() {
		return type;
	}


}

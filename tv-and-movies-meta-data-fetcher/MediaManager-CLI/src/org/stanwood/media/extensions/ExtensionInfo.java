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


/**
 * Used to describe extensions to the media manager
 * @param <T> The type of the extension
 */
public abstract class ExtensionInfo<T extends IExtension> {

	private ParameterType parameterInfos[];
	private String id;
	private T extension = null;
	private ExtensionType type;

	public ExtensionInfo(ExtensionType type)
	{
		this.type = type;
	}

	/**
	 * The constructor
	 * @param id The id of the extension
	 * @param parameterInfos The parameter info
	 */
	public ExtensionInfo(String id,ExtensionType type,ParameterType[] parameterInfos) {
		super();
		this.parameterInfos = parameterInfos;
		this.id = id;
		this.type = type;
	}

	/**
	 * Used to get the extension class type
	 * @return The extension class type
	 * @throws ExtensionException  Thrown if their is a problem creating the extension
	 */
	public T getExtension() throws ExtensionException {
		if (extension==null) {
			extension = createExtension();
		}
		return extension;
	}

	protected abstract T createExtension() throws ExtensionException;

	/**
	 * Used to get information on the parameters
	 * @return The parameter information
	 */
	public ParameterType[] getParameterInfos() {
		return parameterInfos;
	}

	/**
	 * Used to set the parameter information
	 * @param parameterInfos The parameter information
	 */
	protected void setParameterInfos(ParameterType[] parameterInfos) {
		this.parameterInfos = parameterInfos;
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public ExtensionType getType() {
		return type;
	}


}

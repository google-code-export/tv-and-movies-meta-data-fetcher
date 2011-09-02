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
public class ExtensionInfo<T extends IExtension> {

	private Class<? extends T>extension;
	private ParameterType parameterInfos[];

	/**
	 * The constructor
	 * @param extension The extension class type
	 * @param parameterInfos The parameter info
	 */
	public ExtensionInfo(Class<? extends T> extension,
			ParameterType[] parameterInfos) {
		super();
		this.extension = extension;
		this.parameterInfos = parameterInfos;
	}

	/**
	 * Used to get the extension class type
	 * @return The extension class type
	 */
	public Class<? extends T> getExtension() {
		return extension;
	}

	/**
	 * Used to set the extension class type
	 * @param extension The extension class type
	 */
	public void setExtension(Class<? extends T> extension) {
		this.extension = extension;
	}

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
	public void setParameterInfos(ParameterType[] parameterInfos) {
		this.parameterInfos = parameterInfos;
	}
}

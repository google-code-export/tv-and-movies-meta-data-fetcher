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
package org.stanwood.media.actions.command;

import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ParameterType;

public class ExecuteSystemCommandActionInfo extends ExtensionInfo<ExecuteSystemCommandAction> {

	public final static ParameterType PARAM_CMD_ON_FILE_KEY = new ParameterType("commandOnFile",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_CMD_ON_DIR_KEY = new ParameterType("commandOnDirectory",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_EXTENSIONS_KEY = new ParameterType("extensions",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_NEW_FILE_KEY = new ParameterType("newFile",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_DELETED_FILE_KEY = new ParameterType("deletedFile",String.class,false); //$NON-NLS-1$
	public final static ParameterType PARAM_ABORT_IF_FILE_EXISTS = new ParameterType("abortIfFileExists",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_TYPES[] = {PARAM_CMD_ON_FILE_KEY,PARAM_CMD_ON_DIR_KEY,PARAM_EXTENSIONS_KEY,PARAM_NEW_FILE_KEY,PARAM_DELETED_FILE_KEY,PARAM_ABORT_IF_FILE_EXISTS};

	public ExecuteSystemCommandActionInfo() {
		super(ExecuteSystemCommandAction.class, PARAM_TYPES);
	}

}

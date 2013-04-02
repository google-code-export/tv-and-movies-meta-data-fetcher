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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.stanwood.media.Controller;
import org.stanwood.media.progress.IProgressMonitor;

public abstract class AbstractServerCommand {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface param {
		String name();
		String description();
	}

	private Controller controller;

	public AbstractServerCommand(Controller controller) {
		this.controller = controller;
	}

	public Controller getController() {
		return this.controller;
	}

	public abstract boolean execute(ICommandLogger logger,IProgressMonitor monitor);
}

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
package org.stanwood.media.cli.importer;

import org.apache.commons.logging.Log;
import org.stanwood.media.server.commands.ICommandLogger;

public class CLICommandLogger implements ICommandLogger {

	private Log log;

	public CLICommandLogger(Log log) {
		this.log = log;
	}

	@Override
	public void trace(String message, Throwable t) {
		log.trace(message,t);
	}

	@Override
	public void trace(String message) {
		log.trace(message);
	}

	@Override
	public void debug(String message, Throwable t) {
		log.debug(message,t);
	}

	@Override
	public void debug(String message) {
		log.debug(message);
	}

	@Override
	public void error(String message, Throwable t) {
		log.error(message,t);
	}

	@Override
	public void error(String message) {
		log.error(message);
	}

	@Override
	public void warn(String message) {
		log.warn(message);
	}

	@Override
	public void info(String message) {
		log.info(message);
	}

}

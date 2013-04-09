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

/**
 * Used to log output from commands to a commons logger
 */
public class CLICommandLogger implements ICommandLogger {

	private Log log;

	/**
	 * The constructor
	 * @param log The log
	 */
	public CLICommandLogger(Log log) {
		this.log = log;
	}

	/**
	 * Used to log a trace message with exception
	 * @param message The message
	 * @param t The exception
	 */
	@Override
	public void trace(String message, Throwable t) {
		log.trace(message,t);
	}

	/**
	 * Used to log a trace message
	 * @param message The message
	 */
	@Override
	public void trace(String message) {
		log.trace(message);
	}

	/**
	 * Used to log a debug message with exception
	 * @param message The message
	 * @param t The exception
	 */
	@Override
	public void debug(String message, Throwable t) {
		log.debug(message,t);
	}

	/**
	 * Used to log a debug message
	 * @param message The message
	 */
	@Override
	public void debug(String message) {
		log.debug(message);
	}

	/**
	 * Used to log a error message with exception
	 * @param message The message
	 * @param t The exception
	 */
	@Override
	public void error(String message, Throwable t) {
		log.error(message,t);
	}

	/**
	 * Used to log a error message
	 * @param message The message
	 */
	@Override
	public void error(String message) {
		log.error(message);
	}

	/**
	 * Used to log a warn message
	 * @param message The message
	 */
	@Override
	public void warn(String message) {
		log.warn(message);
	}

	/**
	 * Used to log a info message
	 * @param message The message
	 */
	@Override
	public void info(String message) {
		log.info(message);
	}

}

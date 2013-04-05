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

/**
 * Used to log output from commands
 */
public interface ICommandLogger {

	/**
	 * Log trace message
	 * @param message The message
	 * @param t an exception to log
	 */
	public void trace(String message,Throwable t);

	/**
	 * Log trace message
	 * @param message The message
	 */
	public void trace(String message);

	/**
	 * Log debug message
	 * @param message The message
	 * @param t an exception to log
	 */
	public void debug(String message,Throwable t);

	/**
	 * Log a debug message
	 * @param message The message
	 */
	public void debug(String message);


	/**
	 * Log error message
	 * @param message The message
	 * @param t an exception to log
	 */
	public void error(String message,Throwable t);

	/**
	 * Log a debug message
	 * @param message The message
	 */
	public void error(String message);

	/**
	 * Log a warning message
	 * @param message The message
	 */
	public void warn(String message);

	/**
	 * Log a info message
	 * @param message The message
	 */
	public void info(String message);
}

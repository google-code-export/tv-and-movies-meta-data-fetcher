/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.logging;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.RootLogger;

/**
 * This class is used to help with setting up the logging.
 */
public class LogSetupHelper {

	/**
	 * Initialise the logging using the given configuration file
	 * @param logConfigFile The logging configuration file
	 */
	public static void initLogingFromConfigFile(File logConfigFile) {
		RootLogger.getRootLogger().removeAllAppenders();
		PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
	}

	/**
	 * Initialise the logging using the configuration file stored in the same package as this class.
	 * @param configName The name of the configuration file stored in the same package as this class.
	 */
	public static void initLogingInternalConfigFile(String configName) {
		RootLogger.getRootLogger().removeAllAppenders();
		PropertyConfigurator.configure(LogSetupHelper.class.getResource(configName));
	}

}

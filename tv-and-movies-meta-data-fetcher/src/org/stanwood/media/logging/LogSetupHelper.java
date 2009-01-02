package org.stanwood.media.logging;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class is used to help with setting up the logging. 
 */
public class LogSetupHelper {	
	
	/**
	 * Initialise the logging using the given configuration file
	 * @param logConfigFile The logging configuration file
	 */
	public static void initLogingFromConfigFile(File logConfigFile) {
		PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
	}

	/**
	 * Initialise the logging using the configuration file stored in the same package as this class.
	 * @param configName The name of the configuration file stored in the same package as this class.
	 */
	public static void initLogingInternalConfigFile(String configName) {
		PropertyConfigurator.configure(LogSetupHelper.class.getResource(configName));
	}

}

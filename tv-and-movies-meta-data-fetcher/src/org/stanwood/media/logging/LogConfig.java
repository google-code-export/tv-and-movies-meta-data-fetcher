package org.stanwood.media.logging;

/**
 * Logging options usable from the command line
 */
public enum LogConfig {

	/** The info log configuration */
	INFO("info.log4j.properties"),
	/** The info with exceptions log configuration */
	INFOEX("infoex.log4j.properties"),
	/** The error log configuration */
	ERROR("error.log4j.properties"),
	/** The error with exceptions log configuration */
	ERROREX("errorex.log4j.properties"),
	/** The debug log configuration */
	DEBUG("debug.log4j.properties"),
	/** Don't change the logging settings */
	NOINIT("");

	private String filename;

	private LogConfig(String filename) {
		this.filename = filename;
	}

	/**
	 * Get the log configuration filename
	 * @return the log configuration filename
	 */
	public String getFilename() {
		return filename;
	}
 }

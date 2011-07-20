package org.stanwood.media.logging;

/**
 * Logging options usable from the command line
 */
public enum LogConfig {

	/** The info log configuration */
	INFO("info.log4j.properties"), //$NON-NLS-1$
	/** The info with exceptions log configuration */
	INFOEX("infoex.log4j.properties"), //$NON-NLS-1$
	/** The error log configuration */
	ERROR("error.log4j.properties"), //$NON-NLS-1$
	/** The error with exceptions log configuration */
	ERROREX("errorex.log4j.properties"), //$NON-NLS-1$
	/** The debug log configuration */
	DEBUG("debug.log4j.properties"), //$NON-NLS-1$
	/** Don't change the logging settings */
	NOINIT(""); //$NON-NLS-1$

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

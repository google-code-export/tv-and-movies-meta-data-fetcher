package org.stanwood.media.source.xbmc.updater;

/**
 * A interface to output consoles, used by the updater
 */
public interface IConsole {

	/**
	 * Send a error message to the console
	 * @param error The error message
	 */
	public void error(String error);

	/**
	 * Send a info message to the console
	 * @param info The info message
	 */
	public void info(String info);
}

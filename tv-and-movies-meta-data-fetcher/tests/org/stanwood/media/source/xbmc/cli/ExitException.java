package org.stanwood.media.source.xbmc.cli;

/**
 * Used by tests to capture the exit code of a application
 */
public class ExitException extends RuntimeException {

	private int exitCode;

	/**
	 * The constructor
	 * @param exitCode The exit code
	 */
	public ExitException(int exitCode) {
		this.exitCode = exitCode;
	}

	/**
	 * Used to get the exit code
	 * @return The exit code
	 */
	public int getExitCode() {
		return exitCode;
	}


}

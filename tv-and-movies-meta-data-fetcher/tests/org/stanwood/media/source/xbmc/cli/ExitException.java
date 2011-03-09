package org.stanwood.media.source.xbmc.cli;

public class ExitException extends RuntimeException {

	private int exitCode;

	public ExitException(int exitCode) {
		this.exitCode = exitCode;
	}

	public int getExitCode() {
		return exitCode;
	}


}

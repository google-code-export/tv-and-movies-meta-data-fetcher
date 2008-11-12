package org.stanwood.tv.renamer;

/**
 * This is used to handle the program exiting. It is mainly used by tests so that
 * they can override the default beheaviour of calling System.exit.
 */
public interface IExitHandler {

	/**
	 * This is called when the app wants to exit
	 * @param exitCode The exit code it's exiting with.
	 */
	public void exit(int exitCode);
}

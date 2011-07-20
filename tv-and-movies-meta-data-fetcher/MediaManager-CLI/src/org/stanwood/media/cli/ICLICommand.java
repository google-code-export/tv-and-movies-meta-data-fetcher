package org.stanwood.media.cli;


/**
 * All CLI launchers must implement this interface
 */
public interface ICLICommand {

	/**
	 * The name of the command
	 * @return The name
	 */
	public String getName();

}

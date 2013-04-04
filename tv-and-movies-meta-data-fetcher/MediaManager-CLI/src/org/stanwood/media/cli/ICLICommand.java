package org.stanwood.media.cli;

import org.stanwood.media.Controller;


/**
 * All CLI launchers must implement this interface
 */
public interface ICLICommand {

	/**
	 * The name of the command
	 * @return The name
	 */
	public String getName();

	public Controller getController();

}

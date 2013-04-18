package org.stanwood.media.actions;

import java.io.File;

/**
 * This is implemented by the action performer so that actions can notify it about changes
 * in the media directory.
 */
public interface IActionEventHandler {

	/**
	 * Called when a new file is created in the media directory
	 * @param file The file that was created
	 * @throws ActionException Thrown if their are any problems
	 */
	public void sendEventNewFile(File file) throws ActionException;

	/**
	 * Called when a file in the media directory is deleted
	 * @param file The file what was deleted
	 * @throws ActionException Thrown if their are any problems
	 */
	public void sendEventDeletedFile(File file) throws ActionException;

	/**
	 * This is called when a file in the media directory is renamed
	 * @param oldName The old name of the file
	 * @param newName The new name of the file
	 * @throws ActionException Thrown if their are any problems
	 */
	public void sendEventRenamedFile(File oldName,File newName) throws ActionException;

	/**
	 * This is called when a file in a media directory is about to be renamed
	 * @param oldName The old name of the file
	 * @param newName The new name of the file
	 * @throws ActionException Thrown if their are any problems
	 */
	public void sendEventAboutToRenamedFile(File oldName,File newName) throws ActionException;
}

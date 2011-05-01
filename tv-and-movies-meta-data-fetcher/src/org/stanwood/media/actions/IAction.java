package org.stanwood.media.actions;

import java.io.File;
import java.util.List;

import org.stanwood.media.MediaDirectory;

/**
 * This interface should be implemented by classes that are used to represent actions
 * that can be performed on media files in a media directory
 */
public interface IAction {

	/**
	 * Used to perform the action. If a new file is created then it should be added
	 * to the files list. If it's removed by the action, then it should also be removed from the files list.
	 * @param dir File media directory the files belongs to
	 * @param files The media files
	 * @throws ActionException Thrown if their is a problem with the action
	 */
	public void perform(MediaDirectory dir, List<File> files) throws ActionException;

	/**
	 * Used to set the value of actions parameter
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws ActionException Thrown if their is a problem setting the parameter
	 */
	public void setParameter(String key, String value) throws ActionException;

	public void setTestMode(boolean testMode);
}

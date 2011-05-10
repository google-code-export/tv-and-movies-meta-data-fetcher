package org.stanwood.media.actions;

import java.io.File;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;

/**
 * This interface should be implemented by classes that are used to represent actions
 * that can be performed on media files in a media directory
 */
public interface IAction {

	public void init(MediaDirectory dir) throws ActionException;

	/**
	 * Used to perform the action. If a new file is created then it should be added
	 * to the files list. If it's removed by the action, then it should also be removed from the files list.
	 * @param dir File media directory the files belongs to
	 * @param actionEventHandler Just to notify the action performer about changes
	 * @throws ActionException Thrown if their is a problem with the action
	 */
	public void perform(MediaDirectory dir,Episode episode, File file,IActionEventHandler actionEventHandler) throws ActionException;

	public void perform(MediaDirectory dir,Film film, File file,Integer part,IActionEventHandler actionEventHandler) throws ActionException;

	/**
	 * Used to set the value of actions parameter
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws ActionException Thrown if their is a problem setting the parameter
	 */
	public void setParameter(String key, String value) throws ActionException;

	void setTestMode(boolean testMode);

	boolean isTestMode();

	public void performOnDirectory(MediaDirectory dir, File file,IActionEventHandler actionEventHandler) throws ActionException;

	public void finished(MediaDirectory dir) throws ActionException;
}

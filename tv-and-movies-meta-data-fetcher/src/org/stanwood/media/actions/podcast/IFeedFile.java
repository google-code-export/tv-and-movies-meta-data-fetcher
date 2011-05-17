package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * This should be implemented by files that are to be inserted into a RSS feed
 */
public interface IFeedFile {

	/**
	 * Used to get the content type
	 * @return the content Type
	 */
	public String getContentType();

	/**
	 * Used to get the location of the file
	 * @return the location of the file
	 */
	public File getFile();

	/**
	 * Used to get the date the file was last modified
	 * @return the date the file was last modified
	 */
	public Date getLastModified();

	/**
	 * Used to get the title of the file
	 * @return the title of the file
	 */
	public String getTitle();

	/**
	 * Used to get the URL of the file
	 * @return the URL of the file
	 */
	public URL getLink();

	/**
	 * Used to get the description of the file
	 * @return the description of the file
	 */
	public String getDescription();

}

package org.stanwood.media.actions.podcast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.MessageFormat;

import org.stanwood.media.actions.ActionException;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.util.FileHelper;

/**
 * Used to create feed files
 */
public class FeedFileFactory {

	/**
	 * Create a feed file
	 * @param file The location of the file
	 * @param dirConfig The media directory configuration where the file lives
	 * @param media The media file information
	 * @param baseUrl The base URL of the feed
	 * @return The feed file
	 * @throws ActionException Thrown if their is a problem creating the feed or a file type is not supported
	 */
	public static IFeedFile createFile(File file,MediaDirConfig dirConfig, IVideo media, String baseUrl) throws ActionException {
		return createFile(file,dirConfig,media.getTitle(),media.getSummary(),baseUrl);
	}

	/**
	 * Create a feed file
	 * @param file The location of the file
	 * @param dirConfig The media directory configuration where the file lives
	 * @param title The title of the file
	 * @param description The description of the file
	 * @param baseUrl The base URL of the feed
	 * @return The feed file
	 * @throws ActionException Thrown if their is a problem creating the feed or a file type is not supported
	 */
	public static IFeedFile createFile(File file,MediaDirConfig dirConfig, String title,String description, String baseUrl) throws ActionException {
		try {
			String ext = FileHelper.getExtension(file).toLowerCase();
			if (ext.equals("mp4")) { //$NON-NLS-1$
				return new VideoFeedFile(file,dirConfig,title,description,baseUrl,"video/mp4"); //$NON-NLS-1$
			}
			else if (ext.equals("m4v")) {
				return new VideoFeedFile(file,dirConfig,title,description,baseUrl,"video/x-m4v"); //$NON-NLS-1$
			}
			else if (ext.equals("avi")) {
				return new VideoFeedFile(file,dirConfig,title,description,baseUrl,"video/avi"); //$NON-NLS-1$
			}
			else if (ext.equals("mkv")) {
				return new VideoFeedFile(file,dirConfig,title,description,baseUrl,"video/x-matroska"); //$NON-NLS-1$
			}
			throw new ActionException(MessageFormat.format("Unsupport file format '{0}' of file '{1}'",ext,file.getAbsolutePath()));
		}
		catch (MalformedURLException e) {
			throw new ActionException("Unable to create media file URL",e);
		} catch (UnsupportedEncodingException e) {
			throw new ActionException("Unable to create media file URL",e);
		}
	}
}

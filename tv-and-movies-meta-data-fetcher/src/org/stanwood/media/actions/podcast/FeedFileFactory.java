package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.MalformedURLException;

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
		try {
			String ext = FileHelper.getExtension(file).toLowerCase();
			if (ext.equals("mp4")) {
				return new VideoFeedFile(file,dirConfig,media,baseUrl,"video/mp4");
			}
			else if (ext.equals("m4v")) {
				return new VideoFeedFile(file,dirConfig,media,baseUrl,"video/x-m4v");
			}
			else if (ext.equals("avi")) {
				return new VideoFeedFile(file,dirConfig,media,baseUrl,"video/avi");
			}
			else if (ext.equals("mkv")) {
				return new VideoFeedFile(file,dirConfig,media,baseUrl,"video/x-matroska");
			}
			throw new ActionException("Unsupport file format '"+ext+"' of file '"+file.getAbsolutePath()+"'");
		}
		catch (MalformedURLException e) {
			throw new ActionException("Unable to create media file URL",e);
		}
	}
}

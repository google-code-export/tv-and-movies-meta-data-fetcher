package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.MalformedURLException;

import org.stanwood.media.actions.ActionException;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.util.FileHelper;

public class FeedFileFactory {

	public static IFeedFile createFile(File file,MediaDirConfig dirConfig, IVideo media, String baseUrl) throws ActionException {
		try {
			String ext = FileHelper.getExtension(file).toLowerCase();
			if (ext.equals("mp4")) {
				return new VideoFeedFile(file,dirConfig,media,baseUrl,"video/mp4");
			}
			else if (ext.equals("m4v")) {
				return new VideoFeedFile(file,dirConfig,media,baseUrl,"video/x-m4v");
			}
			throw new ActionException("Unsupport file format '"+ext+"' of file '"+file.getAbsolutePath()+"'");
		}
		catch (MalformedURLException e) {
			throw new ActionException("Unable to create media file URL",e);
		}
	}
}

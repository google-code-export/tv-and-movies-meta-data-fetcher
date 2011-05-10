package org.stanwood.media.actions.podcast;

import java.io.File;

import org.stanwood.media.actions.ActionException;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.util.FileHelper;

public class FeedFileFactory {

	public static IFeedFile createFile(File file, IVideo media) throws ActionException {
		String ext = FileHelper.getExtension(file).toLowerCase();
		if (ext.equals("mp4")) {
			return new MP4FeedFile(file,media);
		}
		else if (ext.equals("m4v")) {
			return new MP4FeedFile(file,media);
		}
		throw new ActionException("Unsupport file format '"+ext+"' of file '"+file.getAbsolutePath()+"'");
	}
}

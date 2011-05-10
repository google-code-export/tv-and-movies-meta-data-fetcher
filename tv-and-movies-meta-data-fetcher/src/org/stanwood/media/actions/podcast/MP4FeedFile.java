package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.stanwood.media.model.IVideo;

public class MP4FeedFile implements IFeedFile {

	private File file;
	private IVideo media;

	public MP4FeedFile(File file, IVideo media) {
		this.file = file;
		this.media = media;
	}

	@Override
	public String getContentType() {
		return "audio/mpeg";
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public Date getLastModified() {
		return new Date(file.lastModified());
	}

	@Override
	public String getTitle() {
		return media.getTitle();
	}

	@Override
	public URL getLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		return media.getSummary();
	}

}

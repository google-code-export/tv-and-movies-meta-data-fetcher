package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.stanwood.media.model.IVideo;
import org.stanwood.media.setup.MediaDirConfig;

public class VideoFeedFile implements IFeedFile {

	private File file;
	private IVideo media;
	private URL url;
	private String contentType;

	public VideoFeedFile(File file,MediaDirConfig dirConfig, IVideo media, String baseUrl,String contentType) throws MalformedURLException {
		this.file = file;
		this.media = media;
		this.contentType =contentType;

		String relPath = makePathRelativeToMediaDir(file,dirConfig.getMediaDir());
		if (!File.separator.equals("/")) {
			relPath = relPath.replaceAll("\\"+File.separator, "/");
		}
		url = new URL(baseUrl+"/"+relPath);
	}

	@Override
	public String getContentType() {
		return contentType;
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

	private String makePathRelativeToMediaDir(File episodeFile, File rootMediaDir) {
		String path = rootMediaDir.getAbsolutePath();
		int len = path.length();
		if (episodeFile.getAbsolutePath().startsWith(path)) {
			return episodeFile.getAbsolutePath().substring(len+1);
		}
		else {
			return episodeFile.getAbsolutePath();
		}
	}

	@Override
	public URL getLink() {
		return url;
	}

	@Override
	public String getDescription() {
		return media.getSummary();
	}

}

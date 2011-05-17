package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.stanwood.media.model.IVideo;
import org.stanwood.media.setup.MediaDirConfig;

/**
 * A vidoe base feed file
 */
public class VideoFeedFile implements IFeedFile {

	private File file;
	private IVideo media;
	private URL url;
	private String contentType;

	/**
	 * The constructor
	 * @param file The location of the file
	 * @param dirConfig The media directory configuration
	 * @param media The media file information
	 * @param baseUrl The base URL of the feed
	 * @param contentType The content type of the file
	 * @throws MalformedURLException Thrown if their is a problem create URL's
	 */
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

	/** {@inheritDoc} */
	@Override
	public String getContentType() {
		return contentType;
	}

	/** {@inheritDoc} */
	@Override
	public File getFile() {
		return file;
	}

	/** {@inheritDoc} */
	@Override
	public Date getLastModified() {
		return new Date(file.lastModified());
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public URL getLink() {
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		return media.getSummary();
	}

}

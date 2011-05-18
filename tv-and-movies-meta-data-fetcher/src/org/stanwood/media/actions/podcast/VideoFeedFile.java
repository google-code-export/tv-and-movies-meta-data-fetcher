package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.stanwood.media.setup.MediaDirConfig;

/**
 * A vidoe base feed file
 */
public class VideoFeedFile implements IFeedFile {

	private File file;
	private URL url;
	private String contentType;
	private String description;
	private String title;

	/**
	 * The constructor
	 * @param file The location of the file
	 * @param dirConfig The media directory configuration
	 * @param title The title of the file
	 * @param description The description of the file
	 * @param baseUrl The base URL of the feed
	 * @param contentType The content type of the file
	 * @throws MalformedURLException Thrown if their is a problem create URL's
	 */
	public VideoFeedFile(File file,MediaDirConfig dirConfig, String title,String description, String baseUrl,String contentType) throws MalformedURLException {
		this.file = file;
		this.contentType =contentType;
		this.title = title;
		this.description = description;

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
		return title;
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
		return description;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "VideoFile: " +file.getAbsolutePath();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(IFeedFile o) {
		int compare = Long.valueOf(getLastModified().getTime()).compareTo(o.getLastModified().getTime());
		if (compare==0) {
			if (getTitle().compareTo(o.getTitle())>0) {
				compare=1;
			}
			else {
				compare = -1;
			}
		}
		return compare;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VideoFeedFile other = (VideoFeedFile) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		return true;
	}


}

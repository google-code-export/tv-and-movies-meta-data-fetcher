package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.URL;
import java.util.Date;

public interface IFeedFile {

	String getContentType();

	File getFile();

	Date getLastModified();

	String getTitle();

	URL getLink();

	String getDescription();

}

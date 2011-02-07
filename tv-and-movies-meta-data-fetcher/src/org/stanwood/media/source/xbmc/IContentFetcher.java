package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.stanwood.media.source.SourceException;

/**
 * This interface should be implemented by classes used to fetch content from a URL
 */
public interface IContentFetcher {

	/**
	 * This method should be implemented to fetch content from a URL
	 * @param url The URL
	 * @return A input stream to the content
	 * @throws IOException Thrown if their is a problem with I/O
	 * @throws SourceException Thrown if their are any other problems
	 */
	public InputStream getStreamToURL(URL url) throws IOException, SourceException;
}

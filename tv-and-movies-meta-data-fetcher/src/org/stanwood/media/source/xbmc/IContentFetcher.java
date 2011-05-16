package org.stanwood.media.source.xbmc;

import java.net.URL;

import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.Stream;

/**
 * This interface should be implemented by classes used to fetch content from a URL
 */
public interface IContentFetcher {

	/**
	 * This method should be implemented to fetch content from a URL
	 * @param url The URL
	 * @return A input stream to the content
	 * @throws SourceException Thrown if their are any other problems
	 */
	public Stream getStreamToURL(URL url) throws SourceException;
}

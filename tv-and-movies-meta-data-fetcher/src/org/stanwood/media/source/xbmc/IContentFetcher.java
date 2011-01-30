package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.stanwood.media.source.SourceException;

public interface IContentFetcher {

	public InputStream getStreamToURL(URL url) throws IOException, SourceException;
}

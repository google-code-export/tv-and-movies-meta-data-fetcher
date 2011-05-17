package org.stanwood.media.util;

import java.io.InputStream;

/**
 * This class is used to get a input stream and the details of the stream when downloading
 * a URL
 */
public class Stream {

	private String mineType;
	private InputStream inputStream;
	private String charset;
	private String cacheKey;

	/**
	 * The constructor
	 * @param is The input stream to the contents of the URL
	 * @param mimeType The mime type of the stream
	 * @param charset The charset type of the stream
	 * @param cacheKey When caching the stream, use the cache key
	 */
	public Stream(InputStream is, String mimeType,String charset,String cacheKey) {
		this.inputStream = is;
		this.mineType = mimeType;
		this.charset =charset;
		this.cacheKey = cacheKey;
	}

	/**
	 * Used to get the mime type of the stream
	 * @return the mime type of the stream
	 */
	public String getMineType() {
		return mineType;
	}

	/**
	 * Used to get the input stream
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Used to get the charset type of the stream
	 * @return the charset type of the stream
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * When caching the stream, use the cache key
	 * @return The cache key
	 */
	public String getCacheKey() {
		return cacheKey;
	}
}

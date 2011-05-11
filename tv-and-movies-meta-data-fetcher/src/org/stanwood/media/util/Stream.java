package org.stanwood.media.util;

import java.io.InputStream;

public class Stream {

	private String mineType;
	private InputStream inputStream;
	private String charset;
	private String cacheKey;

	public Stream(InputStream is, String mimeType,String charset,String cacheKey) {
		this.inputStream = is;
		this.mineType = mimeType;
		this.charset =charset;
		this.cacheKey = cacheKey;
	}

	public String getMineType() {
		return mineType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getCharset() {
		return charset;
	}

	public String getCacheKey() {
		return cacheKey;
	}
}

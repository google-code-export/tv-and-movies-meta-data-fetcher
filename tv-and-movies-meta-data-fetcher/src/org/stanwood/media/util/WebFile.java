package org.stanwood.media.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Used to get the contents of a file or stream from the web
 */
public class WebFile {

	private final static String DEFAULT_USER_AGENT = "MediaInfoFetcher";
	
	// Saved response.
	private java.util.Map<String, java.util.List<String>> responseHeader = null;
	private int responseCode = -1;
	private String MIMEtype = null;
	private String charset = null;
	private Object content = null;

	/**
	 * Open a web file. Uses a default user agent string.
	 * 
	 * @param url The URL of the file to open
	 * @throws IOException Thrown if their is a problem fetching the web file
	 */
	public WebFile(URL url) throws IOException {
		this (url,DEFAULT_USER_AGENT);
	}
	
	/**
	 * Open a web file.
	 * 
	 * @param url The URL of the file to open
	 * @param userAgent The user agent to use when access web resources
	 * @throws IOException Thrown if their is a problem fetching the web file
	 */
	public WebFile(URL url,String userAgent) throws IOException {
		// Open a URL connection.		
		final java.net.URLConnection uconn = url.openConnection();
		if (!(uconn instanceof java.net.HttpURLConnection))
			throw new java.lang.IllegalArgumentException("URL protocol must be HTTP.");
		final java.net.HttpURLConnection conn = (java.net.HttpURLConnection) uconn;

		// Set up a request.
		conn.setConnectTimeout(10000); // 10 sec
		conn.setReadTimeout(10000); // 10 sec
		conn.setInstanceFollowRedirects(true);
		conn.setRequestProperty("User-agent", userAgent);

		// Send the request.
		conn.connect();

		// Get the response.
		responseHeader = conn.getHeaderFields();
		responseCode = conn.getResponseCode();	
		final int length = conn.getContentLength();
		final String type = conn.getContentType();
		if (type != null) {
			final String[] parts = type.split(";");
			MIMEtype = parts[0].trim();
			for (int i = 1; i < parts.length && charset == null; i++) {
				final String t = parts[i].trim();
				final int index = t.toLowerCase().indexOf("charset=");
				if (index != -1) {
					charset = t.substring(index + 8);
				}
			}
		}

		// Get the content.
		final java.io.InputStream stream = conn.getErrorStream();
		if (stream != null)
			content = readStream(length, stream);
		else if ((content = conn.getContent()) != null && content instanceof java.io.InputStream)
			content = readStream(length, (java.io.InputStream) content);
		conn.disconnect();
	}

	private byte[] readStream(int length, InputStream stream) throws IOException {
		final int buflen = Math.max(1024, Math.max(length, stream.available()));
		byte[] buf = new byte[buflen];
		;
		byte[] bytes = null;

		for (int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf)) {
			if (bytes == null) {
				bytes = buf;
				buf = new byte[buflen];
				continue;
			}
			final byte[] newBytes = new byte[bytes.length + nRead];
			System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
			System.arraycopy(buf, 0, newBytes, bytes.length, nRead);
			bytes = newBytes;
		}

		return bytes;
	}

	/** 
	 * Get the content. 
	 * @return The Content
	 */
	public Object getContent() {
		return content;
	}

	/** Get the response code.
	 * @return The response code 
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/** 
	 * Get the response header. 
	 * @return The response header fields 
	 */
	public java.util.Map<String, java.util.List<String>> getHeaderFields() {
		return responseHeader;
	}	

	/** 
	 * Get the MIME type.
	 * @return The MIME type 
	 */
	public String getMIMEType() {
		return MIMEtype;
	}

}

package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.Stream;

/**
 * This class is used to process streams that could be zipped. If the stream is not zipped, then
 * {@link #processContents(String)} is called once  with the streams contents read as a string.
 * If the stream is a instance of {@link ZipInputStream}, then {@link #processContents(String)} is
 * called with the contents of each file within the zip stream.
 */
public abstract class StreamProcessor {

	private Stream stream = null;
	private String forcedContentType = null;

	/**
	 * Used to create a instance of the class.
	 * @param stream The stream to be processed.
	 * @param forcedContentType Used to force the stream content type and ignore what the
	 *                          web site reports.
	 */
	public StreamProcessor(Stream stream,String forcedContentType) {
		if (stream==null) {
			throw new NullPointerException("Stream cannot be null");
		}
		this.stream = stream;
		this.forcedContentType = forcedContentType;
	}

	/**
	 * Used to create a instance of the class.
	 * @param stream The stream to be processed.
	 */
	public StreamProcessor(Stream stream) {
		this(stream,null);
	}

	/**
	 * Called to process the stream. This causes the method {@link #processContents(String)} to be
	 * called.
	 * @throws SourceException Thrown in their are any problems
	 */
	public void handleStream() throws SourceException {
		try {
			String contentType = stream.getMineType();
			if (this.forcedContentType!=null) {
				contentType = this.forcedContentType;
			}
			if (stream.getInputStream() instanceof ZipInputStream) {
				ZipInputStream zis = (ZipInputStream) stream.getInputStream();
				ZipEntry entry = null;
	            while ((entry = zis.getNextEntry())!=null) {
	            	StringBuilder contents = new StringBuilder();
	                if (!entry.isDirectory()) {
						int count;
						byte data[] = new byte[1000];
						while ((count = zis.read(data,0,1000)) != -1)
			            {
							byte dest[];
							if (count < 1000) {
								dest = new byte[count];
								System.arraycopy(data, 0, dest, 0, count);
							}
							else {
								dest = data;
							}
							String encoding = "iso-8859-1";
							contents.append(new String(dest,encoding));
			            }
	                }

	                if (contents.length()>0) {
	                	processContents(contents.toString());
	                }
	            }
			}
			else {
				Reader r = null;
				StringWriter sw = null;
				String data = null;
				try {
					r = new InputStreamReader(stream.getInputStream());
					sw = new StringWriter();
					char[] buffer = new char[1024];
					for (int n; (n = r.read(buffer)) != -1; ) {
						sw.write(buffer, 0, n);
					}
					String str = sw.toString();
					if (str.length()>0) {
						data = str;

						if (contentType.equals("text/html")) {
							data = StringEscapeUtils.unescapeHtml(data);
						}

					}
				}
				finally {
					if (sw!=null) {
						sw.close();
					}
					if (r!=null) {
						r.close();
					}
				}
				if (data!=null) {
					processContents(data);
				}
			}
		}
		catch (IOException e) {
			throw new SourceException("Unable to read stream",e);
		}
		finally {
			if (stream!=null) {
				try {
					stream.getInputStream().close();
				} catch (IOException e) {
					throw new SourceException("Unable to close stream",e);
				}
				stream = null;
			}
		}
	}

	/**
	 * This method is called each time a streams contents are read. If the stream is
	 * a instance of {@link ZipInputStream}, then it is called for each of the files within the zip
	 * stream.
	 * @param contents The contents of the stream as a string
	 * @throws SourceException Thrown in their are any problems
	 */
	public abstract void processContents(String contents) throws SourceException;

}
package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.stanwood.media.source.SourceException;

/**
 * This class is used to process streams that could be zipped. If the stream is not zipped, then
 * {@link #processContents(String)} is called once  with the streams contents read as a string.
 * If the stream is a instance of {@link ZipInputStream}, then {@link #processContents(String)} is
 * called with the contents of each file within the zip stream.
 */
public abstract class StreamProcessor {

	private InputStream stream = null;

	/**
	 * Used to create a instance of the class
	 * @param stream The stream to be processed
	 */
	public StreamProcessor(InputStream stream) {
		if (stream==null) {
			throw new NullPointerException("Stream cannot be null");
		}
		this.stream = stream;
	}

	/**
	 * Called to process the stream. This causes the method {@link #processContents(String)} to be
	 * called.
	 * @throws SourceException Thrown in their are any problems
	 */
	public void handleStream() throws SourceException {
		String encoding = "iso-8859-1";
		try {
			if (stream instanceof ZipInputStream) {
				ZipInputStream zis = (ZipInputStream) stream;
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

							contents.append(new String(dest,encoding));
			            }
	                }

	                if (contents.length()>0) {
	                	processContents(contents.toString());
	                }
	            }
			}
			else {
				Reader r = new InputStreamReader(stream);
				StringWriter sw = null;
				try {
					sw = new StringWriter();
					char[] buffer = new char[1024];
					for (int n; (n = r.read(buffer)) != -1; ) {
						sw.write(buffer, 0, n);
					}
					String str = sw.toString();
					if (str.length()>0) {
						processContents(str);
					}
				}
				finally {
					if (sw!=null) {
						sw.close();
					}
				}
			}
		}
		catch (IOException e) {
			throw new SourceException("Unable to read stream",e);
		}
		finally {
			if (stream!=null) {
				try {
					stream.close();
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
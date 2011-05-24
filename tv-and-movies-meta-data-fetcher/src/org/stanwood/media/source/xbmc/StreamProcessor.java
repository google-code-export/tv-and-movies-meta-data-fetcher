package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

	private static Map<String,String> HTML_ENTITIES;
	  static {
	    HTML_ENTITIES = new HashMap<String,String>();
//	    HTML_ENTITIES.put("&lt;","<")    ; HTML_ENTITIES.put("&gt;",">");
//	    HTML_ENTITIES.put("&amp;","&")   ; HTML_ENTITIES.put("&quot;","\"");
	    HTML_ENTITIES.put("&agrave;","à"); HTML_ENTITIES.put("&Agrave;","À");
	    HTML_ENTITIES.put("&acirc;","â") ; HTML_ENTITIES.put("&auml;","ä");
	    HTML_ENTITIES.put("&Auml;","Ä")  ; HTML_ENTITIES.put("&Acirc;","Â");
	    HTML_ENTITIES.put("&aring;","å") ; HTML_ENTITIES.put("&Aring;","Å");
	    HTML_ENTITIES.put("&aelig;","æ") ; HTML_ENTITIES.put("&AElig;","Æ" );
	    HTML_ENTITIES.put("&ccedil;","ç"); HTML_ENTITIES.put("&Ccedil;","Ç");
	    HTML_ENTITIES.put("&eacute;","é"); HTML_ENTITIES.put("&Eacute;","É" );
	    HTML_ENTITIES.put("&egrave;","è"); HTML_ENTITIES.put("&Egrave;","È");
	    HTML_ENTITIES.put("&ecirc;","ê") ; HTML_ENTITIES.put("&Ecirc;","Ê");
	    HTML_ENTITIES.put("&euml;","ë")  ; HTML_ENTITIES.put("&Euml;","Ë");
	    HTML_ENTITIES.put("&iuml;","ï")  ; HTML_ENTITIES.put("&Iuml;","Ï");
	    HTML_ENTITIES.put("&ocirc;","ô") ; HTML_ENTITIES.put("&Ocirc;","Ô");
	    HTML_ENTITIES.put("&ouml;","ö")  ; HTML_ENTITIES.put("&Ouml;","Ö");
	    HTML_ENTITIES.put("&oslash;","ø") ; HTML_ENTITIES.put("&Oslash;","Ø");
	    HTML_ENTITIES.put("&szlig;","ß") ; HTML_ENTITIES.put("&ugrave;","ù");
	    HTML_ENTITIES.put("&Ugrave;","Ù"); HTML_ENTITIES.put("&ucirc;","û");
	    HTML_ENTITIES.put("&Ucirc;","Û") ; HTML_ENTITIES.put("&uuml;","ü");
	    HTML_ENTITIES.put("&Uuml;","Ü")  ; HTML_ENTITIES.put("&nbsp;"," ");
	    HTML_ENTITIES.put("&copy;","\u00a9");
	    HTML_ENTITIES.put("&reg;","\u00ae");
	    HTML_ENTITIES.put("&euro;","\u20a0");
	  }

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
					r = new InputStreamReader(stream.getInputStream(),stream.getCharset());
					sw = new StringWriter();
					char[] buffer = new char[1024];
					for (int n; (n = r.read(buffer)) != -1; ) {
						sw.write(buffer, 0, n);
					}
					String str = sw.toString();
					if (str.length()>0) {
						data = str;

						if (contentType.equals("text/html")) {
							data = unescapeHTML(data);
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
			throw new SourceException("Unable to read stream for URL: " + stream.getURL(),e);
		}
		catch (SourceException e) {
			throw new SourceException("Unable to read stream for URL: " + stream.getURL(),e);
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


	private String unescapeHTML(String source) {
	      int i, j;

	      boolean continueLoop;
	      int skip = 0;
	      do {
	         continueLoop = false;
	         i = source.indexOf("&", skip);
	         if (i > -1) {
	           j = source.indexOf(";", i);
	           if (j > i) {
	             String entityToLookFor = source.substring(i, j + 1);
	             String value = HTML_ENTITIES.get(entityToLookFor);
	             if (value != null) {
	               source = source.substring(0, i)
	                        + value + source.substring(j + 1);
	               continueLoop = true;
	             }
	             else if (value == null){
	                skip = i+1;
	                continueLoop = true;
	             }
	           }
	         }
	      } while (continueLoop);
	      return source;
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
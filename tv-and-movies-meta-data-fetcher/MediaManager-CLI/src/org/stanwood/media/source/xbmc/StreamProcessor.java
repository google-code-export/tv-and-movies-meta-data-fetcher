package org.stanwood.media.source.xbmc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.HttpCache;
import org.stanwood.media.util.Stream;

/**
 * This class is used to process streams that could be zipped. If the stream is not zipped, then
 * {@link #processContents(String)} is called once  with the streams contents read as a string.
 * If the stream is a instance of {@link ZipInputStream}, then {@link #processContents(String)} is
 * called with the contents of each file within the zip stream.
 */
public abstract class StreamProcessor {

	private final static Log log = LogFactory.getLog(StreamProcessor.class);

	private Stream stream = null;
	private String forcedContentType = null;

	private String cacheKey;

	private HttpCache cache = HttpCache.getInstance();

	private static Map<String,String> HTML_ENTITIES;
	  static {
	    HTML_ENTITIES = new HashMap<String,String>();
//	    HTML_ENTITIES.put("&lt;","<")    ; HTML_ENTITIES.put("&gt;",">");
//	    HTML_ENTITIES.put("&amp;","&")   ; HTML_ENTITIES.put("&quot;","\"");
	    HTML_ENTITIES.put("&agrave;","à"); HTML_ENTITIES.put("&Agrave;","À"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&acirc;","â") ; HTML_ENTITIES.put("&auml;","ä"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&Auml;","Ä")  ; HTML_ENTITIES.put("&Acirc;","Â"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&aring;","å") ; HTML_ENTITIES.put("&Aring;","Å"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&aelig;","æ") ; HTML_ENTITIES.put("&AElig;","Æ" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&ccedil;","ç"); HTML_ENTITIES.put("&Ccedil;","Ç"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&eacute;","é"); HTML_ENTITIES.put("&Eacute;","É" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&egrave;","è"); HTML_ENTITIES.put("&Egrave;","È"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&ecirc;","ê") ; HTML_ENTITIES.put("&Ecirc;","Ê"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&euml;","ë")  ; HTML_ENTITIES.put("&Euml;","Ë"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&iuml;","ï")  ; HTML_ENTITIES.put("&Iuml;","Ï"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&ocirc;","ô") ; HTML_ENTITIES.put("&Ocirc;","Ô"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&ouml;","ö")  ; HTML_ENTITIES.put("&Ouml;","Ö"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&oslash;","ø") ; HTML_ENTITIES.put("&Oslash;","Ø"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&szlig;","ß") ; HTML_ENTITIES.put("&ugrave;","ù"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&Ugrave;","Ù"); HTML_ENTITIES.put("&ucirc;","û"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&Ucirc;","Û") ; HTML_ENTITIES.put("&uuml;","ü"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&Uuml;","Ü")  ; HTML_ENTITIES.put("&nbsp;"," "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    HTML_ENTITIES.put("&copy;","\u00a9"); //$NON-NLS-1$ //$NON-NLS-2$
	    HTML_ENTITIES.put("&reg;","\u00ae"); //$NON-NLS-1$ //$NON-NLS-2$
	    HTML_ENTITIES.put("&euro;","\u20a0"); //$NON-NLS-1$ //$NON-NLS-2$
	  }

	/**
	 * The constructor
	 * @param cacheKey The cached key associated with this stream
	 * @param forcedContentType Used to override the content type, null to use the default
	 */
	public StreamProcessor(String cacheKey,String forcedContentType) {
		this.forcedContentType = forcedContentType;
		this.cacheKey = cacheKey;
	}

	/**
	 * The constructor
	 * @param cacheKey The cached key associated with this stream
	 */
	public StreamProcessor(String cacheKey) {
		this(cacheKey,null);
	}

	abstract protected Stream getStream() throws ExtensionException, IOException;

	private Stream openStream() throws ExtensionException, IOException {
		Stream stream = getStream();
		if (stream==null || stream.getInputStream()==null) {
			throw new NullPointerException(Messages.getString("StreamProcessor.STREAM_NULL")); //$NON-NLS-1$
		}
		return stream;
	}

	/**
	 * Called to process the stream. This causes the method {@link #processContents(String)} to be
	 * called.
	 * @throws SourceException Thrown in their are any problems
	 */
	public void handleStream() throws SourceException {
		SocketTimeoutException e = null;
		for (int tryCount=0;tryCount<FileHelper.MAX_RETRIES;tryCount++) {
			try {
				processStream();
				return;
			}
			catch (SocketTimeoutException e1) {
				log.warn(Messages.getString("StreamProcessor.Timedout")); //$NON-NLS-1$
				if (e==null) {
					e = e1;
				}
				try {
					Thread.sleep(FileHelper.RETRY_SLEEP_TIME);
				} catch (InterruptedException e2) {
					// Ignore
				}
			}
		}
		if (e!=null) {
			if (stream==null) {
				throw new SourceException(MessageFormat.format(Messages.getString("StreamProcessor.UNABLE_READ_URL"),"NULL"),e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				throw new SourceException(MessageFormat.format(Messages.getString("StreamProcessor.UNABLE_READ_URL"),stream.getURL()),e); //$NON-NLS-1$
			}
		}
	}

	private void processStream() throws SourceException, SocketTimeoutException {
		List<String> cacheValue = cache.get(cacheKey);
		if (cacheValue!=null) {
			if (log.isDebugEnabled()) {
				log.debug("Cache hit for key "+cacheKey);
			}
			for (String contents : cacheValue) {
				processContents(contents);
			}
			return;
		}
		try {
			this.stream = openStream();

			String contentType = stream.getMineType();
			if (this.forcedContentType!=null) {
				contentType = this.forcedContentType;
			}
			if (stream.getInputStream() instanceof ZipInputStream) {
				ZipInputStream zis = (ZipInputStream) stream.getInputStream();
				ZipEntry entry = null;
				List<String>lcontents = new ArrayList<String>();
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
							//TODO check the encoding
							String encoding = "iso-8859-1"; //$NON-NLS-1$
							if (entry.getName().endsWith(".xml")) { //$NON-NLS-1$
								encoding = "UTF-8"; //$NON-NLS-1$
							}
							contents.append(new String(dest,encoding));
			            }
	                }

	                if (contents.length()>0) {
	                	lcontents.add(contents.toString());
	                }
	            }
	            for (String contents : lcontents) {
                	cache(cacheKey,lcontents);
	            	processContents(contents);
	            }
			}
			else {
				String data = null;
				String encoding = stream.getCharset();
				if (contentType.equals("text/xml")) { //$NON-NLS-1$
					//TODO check the encoding
					encoding = "UTF-8"; //$NON-NLS-1$
				}
				Reader r = null;
				StringWriter sw = null;
				try {
					try {
						r = new InputStreamReader(stream.getInputStream(),encoding);
					}
					catch (NullPointerException ee) {
						throw ee;
					}
					sw = new StringWriter();

					char[] buffer = new char[1024];
					for (int n; (n = r.read(buffer)) != -1; ) {
						sw.write(buffer, 0, n);
					}
					String str = sw.toString();
					if (str.length()>0) {
						data = str;

						if (contentType.equals("text/html")) { //$NON-NLS-1$
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
					List<String>lcontents = new ArrayList<String>();
					lcontents.add(data);
					cache(cacheKey,lcontents);
					processContents(data);
				}
			}
		}
		catch (SocketTimeoutException e) {
			throw e;
		}
		catch (IOException e) {
			if (stream==null) {
				throw new SourceException(MessageFormat.format(Messages.getString("StreamProcessor.UNABLE_READ_URL"),"NULL"),e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				throw new SourceException(MessageFormat.format(Messages.getString("StreamProcessor.UNABLE_READ_URL"),stream.getURL()),e); //$NON-NLS-1$
			}
		}
		catch (ExtensionException e) {
			if (stream==null) {
				throw new SourceException(MessageFormat.format(Messages.getString("StreamProcessor.UNABLE_READ_URL"),"NULL"),e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				throw new SourceException(MessageFormat.format(Messages.getString("StreamProcessor.UNABLE_READ_URL"),stream.getURL()),e); //$NON-NLS-1$
			}
		}
		finally {
			if (stream!=null) {
				try {
					if (stream.getInputStream()!=null) {
						stream.getInputStream().close();
					}
				} catch (IOException e) {
					throw new SourceException(Messages.getString("StreamProcessor.UNABLE_CLOSE_STREAM"),e); //$NON-NLS-1$
				}
				stream = null;
			}
		}
	}

	private void cache(String cacheKey,List<String> value) {
		cache.put(cacheKey,value);
	}


	private String unescapeHTML(String source) {
	      int i, j;

	      boolean continueLoop;
	      int skip = 0;
	      do {
	         continueLoop = false;
	         i = source.indexOf("&", skip); //$NON-NLS-1$
	         if (i > -1) {
	           j = source.indexOf(";", i); //$NON-NLS-1$
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
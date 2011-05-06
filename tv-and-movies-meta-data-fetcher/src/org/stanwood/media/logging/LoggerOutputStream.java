package org.stanwood.media.logging;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;

/**
 * Used to write stream data to the log
 */
public class LoggerOutputStream extends OutputStream {

	private final static Log log = LogFactory.getLog(LoggerOutputStream.class);
	private final static String LINE_END = "\n";

	private StringBuilder buffer = new StringBuilder();
	private Level level;

	public LoggerOutputStream(Level level) {
		this.level = level;
	}


	/**
	 * Append char to the buffer. if a
	 */
	@Override
	public void write(int c) throws IOException {
		buffer.append((char)c);
		checkBuffer();
	}

	private void checkBuffer() {
		int pos = -1;

		while ((pos = buffer.indexOf(LINE_END))!=-1) {
			log(buffer.substring(0,pos));
			buffer.delete(0,pos+LINE_END.length());
		}
	}

	protected void log(String msg) {
		if (level.equals(Level.TRACE)) {
			log.trace(msg);
		}
		else if (level.equals(Level.DEBUG)) {
			log.debug(msg);
		}
		else if (level.equals(Level.INFO)) {
			log.info(msg);
		}
		else if (level.equals(Level.WARN)) {
			log.warn(msg);
		}
		else if (level.equals(Level.ERROR)) {
			log.error(msg);
		}
		else if (level.equals(Level.FATAL)) {
			log.fatal(msg);
		}
		else {
			log.info(msg);
		}
	}


	/**
	 * This will close the stream and esure that the buffer has been written to the log
	 */
	@Override
	public void close() throws IOException {
		super.close();
		if (buffer.length()>0) {
			buffer.append("\n");
			checkBuffer();
		}
	}



}

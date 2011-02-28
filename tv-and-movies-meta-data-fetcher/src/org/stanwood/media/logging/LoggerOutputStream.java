package org.stanwood.media.logging;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerOutputStream extends OutputStream {

	private final static Log log = LogFactory.getLog(LoggerOutputStream.class);
	private final static String LINE_END = "\n";

	private StringBuilder buffer = new StringBuilder();

	@Override
	public void write(int c) throws IOException {
		buffer.append((char)c);
		checkBuffer();
	}

	private void checkBuffer() {
		int pos = -1;

		while ((pos = buffer.indexOf(LINE_END))!=-1) {
			log.info(buffer.substring(0,pos));
			buffer.delete(0,pos+LINE_END.length());
		}
	}

}

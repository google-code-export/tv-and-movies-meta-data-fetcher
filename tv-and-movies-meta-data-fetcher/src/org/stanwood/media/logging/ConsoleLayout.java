package org.stanwood.media.logging;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A layout for the console that prints the message and if not a info mesage, also
 * prefixes it with the level.
 */
public class ConsoleLayout extends Layout {

	private StringBuffer sbuf = new StringBuffer(128);

	/**
	 * Does not do anything
	 */
	@Override
	public void activateOptions() {
		// nothing to do.
	}

	/**
	 * Format the logging event for output
	 * @param event The logging event
	 * @return The message to be logged
	 */
	@Override
	public String format(LoggingEvent event) {
	    sbuf.setLength(0);
	    if (!event.getLevel().equals(Level.INFO)) {
	    	sbuf.append(event.getLevel().toString());
	    	sbuf.append(": ");
	    }
	    sbuf.append(event.getMessage());
		return sbuf.toString();
	}


	/** The ConsoleLayout does not handle the throwable contained within
	  * {@link LoggingEvent LoggingEvents}. Thus, it returns
	  * <code>true</code>.
	  */
	@Override
	public boolean ignoresThrowable() {
		return true;
	}

}

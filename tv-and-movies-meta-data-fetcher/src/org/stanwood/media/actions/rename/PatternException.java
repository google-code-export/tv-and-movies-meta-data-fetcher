package org.stanwood.media.actions.rename;

/** Thrown if their is a problem related to rename patters */
public class PatternException extends Exception {

	/**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialised, and may subsequently be initialised by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
	public PatternException(String message) {
		super(message);
	}

}

package org.stanwood.media.source.xbmc;

import org.stanwood.media.source.SourceException;

/**
 * Thrown if their is a problem related XBMC addons
 */
public class XBMCException extends SourceException {

	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialised, and may subsequently be initialised by a
     * call to {@link #initCause}.
     */
	public XBMCException() {
		super();
	}

	/**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
	public XBMCException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialised, and may subsequently be initialised by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
	public XBMCException(String message) {
		super(message);
	}

	/**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialised, and may subsequently be initialised by
     * a call to {@link #initCause}.
     *
     * @param   cause   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
	public XBMCException(Throwable cause) {
		super(cause);
	}


}

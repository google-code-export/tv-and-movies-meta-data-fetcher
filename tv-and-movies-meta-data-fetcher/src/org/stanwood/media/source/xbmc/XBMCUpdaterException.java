package org.stanwood.media.source.xbmc;

/**
 * Thrown if their is a problem related XBMC updater
 */
public class XBMCUpdaterException extends XBMCException {

	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialised, and may subsequently be initialised by a
     * call to {@link #initCause}.
     */
	public XBMCUpdaterException() {
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
	public XBMCUpdaterException(String message, Throwable cause) {
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
	public XBMCUpdaterException(String message) {
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
	public XBMCUpdaterException(Throwable cause) {
		super(cause);
	}

}

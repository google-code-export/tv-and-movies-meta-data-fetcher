package org.stanwood.media.logging;

/**
 * The base exception class of all MediaManager exceptions
 */
public class StanwoodException extends Exception {

	private static final long serialVersionUID = -1982459325296547366L;


    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialised, and may subsequently be initialised by a
     * call to {@link #initCause}.
     */
	public StanwoodException() {
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
	public StanwoodException(String message, Throwable cause) {
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
	public StanwoodException(String message) {
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
	public StanwoodException(Throwable cause) {
		super(cause);
	}

	/**
	 * Used to pretty print the exception details and it's causes
	 * @return the pretty print results
	 */
	public String printException() {
		StringBuilder result = new StringBuilder();

		result.append(getLocalizedMessage());
		Throwable cause = this;
		while (true) {
			if (cause.getCause()==null || cause.getCause()==cause) {
				break;
			}
			cause = cause.getCause();
			if (cause.getLocalizedMessage()!=null) {
				result.append("\n - Caused by: "+cause.getLocalizedMessage());
			}
			else {
				result.append("\n - Caused by: "+cause.getClass().getName());
			}
		}

		return result.toString();
	}
}

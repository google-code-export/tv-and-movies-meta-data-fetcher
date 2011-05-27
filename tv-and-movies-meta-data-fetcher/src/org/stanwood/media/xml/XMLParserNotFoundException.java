package org.stanwood.media.xml;

/**
 * This is thrown if a attempt was made to get a XML node that does not exist.
 */
public class XMLParserNotFoundException extends XMLParserException {

	private static final long serialVersionUID = 1894084713786871271L;

	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialised, and may subsequently be initialised by a
     * call to {@link #initCause}.
     */
	public XMLParserNotFoundException() {
		super();
	}

	/**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialised, and may subsequently be initialised by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
	public XMLParserNotFoundException(String message) {
		super(message);
	}


}

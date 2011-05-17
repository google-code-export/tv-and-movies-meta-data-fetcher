package org.stanwood.media.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A XML error handler that output the errors to the log
 */
public class XMLErrorHandler implements ErrorHandler {

	private final static Log log = LogFactory.getLog(XMLErrorHandler.class);

	private boolean foundErrors = false;

	/** {@inheritDoc} */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		log.warn("Unable to validate xml, " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
	}

	/** {@inheritDoc} */
	@Override
	public void error(SAXParseException e) throws SAXException {
		foundErrors = true;
		log.error("Unable to validate xml, " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
	}

	/** {@inheritDoc} */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		foundErrors = true;
		log.fatal("Unable to validate xml, " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
	}

	/**
	 * Used to find out if errors were found.
	 * @return True if errors were found, otherwise false
	 */
	public boolean hasErrors() {
		return foundErrors;
	}
}

package org.stanwood.media.xml;

import java.text.MessageFormat;

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
		log.warn(MessageFormat.format(Messages.getString("XMLErrorHandler.UNABLE_VALIDATE_XML"),e.getMessage(),e.getLineNumber(), e.getColumnNumber())); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void error(SAXParseException e) throws SAXException {
		foundErrors = true;
		log.error(MessageFormat.format(Messages.getString("XMLErrorHandler.UNABLE_VALIDATE_XML"),e.getMessage(),e.getLineNumber(), e.getColumnNumber())); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		foundErrors = true;
		log.fatal(MessageFormat.format(Messages.getString("XMLErrorHandler.UNABLE_VALIDATE_XML"),e.getMessage(),e.getLineNumber(), e.getColumnNumber())); //$NON-NLS-1$
	}

	/**
	 * Used to find out if errors were found.
	 * @return True if errors were found, otherwise false
	 */
	public boolean hasErrors() {
		return foundErrors;
	}
}

package org.stanwood.media.store.xmlstore;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.util.FileHelper;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class is used to handle parsing errors that occur when validating a xml file.
 * It will usally display 5 lines either side of the error and give a messages
 * with info about the problem that was found.
 */
public class SimpleErrorHandler implements ErrorHandler {

	private final static Log log = LogFactory.getLog(SimpleErrorHandler.class);
	private File xmlFile;
	private boolean foundErrors = false;

	/**
	 * Used to construct the error handler
	 * @param xmlFile The file that is been parsed
	 */
	public SimpleErrorHandler(File xmlFile ) {
		this.xmlFile = xmlFile;
	}

	/**
	 * Used to print warings when they occur while validating the XML file
	 * @param e The exception that is been processed
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
        log.warn("Unable to validate xml, " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        if (log.isDebugEnabled()) {
			try {
				FileHelper.displayFile(xmlFile,e.getLineNumber()-5,e.getLineNumber()+5, System.out);
			} catch (IOException e1) {
				log.error(e1.getMessage(),e1);
			}
		}
    }

	/**
	 * Used to print errors when they occur while validating the XML file
	 * @param e The exception that is been processed
	 */
	@Override
    public void error(SAXParseException e) throws SAXException {
		foundErrors = true;
		log.error("Unable to validate xml, " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
		if (log.isDebugEnabled()) {
			try {
				FileHelper.displayFile(xmlFile,e.getLineNumber()-5,e.getLineNumber()+5, System.out);
			} catch (IOException e1) {
				log.error(e1.getMessage(),e1);
			}
		}
    }

	/**
	 * Used to print fatal errors when they occur while validating the XML file
	 * @param e The exception that is been processed
	 */
	@Override
    public void fatalError(SAXParseException e) throws SAXException {
		foundErrors = true;
		log.fatal("Unable to validate xml, " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
		if (log.isDebugEnabled()) {
			try {
				FileHelper.displayFile(xmlFile,e.getLineNumber()-5,e.getLineNumber()+5, System.out);
			} catch (IOException e1) {
				log.error(e1.getMessage(),e1);
			}
		}

    }

	/**
	 * Used to find out if errors were found.
	 * @return True if errors were found, otherwise false
	 */
	public boolean hasErrors() {
		return foundErrors;
	}


}

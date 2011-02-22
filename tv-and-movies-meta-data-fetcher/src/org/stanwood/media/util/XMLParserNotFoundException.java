package org.stanwood.media.util;

/**
 * This is thrown if a attempt was made to get a XML node that does not exist.
 */
public class XMLParserNotFoundException extends XMLParserException {

	public XMLParserNotFoundException() {
		super();
	}

	public XMLParserNotFoundException(String message) {
		super(message);
	}


}

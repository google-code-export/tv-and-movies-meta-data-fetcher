package org.stanwood.media.source.xbmc.expression;

public class ExpressionParserException extends RuntimeException {

	public ExpressionParserException() {
	}

	public ExpressionParserException(String message) {
		super(message);
	}

	public ExpressionParserException(Throwable cause) {
		super(cause);
	}

	public ExpressionParserException(String message, Throwable cause) {
		super(message, cause);
	}

}

package org.stanwood.media.actions;

import org.stanwood.media.logging.StanwoodException;

/**
 * Thrown if their is a problem related to actions
 */
public class ActionException extends StanwoodException {


	public ActionException() {
		super();
	}

	public ActionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionException(String message) {
		super(message);
	}

	public ActionException(Throwable cause) {
		super(cause);
	}


}

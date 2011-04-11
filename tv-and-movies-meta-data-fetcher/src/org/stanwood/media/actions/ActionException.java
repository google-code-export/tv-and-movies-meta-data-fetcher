package org.stanwood.media.actions;

/**
 * Thrown if their is a problem releated to actions
 */
public class ActionException extends Exception {

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

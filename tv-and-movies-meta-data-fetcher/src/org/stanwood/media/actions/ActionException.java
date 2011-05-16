package org.stanwood.media.actions;

import org.stanwood.media.logging.StanwoodException;

/**
 * Thrown if their is a problem related to actions
 */
public class ActionException extends StanwoodException {


	/** {@inheritDoc} */
	public ActionException() {
		super();
	}

	/** {@inheritDoc} */
	public ActionException(String message, Throwable cause) {
		super(message, cause);
	}

	/** {@inheritDoc} */
	public ActionException(String message) {
		super(message);
	}

	/** {@inheritDoc} */
	public ActionException(Throwable cause) {
		super(cause);
	}


}

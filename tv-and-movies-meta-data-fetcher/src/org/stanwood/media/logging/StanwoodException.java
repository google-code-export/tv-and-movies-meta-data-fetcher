package org.stanwood.media.logging;

public class StanwoodException extends Exception {

	private static final long serialVersionUID = -1982459325296547366L;

	/** {@inheritDoc} */
	public StanwoodException() {
		super();
	}

	/** {@inheritDoc} */
	public StanwoodException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/** {@inheritDoc} */
	public StanwoodException(String arg0) {
		super(arg0);
	}

	/** {@inheritDoc} */
	public StanwoodException(Throwable arg0) {
		super(arg0);
	}

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

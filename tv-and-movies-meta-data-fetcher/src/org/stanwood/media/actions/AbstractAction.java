package org.stanwood.media.actions;

import java.io.File;

import org.stanwood.media.MediaDirectory;

/**
 * Helper class that actions should extends so that they only have to
 * implement action methods that are needed.
 */
public abstract class AbstractAction implements IAction {

	private boolean testMode;

	/** {@inheritDoc} */
	@Override
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isTestMode() {
		return this.testMode;
	}

	/** {@inheritDoc} */
	@Override
	public void performOnDirectory(MediaDirectory dir, File file,IActionEventHandler actionEventHandler) throws ActionException {
	}

	/** {@inheritDoc}
	 */
	@Override
	public void init(MediaDirectory dir) throws ActionException {
	}

	/** {@inheritDoc} */
	@Override
	public void finished(MediaDirectory dir) throws ActionException {

	}
}

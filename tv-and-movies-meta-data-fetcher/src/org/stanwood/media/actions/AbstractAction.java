package org.stanwood.media.actions;

import java.io.File;

import org.stanwood.media.MediaDirectory;

public abstract class AbstractAction implements IAction {


	private boolean testMode;

	@Override
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	@Override
	public boolean isTestMode() {
		return this.testMode;
	}

	@Override
	public void performOnDirectory(MediaDirectory dir, File file,IActionEventHandler actionEventHandler) throws ActionException {
	}

}

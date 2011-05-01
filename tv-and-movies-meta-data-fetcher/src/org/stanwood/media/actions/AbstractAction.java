package org.stanwood.media.actions;

public abstract class AbstractAction implements IAction {


	private boolean testMode;

	@Override
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public boolean isTestMode() {
		return this.testMode;
	}
}

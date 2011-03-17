package org.stanwood.media.source.xbmc.updater;

public enum AddonStatus {

	INSTALLED("Installed"),
	NOT_INSTALLED("Not Installed"),
    OUT_OF_DATE("Update available");

    private String displayName;

    private AddonStatus(String displayName) {
		this.displayName =displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}

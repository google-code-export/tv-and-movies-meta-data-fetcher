package org.stanwood.media.source.xbmc.updater;

/**
 * Used to represent the different statuses of addons
 */
public enum AddonStatus {

	/** Indicates that the addon is installed on the users system */
	INSTALLED("Installed"),
	/** Indicates that the addon is not installed on the users system */
	NOT_INSTALLED("Not Installed"),
	/** Indicates that the plugin is instlled, but their is a new version avaliable */
    OUT_OF_DATE("Update available");

    private String displayName;

    private AddonStatus(String displayName) {
		this.displayName =displayName;
	}

    /**
     * Used to get the display name of the status
     * @return The display name of the status
     */
	public String getDisplayName() {
		return displayName;
	}

}

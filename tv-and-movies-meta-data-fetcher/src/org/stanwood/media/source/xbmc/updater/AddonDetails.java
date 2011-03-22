package org.stanwood.media.source.xbmc.updater;

import java.util.Set;

import org.stanwood.media.util.Version;

/**
 * Used to store information about XBMC addons
 */
public class AddonDetails {

	private String id;
	private Version avaliableVersion;
	private Version installedVersion;
	private AddonStatus status;
	private Set<String> requiredAddons;

	/**
	 * Constructor
	 * @param id The ID of the addon
	 * @param installedVersion The installed version number or null if it's not installed
	 * @param avaliableVersion The latest version number as found on the update site
	 * @param status The status of the addon
	 */
	public AddonDetails(String id, Version installedVersion,Version avaliableVersion,AddonStatus status) {
		this.id = id;
		this.installedVersion = installedVersion;
		this.avaliableVersion = avaliableVersion;
		this.status = status;
	}

	/**
	 * Used to get the ID of the addon
	 * @return the ID of the addon
	 */
	public String getId() {
		return id;
	}

	/**
	 * Used to get the status of the addon
	 * @return The status of the addon
	 */
	public AddonStatus getStatus() {
		return status;
	}

	/**
	 * Used to set the status of the addon
	 * @param status The status of the addon
	 */
	public void setStatus(AddonStatus status) {
		this.status = status;
	}

	/**
	 * Used to get the addon id's that this addon requires
	 * @return The required addon
	 */
	public Set<String> getRequiredAddons() {
		return requiredAddons;
	}

	/**
	 * Used to set the addon id's that this addon requires
	 * @param requiredAddons The required addon
	 */
	public void setRequiredAddons(Set<String> requiredAddons) {
		this.requiredAddons = requiredAddons;
	}

	/**
	 * Used to get the version of the addon available the update site
	 * @return The version of the addon available the update site
	 */
	public Version getAvaliableVersion() {
		return avaliableVersion;
	}

	/**
	 * Used to set the version of the addon available the update site
	 * @param avaliableVersion the version of the addon available the update site
	 */
	public void setAvaliableVersion(Version avaliableVersion) {
		this.avaliableVersion = avaliableVersion;
	}

	/**
	 * Used to get the installed version of the addon or null if it's not installed
	 * @return the installed version of the addon or null if it's not installed
	 */
	public Version getInstalledVersion() {
		return installedVersion;
	}

	/**
	 * Used to set the installed version of the addon
	 * @param installedVersion The installed version of the addon, or null if it's not installed
	 */
	public void setInstalledVersion(Version installedVersion) {
		this.installedVersion = installedVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id+" : " + installedVersion + " : " + avaliableVersion +" : " + status;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AddonDetails other = (AddonDetails) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}

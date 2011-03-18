package org.stanwood.media.source.xbmc.updater;

import java.util.Set;

import org.stanwood.media.util.Version;

public class AddonDetails {

	private String id;
	private Version avaliableVersion;
	private Version installedVersion;
	private AddonStatus status;
	private Set<String> requiredPlugins;

	public AddonDetails(String id, Version installedVersion,Version avaliableVersion,AddonStatus status) {
		this.id = id;
		this.installedVersion = installedVersion;
		this.avaliableVersion = avaliableVersion;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public AddonStatus getStatus() {
		return status;
	}

	public void setStatus(AddonStatus status) {
		this.status = status;
	}

	public Set<String> getRequiredPlugins() {
		return requiredPlugins;
	}

	public void setRequiredPlugins(Set<String> requiredPlugins) {
		this.requiredPlugins = requiredPlugins;
	}

	public Version getAvaliableVersion() {
		return avaliableVersion;
	}

	public void setAvaliableVersion(Version avaliableVersion) {
		this.avaliableVersion = avaliableVersion;
	}

	public Version getInstalledVersion() {
		return installedVersion;
	}

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

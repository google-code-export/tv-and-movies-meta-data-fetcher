package org.stanwood.media.source.xbmc.updater;

import java.util.List;

import org.stanwood.media.util.Version;

public class AddonDetails {

	private String id;
	private Version avaliableVersion;
	private Version installedVersion;
	private AddonStatus status;
	private List<String> requiredPlugins;

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

	public List<String> getRequiredPlugins() {
		return requiredPlugins;
	}

	public void setRequiredPlugins(List<String> requiredPlugins) {
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


}

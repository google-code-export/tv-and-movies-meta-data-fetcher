package org.stanwood.media.source.xbmc;

import java.util.List;

import org.stanwood.media.util.Version;

public class AddonDetails {

	private String id;
	private Version version;
	private AddonStatus status;
	private List<String> requiredPlugins;

	public AddonDetails(String id, Version version,AddonStatus status) {
		this.id = id;
		this.version = version;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public Version getVersion() {
		return version;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id+" : " + version +" : " + status;
	}


}

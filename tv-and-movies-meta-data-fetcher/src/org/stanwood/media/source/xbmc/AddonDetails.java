package org.stanwood.media.source.xbmc;

import org.stanwood.media.util.Version;

public class AddonDetails {

	private String id;
	private Version version;
	private AddonStatus status;

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


}

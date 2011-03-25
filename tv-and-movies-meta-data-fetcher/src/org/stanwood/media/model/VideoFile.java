package org.stanwood.media.model;

import java.io.File;

public class VideoFile {

	private File location;
	private File orginalLocation;
	private Integer part;

	public VideoFile(File filename,File originalLocation,Integer part) {
		this.location = filename;
		this.orginalLocation = originalLocation;
		this.part = part;
	}

	public File getLocation() {
		return location;
	}

	public File getOrginalLocation() {
		return orginalLocation;
	}

	public void setOrginalLocation(File orginalLocation) {
		this.orginalLocation = orginalLocation;
	}

	public void setLocation(File location) {
		this.location = location;
	}

	public Integer getPart() {
		return part;
	}

	public void setPart(Integer part) {
		this.part = part;
	}
}

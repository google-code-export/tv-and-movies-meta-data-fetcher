package org.stanwood.media.model;

import java.io.File;

/**
 * This is used to store information about files in a media directory
 */
public class VideoFile implements IVideoFile {

	private File location;
	private File orginalLocation;
	private Integer part;
	private File rootMediaDir;

	/**
	 * The constructor
	 * @param filename The current location of the video file
	 * @param originalLocation The original location of the file
	 * @param part The part number or null if not known/supported
	 * @param rootMediaDir The media dir the file is in
	 */
	public VideoFile(File filename,File originalLocation,Integer part,File rootMediaDir) {
		this.location = filename;
		this.orginalLocation = originalLocation;
		this.part = part;
		this.rootMediaDir = rootMediaDir;
	}

	/**
	 * Used to get the current location of the video file
	 * @return The current location of the video file
	 */
	@Override
	public File getLocation() {
		return location;
	}

	/**
	 * Used to get the original location of the video file. This is the location
	 * it was first seen in.
	 * @return The original location of the file
	 */
	@Override
	public File getOrginalLocation() {
		return orginalLocation;
	}

	/**
	 * Used to set the original location of the video file. This is the location
	 * it was first seen in.
	 * @param orginalLocation The original location of the file
	 */
	@Override
	public void setOrginalLocation(File orginalLocation) {
		this.orginalLocation = orginalLocation;
	}

	/**
	 * Used to set the current location of the video file
	 * @param location The current location of the video file
	 */
	@Override
	public void setLocation(File location) {
		this.location = location;
	}

	/**
	 * Used to get the part number of the file
	 * @return The part number or null if not known/supported
	 */
	@Override
	public Integer getPart() {
		return part;
	}

	/**
	 * Used to set the part number of the file
	 * @param part The part number or null if not known/supported
	 */
	@Override
	public void setPart(Integer part) {
		this.part = part;
	}

	/** {@inheritDoc} */
	@Override
	public File getMediaDirectory() {
		return rootMediaDir;
	}
}

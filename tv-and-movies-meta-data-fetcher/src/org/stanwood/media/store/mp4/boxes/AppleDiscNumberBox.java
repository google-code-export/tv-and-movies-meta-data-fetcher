package org.stanwood.media.store.mp4.boxes;

import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.apple.AppleDataBox;

/**
 * Used to store the disk numbers of media
 */
public class AppleDiscNumberBox extends AbstractAppleMetaDataBox implements ContainerBox {

	private static final String TYPE = "disk";

	/**
	 * The constructor
	 */
	public AppleDiscNumberBox() {
		super(TYPE);
	}

	/** {@inheritDoc} */
	@Override
	public String getDisplayName() {
		return "iTunes Disc Number";
	}

	/**
	 * @param track the actual track number
	 * @param of number of tracks overall
	 */
	public void setDiskNumber(byte track, byte of) {
		appleDataBox = new AppleDataBox();
		appleDataBox.setVersion(0);
		appleDataBox.setFlags(0);
		appleDataBox.setFourBytes(new byte[4]);
		appleDataBox.setContent(new byte[] { 0, 0, 0, track, 0, of, 0, 0 });
	}

	/**
	 * Used to get the disk number
	 * @return The disk number
	 */
	public byte getDiskNumber() {
		return appleDataBox.getContent()[3];
	}

	/**
	 * Used to get the total number of disks
	 * @return the total number of disks
	 */
	public byte getNumberOfDisks() {
		return appleDataBox.getContent()[5];
	}

	/**
	 * Used to set the total number of disks
	 * @param numberOfDisks The total number of disks
	 */
	public void setNumberOfDisks(byte numberOfDisks) {
		byte[] content = appleDataBox.getContent();
		content[5] = numberOfDisks;
		appleDataBox.setContent(content);
	}

	/**
	 * Used to set the disk number
	 * @param diskNumber The disk number
	 */
	public void setDiskNumber(byte diskNumber) {
		byte[] content = appleDataBox.getContent();
		content[3] = diskNumber;
		appleDataBox.setContent(content);
	}

}
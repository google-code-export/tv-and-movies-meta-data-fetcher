package org.stanwood.media.store.mp4;

import java.util.StringTokenizer;

import com.coremedia.iso.boxes.AbstractBox;

/**
 * Used to store &quot;disk&quot; atom data
 */
public class AtomDisk extends Atom {

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param disk The disk number
	 * @param total The total number of disks
	 */
	public AtomDisk(String name, Byte disk,Byte total) {
		super(name, ""+disk+"/"+total);
	}

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param value Parse the value in the format disk/total
	 */
	public AtomDisk(String name, String value) {
		super(name,null);
		StringTokenizer tok = new StringTokenizer(getValue(),"/");
		setValue(tok.nextToken()+"/"+tok.nextToken());
	}

	/**
	 * Used to get the disk number
	 * @return The disk number
	 */
	public Byte getDiskNumber() {
		StringTokenizer tok = new StringTokenizer(getValue(),"/");
		return Byte.parseByte(tok.nextToken());
	}

	/**
	 * Used to get the total number of disks
	 * @return The total number of disks
	 */
	public Byte getTotalDisks() {
		StringTokenizer tok = new StringTokenizer(getValue(),"/");
		tok.nextToken();
		return Byte.parseByte(tok.nextToken());
	}

	/** {@inheritDoc} */
	@Override
	public void updateBoxValue(AbstractBox b) throws MP4Exception {
		if (b instanceof AppleDiscNumberBox) {
			((AppleDiscNumberBox)b).setDiskNumber(getDiskNumber(), getTotalDisks());
		}
	}


}

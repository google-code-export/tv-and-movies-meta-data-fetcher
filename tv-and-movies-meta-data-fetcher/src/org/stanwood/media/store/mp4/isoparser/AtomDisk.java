package org.stanwood.media.store.mp4.isoparser;

import java.util.StringTokenizer;

import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.isoparser.boxes.AppleDiscNumberBox;

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
		super("Disk Number",name, ""+disk+"/"+total);
	}

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param value Parse the value in the format disk/total
	 */
	public AtomDisk(String name, String value) {
		super("Disk Number",name,null);
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

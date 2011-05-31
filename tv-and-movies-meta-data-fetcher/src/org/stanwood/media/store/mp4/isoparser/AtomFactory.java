package org.stanwood.media.store.mp4.isoparser;

import org.stanwood.media.store.mp4.isoparser.AtomStik.Value;

import com.coremedia.iso.boxes.Box;

/**
 * Used to create MP4 atoms
 */
public class AtomFactory {

	/**
	 * Used to create a atom
	 * @param displayName The display name
	 * @param name The name of the atom
	 * @param value The value of the atom
	 * @return the atom
	 */
	public static Atom createAtom(String displayName,String name, String value) {
		if (name.equals("stik")) {
			return new AtomStik(value);
		}
		else if (name.equals("disk")) {
			return new AtomDisk(name,value);
		}
		else {
			return new Atom(displayName,name,value);
		}
	}

	/**
	 * Used to create a &quot;stik&quot; atom
	 * @param value The atom value
	 * @return The atom
	 */
	public static Atom createAtom(Value value) {
		return createAtom("Media Type","stik",value.getId());
	}

	/**
	 * Used to create a unknown atom box
	 * @param type The type of the box
	 * @param box The box
	 * @return The atom
	 */
	public static Atom createUnkownAtom(String type, Box box) {
		return new AtomUnknown(type,box);
	}

	/**
	 * Used to create a disk number box
	 * @param diskNumber The disk number
	 * @param numberOfDisks The total number of disks
	 * @return The atom
	 */
	public static Atom createDiskAtom( byte diskNumber,byte numberOfDisks) {
		return new AtomDisk("disk",diskNumber,numberOfDisks);
	}
}

package org.stanwood.media.store.mp4;

import org.stanwood.media.store.mp4.AtomStik.Value;

/**
 * Used to create MP4 atoms
 */
public class AtomFactory {

	/**
	 * Used to create a atom
	 * @param name The name of the atom
	 * @param value The value of the atom
	 * @return the atom
	 */
	public static Atom createAtom(String name, String value) {
		if (name.equals("stik")) {
			return new AtomStik(value);
		}
		else {
			return new Atom(name,value);
		}
	}

	/**
	 * Used to create a &quot;stik&quot; atom
	 * @param value The atom value
	 * @return The atom
	 */
	public static Atom createAtom(Value value) {
		return createAtom("stik",value.getId());
	}
}

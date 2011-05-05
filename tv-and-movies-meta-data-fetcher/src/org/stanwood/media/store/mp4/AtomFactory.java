package org.stanwood.media.store.mp4;

import org.stanwood.media.store.mp4.AtomStik.Value;

public class AtomFactory {

	public static Atom createAtom(String name, String value) {
		if (name.equals("stik")) {
			return new AtomStik(value);
		}
		else {
			return new Atom(name,value);
		}
	}

	public static Atom createAtom(Value value) {
		return createAtom("stik",value.getId());
	}
}

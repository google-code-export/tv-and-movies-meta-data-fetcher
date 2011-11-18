package org.stanwood.media.store.mp4;


/**
 * Used to lookup atom display names from their keys
 */
public class AtomNameLookup {


	/**
	 * The Constructor
	 */
	@SuppressWarnings("nls")
	public AtomNameLookup() {

	}

	/**
	 * Used to get the display name
	 * @param key The key of the atom
	 * @return the display name
	 */
	public String getDisplayName(String key) {
		return MP4AtomKey.fromKey(key).getDisplayName();
	}
}

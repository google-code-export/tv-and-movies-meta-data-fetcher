package org.stanwood.media.store.mp4;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to lookup atom display names from their keys
 */
public class AtomNameLookup {

	private Map<String,String> names = new HashMap<String,String>();

	/**
	 * The Constructor
	 */
	public AtomNameLookup() {
		names.put("stik","Media type");
		names.put("tven","Episode ID");
		names.put("tvsh","TV show name");
		names.put("tvsn","TV season number");
		names.put("tves","TV episode number");
		names.put("©day","Media year");
		names.put("©nam","Title");
		names.put("desc","Summary");
		names.put("©gen","Genre");
		names.put("catg","Genre");
		names.put("©too","Encoder");
		names.put("covr","Cover artwork");
		names.put("rtng","Rating");
		names.put("disk","Disk number");
	}

	/**
	 * Used to get the display name
	 * @param key The key of the atom
	 * @return the display name
	 */
	public String getDisplayName(String key) {
		return names.get(key);
	}
}

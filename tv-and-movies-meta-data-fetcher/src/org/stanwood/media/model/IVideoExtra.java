package org.stanwood.media.model;

import java.util.Map;

/**
 * Should be implemented by video classes that store extra information not returned by usual getters and setters.
 */
public interface IVideoExtra {

	/**
	 * Used to get extra information to a show that their are no getters/setters for in the regular fields
	 * @return The extra information in a map of key value pairs
	 */
	public Map<String,String>getExtraInfo();

	/**
	 * Used to add extra information to a show that their are no getters/setters for in the regular fields
	 * @param params The extra information in a map of key value pairs
	 */
	public void setExtraInfo(Map<String,String>params);
}

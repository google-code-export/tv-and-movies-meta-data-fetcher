package org.stanwood.media.setup;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a base class for all sub items of the media directory configuration.
 */
public class BaseMediaDirSubItem {

	private String id;
	private Map<String,String> params = new HashMap<String,String>();

	/**
	 * Used to get the ID of the store/store/action. The ID is the full class name of the store/store/action and
	 * will be used to create the store/store/action.
	 * @return The ID of the store/store/action.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Used to set the ID of the store/store/action. The ID is the full class name of the store/store/action and
	 * will be used to create the store/store/action.
	 * @param id The ID of the store.
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Used to get the key/value pair parameters of the store/store/action.
	 * @return A hash map containing store parameters.
	 */
	public Map<String,String> getParams() {
		return params;
	}

	/**
	 * Used to add a parameter of the store/store/action to it's configuration.
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 */
	public void addParam(String key,String value) {
		params.put(key,value);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return id;
	}

	/**
	 * Used to set the parameters
	 * @param parameters The parameters
	 */
	public void setParameters(Map<String, String> parameters) {
		params = parameters;
	}

}

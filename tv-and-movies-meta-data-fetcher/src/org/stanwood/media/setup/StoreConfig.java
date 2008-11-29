package org.stanwood.media.setup;

import java.util.HashMap;
import java.util.Map;

/**
 * This is used to hold the configuration for each store found in the configuration file.
 */
public class StoreConfig {

	private String id;
	private Map<String,String> params = new HashMap<String,String>();
	
	/**
	 * Used to get the ID of the store. The ID is the full class name of the store and
	 * will be used to create the store.
	 * @return The ID of the store
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Used to set the ID of the store. The ID is the full class name of the store and
	 * will be used to create the store. 
	 * @param id The ID of the store.
	 */
	public void setID(String id) {
		this.id = id;
	}
	
	/**
	 * Used to get the key/value pair parameters of the store.
	 * @return A hash map containing store parameters.
	 */
	public Map<String,String> getParams() {
		return params;
	}
	
	/**
	 * Used to add a parameter of the store to it's configuration.
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 */
	public void addParam(String key,String value) {		
		params.put(key,value);
	}
	
}

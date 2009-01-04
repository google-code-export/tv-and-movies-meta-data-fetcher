package org.stanwood.media.database;

/**
 * This is used to represent a field in a database table. It is mainly used for inserting rows into 
 * tables.
 */
public class Field {
	
	private String key;
	private Object value;	
	
	/**
	 * Used to construct the filed.
	 * @param key The key of the filed
	 * @param value The value of the filed
	 */
	public Field(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Used to get a the key of the field in the table
	 * @return The key of the field
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Used to set the key of the field
	 * @param key The key of the field
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Used to get the value of the field
	 * @return The value of the field
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * USed to set the value of the field
	 * @param value The value of the field
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Print out the fields key and value
	 * @return the key and value
	 */
	@Override
	public String toString() {
		return key+"="+value;
	}
	
	
	
}

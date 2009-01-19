package org.stanwood.media.database;

/**
 * This interfaces should be implemented by classes that want
 * to translate a parts of a SQL Query from MySQL to another 
 * native type of SQL.
 */
public interface IDBTokenMappings {

	/**
	 * Part of the SQL is passed into this method. If it to be 
	 * converted, then this method should return true.
	 * @param token The SQL token that is been checked
	 * @return True if handled by the mapping, otherwise false.
	 */
	public boolean accept(String token);
	
	/**
	 * If this mapping handled a token with {@link #accept(String)} was called,
	 * then this will return the replacement token.
	 * @return The replacement token
	 */
	public String getNativeToken();
}

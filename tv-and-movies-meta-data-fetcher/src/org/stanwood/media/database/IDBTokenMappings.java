package org.stanwood.media.database;

public interface IDBTokenMappings {

	public boolean accept(String token);
	
	public String getNativeToken();
}

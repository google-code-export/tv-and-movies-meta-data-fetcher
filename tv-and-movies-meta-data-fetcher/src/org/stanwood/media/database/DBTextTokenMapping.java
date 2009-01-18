package org.stanwood.media.database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBTextTokenMapping implements IDBTokenMappings {

	private String nativeToken;
	private Pattern mysqlRegexp;

	public DBTextTokenMapping(String mysqlRegexp,String nativeToken) {
		this.mysqlRegexp = Pattern.compile(mysqlRegexp,Pattern.CASE_INSENSITIVE);
		this.nativeToken = nativeToken;
	}
	
	public boolean accept(String token) {
		Matcher m = mysqlRegexp.matcher(token);
		return m.matches();
	}
	
	public String getNativeToken() {
		return nativeToken;
 	}
}

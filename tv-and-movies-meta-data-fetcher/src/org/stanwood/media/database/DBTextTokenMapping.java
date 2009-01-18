package org.stanwood.media.database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBTextTokenMapping implements IDBTokenMappings {

	private String nativeToken;
	private Pattern mysqlRegexp;
	private String result = "";

	public DBTextTokenMapping(String mysqlRegexp, String nativeToken) {
		this.mysqlRegexp = Pattern.compile(mysqlRegexp, Pattern.CASE_INSENSITIVE);
		this.nativeToken = nativeToken;
	}

	public boolean accept(String token) {
		result = nativeToken;
		Matcher m = mysqlRegexp.matcher(token);
		if (m.matches()) {
			for (int i = 0; i < m.groupCount(); i++) {
				result = result.replaceAll("\\$" + (i+1), m.group(i+1));
			}
			return true;
		}
		return false;		
	}

	public String getNativeToken() {
		return result;
	}
}

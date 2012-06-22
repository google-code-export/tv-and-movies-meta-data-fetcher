package org.stanwood.media.database.sdb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is used to translate DB tokens from MySQL to another
 * database. It does this through the use of regular expressions.
 * If the regular expression is used to extract groups from
 * the SQL, then these can be placed in the native token, via
 * the syntax $1....$n for groups 1....n.
 */
public class DBRegExpTokenMapping implements IDBTokenMappings {

	private String nativeToken;
	private Pattern mysqlRegexp;
	private String result = ""; //$NON-NLS-1$

	/**
	 * Used to construct a regular expression token mapping.
	 * @param mysqlRegexp The regular expression to match on.
	 * @param nativeToken The returned token pattern.
	 */
	public DBRegExpTokenMapping(String mysqlRegexp, String nativeToken) {
		this.mysqlRegexp = Pattern.compile(mysqlRegexp, Pattern.CASE_INSENSITIVE);
		this.nativeToken = nativeToken;
	}

	/**
	 * Part of the SQL is passed into this method. If it to be
	 * converted, then this method should return true.
	 * @param token The SQL token that is been checked
	 * @return True if handled by the mapping, otherwise false.
	 */
	@Override
	public boolean accept(String token) {
		result = nativeToken;
		Matcher m = mysqlRegexp.matcher(token);
		if (m.matches()) {
			for (int i = 0; i < m.groupCount(); i++) {
				result = result.replaceAll("\\$" + (i+1), m.group(i+1)); //$NON-NLS-1$
			}
			return true;
		}
		return false;
	}

	/**
	 * If this mapping handled a token with {@link #accept(String)} was called,
	 * then this will return the replacement token.
	 * @return The replacement token
	 */
	@Override
	public String getNativeToken() {
		return result;
	}
}

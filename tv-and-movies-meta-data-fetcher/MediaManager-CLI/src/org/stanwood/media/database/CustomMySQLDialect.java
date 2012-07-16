package org.stanwood.media.database;

import java.sql.Types;

import org.hibernate.dialect.MySQL5Dialect;

/**
 * Used to fixed a issue with the MySQL dialect
 */
public class CustomMySQLDialect extends MySQL5Dialect {

	/**
	 * The constructor
	 */
	public CustomMySQLDialect() {
		super();
		registerColumnType(Types.BOOLEAN, "bit(1)"); //$NON-NLS-1$
	}

}

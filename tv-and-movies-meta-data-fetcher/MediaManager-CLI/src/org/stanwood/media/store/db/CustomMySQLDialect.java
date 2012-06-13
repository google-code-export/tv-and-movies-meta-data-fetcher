package org.stanwood.media.store.db;

import java.sql.Types;

import org.hibernate.dialect.MySQLDialect;

/**
 * Used to fixed a issue with the MySQL dialect
 */
public class CustomMySQLDialect extends MySQLDialect {

	/**
	 * The constructor
	 */
	public CustomMySQLDialect() {
		super();
		registerColumnType(Types.BOOLEAN, "bit(1)"); //$NON-NLS-1$
	}

}

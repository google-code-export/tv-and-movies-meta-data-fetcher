package org.stanwood.media.database;

import java.sql.Types;

import org.hibernate.dialect.MySQLDialect;

/**
 * Used to fixed a issue with the MySQL dialect
 */
public class CustomMySQL5Dialect extends MySQLDialect {

	/**
	 * The constructor
	 */
	public CustomMySQL5Dialect() {
		super();
		registerColumnType(Types.BOOLEAN, "bit(1)"); //$NON-NLS-1$
	}

}

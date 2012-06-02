package org.stanwood.media.store.db;

import java.sql.Types;

import org.hibernate.dialect.MySQLDialect;

public class CustomMySQLDialect extends MySQLDialect {

	public CustomMySQLDialect() {
		super();
		registerColumnType(Types.BOOLEAN, "bit(1)");
	}

}

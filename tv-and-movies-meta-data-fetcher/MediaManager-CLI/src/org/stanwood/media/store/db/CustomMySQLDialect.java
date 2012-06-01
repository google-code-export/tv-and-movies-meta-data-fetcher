package org.stanwood.media.store.db;

import org.hibernate.dialect.MySQLDialect;
import org.hsqldb.types.Types;

public class CustomMySQLDialect extends MySQLDialect {

	public CustomMySQLDialect() {
		super();
		registerColumnType(Types.BOOLEAN, "bit(1)");
	}

}

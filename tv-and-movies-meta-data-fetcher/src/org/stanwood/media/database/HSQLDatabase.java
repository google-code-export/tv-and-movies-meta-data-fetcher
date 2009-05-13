/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a database manager class were the database is connected directly via
 * TCP/IP to to a memory only database called HSQLDB. This is mainly used with
 * JUnit tests.
 */
public class HSQLDatabase extends AbstractGenericDatabase implements
		IDatabase {

	private final static Log log = LogFactory.getLog(HSQLDatabase.class);
	private static final String DB_DRIVER_CLASS = "org.hsqldb.jdbcDriver";	
	private final static IDBTokenMappings mappings[] = new IDBTokenMappings[] {		
		new DBRegExpTokenMapping("FLOAT", "REAL"),
		new DBRegExpTokenMapping("auto_increment|AUTO_INCREMENT", "IDENTITY"),
		new DBRegExpTokenMapping("INTEGER\\([\\d]*\\)", "INTEGER"),
		new DBRegExpTokenMapping("INT\\([\\d]*\\)", "INTEGER"),
		new DBRegExpTokenMapping("tinyint\\([\\d]*\\)", "INTEGER"),
		new DBRegExpTokenMapping("unsigned", ""),
		new DBRegExpTokenMapping("(.*)default '.*'(.*)", "$1$2"),
		new DBRegExpTokenMapping("(.*)default null(.*)", "$1$2"),
		new DBRegExpTokenMapping("key \\\".*\\\" \\(.*\\)", ""),
		new DBRegExpTokenMapping("primary key.*\\(.*\\)", ""),
		new DBRegExpTokenMapping("text", "varchar")
	};
		
	private String database = null;
	private String password;
	private String username;	
	
	/**
	 * Used to create a MYSQL database controller class.
	 * @param host The database host
	 * @param username The name of the user used to access the database
	 * @param password The name of the password used to access the database
	 * @param database The name of the database to connect to
	 */
	public HSQLDatabase(String host,String username,String password,String database) {
		super();		
		this.password = password;
		this.database = database;
	}
	
	/**
	 * This is used to setup the database manager class, it should be called
	 * after creating a database manager class.
	 * 
	 * @throws UnableToConnectToDatabaseException
	 */
	public void init() throws UnableToConnectToDatabaseException {
		try {
			Class.forName(DB_DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			throw new UnableToConnectToDatabaseException(
					"Cant find HSQLDB driver class : " + DB_DRIVER_CLASS);
		}
		log.debug("Atempting to connect to the database...");
	}

	/**
	 * This is used to get a connection to the database to the memory only
	 * database
	 * 
	 * @return The connection to the database
	 * @throws SQLException
	 *             Thrown if their is a problem getting the connection to the
	 *             database
	 */
	public Connection createConnection() throws SQLException {
		try {
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:"+database, username, password);
			log.debug("Connected to the database");
			return connection;
		} catch (RuntimeException e) {
			log.error("Unable to connect to the database: " + e.getMessage());
			throw new SQLException(e.getMessage());
		}

	}

	/**
	 * Used to create the test database. 
	 */
	public void createTestDatabase() {
		try {
			executeSQL("create database "+database);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * This is called to delete a table from the database. It uses the
	 * connection passed in.
	 * 
	 * @param tableName The table to delete
	 * @param connection The connection to the database
	 * @return True if it was successful, otherwise false;
	 */
	public boolean dropTable(Connection connection, String tableName) {
		try {
			executeSQL(connection, "DROP TABLE IF EXISTS " + tableName + "");
			return true;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * This is used to create a PreparedStatement from the give SQL. This is
	 * useful as different implementations of the interface can translate the
	 * SQL so that it is under stood by the database. The SQL should be in the
	 * format of MySQL SQL. Because this is not a MySQL database, This will
	 * attempt to translate the SQL into something that can be understood by
	 * HSQLDB.
	 * 
	 * @see PreparedStatement
	 * @param connection
	 *            A connection to the database
	 * @param sql The statements sql
	 * @return A Prepared Statement
	 * @throws SQLException
	 *             Thrown if their is a problem creating the statement
	 */
	public PreparedStatement getStatement(Connection connection, String sql)
			throws SQLException {
		String fixedSQL = fixSQL(sql);
		log.debug("SQL : " +fixedSQL);
		return connection.prepareStatement(fixedSQL);
	}

	/**
	 * This is used to make sure that all DB resources are closed. If any of the
	 * parameters are null, then they an attempt to close them is not made.
	 * 
	 * @param connection
	 *            The connection to close
	 * @param stmt The statement to close
	 * @param rs The result set to close
	 */
	public void closeDatabaseResources(Connection connection,
			PreparedStatement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
			rs = null;
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
			stmt = null;
		}
		if (connection != null) {
			try {
				closeConnection(connection);
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
			connection = null;
		}
	}

	private String fixSQL(String sql) {
		sql = sql.replaceAll("`", "\"");
		if (sql.toLowerCase().startsWith("create table")) {
			sql = fixSQLCreateTable(sql);
		}

		return sql;
	}
	
	private int findCloseBracket(String sql,int start) {
		int depth = 0;
		for (int i=start;i<sql.length();i++) {
			if (sql.charAt(i)=='(') {
				depth++;
			}
			else if (sql.charAt(i)==')') {
				depth--;
				if (depth==0) {
					return i;
				}
			}
		}
		return -1;
	}
	
	private String fixSQLCreateTable(String sql) {
		StringBuffer result = new StringBuffer();
		
		int start = sql.indexOf("(");
		int end = findCloseBracket(sql, start);
		if (end==-1) {
			log.error("Bracket mismatch in SQL statement");
			return null;
		}
		result.append(translateSQL(sql.substring(0,start)));
		
		String tableDef = sql.substring(start,end);
		
		StringTokenizer tok = new StringTokenizer(tableDef,",");
		boolean first = true;
		while (tok.hasMoreTokens()) {
			String token = translateSQL(tok.nextToken());
			if (token.length()>0) {
				if (!first) {
					result.append(",");				
				}
				result.append(token);
				first = false;
			}				
		}
		
		result.append(translateSQL(sql.substring(end)));
		return result.toString();
	}
	
	private String translateSQL(String sql) {
		sql = translateTokens(sql);	
		StringBuilder result = new StringBuilder();
		StringTokenizer tok = new StringTokenizer(sql," ",true);
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			token = translateTokens(token);	
			result.append(token);
		}
		
		return result.toString().trim();
	}

	private String translateTokens(String token) {
		for (IDBTokenMappings mapping : mappings) {
			if (mapping.accept(token)) {
				token = mapping.getNativeToken();
				break;
			}
		}
		return token;
	}	

	/**
	 * This is used to execute a simple SQL statement on the database.
	 * @param connection	a connection to be re-used, useful for running a series 
	 *						of updates as a transaction
	 * @param sql			the SQL to execute on the database
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public void executeSQL(Connection connection, String sql) throws SQLException {
		sql = fixSQL(sql);
		log.debug("SQL : " + sql);

		PreparedStatement stmt = null;

		try {			
			stmt = connection.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			closeDatabaseResources(null, stmt, null);
			stmt = null;
		}
	}

}

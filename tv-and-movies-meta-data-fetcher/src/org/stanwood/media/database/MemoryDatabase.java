package org.stanwood.media.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a database manager class were the database is connected directly via
 * TCP/IP to to a memory only database called HSQLDB. This is mainly used with
 * JUnit tests.
 */
public class MemoryDatabase extends AbstractGenericDatabase implements
		IDatabase {

	private final static Log log = LogFactory.getLog(MemoryDatabase.class);
	private static final String DB_DRIVER_CLASS = "org.hsqldb.jdbcDriver";

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
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:aname", "sa", "");
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
			executeSQL("create database testdb");
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
	 * @param sql
	 *            The statements sql
	 * @return A Prepared Statement
	 * @throws SQLException
	 *             Thrown if their is a problem creating the statement
	 */
	public PreparedStatement getStatement(Connection connection, String sql)
			throws SQLException {
		return connection.prepareStatement(fixSQL(sql));
	}

	/**
	 * This is used to make sure that all DB resources are closed. If any of the
	 * parameters are null, then they an attempt to close them is not made.
	 * 
	 * @param connection
	 *            The connection to close
	 * @param stmt
	 *            The statement to close
	 * @param rs
	 *            The result set to close
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
		sql = sql.replaceAll("`", "");
		sql = sql.replaceAll("FLOAT", "REAL");
		sql = sql.replaceAll("auto_increment|AUTO_INCREMENT", "IDENTITY");
		sql = sql.replaceAll("INTEGER\\([\\d]*\\)", "INTEGER");
		return sql;
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
		System.out.println("SQL : " + sql);

		PreparedStatement stmt = null;

		try {
			connection = createConnection();
			stmt = connection.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			closeDatabaseResources(null, stmt, null);
			stmt = null;
//			connection = null;
		}
	}

}

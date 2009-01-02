package org.stanwood.media.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class contains methods for database connections that are generic for all
 * database implementations.
 */
public abstract class AbstractGenericDatabase implements IDatabase {

	private final static Log log = LogFactory.getLog(AbstractGenericDatabase.class);
	
	
	
	/**
	 * This is used to get a non-auto-committing connection to the database from a datasource
	 * 
	 * @return a connection to the database
	 * @throws SQLException Thrown if their is a problem getting the connection to the
	 *             database
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public Connection createTransactionConnection() throws SQLException {
		Connection connection = null;
		try {
			connection = createConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		return connection;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void commitTransactionConnection(Connection connection) throws SQLException {
		log.debug("Committing Transacton");
		try {
			connection.commit();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}	
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void rollbackTransactionConnection (Connection connection) {
		log.debug("Rolling back Transaction");
		
		try {
			connection.rollback();
			
			//TODO log all the roll back activities if possible?
			
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("Serious error, a database roll back has failed.  See logs, now!");
		}
	}

	
	/**
	 * This is used to make sure that all DB resources are closed. If any of the
	 * parameters are null, then they an attempt to close them is not made.
	 * 
	 * @param connection	the connection to close
	 * @param stmt			the statement to close
	 * @param rs		    the result set to close
	 */
	@Override
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

	/**
	 * This is used to close a connection. This is done in the database interface so that 
	 * connection closure can be logged from a central location and open connection count can 
	 * be kept
	 * @param connection The connection to close
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public void closeConnection(Connection connection) throws SQLException {
		connection.close();		
	}
	
	/**
	 * This is called to delete a table from the database It creates it's own connection too the DB
	 * @param tableName The table to delete
	 * @return True if it was successful, otherwise false;
	 */
	@Override
	public boolean dropTable(String tableName) {
		Connection connection = null;
		try {
			connection = createConnection();
			dropTable(connection, tableName);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return false;				
		} finally {
			closeDatabaseResources(connection, null, null);
			connection = null;
		}	
		return true;
	}
	
	/**
	 * This is used to execute a simple SQL statement on the database.
	 * 
	 * @param sql	the SQL to execute on the database
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public void executeSQL(String sql) throws SQLException {
		Connection connection = null;
		try {
			connection = createConnection();
			executeSQL(connection, sql);
		} finally {
			closeDatabaseResources(connection, null, null);
			connection = null;
		}
	}
	
	/**
	 * This is used to create a PreparedStatement from the given SQL. The SQL should
	 * contain ? were the params should be inserted.
	 * 
	 * @see PreparedStatement
	 * @param connection	a connection to the database
	 * @param sql 			the statements SQL
	 * @param 				params the parameters to place into the statement       
	 * @return 				a Prepared Statement
	 * @throws SQLException	thrown if their is a problem creating the statement
	 */
	@Override
	public PreparedStatement getStatement(Connection connection, String sql,
			Object[] params) throws SQLException {
		
		PreparedStatement stmt = null;
		
		log.debug("Preparing statement");
		stmt = getStatement(connection, sql);
				
		log.debug("Setting variables");
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null) {
				throw new RuntimeException("A null param at index " + i 
						+ " is unsupported.");
			}
			else if (params[i] instanceof String) {
				stmt.setString(i + 1, (String) params[i]);
			} else if (params[i] instanceof Float) {
				stmt.setFloat(i + 1, (Float) params[i]);
			} else if (params[i] instanceof Integer) {
				stmt.setInt(i + 1, (Integer) params[i]);
			} else if (params[i] instanceof Boolean) {
				stmt.setBoolean(i + 1, (Boolean) params[i]);
			} else if (params[i] instanceof Long) {
				stmt.setLong(i + 1, (Long) params[i]);
			} else {					
				throw new RuntimeException("A param type of "
						+ params[i].getClass().getName()
						+ " is unsupported.");
			}
		}
		return stmt;
	}
	
	
	/**
	 * This is used to execute an update statement that takes parameters. The SQL should
	 * contain ? were the parameters should be inserted.
	 * 
	 * @param sql			the SQL to execute on the database
	 * @param params		the parameters to insert into the SQL statement, replacing ?.
	 * @return 				if a key was generated, then it is returned, otherwise -1
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public long executeUpdate(String sql, Object[] params) throws SQLException {
		
		Connection connection = null;
		try {
			//Get a connection
			connection = createConnection();
			return executeUpdate(connection, sql, params);
		} finally {
			closeDatabaseResources(connection, null, null);			
		}

	}
	
	/**
	 * This is used to execute an update statement that takes parameters. The SQL should
	 * contain ? were the parameters should be inserted.
	 * 
	 * @param connection	a connection to be re-used, useful for running a series of updates as a 
	 * 						transaction
	 * @param sql			The SQL to execute on the database
	 * @param params		the parameters to insert into the SQL statement, replacing ?.	
	 * @return 				if a key was generated, then it is returned, otherwise -1
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public long executeUpdate(Connection connection, String sql, Object[] params) throws SQLException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {			
			//Prepare and execute the update
			stmt = getStatement(connection, sql, params);
			stmt.executeUpdate();
			
			//Check for any keys
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				long key = rs.getLong(1);
				return key;
			}
			
			//If not return
			return -1;
			
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			closeDatabaseResources(null, stmt, rs);
		}
	}
}

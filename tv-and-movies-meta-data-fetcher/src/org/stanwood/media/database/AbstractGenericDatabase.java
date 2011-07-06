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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
		log.debug("Committing Transacton"); //$NON-NLS-1$
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
		log.debug("Rolling back Transaction"); //$NON-NLS-1$

		try {
			connection.rollback();

			//TODO log all the roll back activities if possible?

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("Serious error, a database roll back has failed.  See logs, now!"); //$NON-NLS-1$
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

		log.debug("Preparing statement"); //$NON-NLS-1$
		stmt = getStatement(connection, sql);

		log.debug("Setting variables"); //$NON-NLS-1$
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null) {
				throw new RuntimeException("A null param at index " + i  //$NON-NLS-1$
						+ " is unsupported."); //$NON-NLS-1$
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
				throw new RuntimeException("A param type of " //$NON-NLS-1$
						+ params[i].getClass().getName()
						+ " is unsupported."); //$NON-NLS-1$
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

	/**
	 * This is used to insert table row into a table. The table row is made up from fields.
	 * @param connection a connection to be re-used, useful for running a series
	 * @param tableName  The name of the table
	 * @param fields     The fields of the table that are to be inserted.
	 * @return If a key was generated, then it is pass here, otherwise -1
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public long insertIntoTable(Connection connection, String tableName, List<Field> fields) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO `" + tableName + "` ("); //$NON-NLS-1$ //$NON-NLS-2$
		boolean first = true;
		for (Field field : fields) {
			if (!first) {
				sql.append(","); //$NON-NLS-1$
			}
			sql.append("`"); //$NON-NLS-1$
			sql.append(field.getKey());
			sql.append("`"); //$NON-NLS-1$
			first = false;
		}
		sql.append(") VALUES ("); //$NON-NLS-1$
		Object params[] = new Object[fields.size()];
		for (int i=0;i<fields.size();i++) {
			if (i==0) {
				sql.append("?"); //$NON-NLS-1$
			}
			else {
				sql.append(",?"); //$NON-NLS-1$
			}
			params[i] = fields.get(i).getValue();
		}
		sql.append(")"); //$NON-NLS-1$


		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//Prepare and execute the update
			stmt = getStatement(connection, sql.toString(), params);
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

	/**
	 * This is used to insert table row into a table. The table row is made up from fields.
	 * @param tableName  The name of the table
	 * @param fields     The fields of the table that are to be inserted.
	 * @return If a key was generated, then it is pass here, otherwise -1
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public long insertIntoTable(String tableName,List<Field> fields) throws SQLException {
		Connection connection = null;
		try {
			//Get a connection
			connection = createConnection();
			return insertIntoTable(connection, tableName, fields);
		} finally {
			closeDatabaseResources(connection, null, null);
		}
	}
}

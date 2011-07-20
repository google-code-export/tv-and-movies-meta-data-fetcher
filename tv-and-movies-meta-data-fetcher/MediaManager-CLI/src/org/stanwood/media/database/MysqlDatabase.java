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
import java.sql.SQLException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a database manager class were the database is connected directly via TCP/IP to a MySQL database.
 */
public class MysqlDatabase extends AbstractGenericDatabase implements IDatabase{

	final static Log log = LogFactory.getLog(MysqlDatabase.class);

	private static final String DB_DRIVER_CLASS = "com.mysql.jdbc.Driver"; //$NON-NLS-1$
	private String host = "localhost"; //$NON-NLS-1$
	private String port = "3306"; //$NON-NLS-1$
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
	public MysqlDatabase(String host,String username,String password,String database) {
		super();
		this.host = host;
		this.password = password;
		this.username = username;
		this.database = database;
	}

	/**
	 * This is used to setup the database manager class, it should be called after creating a
	 * database manager class.
	 * @throws UnableToConnectToDatabaseException
	 */
	@Override
	public void init() throws UnableToConnectToDatabaseException {
		try
		{
			Class.forName(DB_DRIVER_CLASS);
		}
		catch (ClassNotFoundException e) {
			throw new UnableToConnectToDatabaseException(MessageFormat.format(Messages.getString("MysqlDatabase.CANT_FIND_DRIVER"),DB_DRIVER_CLASS)); //$NON-NLS-1$
		}
        String url = getConnectionUrl();
        if (log.isDebugEnabled()) {
        	log.debug("Attempting to connect to database: " + username + "@" + url); //$NON-NLS-1$ //$NON-NLS-2$
        }
	}

	/**
	 * Used to get the connection url used to connect to the database
	 * @return The connection url
	 */
	public String getConnectionUrl() {
		return "jdbc:mysql://"+host+":"+port+"/"+database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * This is used to get a connection to the database
	 * @return The connection to the database
	 * @throws SQLException Thrown if their is a problem getting the connection to the database
	 */
	@Override
	public Connection createConnection() throws SQLException {
		try
		{
			String url = getConnectionUrl();
			Connection connection = DriverManager.getConnection(url,username,password);
			if (log.isDebugEnabled()) {
				log.debug("Connected to the database"); //$NON-NLS-1$
			}
			return connection;
		}
		catch (RuntimeException e)
		{
			log.error(Messages.getString("MysqlDatabase.UNABLE_TO_CONNECT_DB"),e); //$NON-NLS-1$
			throw new SQLException(e.getMessage());
		}

	}

	/**
	 * Used to set the host name of the database server
	 * @param host The host name of the database server
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Used to set the username used to connect to the database
	 * @param username The username used to connect to the database
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Used to set the password used to connect to the database
	 * @param password The password used to connect to the database
	 */
	public void setPassword(String password) {
		this.password = password;
	}



	/**
	 * This is called to delete a table from the database
	 * @param connection The connection to the database
	 * @param tableName The table to delete
	 * @return True if it was successful, otherwise false;
	 */
	@Override
	public boolean dropTable(Connection connection,String tableName) {
		try {
			executeSQL(connection,"DROP TABLE IF EXISTS `" + tableName + "`"); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * This is used to create a PreparedStatement from the give SQL. This is useful as
	 * different implementations of the interface can translate the SQL so that it is under
	 * stood by the database. The SQL should be in the format of MySQL SQL.
	 * @see PreparedStatement
	 * @param connection A connection to the database
	 * @param sql The statements sql
	 * @return A Prepared Statement
	 * @throws SQLException Thrown if their is a problem creating the statement
	 */
	@Override
	public PreparedStatement getStatement(Connection connection,String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	/**
	 * This is used to execute a simple SQL statement on the database.
	 * @param connection	a connection to be re-used, useful for running a series
	 *						of updates as a transaction
	 * @param sql			the SQL to execute on the database
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	@Override
	public void executeSQL(Connection connection, String sql)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug("SQL executed : " + sql); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			closeDatabaseResources(null, stmt, null);
		}
	}
}

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
package org.stanwood.media.database.sdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * This interface should be implemented by database manager classes that want to provide a connection 
 * to the database.
 */
public interface IDatabase {
	
	/**
	 * This is used to setup the database manager class, it should be called after creating a 
	 * database manager class.
	 * @throws UnableToConnectToDatabaseException Thrown if unable to connect to the database
	 */
	public void init() throws UnableToConnectToDatabaseException;
	
	/**
	 * This is used to get a connection to the database
	 * @return The connection to the database
	 * @throws SQLException Thrown if there is a problem getting the connection to the database
	 */
	public Connection createConnection() throws SQLException;
	
	/**
	 * This is used to get a connection to the database
	 * @return The connection to the database, which is not auto-committing
	 * @throws SQLException Thrown if there is a problem getting the connection to the database
	 */
	public Connection createTransactionConnection() throws SQLException;
	
	/**
	 * This is used to commit a connection to the database
	 * @param connection This connection is committed
	 * @throws SQLException Thrown if there is a problem getting the connection to the database
	 */
	public void commitTransactionConnection(Connection connection) throws SQLException;
	
	/**
	 * This is used to rollback all statements pushed through this connection since the last commit or save from the database
	 * @param connection This connection is rolled back
	 */
	public void rollbackTransactionConnection(Connection connection);
	
	/**
	 * This is used to execute a simple SQL statement on the database.
	 * @param sql The SQL to execute on the database
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public void executeSQL(String sql) throws SQLException;
	
	/**
	 * This is used to execute a simple SQL statement on the database.
	 * @param connection	a connection to be re-used, useful for running a series 
	 *						of updates as a transaction
	 * @param sql			the SQL to execute on the database
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public void executeSQL(Connection connection, String sql) throws SQLException;
	
	/**
	 * This is used to execute a update state that takes params. The SQL should
	 * contain ? were the params should be inserted. 
	 * @param sql The SQL to execute on the database
	 * @param params The params to insert into the SQL statement.
	 * @return If a key was generated, then it is pass here, otherwise -1 
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public long executeUpdate(String sql, Object[] params) throws SQLException;
	
	/**
	 * This is used to insert table row into a table. The table row is made up from fields.
	 * @param connection a connection to be re-used, useful for running a series 
	 * @param tableName  The name of the table
	 * @param fields     The fields of the table that are to be inserted.
	 * @return If a key was generated, then it is pass here, otherwise -1 
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public long insertIntoTable(Connection connection, String tableName,List<Field> fields) throws SQLException;

	/**
	 * This is used to insert table row into a table. The table row is made up from fields.
	 * @param tableName  The name of the table
	 * @param fields     The fields of the table that are to be inserted.
	 * @return If a key was generated, then it is pass here, otherwise -1 
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public long insertIntoTable(String tableName,List<Field> fields) throws SQLException;
	
	/**
	 * This is used to execute a update state that takes params. The SQL should
	 * contain ? were the params should be inserted.
	 * @param connection A connection to the database
	 * @param sql The SQL to execute on the database
	 * @param params The params to insert into the SQL statement.	
	 * @return If a key was generated, then it is pass here, otherwise -1 
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public long executeUpdate(Connection connection, String sql, Object[] params) throws SQLException;
	
	/**
	 * This is called to delete a table from the database It creates it's own connection too the DB
	 * @param tableName The table to delete
	 * @return True if it was successful, otherwise false;
	 */
	public boolean dropTable(String tableName);
	
	/**
	 * This is called to delete a table from the database. It uses the connection passed in.
	 * @param tableName The table to delete
	 * @param connection The database connection
	 * @return True if it was successful, otherwise false;
	 */
	public boolean dropTable(Connection connection,String tableName);
	
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
	public PreparedStatement getStatement(Connection connection, String sql) throws SQLException;
	
	/**
	 * This is used to create a PreparedStatement from the given SQL. The SQL should
	 * contain ? were the params should be inserted.
	 * 
	 * @see PreparedStatement
	 * @param connection	a connection to the database
	 * @param sql 			the statements sql
	 * @param 				params the params to place into the statement       
	 * @return 				a Prepared Statement
	 * @throws SQLException	thrown if their is a problem creating the statement
	 */
	public PreparedStatement getStatement(Connection connection, String sql,
			Object[] params) throws SQLException;
	
	/**
	 * This is used to make sure that all DB resources are closed. If any of the parameters are null, then
	 * they an attempt to close them is not made. 
	 * @param connection The connection to close
	 * @param stmt The statement to close
	 * @param rs The result set to close
	 */
	public void closeDatabaseResources(Connection connection, PreparedStatement stmt, ResultSet rs);

	/**
	 * This is used to close a connection. This is done in the database interface so that 
	 * connection closure can be logged from a central location and open connection count can 
	 * be kept
	 * @param connection The connection to close
	 * @throws SQLException Thrown if their is a problem talking to the database
	 */
	public void closeConnection(Connection connection) throws SQLException;
	
}

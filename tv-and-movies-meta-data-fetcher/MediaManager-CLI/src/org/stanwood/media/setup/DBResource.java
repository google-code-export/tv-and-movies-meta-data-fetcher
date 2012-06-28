/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.setup;

/**
 * Used to hold information on database resources
 */
public class DBResource {

	private String url;
	private String username;
	private String password;
	private String dialect;
	private String resourceId;
	private SchemaCheck schemaCheck;

	/**
	 * Used to get the schema check
	 * @return the schema check
	 */
	public SchemaCheck getSchemaCheck() {
		return schemaCheck;
	}

	/**
	 * Used to set the schema check option
	 * @param schemaCheck the schema check option
	 */
	public void setSchemaCheck(SchemaCheck schemaCheck) {
		this.schemaCheck = schemaCheck;
	}

	/**
	 * Used to get the resource ID
	 * @return The resource ID
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * Used to set the resource ID
	 * @param resourceId The resource ID
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Used to get the database connection URL
	 * @return the database connection URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Used to set the database connection URL
	 * @param url the database connection URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Used to get the user name of the user used to access the database
	 * @return the user name of the user used to access the database
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Used to set the user name of the user used to access the database
	 * @param username the user name of the user used to access the database
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Used to get the password of the user used to access the database
	 * @return the password of the user used to access the database
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Used to set the password of the user used to access the database
	 * @param password the password of the user used to access the database
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Used to get the SQL dialect class name used to talk to the database
	 * @return the SQL dialect class name used to talk to the database
	 */
	public String getDialect() {
		return dialect;
	}

	/**
	 * Used to set the SQL dialect class name used to talk to the database
	 * @param dialect the SQL dialect class name used to talk to the database
	 */
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}


}

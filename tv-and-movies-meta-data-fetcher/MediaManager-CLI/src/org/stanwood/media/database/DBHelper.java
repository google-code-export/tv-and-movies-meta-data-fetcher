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
package org.stanwood.media.database;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.setup.SchemaCheck;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to get DB sessions
 */
public class DBHelper {

	private static DBHelper instance = null;
	private Map<String,Session>sessions = new HashMap<String,Session>();

	private DBHelper() {

	}

	/**
	 * Used to get a instance of the helper
	 * @return a instance of the helper
	 */
	public synchronized static DBHelper getInstance() {
		if (instance==null) {
			instance = new DBHelper();
		}
		return instance;
	}

	/**
	 * Used to get the database session for a given resource
	 * @param resource The resource
	 * @return The database session
	 * @throws DatabaseException Thrown if their is a problem
	 */
	public synchronized Session getSession(DBResource resource) throws DatabaseException {
		Session session = sessions.get(resource.getResourceId());
		if (session == null) {
			Configuration configuration = DBHelper.getInstance().getConfiguration(resource);
			try {
				SessionFactory factory = configuration.buildSessionFactory();
				session = factory.openSession();
			}
			catch (HibernateException e1) {
				new SchemaExport(configuration).create(false, true);
				SessionFactory factory = configuration.buildSessionFactory();
				session = factory.openSession();
			}
			sessions.put(resource.getResourceId(),session);
		}
		return session;
	}

	/**
	 * Used to get the database configuration
	 * @param resource The database resource
	 * @return The hibernate database configuration
	 * @throws DatabaseException Thrown if their is a problem
	 */
	public Configuration getConfiguration(DBResource resource) throws DatabaseException {
		String connectionUserName = resource.getUsername();
		if (connectionUserName==null) {
			connectionUserName = ""; //$NON-NLS-1$
		}
		String connectionPassword = resource.getPassword();
		if (connectionPassword==null) {
			connectionPassword = ""; //$NON-NLS-1$
		}
		String dialect = resource.getDialect();
		if (dialect.equals(MySQLDialect.class.getName())) {
			dialect=CustomMySQLDialect.class.getName();
		}
		else if (dialect.equals(MySQL5Dialect.class.getName())) {
			dialect=CustomMySQL5Dialect.class.getName();
		}
		Configuration configuration;
		try {
			configuration = getConfiguration(resource.getUrl(),
					connectionUserName, connectionPassword,dialect,resource.getSchemaCheck());
		} catch (XMLParserException e) {
			throw new DatabaseException("Unable to configure database", e);
		}
		return configuration;
	}


	/**
	 * Used to create a database configuration
	 * @param url The URL of the database connection
	 * @param username User name of the DB user
	 * @param password Password of the DB user
	 * @param dialect The SQL dialect to used when talking to the database
	 * @param schemaCheck The hibernate hbm2ddl.auto setting value
	 * @return The Configuration
	 * @throws XMLParserException Thrown if their is a problem
	 */
	private static Configuration getConfiguration(String url, String username,
			String password, String dialect,SchemaCheck schemaCheck) throws XMLParserException {
		Document dom = XMLParser
				.parse(DBHelper.class
						.getResourceAsStream("hibernate.config.xml"), null); //$NON-NLS-1$
		Element element = XMLParser.firstChild(dom);
		element = XMLParser.firstChild(element);
		Element propEl = dom.createElement("property"); //$NON-NLS-1$
		propEl.setAttribute("name", "hibernate.connection.url"); //$NON-NLS-1$ //$NON-NLS-2$
		propEl.appendChild(dom.createTextNode(url));
		element.appendChild(propEl);
		propEl = dom.createElement("property"); //$NON-NLS-1$
		propEl.setAttribute("name", "hibernate.dialect"); //$NON-NLS-1$//$NON-NLS-2$
		propEl.appendChild(dom.createTextNode(dialect));
		element.appendChild(propEl);
		propEl = dom.createElement("property"); //$NON-NLS-1$
		propEl.setAttribute("name", "hibernate.connection.password"); //$NON-NLS-1$ //$NON-NLS-2$
		propEl.appendChild(dom.createTextNode(password));
		element.appendChild(propEl);
		propEl = dom.createElement("property"); //$NON-NLS-1$
		propEl.setAttribute("name", "hibernate.connection.username"); //$NON-NLS-1$//$NON-NLS-2$
		propEl.appendChild(dom.createTextNode(username));
		element.appendChild(propEl);

		if (schemaCheck!=null && !schemaCheck.getValue().equals("validate")) { //$NON-NLS-1$
			Node node = XMLParser.selectSingleNode(element, "property[@name='hbm2ddl.auto']"); //$NON-NLS-1$
			node.setTextContent(schemaCheck.getValue());
		}

		Configuration configuration = new Configuration().configure(dom);
		return configuration;
	}

	/**
	 * Uses to get the database schema for a given dialect
	 * @param dialect The dialect
	 * @return The schema
	 * @throws DatabaseException Thrown if their is a problem with hibernate
	 */
	public String getSchema(String dialect) throws DatabaseException {
		try {
			Configuration config = getConfiguration("","","",dialect,SchemaCheck.NONE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			SchemaExport exporter = new SchemaExport(config);
			exporter.setFormat(true);
			File file = FileHelper.createTempFile("schema", ".sql");  //$NON-NLS-1$//$NON-NLS-2$
			try {
				exporter.setOutputFile(file.getAbsolutePath());
				exporter.create(true, false);
				return FileHelper.readFileContents(file);
			}
			finally {
				FileHelper.delete(file);
			}
		}
		catch (HibernateException e) {
			throw new DatabaseException("Unable to print database schema",e);
		} catch (XMLParserException e) {
			throw new DatabaseException("Unable to print database schema",e);
		} catch (IOException e) {
			throw new DatabaseException("Unable to print database schema",e);
		}
	}
}

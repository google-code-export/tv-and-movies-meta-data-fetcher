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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.setup.SchemaCheck;
import org.stanwood.media.store.db.DatabaseStore;
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

	public synchronized static DBHelper getInstance() {
		if (instance==null) {
			instance = new DBHelper();
		}
		return instance;
	}

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
				.parse(DatabaseStore.class
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


}

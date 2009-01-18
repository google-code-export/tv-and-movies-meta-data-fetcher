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
package org.stanwood.media.store.mythtv;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.stanwood.media.database.IDatabase;
import org.stanwood.media.database.MemoryDatabase;

/**
 * Used to test the {@link MythTVStore} class.
 */
public class TestMythTVStore extends TestCase {

	private IDatabase database = null;

	/**
	 * Setup the database to the memory only database. And create the default tables
	 * 
	 * @throws Exception Thrown if not able to connect to DB or create the tables
	 */
	public void setUp() throws Exception {
		database = new MemoryDatabase();
		database.init();
		((MemoryDatabase) database).createTestDatabase();
		setupTables();
	}

	private void setupTables() throws SQLException {
		database.executeSQL("CREATE TABLE videocast(`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `cast` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`)) ");
		database.executeSQL("CREATE TABLE videocategory(`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `category` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`)) ");
		database.executeSQL("CREATE TABLE videocountry(`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `country` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`)) ");
		database.executeSQL("CREATE TABLE videogenre(`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `genre` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`)) ");
		database.executeSQL("CREATE TABLE `videometadata` (" + "`intid` int(10) unsigned NOT NULL auto_increment,"
				+ "`title` varchar(128) NOT NULL," + "`director` varchar(128) NOT NULL," + "`plot` text,"
				+ "`rating` varchar(128) NOT NULL," + "`inetref` varchar(255) NOT NULL,"
				+ "`year` int(10) unsigned NOT NULL," + "`userrating` float NOT NULL,"
				+ "`length` int(10) unsigned NOT NULL," + "`showlevel` int(10) unsigned NOT NULL,"
				+ "`filename` text NOT NULL," + "`coverfile` text NOT NULL,"
				+ "`childid` int(11) NOT NULL default '-1'," + "`browse` tinyint(1) NOT NULL default '1',"
				+ "`playcommand` varchar(255) default NULL," + "`category` int(10) unsigned NOT NULL default '0',"
				+ "`trailer` text," + "PRIMARY KEY  (`intid`)," + "KEY `director` (`director`),"
				+ "KEY `title` (`title`)," + "KEY `title_2` (`title`))");

		database.executeSQL("CREATE TABLE `videometadatacast` (" + "`idvideo` int(10) unsigned NOT NULL,"
				+ "`idcast` int(10) unsigned NOT NULL" + ")");

		database.executeSQL("CREATE TABLE `videometadatacountry` (" + "`idvideo` int(10) unsigned NOT NULL,"
				+ "`idcountry` int(10) unsigned NOT NULL," + "KEY `idvideo` (`idvideo`),"
				+ "KEY `idcountry` (`idcountry`)");

		database.executeSQL("CREATE TABLE `videometadatagenre` (" + "`idvideo` int(10) unsigned NOT NULL,"
				+ "`idgenre` int(10) unsigned NOT NULL," + "KEY `idvideo` (`idvideo`)," + "KEY `idgenre` (`idgenre`)");

	}

	/**
	 * Used to close the database after the tests have finished
	 */
	public void tearDown() {
		database = null;
	}

	public void testCacheFilm() {

	}
}

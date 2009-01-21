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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.stanwood.media.FileHelper;
import org.stanwood.media.database.HSQLDatabase;
import org.stanwood.media.database.IDatabase;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.source.IMDBSource;
import org.stanwood.media.store.StoreException;

/**
 * Used to test the {@link MythTVStore} class.
 */
public class TestMythTVStore extends TestCase {

	private static final String DB_USER = "ua";
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private final static String DB_NAME = "aname";
	
	private IDatabase database = null;

	/**
	 * Setup the database to the memory only database. And create the default tables
	 * 
	 * @throws Exception Thrown if not able to connect to DB or create the tables
	 */
	public void setUp() throws Exception {
		database = new HSQLDatabase("",DB_USER,"",DB_NAME);
		database.init();
		((HSQLDatabase) database).createTestDatabase();
		setupTables();
	}
		
	private void setupTables() throws SQLException {		
		database.executeSQL("CREATE TABLE `videocast` (`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `cast` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`))");
		database.executeSQL("CREATE TABLE `videocategory` (`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `category` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`))");
		database.executeSQL("CREATE TABLE `videocountry` (`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `country` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`))");
		database.executeSQL("CREATE TABLE `videogenre` (`intid` INTEGER(10) NOT NULL AUTO_INCREMENT, `genre` VARCHAR(128) NOT NULL,PRIMARY KEY (`intid`))");
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
				+ "KEY `idcountry` (`idcountry`))");

		database.executeSQL("CREATE TABLE `videometadatagenre` (" + "`idvideo` int(10) unsigned NOT NULL,"
				+ "`idgenre` int(10) unsigned NOT NULL," + "KEY `idvideo` (`idvideo`)," + "KEY `idgenre` (`idgenre`))");
		
		assertEquals("Check number of films",0,getNumberOfFilms());
	}
	
	private long getNumberOfFilms() throws SQLException{
		long count = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = null; 
		try {
			connection = database.createConnection();
			stmt = database.getStatement(connection, "SELECT count(*) FROM `videometadata`");
			rs = stmt.executeQuery();
			if (rs.next()) {
				count = rs.getLong(1);
			}
		} finally {
			database.closeDatabaseResources(connection, stmt, rs);
			stmt = null;
			rs = null;
			connection =null;
		}
		return count;
	}

	/**
	 * Used to close the database after the tests have finished
	 * @throws Exception Thriwn if their is a problem shutting down the DB
	 */
	public void tearDown() throws Exception{
		database.executeSQL("SHUTDOWN");
		database = null;
	}

	/**
	 * Test that the film is cached correctly
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testCacheFilm() throws Exception {
		MythTVStore xmlSource = createStore();
		
		File dir = FileHelper.createTmpDir("film");
		try {
			File filmFile1 = new File(dir,"The Usual Suspects part1.avi");
			File filmFile2 = new File(dir,"The Usual Suspects part2.avi");
			Film film = createTestFilm();
						
			xmlSource.cacheFilm(filmFile1, film);
			xmlSource.cacheFilm(filmFile2, film);
			
			checkFilm(0,filmFile1.getAbsolutePath());
			checkFilm(1,filmFile2.getAbsolutePath());
			
			assertEquals("Check number of films",2,getNumberOfFilms());												
		} finally {
			FileHelper.deleteDir(dir);
		}
	}

	private void checkFilm(int id,String filename) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = null; 
		try {
			connection = database.createConnection();
			stmt = database.getStatement(connection, "SELECT * FROM `videometadata` where `intid` = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			assertTrue(rs.next());
			assertEquals("Check id",id,rs.getInt("intid"));
			assertEquals("Check title","The Usual Suspects",rs.getString("title"));		
			assertEquals("Check rating","R",rs.getString("rating"));
			assertEquals("Check inetref","114814",rs.getString("inetref"));
			assertEquals("Check inetref",8.7F,rs.getFloat("userrating"));
			assertEquals("Check directory","Bryan Singer",rs.getString("director"));			
			assertEquals("Check directory",filename,rs.getString("filename"));
			
			List<String> countries = getCountry(id);
			assertEquals("Check number of countries",1,countries.size());
			assertEquals("Check the country","USA",countries.get(0));
			assertFalse(rs.next());
			
		} finally {
			database.closeDatabaseResources(connection, stmt, rs);
			stmt = null;
			rs = null;
			connection =null;
		}
	}

	private List<Long> getVideoCountryId(long videoId) throws SQLException {
		List<Long> list = new ArrayList<Long>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = null; 
		try {
			connection = database.createConnection();
			stmt = database.getStatement(connection, "SELECT * FROM `videometadatacountry`" +					                                 
                                                     " WHERE `idvideo` = ?" );
                    			
			stmt.setLong(1, videoId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(rs.getLong("idcountry"));
			}
		}
		finally {
			database.closeDatabaseResources(connection, stmt, rs);
			stmt = null;
			rs = null;
			connection =null;
		}
		return list;
	}
	
	private List<String> getCountry(long filmId) throws SQLException {
		List<Long>countryIds = getVideoCountryId(filmId);
		List<String> countries = new ArrayList<String>();
		
		for (Long countryId : countryIds) {
		
			PreparedStatement stmt = null;
			ResultSet rs = null;
			Connection connection = null; 
			try {
				connection = database.createConnection();
				stmt = database.getStatement(connection, "SELECT * FROM `videocountry`"+					                                 
						                                 " WHERE `intid` = ?");
				stmt.setLong(1, countryId);
				rs = stmt.executeQuery();
				while (rs.next()) {
					countries.add(rs.getString("country"));
				}
			}
			finally {
				database.closeDatabaseResources(connection, stmt, rs);
				stmt = null;
				rs = null;
				connection =null;
			}
		}
		return countries;
	}
	
	private MythTVStore createStore() {
		MythTVStore xmlSource = new MythTVStore() {
			@Override
			protected IDatabase connectToDatabase() throws StoreException {				
				return database;
			}
			
		};
		xmlSource.setDatabaseClass(HSQLDatabase.class.getName());
		xmlSource.setDatabaseHost("");
		xmlSource.setDatabaseName(DB_NAME);
		xmlSource.setDatabasePassword("");
		xmlSource.setDatabaseUser(DB_USER);
		return xmlSource;
	}

	private Film createTestFilm() throws MalformedURLException, ParseException {
		Film film = new Film("114814");
		film.setImageURL(new URL("http://test/image.jpg"));
		film.setTitle("The Usual Suspects");
		film.setCountry(new Link("http://www.imdb.com/Sections/Countries/USA/","USA"));
		List<String> genres = new ArrayList<String>();
		genres.add("Crime");
		genres.add("Drama");
		genres.add("Mystery");
		genres.add("Thriller");			
		film.setGenres(genres);
		
		film.setPreferredGenre("Drama");
		List<Certification> certifications= new ArrayList<Certification>();
		certifications.add(new Certification("16","Iceland"));
		certifications.add(new Certification("R-18","Philippines"));
		certifications.add(new Certification("16","Argentina"));
		certifications.add(new Certification("MA","Australia"));
		certifications.add(new Certification("16","Brazil"));
		certifications.add(new Certification("14A","Canada"));
		certifications.add(new Certification("18","Chile"));
		certifications.add(new Certification("16","Denmark"));
		certifications.add(new Certification("K-16","Finland"));
		certifications.add(new Certification("U","France"));
		certifications.add(new Certification("16","Germany"));
		certifications.add(new Certification("IIB","Hong Kong"));
		certifications.add(new Certification("16","Hungary"));
		certifications.add(new Certification("18","Ireland"));
		certifications.add(new Certification("T","Italy"));
		certifications.add(new Certification("PG-12","Japan"));
		certifications.add(new Certification("16","Netherlands"));
		certifications.add(new Certification("R18","New Zealand"));
		certifications.add(new Certification("15","Norway"));
		certifications.add(new Certification("M/16","Portugal"));
		certifications.add(new Certification("M18","Singapore"));
		certifications.add(new Certification("PG (cut)","Singapore"));
		certifications.add(new Certification("18","South Korea"));
		certifications.add(new Certification("18","Spain"));
		certifications.add(new Certification("15","Sweden"));
		certifications.add(new Certification("18","UK"));
		certifications.add(new Certification("R","USA"));
		film.setCertifications(certifications);
		film.setDate(df.parse("1995-08-25"));
		List<Link> directors = new ArrayList<Link>();
		directors.add(new Link("http://www.imdb.com/name/nm0001741/","Bryan Singer"));
		film.setDirectors(directors);
		film.setFilmUrl(new URL("http://www.imdb.com/title/tt0114814/"));			
		List<Link> guestStars = new ArrayList<Link>();
		guestStars.add(new Link("http://www.imdb.com/name/nm0000286/","Stephen Baldwin"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0000321/","Gabriel Byrne"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0001125/","Benicio Del Toro"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0001629/","Kevin Pollak"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0000228/","Kevin Spacey"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0001590/","Chazz Palminteri"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0000592/","Pete Postlethwaite"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0002064/","Giancarlo Esposito"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0000751/","Suzy Amis"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0000445/","Dan Hedaya"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0000860/","Paul Bartel"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0107808/","Carl Bressler"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0800342/","Phillip Simon"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0790436/","Jack Shearer"));
		guestStars.add(new Link("http://www.imdb.com/name/nm0261452/","Christine Estabrook"));
		film.setGuestStars(guestStars);
		film.setRating(8.7F);
		film.setSourceId(IMDBSource.SOURCE_ID);
		film.setSummary("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.");
		film.setDescription("Test description of the film");
		List<Link>writers = new ArrayList<Link>();
		writers.add(new Link("http://www.imdb.com/name/nm0003160/","Christopher McQuarrie"));
		film.setWriters(writers);
		
		film.addChapter(new Chapter("The start",1));
		film.addChapter(new Chapter("The end",3));
		film.addChapter(new Chapter("Second Chapter",2));
		return film;
	}
}

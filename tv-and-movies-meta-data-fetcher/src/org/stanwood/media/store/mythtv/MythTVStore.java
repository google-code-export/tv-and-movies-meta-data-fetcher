package org.stanwood.media.store.mythtv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.database.Field;
import org.stanwood.media.database.IDatabase;
import org.stanwood.media.database.MysqlDatabase;
import org.stanwood.media.database.UnableToConnectToDatabaseException;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

public class MythTVStore implements IStore {

	private final static Log log = LogFactory.getLog(MythTVStore.class);

	private String databaseName;
	private String databaseHost;
	private String databaseUser;
	private String databasePassword;
	private String databaseClass = MysqlDatabase.class.getName();
	private String coversPath;

	@Override
	public void cacheFilm(File filmFile, Film film) throws StoreException {
		validateOptions();
		IDatabase db = connectToDatabase();

		Connection connection = null;

		try {
			connection = db.createConnection();
			Long id = getFilmIdFromDB(filmFile, db, connection);
			if (id != null) {
				deleteFilmMetaData(id, db, connection);
			}
			id = insertFilmMetaData(filmFile, film, db, connection);
		} catch (SQLException e) {
			throw new StoreException("Database error: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				try {
					db.closeConnection(connection);
				} catch (SQLException e) {
					throw new StoreException("Database error: " + e.getMessage(), e);
				}
				connection = null;
			}
		}
	}

	private long insertFilmMetaData(File filmFile, Film film, IDatabase db, Connection connection) throws SQLException {
		File coverImage = getCoverImage(film);
		List<Field> fields = new ArrayList<Field>();
		fields.add(new Field("title", film.getTitle()));
		if (film.getDirectors() != null && film.getDirectors().size() > 0) {
			fields.add(new Field("director", film.getDirectors().get(0).getTitle()));
		} else {
			fields.add(new Field("director", "Unknown"));
		}
		if (film.getSummary() != null) {
			fields.add(new Field("plot", film.getSummary()));
		}
		boolean foundCert = false;
		for (Certification cert : film.getCertifications()) {
			if (cert.getCountry().equals("USA")) {
				fields.add(new Field("rating", cert.getCertification()));
				foundCert = true;
				break;
			}
		}
		if (!foundCert) {
			fields.add(new Field("rating", "NR"));
		}

		fields.add(new Field("inetref", film.getId())); // TODO find correct IMDB reference
		if (film.getDate() != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(film.getDate());
			fields.add(new Field("year", c.get(Calendar.YEAR)));
		} else {
			fields.add(new Field("year", 0));
		}
		if (film.getRating() != null) {
			fields.add(new Field("userrating", film.getRating()));
		} else {
			fields.add(new Field("userrating", 0));
		}
		fields.add(new Field("length", filmFile.length()));
		fields.add(new Field("showlevel", 1)); // TODO Get correct showlevel
		fields.add(new Field("filename", filmFile.getAbsolutePath()));
		if (coverImage == null) {
			fields.add(new Field("coverfile", "No Cover"));
		} else {
			fields.add(new Field("coverfile", coverImage.getAbsolutePath()));
		}

		long id = db.insertIntoTable(connection, "videometadata", fields);

		if (film.getGuestStars() != null) {
			for (Link cast : film.getGuestStars()) {
				Long castId = getCastId(cast.getTitle(), db, connection);
				if (castId == null) {
					castId = insertNewCast(cast.getTitle(), db, connection);
				}
				db.executeUpdate(connection, "insert into videometadatacast values(?,?)", new Object[] { id, castId });
			}
		}

		if (film.getGenres() != null) {
			for (String genre : film.getGenres()) {
				Long genreId = getGenreId(genre, db, connection);
				if (genreId == null) {
					genreId = insertNewGenre(genre, db, connection);
				}
				db
						.executeUpdate(connection, "insert into videometadatagenre values(?,?)", new Object[] { id,
								genreId });
			}
		}

		// TODO insert country metadata
		// TODO insert category metadata

		return id;
	}

	private long insertNewCast(String castName, IDatabase db, Connection connection) throws SQLException {
		return db.executeUpdate(connection, "insert into videocast(cast) values (?)", new Object[] { castName });
	}

	private long insertNewCountry(String countryName, IDatabase db, Connection connection) throws SQLException {
		return db.executeUpdate(connection, "insert into videocountry(country) values (?)",
				new Object[] { countryName });
	}

	private long insertNewGenre(String genreName, IDatabase db, Connection connection) throws SQLException {
		return db.executeUpdate(connection, "insert into videogenre(genre) values (?)", new Object[] { genreName });
	}

	private long insertNewCategory(String categoryName, IDatabase db, Connection connection) throws SQLException {
		return db.executeUpdate(connection, "insert into videocategory(category) values (?)",
				new Object[] { categoryName });
	}

	private File getCoverImage(Film film) {
		File coversImage = null;
		if (film.getImageURL() != null && coversPath != null) {
			String filename = film.getTitle() + ".jpg";
			filename = filename.replaceAll(":", "-");
			File coverFile = new File(coversPath, filename);
			if (coverFile.exists()) {
				log.warn("Cover image file already '" + coverFile.getAbsolutePath() + "', replacing....");
				if (!coverFile.delete()) {
					log.error("Unable to delete old cover image file '" + coverFile.getAbsolutePath() + "'");
					return null;
				}
			}

			// TODO set the correct image extension
			try {
				coversImage = downloadToFile(coverFile, film.getImageURL());
			} catch (IOException e) {
				log.error("Unable to download cover image for film '" + film.getTitle() + "'. " + e.getMessage(), e);
			}

		}
		return coversImage;
	}

	private void deleteFilmMetaData(Long id, IDatabase db, Connection connection) throws SQLException {
		db.executeSQL(connection, "DELETE FROM videometadata WHERE intid = " + id);
		db.executeSQL(connection, "DELETE FROM videometadatacast WHERE idvideo = " + id);
		db.executeSQL(connection, "DELETE FROM videometadatacountry WHERE idvideo = " + id);
		db.executeSQL(connection, "DELETE FROM videometadatagenre WHERE idvideo = " + id);
	}

	private Long getCastId(String castName, IDatabase db, Connection connection) throws SQLException {
		Long id = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = db.getStatement(connection, "SELECT intid FROM videocast WHERE cast = ?", new Object[] { castName });
			rs = stmt.executeQuery();
			if (rs.next()) {
				id = rs.getLong(1);
			}
		} finally {
			db.closeDatabaseResources(null, stmt, rs);
			stmt = null;
			rs = null;
		}
		return id;
	}

	private Long getCategoryId(String categoryName, IDatabase db, Connection connection) throws SQLException {
		Long id = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = db.getStatement(connection, "SELECT intid FROM videocategory WHERE category = ?",
					new Object[] { categoryName });
			rs = stmt.executeQuery();
			if (rs.next()) {
				id = rs.getLong(1);
			}
		} finally {
			db.closeDatabaseResources(null, stmt, rs);
			stmt = null;
			rs = null;
		}
		return id;
	}

	private Long getCountryId(String countryName, IDatabase db, Connection connection) throws SQLException {
		Long id = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = db.getStatement(connection, "SELECT intid FROM videocountry WHERE country = ?",
					new Object[] { countryName });
			rs = stmt.executeQuery();
			if (rs.next()) {
				id = rs.getLong(1);
			}
		} finally {
			db.closeDatabaseResources(null, stmt, rs);
			stmt = null;
			rs = null;
		}
		return id;
	}

	private Long getGenreId(String genreName, IDatabase db, Connection connection) throws SQLException {
		Long id = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = db.getStatement(connection, "SELECT intid FROM videogenre WHERE genre = ?",
					new Object[] { genreName });
			rs = stmt.executeQuery();
			if (rs.next()) {
				id = rs.getLong(1);
			}
		} finally {
			db.closeDatabaseResources(null, stmt, rs);
			stmt = null;
			rs = null;
		}
		return id;
	}

	private Long getFilmIdFromDB(File filmFile, IDatabase db, Connection connection) throws SQLException {
		Long id = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = db.getStatement(connection, "SELECT intid FROM videometadata WHERE filename = ?",
					new Object[] { filmFile.getAbsolutePath() });
			rs = stmt.executeQuery();
			if (rs.next()) {
				id = rs.getLong(1);
			}
		} finally {
			db.closeDatabaseResources(null, stmt, rs);
			stmt = null;
			rs = null;
		}
		return id;
	}

	/**
	 * This store does not support the caching of episodes, so this does nothing
	 * 
	 * @param episode The episode or special too write
	 * @param episodeFile the file witch the episode is stored in
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheEpisode(File episodeFile, Episode episode) throws StoreException {

	}

	/**
	 * This store does not support the caching of seasons, so this does nothing
	 * 
	 * @param season The season too write
	 * @param episodeFile The file the episode is stored in
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheSeason(File episodeFile, Season season) throws StoreException {

	}

	/**
	 * This store does not support the caching of shows, so this does nothing
	 * 
	 * @param show The show too write
	 * @param episodeFile The file the episode is stored in
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheShow(File episodeFile, Show show) throws StoreException {

	}

	/**
	 * This will always return null as this is a write only store
	 * 
	 * @param episodeFile the file which the episode is stored in
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	public Episode getEpisode(File episodeFile, Season season, int episodeNum) throws StoreException {
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * 
	 * @param episodeFile the file which the episode is stored in
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	public Season getSeason(File episodeFile, Show show, int seasonNum) throws StoreException {
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * 
	 * @param episodeFile the file which the episode is stored in
	 * @param showId The show Id of the show too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	public Show getShow(File episodeFile, String showId) throws StoreException {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * 
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 */
	@Override
	public Film getFilm(File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * 
	 * @param episodeFile the file which the special episode is stored in
	 * @param season The season the episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Episode getSpecial(File episodeFile, Season season, int specialNumber) throws MalformedURLException,
			IOException, StoreException {
		return null;
	}

	@Override
	public void renamedFile(File oldFile, File newFile) throws StoreException {

	}

	/**
	 * This will always return null as this store does not support searching
	 * 
	 * @param episodeFile The file the episode is located in
	 * @param mode The mode that the search operation should be performed in
	 * @return Always returns null
	 */
	@Override
	public SearchResult searchForVideoId(Mode mode, File episodeFile) throws StoreException {
		return null;
	}

	private void validateOptions() throws StoreException {
		if (databaseName == null) {
			throw new StoreException("No database name was set for the " + this.getClass().getName() + " store");
		}
		if (databaseHost == null) {
			throw new StoreException("No database hostname was set for the " + this.getClass().getName() + " store");
		}
		if (databaseUser == null) {
			throw new StoreException("No database username was set for the " + this.getClass().getName() + " store");
		}
		if (databasePassword == null) {
			throw new StoreException("No database password was set for the " + this.getClass().getName() + " store");
		}
	}

	private IDatabase connectToDatabase() throws StoreException {
		try {
			Class<? extends IDatabase> c = Class.forName(databaseClass).asSubclass(IDatabase.class);
			Constructor<? extends IDatabase> constructor = c.getConstructor(String.class, String.class, String.class,
					String.class);
			IDatabase database = constructor.newInstance(databaseHost, databaseUser, databasePassword, databaseName);

			database.init();

			return database;
		} catch (ClassNotFoundException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (SecurityException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		} catch (UnableToConnectToDatabaseException e) {
			throw new StoreException("Unable to connect to the database: " + e.getMessage(), e);
		}
	}

	/**
	 * Used to get the name of the database to connect to
	 * 
	 * @return The database name to connect to
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Used to set the name of the database to connect to
	 * 
	 * @param databaseName The name of the database to connect to
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * Used to get the hostname of the database to connect to
	 * 
	 * @return The hostname of the database to connect to
	 */
	public String getDatabaseHost() {
		return databaseHost;
	}

	/**
	 * Used to set the hostname of the database to connect to
	 * 
	 * @param databaseHost The hostname of the database to connect to
	 */
	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}

	/**
	 * Used to get the user name of the user to use when connecting to the database.
	 * 
	 * @return the user name of the user to use when connecting to the database.
	 */
	public String getDatabaseUser() {
		return databaseUser;
	}

	/**
	 * Used to set the user name of the user to use when connecting to the database
	 * 
	 * @param databaseUser the user name of the user to use when connecting to the database
	 */
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	/**
	 * Used to get the users password which is used when connecting to the database
	 * 
	 * @return the users password which is used when connecting to the database
	 */
	public String getDatabasePassword() {
		return databasePassword;
	}

	/**
	 * Used to set the users password which is used when connecting to the database
	 * 
	 * @param databasePassword the users password which is used when connecting to the database
	 */
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 * Used to get the database controller class
	 * 
	 * @return The database controller class
	 */
	public String getDatabaseClass() {
		return databaseClass;
	}

	/**
	 * Used to set the class of the database controller
	 * 
	 * @param databaseClass The class name of the database controller
	 */
	public void setDatabaseClass(String databaseClass) {
		this.databaseClass = databaseClass;
	}

	/**
	 * Used to get the location covers should be stored at
	 * 
	 * @return The location covers should be started at
	 */
	public String getCoversPath() {
		return coversPath;
	}

	/**
	 * Used to set the location covers should be stored at
	 * 
	 * @param coversPath The location covers should be stored at
	 */
	public void setCoversPath(String coversPath) {
		this.coversPath = coversPath;
	}

	private File downloadToFile(File file, URL url) throws IOException {
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			return file;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

}

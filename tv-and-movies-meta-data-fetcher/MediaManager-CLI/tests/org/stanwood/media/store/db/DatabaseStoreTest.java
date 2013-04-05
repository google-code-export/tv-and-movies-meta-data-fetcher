package org.stanwood.media.store.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.setup.SchemaCheck;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.testdata.EpisodeData;
import org.stanwood.media.util.FileHelper;

/**
 * This is used to test the class {@DatabaseStore}
 */
@SuppressWarnings("nls")
public class DatabaseStoreTest {

//	private final static String URL = "jdbc:mysql://localhost/mediamanager";
//	private final static String USERNAME = "mm";
//	private final static String PASSWORD = "mm-test";
//	private final static String DIALECT = "org.hibernate.dialect.MySQLDialect";
	private final static String URL = "jdbc:hsqldb:mem:db";
	private final static String USERNAME = "sa";
	private final static String PASSWORD = "";
	private final static String DIALECT = "org.hibernate.dialect.HSQLDialect";

	private DatabaseStore createStore() throws StoreException {
		DatabaseStore store = new DatabaseStore(null);
		DBResource resource = new DBResource();
		resource.setDialect(DIALECT);
		resource.setUrl(URL);
		resource.setResourceId("test");
		resource.setUsername(USERNAME);
		resource.setPassword(PASSWORD);
		resource.setSchemaCheck(SchemaCheck.UPDATE);
		store.init(resource);

		return store;
	}

	/**
	 * Used to setup the database if it's needed
	 * @throws Exception Thrown if their is a problem
	 */
	@Before
	public void createDatabase() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
//		Configuration configuration = DatabaseStore.getConfiguration(URL, USERNAME, PASSWORD, DIALECT);
//		new SchemaExport(configuration).drop(false, true);
//		new SchemaExport(configuration).create(false, false);
	}

	/**
	 * Used to test that TV shows/seasons/episodes can be stored and retrieved in the DatabaseStore
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testCacheShow() throws Exception {
		DatabaseStore store = createStore();
		File rawMediaDir = FileHelper.createTmpDir("media");
		try {
			final MediaDirConfig config = new MediaDirConfig();
			config.setMediaDir(rawMediaDir);
			config.setMode(Mode.TV_SHOW);

			File eurekaDir = new File(rawMediaDir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File heroesDir = new File(rawMediaDir, "Heroes");
			if (!heroesDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File orgFile = new File(rawMediaDir,"Eureka"+File.separator+"1x01 - blah");
			List<Integer>episodeNums = new ArrayList<Integer>();
			episodeNums.add(1);
			Assert.assertNull("Test show is not found before caching",store.getEpisode(rawMediaDir, orgFile, null, episodeNums) );

			List<EpisodeData> epsiodes = Data.createEurekaShow(eurekaDir);
			epsiodes.addAll(Data.createHeroesShow(heroesDir));

			cacheEpisodes(store, rawMediaDir, epsiodes);

			SearchResult result = store.searchMedia("Blah", Mode.TV_SHOW, null, config, orgFile);
			Assert.assertNotNull(result);
			Assert.assertNull(result.getPart());
			Assert.assertEquals("http://www.tv.com/show/58448/summary.html",result.getUrl());
			Assert.assertEquals("58448",result.getId());
			Assert.assertEquals(Mode.TV_SHOW,result.getMode());
			Assert.assertEquals(1,result.getEpisodes().get(0).intValue());
			Assert.assertEquals(1,result.getSeason().intValue());

			Collection<IEpisode> episodes = store.listEpisodes(config, new NullProgressMonitor());
			Assert.assertEquals(5,episodes.size());
			IEpisode episode = episodes.iterator().next();
			Assert.assertEquals("Pilot",episode.getTitle());
			VideoFile vf = episode.getFiles().get(0);

			Assert.assertEquals(orgFile,vf.getLocation());
			Assert.assertEquals(orgFile,vf.getOrginalLocation());
			MediaDirectory mediaDir = getMediaDir(config,rawMediaDir);
			Assert.assertNotNull(store.getEpisode(mediaDir, orgFile));
			store.renamedFile(rawMediaDir, orgFile, new File(rawMediaDir,"Eureka"+File.separator+"1x01 - blah.m4v"));
			Assert.assertNull(store.getEpisode(mediaDir, orgFile));
			episode = store.getEpisode(mediaDir, new File(rawMediaDir,"Eureka"+File.separator+"1x01 - blah.m4v"));
			Assert.assertNotNull(episode);
			Assert.assertEquals(new File(rawMediaDir,"Eureka"+File.separator+"1x01 - blah.m4v"),vf.getLocation());
			Assert.assertEquals(orgFile,vf.getOrginalLocation());

			store.fileDeleted(mediaDir, new File(rawMediaDir,"Eureka"+File.separator+"1x01 - blah.m4v"));
			Assert.assertNull(store.getEpisode(mediaDir, new File(rawMediaDir,"Eureka"+File.separator+"1x01 - blah.m4v")));

			store.fileUpdated(mediaDir, orgFile);
			episodeNums = new ArrayList<Integer>();
			episodeNums.add(0);

			Assert.assertNull(store.getEpisode(rawMediaDir,new File(eurekaDir,"000 - blah"),null,episodeNums));
			IEpisode special = store.getSpecial(rawMediaDir,new File(eurekaDir,"000 - blah"),null,episodeNums);
			Assert.assertNotNull(special);

			store.performedActions(mediaDir);
		}
		finally {
			FileHelper.delete(rawMediaDir);
		}
	}

	public static void cacheEpisodes(DatabaseStore store, File rawMediaDir,
			List<EpisodeData> epsiodes) throws Exception {
		for (EpisodeData ed : epsiodes) {
			FileHelper.copy(Data.class.getResourceAsStream("a_video.mp4"),ed.getFile());
			File episodeFile = ed.getFile();
			IEpisode episode = ed.getEpisode();
			ISeason season = episode.getSeason();
			IShow show = season.getShow();
			Assert.assertNull("Check not found before episode is cached",store.getShow(rawMediaDir, episodeFile, show.getShowId()));
			Assert.assertNull("Check not found before episode is cached",store.getSeason(rawMediaDir, episodeFile, show,1));
			store.cacheShow(rawMediaDir, episodeFile, show);
			store.cacheSeason(rawMediaDir, episodeFile, season);
			store.cacheEpisode(rawMediaDir, episodeFile, episode);


			IShow foundShow = store.getShow(rawMediaDir, episodeFile, show.getShowId());
			Assert.assertNotNull(foundShow);
			ISeason foundSeason = store.getSeason(rawMediaDir, episodeFile, foundShow, 1);
			Assert.assertNotNull(foundSeason);
			if (episode.isSpecial()) {
				IEpisode foundEpisode = store.getSpecial(rawMediaDir, episodeFile, foundSeason, null) ;
				Assert.assertNotNull(foundEpisode);
				Assert.assertNull(store.getEpisode(rawMediaDir, episodeFile, foundSeason, null));
			}
			else {
				IEpisode foundEpisode = store.getEpisode(rawMediaDir, episodeFile, foundSeason, null) ;
				Assert.assertNotNull(foundEpisode);
				Assert.assertNull(store.getSpecial(rawMediaDir, episodeFile, foundSeason, null));
			}
		}
	}


	private MediaDirectory getMediaDir(final MediaDirConfig mediaDirConfig,File rawMediaDir) throws ConfigException {
		return new MediaDirectory(null, new ConfigReader(null) {
			@Override
			public MediaDirConfig getMediaDirectory(File directory)
					throws ConfigException {
				return mediaDirConfig;
			}

		}, rawMediaDir);
	}

	/**
	 * Used to test that films can be stored and retrieved in the DatabaseStore
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testCacheFilm() throws Exception {

		DatabaseStore store = createStore();
		File dir = FileHelper.createTmpDir("films");
		try {
			MediaDirConfig config = new MediaDirConfig();
			config.setMediaDir(dir);
			File filmFile = new File(dir,"The Usual Suspects.avi");
			IFilm film = Data.createFilm();
			film.getFiles().add(new VideoFile(filmFile.getAbsoluteFile(), filmFile.getAbsoluteFile(), 1, dir));
			store.cacheFilm(dir,filmFile, film,1);
//			store.performedActions(null);

			IFilm foundFilm = store.getFilm(dir, filmFile, film.getId());
			Assert.assertNotNull(foundFilm);
			Assert.assertEquals("The Usual Suspects",foundFilm.getTitle());

			Collection<IFilm> list = store.listFilms(config, null);
			Assert.assertNotNull(list);
			Assert.assertEquals(1,list.size());
			foundFilm = list.iterator().next();
			Assert.assertEquals("The Usual Suspects",film.getTitle());
			Assert.assertEquals(1,foundFilm.getFiles().size());
			File file = foundFilm.getFiles().get(0).getLocation();


			SearchResult result = store.searchMedia("Blah", Mode.FILM, null, config, file);
			Assert.assertNotNull(result);
			Assert.assertNull(result.getPart());
			Assert.assertEquals("http://www.imdb.com/title/tt0114814/",result.getUrl());
			Assert.assertEquals("114814",result.getId());
			Assert.assertEquals(Mode.FILM,result.getMode());
			Assert.assertNull(result.getEpisodes());
			Assert.assertNull(result.getSeason());


			MediaDirectory mediaDir = getMediaDir(config,dir);
			foundFilm = store.getFilm(mediaDir,file);
			Assert.assertNotNull(foundFilm);

			store.renamedFile(dir, file, new File(dir,"blah blah blah.m4v"));
			foundFilm = store.getFilm(mediaDir,file);
			Assert.assertNull(foundFilm);
			file = new File(dir,"blah blah blah.m4v");
			foundFilm = store.getFilm(mediaDir,file);
			Assert.assertNotNull(foundFilm);

			store.fileDeleted(mediaDir,file);
			foundFilm = store.getFilm(mediaDir,file);
			Assert.assertNull(foundFilm);

		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Tests that the upgrade does not have any errors.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUpgrade() throws Exception {
		DatabaseStore store = createStore();
		store.upgrade(null);
	}
}

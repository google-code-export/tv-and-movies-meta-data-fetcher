package org.stanwood.media.actions.podcast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.ActionPerformer;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.cli.manager.TestCLIMediaManager;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * This is a test of the action {@link PodCastAction}
 */
@SuppressWarnings("nls")
public class PodCastActionTest {

	private final static String EXTS[] = new String[] {"avi","mkv","mov","mpg","mp4","m4a","m4v","srt","sub","divx"};
	private File mediaDirLocation;
	private MediaDirectory mediaDir;
	private Film dummyFilm;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");

	/**
	 * Used to setup the test
	 * @throws Exception Thrown if their are any problems
	 */
	@Before
	public void setupTest() throws Exception {
		mediaDirLocation = FileHelper.createTmpDir("Films");
		mediaDir = getMediaDir(mediaDirLocation,"%t{ (%y)}{ Part %p}.%x",Mode.FILM);
		dummyFilm = Data.createFilm();
	}

	/**
	 * Used to tidy up after the test
	 * @throws Exception Thrown if their are any problems
	 */
	@After
	public void tearDownTest() throws Exception {
		if (mediaDirLocation!=null) {
			FileHelper.delete(mediaDirLocation);
		}
	}

	/**
	 * Test that the podcast can be restricted by the number of entries
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testPodcastCreationWithLimitedEntries() throws Exception {
		createFile(dummyFilm.getTitle()+" (2009) Part 1.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009) Part 2.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).avi","2011-05-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).m4v","2011-02-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).srt","2011-03-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).mp4","2011-04-18:10-14-10");

		Map<String, String> params = new HashMap<String,String>();
		params.put("mediaDirURL","http://blah.com/Films");
		params.put("fileLocation","rss.xml");
		params.put("extensions","avi,mkv,mp4,m4v");
		params.put("numberEntries","3");

		runAction(params);

		List<String>files = FileHelper.listFilesAsStrings(mediaDirLocation);
		Assert.assertEquals(7,files.size());
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009) Part 1.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009) Part 2.avi").getAbsolutePath(),files.get(1));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).avi").getAbsolutePath(),files.get(2));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).m4v").getAbsolutePath(),files.get(3));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).mp4").getAbsolutePath(),files.get(4));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).srt").getAbsolutePath(),files.get(5));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+"rss.xml").getAbsolutePath(),files.get(6));

		String actual = FileHelper.readFileContents(new File(mediaDirLocation,File.separator+"rss.xml"));
		String expected = FileHelper.readFileContents(PodCastActionTest.class.getResourceAsStream("expected-rss-2.xml"));
		Assert.assertEquals(expected.trim(), actual.trim());
	}

	/**
	 * Used to test that the correct rss feed is generated when one does not already exist.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testPodcastCreation() throws Exception {
		createFile(dummyFilm.getTitle()+" (2009) Part 1.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009) Part 2.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).avi","2011-05-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).m4v","2011-02-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).srt","2011-03-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).mp4","2011-04-18:10-14-10");

		Map<String, String> params = new HashMap<String,String>();
		params.put("mediaDirURL","http://blah.com/Films");
		params.put("fileLocation","rss.xml");
		params.put("extensions","avi,mkv,mp4,m4v");

		runAction(params);

		List<String>files = FileHelper.listFilesAsStrings(mediaDirLocation);
		Assert.assertEquals(7,files.size());
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009) Part 1.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009) Part 2.avi").getAbsolutePath(),files.get(1));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).avi").getAbsolutePath(),files.get(2));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).m4v").getAbsolutePath(),files.get(3));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).mp4").getAbsolutePath(),files.get(4));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).srt").getAbsolutePath(),files.get(5));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+"rss.xml").getAbsolutePath(),files.get(6));

		String actual = FileHelper.readFileContents(new File(mediaDirLocation,File.separator+"rss.xml"));
		String expected = FileHelper.readFileContents(PodCastActionTest.class.getResourceAsStream("expected-rss-1.xml"));
		Assert.assertEquals(expected.trim(), actual.trim());
	}

	/**
	 * Used to test that the correct RSS feed is generated when one does not already exist.
	 * Also test that it has a title and description.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testPodcastCreationWithTitleAndDesc() throws Exception {
		createFile(dummyFilm.getTitle()+" (2009) Part 1.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009) Part 2.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).avi","2011-05-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).m4v","2011-02-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).srt","2011-03-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).mp4","2011-04-18:10-14-10");

		Map<String, String> params = new HashMap<String,String>();
		params.put("mediaDirURL","http://blah.com/Films");
		params.put("fileLocation","rss.xml");
		params.put("extensions","avi,mkv,mp4,m4v");
		params.put("feedTitle","Films");
		params.put("feedDescription","All of my films");

		runAction(params);

		List<String>files = FileHelper.listFilesAsStrings(mediaDirLocation);
		Assert.assertEquals(7,files.size());
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009) Part 1.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009) Part 2.avi").getAbsolutePath(),files.get(1));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).avi").getAbsolutePath(),files.get(2));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).m4v").getAbsolutePath(),files.get(3));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).mp4").getAbsolutePath(),files.get(4));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+dummyFilm.getTitle()+" (2009).srt").getAbsolutePath(),files.get(5));
		Assert.assertEquals(new File(mediaDirLocation,File.separator+"rss.xml").getAbsolutePath(),files.get(6));

		String actual = FileHelper.readFileContents(new File(mediaDirLocation,File.separator+"rss.xml"));
		String expected = FileHelper.readFileContents(PodCastActionTest.class.getResourceAsStream("expected-rss-5.xml"));
		Assert.assertEquals(expected.trim(), actual.trim());
	}


	/**
	 * Test that after creating a pod cast a new file can be inserted into it.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUpdateOfPodCast() throws Exception {
		createFile(dummyFilm.getTitle()+" (2009) Part 1.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009) Part 2.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).avi","2011-05-18:10-10-10");

		Map<String, String> params = new HashMap<String,String>();
		params.put("mediaDirURL","http://blah.com/Films");
		params.put("fileLocation","rss.xml");
		params.put("extensions","avi,mkv,mp4,m4v");

		runAction(params);

		List<String>files = FileHelper.listFilesAsStrings(mediaDirLocation);
		Assert.assertEquals(4,files.size());

		String actual = FileHelper.readFileContents(new File(mediaDirLocation,File.separator+"rss.xml"));
		String expected = FileHelper.readFileContents(PodCastActionTest.class.getResourceAsStream("expected-rss-3.xml"));
		Assert.assertEquals(expected.trim(), actual.trim());

		File mediaFile = createFile(dummyFilm.getTitle()+" (2009).m4v","2011-02-18:10-10-10");

		PodCastAction action = createAction(params);
		action.init(mediaDir);
		action.perform(mediaDir, dummyFilm, mediaFile, null, new IActionEventHandler() {
			@Override
			public void sendEventRenamedFile(File oldName, File newName)
					throws ActionException {

			}

			@Override
			public void sendEventNewFile(File file) throws ActionException {

			}

			@Override
			public void sendEventDeletedFile(File file) throws ActionException {

			}
		});
		action.finished(mediaDir);

		files = FileHelper.listFilesAsStrings(mediaDirLocation);
		Assert.assertEquals(5,files.size());

		actual = FileHelper.readFileContents(new File(mediaDirLocation,File.separator+"rss.xml"));
		expected = FileHelper.readFileContents(PodCastActionTest.class.getResourceAsStream("expected-rss-4.xml"));
		Assert.assertEquals(expected.trim(), actual.trim());
	}

	/**
	 * This will test that the action detects unsupported media file types
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUnsupportedFileTypes() throws Exception {
		createFile(dummyFilm.getTitle()+" (2009) Part 1.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009) Part 2.avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).avi","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).m4v","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).srt","2011-04-18:10-10-10");
		createFile(dummyFilm.getTitle()+" (2009).mp4","2011-04-18:10-10-10");

		Map<String, String> params = new HashMap<String,String>();
		params.put("mediaDirURL","http://blah.com/Films");
		params.put("fileLocation","rss.xml");

		try {
			runAction(params);
			Assert.fail("Did not detect the exception");
		}
		catch (ActionException e) {
			Assert.assertEquals("Unsupport file format 'srt' of file '"+mediaDirLocation+File.separator+"The Usual Suspects (2009).srt'",e.getMessage());
		}
	}

	/**
	 * This will test that missing required parameters are detected
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testFindsMissingParams() throws Exception {
		try {
			runAction(new HashMap<String,String>());
			Assert.fail("Did not detect missing parameters");
		}
		catch (ActionException e) {
			Assert.assertEquals("Missing required parameters for PodCastAction: 'fileLocation', 'mediaDirURL'",e.getMessage());
		}

		try {
			Map<String, String> params = new HashMap<String,String>();
			params.put("mediaDirURL","http://blah.com/Films");
			runAction(params);
			Assert.fail("Did not detect missing parameters");
		}
		catch (ActionException e) {
			Assert.assertEquals("Missing required parameters for PodCastAction: 'fileLocation'",e.getMessage());
		}
	}

	private void runAction(Map<String,String>params) throws ActionException {
		List<IAction>actions = new ArrayList<IAction>();
		PodCastAction action = createAction(params);

		actions.add(action);

		List<String>exts = new ArrayList<String>();
		for (String ext : EXTS) {
			exts.add(ext);
		}

		ActionPerformer ap = new ActionPerformer(null,null,actions,mediaDir,exts,false);
		ap.performActions();
	}

	protected PodCastAction createAction(Map<String, String> params)
			throws ActionException {
		PodCastAction action = new PodCastAction();
		for (Entry<String,String> e : params.entrySet()) {
			action.setParameter(e.getKey(), e.getValue());
		}
		return action;
	}

	private File createFile(String filename,String time) throws IOException, ParseException {
		File f = new File(mediaDirLocation,filename);
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		if (!f.setLastModified(df.parse(time).getTime())) {
			throw new IOException("Unable to set modified time of file: " + f.getAbsolutePath());
		}
		return f;
	}

	private static MediaDirectory getMediaDir(File mediaDir,String pattern,Mode mode) throws Exception {
		ConfigReader config = TestCLIMediaManager.setupTestController(mediaDir, pattern, mode, DummySource.class,null,null,null);
		Controller controller = new Controller(config);
		return new MediaDirectory(controller, config, mediaDir);
	}

	/**
	 * A dummy store for test purposes
	 */
	public static class DummySource implements ISource {

		/** {@inheritDoc} */
		@Override
		public Episode getEpisode(Season season, int episodeNum, File file)
				throws SourceException, MalformedURLException, IOException {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public Season getSeason(Show show, int seasonNum)
				throws SourceException, IOException {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public Show getShow(String showId, URL url, File file)
				throws SourceException, MalformedURLException, IOException {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public Film getFilm(String filmId, URL url, File filmFile)
				throws SourceException, MalformedURLException, IOException {
			try {
				return  Data.createFilm();
			} catch (Exception e) {
				throw new SourceException("Unable to get film",e);
			}
		}


		/** {@inheritDoc} */
		@Override
		public Episode getSpecial(Season season, int specialNumber,
				File file) throws SourceException, MalformedURLException,
				IOException {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public String getSourceId() {
			return "DummySource";
		}

		/** {@inheritDoc} */
		@Override
		public void setParameter(String key, String value)
				throws SourceException {
		}

		/** {@inheritDoc} */
		@Override
		public String getParameter(String key) throws SourceException {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public void setMediaDirConfig(MediaDirectory dir)
				throws SourceException {
		}

		/** {@inheritDoc} */
		@Override
		public SearchResult searchMedia(String name,String year, Mode mode, Integer part)
				throws SourceException {
			return new SearchResult("The Usual Suspects","DummySource","http://blah/1234",part);
		}
	};
}

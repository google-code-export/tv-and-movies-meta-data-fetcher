package org.stanwood.media.store.mp4.atomicparsley;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.MP4ITunesStore;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link MP4v2CLIManager} class.
 */
@SuppressWarnings("nls")
public class TestMP4AtomicParsleyManager {

	private SimpleDateFormat DF = new SimpleDateFormat("dd-MM-yyyy");

	private File nativeDir = null;

	public TestMP4AtomicParsleyManager() {
		if (System.getProperty("NATIVE_DIR")!=null) {
			nativeDir = new File(System.getProperty("NATIVE_DIR"));
		}
	}

	/**
	 * Used to test reading atoms when the MP4 file has no atoms
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testNoAtomsFound() throws Exception {
		URL url = Data.class.getResource("a_video.mp4");
		File mp4File = new File(url.toURI());
		Assert.assertTrue(mp4File.exists());

		IMP4Manager ap = createMP4Manager();
		List<IAtom> atoms = ap.listAtoms(mp4File);

		Assert.assertEquals(0,atoms.size());
	}

	/**
	 * Used to test reading atoms when the MP4 has meta data
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testAtomsFound() throws Exception {
		URL url = Data.class.getResource("videoWithMetaData.mp4");
		File mp4File = new File(url.toURI());
		Assert.assertTrue(mp4File.exists());

		IMP4Manager ap = createMP4Manager();
		List<IAtom> atoms = ap.listAtoms(mp4File);
		Collections.sort(atoms, new Comparator<IAtom>() {
			@Override
			public int compare(IAtom o1, IAtom o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		Assert.assertEquals(10,atoms.size());
		Assert.assertEquals("Category: [catg=SciFi]",atoms.get(0).toString());
		Assert.assertEquals("Description: [desc=This is a test show summary]",atoms.get(1).toString());
		Assert.assertEquals("Media Type: [stik=10]",atoms.get(2).toString());
		Assert.assertEquals("TV Episode ID: [tven=103]",atoms.get(3).toString());
		Assert.assertEquals("TV Episode Number: [tves=3]",atoms.get(4).toString());
		Assert.assertEquals("TV Show Name: [tvsh=Test Show Name]",atoms.get(5).toString());
		Assert.assertEquals("TV Season Number: [tvsn=1]",atoms.get(6).toString());
		Assert.assertEquals("Release Date: [©day=Thu Nov 10 00:00:00 GMT 2005]",atoms.get(7).toString());
		Assert.assertEquals("Genre, User defined: [©gen=SciFi]",atoms.get(8).toString());
		Assert.assertEquals("Name: [©nam=Test Episode]",atoms.get(9).toString());
		try {
			atoms.get(10);
			Assert.fail("Did not detect exception");
		}
		catch (IndexOutOfBoundsException e) {
			// Ignore
		}
	}

	/**
	 * This is used to test what happens when the file does not contain apple metadata box
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testWriteNoAppleContainer() throws Exception {
		URL url = Data.class.getResource("a_video.mp4");
		File srcFile = new File(url.toURI());
		Assert.assertTrue(srcFile.exists());

		File mp4File = FileHelper.createTempFile("test", ".mp4");
		if (!mp4File.delete()) {
			throw new IOException("Unable to delete file");
		}
		FileHelper.copy(srcFile, mp4File);
		Episode episode = createTestEpisode();
		IMP4Manager ap = createMP4Manager();
		MP4ITunesStore.updateEpsiode(ap,mp4File, episode);
	}

	/**
	 * Used to test that the episode details can be written to the MP4 file.
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testWriteEpsiode() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		URL url = Data.class.getResource("a_video2.mp4");
		File srcFile = new File(url.toURI());
		Assert.assertTrue(srcFile.exists());

		File mp4File = FileHelper.createTempFile("test", ".mp4");
		if (!mp4File.delete()) {
			throw new IOException("Unable to delete file");
		}
		FileHelper.copy(srcFile, mp4File);
		Episode episode = createTestEpisode();
		IMP4Manager ap = createMP4Manager();
		MP4ITunesStore.updateEpsiode(ap,mp4File, episode);

		List<IAtom> atoms = ap.listAtoms(mp4File);
		for (IAtom a : atoms) {
			System.out.println(a.toString());
		}
		Assert.assertEquals(20,atoms.size());
		int index = 0;
		Assert.assertEquals("Encoding Tool: [©too=HandBrake svn3878 2011041801]",atoms.get(index++).toString());
		Assert.assertEquals("Media Type: [stik=10]",atoms.get(index++).toString());
		Assert.assertEquals("TV Episode ID: [tven=34567]",atoms.get(index++).toString());
		Assert.assertEquals("TV Show Name: [tvsh=Test Show Name]",atoms.get(index++).toString());
		Assert.assertEquals("TV Season Number: [tvsn=1]",atoms.get(index++).toString());
		Assert.assertEquals("TV Episode Number: [tves=3]",atoms.get(index++).toString());
		Assert.assertEquals("Artist: [©ART=Test Show Name]",atoms.get(index++).toString());
		Assert.assertEquals("Album: [©alb=Test Show Name, Series 1]",atoms.get(index++).toString());
		Assert.assertEquals("Sort Album: [soal=Test Show Name, Series 1]",atoms.get(index++).toString());
		Assert.assertEquals("Sort Artist: [soar=Test Show Name]",atoms.get(index++).toString());
		Assert.assertEquals("Album Artist: [aART=Test Show Name]",atoms.get(index++).toString());
		Assert.assertEquals("Sort Album Artist: [soaa=Test Show Name]",atoms.get(index++).toString());
		Assert.assertEquals("Track Number: [trkn=3 of 0]",atoms.get(index++).toString());
		Assert.assertEquals("Release Date: [©day=2005]",atoms.get(index++).toString());
		Assert.assertEquals("Name: [©nam=Test Episode]",atoms.get(index++).toString());
		Assert.assertEquals("Sort Name: [sonm=Test Episode]",atoms.get(index++).toString());
		Assert.assertEquals("Description: [desc=This is a test show summary]",atoms.get(index++).toString());
		Assert.assertEquals("Certification: [----;com.apple.iTunes;iTunEXTC=us-tv|TV-PG|400|]",atoms.get(index++).toString());
		Assert.assertEquals("Genre, User defined: [©gen=SciFi]",atoms.get(index++).toString());
		Assert.assertEquals("Category: [catg=SciFi]",atoms.get(index++).toString());
		try {
			atoms.get(index++);
			Assert.fail("Did not detect exception");
		}
		catch (IndexOutOfBoundsException e) {
			// Ignore
		}
	}

	protected IMP4Manager createMP4Manager() throws MP4Exception {
		IMP4Manager manager = new MP4AtomicParsleyManager();
		manager.init(nativeDir);
		return manager;
	}

	/**
	 * Used to test that a film meta data can be written to a .mp4 file
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testWriteFilm() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		URL url = Data.class.getResource("a_video2.mp4");
		File srcFile = new File(url.toURI());
		Assert.assertTrue(srcFile.exists());

		File mp4File = FileHelper.createTempFile("test", ".mp4");
		if (!mp4File.delete() && mp4File.exists()) {
			throw new IOException("Unable to delete file");
		}
		FileHelper.copy(srcFile, mp4File);
		Film film = createTestFilm();

		IMP4Manager ap = createMP4Manager();
		MP4ITunesStore.updateFilm(ap,mp4File, film,1);

		List<IAtom> atoms = ap.listAtoms(mp4File);
		Collections.sort(atoms, new Comparator<IAtom>() {
			@Override
			public int compare(IAtom o1, IAtom o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (IAtom atom : atoms) {
			System.out.println(atom);
		}

		Assert.assertEquals(14,atoms.size());
		int index=0;
		Assert.assertEquals("Certification: [----;com.apple.iTunes;iTunEXTC=mpaa|R|400|]",atoms.get(index++).toString());
		Assert.assertEquals("Category: [catg=SciFi]",atoms.get(index++).toString());
		Assert.assertEquals("Cover Artwork: [covr=1 piece of artwork]",atoms.get(index++).toString());
		Assert.assertEquals("Description: [desc=A test summary]",atoms.get(index++).toString());
		Assert.assertEquals("Disc Number: [disk=1 of 1]",atoms.get(index++).toString());
		Assert.assertEquals("Long description: [ldes=A test description]",atoms.get(index++).toString());
		Assert.assertEquals("Sort Artist: [soar=Bryan Singer]",atoms.get(index++).toString());
		Assert.assertEquals("Sort Name: [sonm=Test film name]",atoms.get(index++).toString());
		Assert.assertEquals("Media Type: [stik=6]",atoms.get(index++).toString());
		Assert.assertEquals("Artist: [©ART=Bryan Singer]",atoms.get(index++).toString());
		Assert.assertEquals("Release Date: [©day=2005]",atoms.get(index++).toString());
		Assert.assertEquals("Genre, User defined: [©gen=SciFi]",atoms.get(index++).toString());
		Assert.assertEquals("Name: [©nam=Test film name]",atoms.get(index++).toString());
		Assert.assertEquals("Encoding Tool: [©too=HandBrake svn3878 2011041801]",atoms.get(index++).toString());
		try {
			atoms.get(index++);
			Assert.fail("Did not detect exception");
		}
		catch (IndexOutOfBoundsException e) {
			// Ignore
		}
	}

	private Film createTestFilm() throws Exception {
		Film film = new Film("123");
		List<Certification> certifications= new ArrayList<Certification>();
		certifications.add(new Certification("R","mpaa"));
		film.setCertifications(certifications);
		film.setDate(DF.parse("10-11-2005"));
		List<String> directors = new ArrayList<String>();
		directors.add("Bryan Singer");
		film.setDirectors(directors);
		film.setFilmUrl(new URL("http://www.imdb.com/title/tt0114814/"));
		List<String>genres = new ArrayList<String>();
		genres.add("SciFi");
		genres.add("Drama");
		film.setGenres(genres);
		List<Actor> guestStars = new ArrayList<Actor>();
		guestStars.add(new Actor("Stephen Baldwin",""));
		guestStars.add(new Actor("Gabriel Byrne",""));
		guestStars.add(new Actor("Benicio Del Toro",""));
		guestStars.add(new Actor("Kevin Pollak",""));
		film.setActors(guestStars);
		film.setRating(new Rating(8.4F,345));
		film.setSourceId("imdb");
		film.setSummary("A test summary");
		film.setDescription("A test description");
		film.setTitle("Test film name");
		List<String>writers = new ArrayList<String>();
		writers.add("Christopher McQuarrie");
		film.setWriters(writers);
		film.setImageURL(Data.class.getResource("test_image.jpeg"));
		return film;
	}

	private Episode createTestEpisode() throws Exception {
		Show show = new Show("123");
		show.setStudio("Blah");
		List<Certification> certifications= new ArrayList<Certification>();
		certifications.add(new Certification("TV-PG","mpaa"));
		show.setCertifications(certifications);
		show.setName("Test Show Name");
		List<String>genres = new ArrayList<String>();
		genres.add("SciFi");
		genres.add("Drama");
		show.setGenres(genres);
		Season season = new Season(show,1);
		Episode episode = new Episode(3,season,false);
		episode.setDate(DF.parse("10-11-2005"));
		episode.setEpisodeId("34567");
		episode.setRating(new Rating(5.4F,345));
		episode.setSummary("This is a test show summary");
		episode.setTitle("Test Episode");

		return episode;
	}
}

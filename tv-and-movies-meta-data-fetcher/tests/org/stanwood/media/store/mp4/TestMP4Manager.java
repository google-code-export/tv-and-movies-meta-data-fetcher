package org.stanwood.media.store.mp4;

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
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link MP4Manager} class.
 */
public class TestMP4Manager {

	private SimpleDateFormat DF = new SimpleDateFormat("dd-MM-yyyy");

	/**
	 * Used to test reading atoms when the MP4 file has no atoms
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testNoAtomsFound() throws Exception {
		URL url = Data.class.getResource("a_video.mp4");
		File mp4File = new File(url.toURI());
		Assert.assertTrue(mp4File.exists());

		IMP4Manager ap = new MP4Manager();
		List<Atom> atoms = ap.listAtoms(mp4File);

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

		IMP4Manager ap = new MP4Manager();
		List<Atom> atoms = ap.listAtoms(mp4File);
		Collections.sort(atoms, new Comparator<Atom>() {
			@Override
			public int compare(Atom o1, Atom o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		Assert.assertEquals(10,atoms.size());
		Assert.assertEquals("catg",atoms.get(0).getName());
		Assert.assertEquals("SciFi",atoms.get(0).getValue());
		Assert.assertEquals("desc",atoms.get(1).getName());
		Assert.assertEquals("This is a test show summary",atoms.get(1).getValue());
		Assert.assertEquals("stik",atoms.get(2).getName());
		Assert.assertEquals("10",atoms.get(2).getValue());
		Assert.assertEquals("tven",atoms.get(3).getName());
		Assert.assertEquals("103",atoms.get(3).getValue());
		Assert.assertEquals("tves",atoms.get(4).getName());
		Assert.assertEquals("3",atoms.get(4).getValue());
		Assert.assertEquals("tvsh",atoms.get(5).getName());
		Assert.assertEquals("Test Show Name",atoms.get(5).getValue());
		Assert.assertEquals("tvsn",atoms.get(6).getName());
		Assert.assertEquals("1",atoms.get(6).getValue());
		Assert.assertEquals("©day",atoms.get(7).getName());
		Assert.assertEquals("Thu Nov 10 00:00:00 2005",atoms.get(7).getValue().replaceAll("0 ... ", "0 "));
		Assert.assertEquals("©gen",atoms.get(8).getName());
		Assert.assertEquals("SciFi",atoms.get(8).getValue());
		Assert.assertEquals("©nam",atoms.get(9).getName());
		Assert.assertEquals("Test Episode",atoms.get(9).getValue());
	}

	/**
	 * Used to test that the episode details can be written to the MP4 file.
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testWriteEpsiode() throws Exception {
		URL url = Data.class.getResource("a_video.mp4");
		File srcFile = new File(url.toURI());
		Assert.assertTrue(srcFile.exists());

		File mp4File = FileHelper.createTempFile("test", ".mp4");
		if (!mp4File.delete()) {
			throw new IOException("Unable to delete file");
		}
		FileHelper.copy(srcFile, mp4File);
		Episode episode = createTestEpisode();
		IMP4Manager ap = new MP4Manager();
		ap.updateEpsiode(mp4File, episode);

		List<Atom> atoms = ap.listAtoms(mp4File);
		Assert.assertEquals(10,atoms.size());
		Assert.assertEquals("TV Show",((AtomStik)atoms.get(0)).getTypedValue().getDescription());
		Assert.assertEquals("10",((AtomStik)atoms.get(0)).getTypedValue().getId());
		Assert.assertEquals("stik",atoms.get(0).getName());
		Assert.assertEquals("34567",atoms.get(1).getValue());
		Assert.assertEquals("tven",atoms.get(1).getName());
		Assert.assertEquals("Test Show Name",atoms.get(2).getValue());
		Assert.assertEquals("tvsh",atoms.get(2).getName());
		Assert.assertEquals("1",atoms.get(3).getValue());
		Assert.assertEquals("tvsn",atoms.get(3).getName());
		Assert.assertEquals("3",atoms.get(4).getValue());
		Assert.assertEquals("tves",atoms.get(4).getName());
		Assert.assertEquals("2005",atoms.get(5).getValue());
		Assert.assertEquals("©day",atoms.get(5).getName());
		Assert.assertEquals("Test Episode",atoms.get(6).getValue());
		Assert.assertEquals("©nam",atoms.get(6).getName());
		Assert.assertEquals("This is a test show summary",atoms.get(7).getValue());
		Assert.assertEquals("desc",atoms.get(7).getName());
		Assert.assertEquals("SciFi",atoms.get(8).getValue());
		Assert.assertEquals("©gen",atoms.get(8).getName());
		Assert.assertEquals("SciFi",atoms.get(9).getValue());
		Assert.assertEquals("catg",atoms.get(9).getName());
	}

	/**
	 * Used to test that a film meta data can be written to a .mp4 file
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testWriteFilm() throws Exception {
		URL url = Data.class.getResource("a_video.mp4");
		File srcFile = new File(url.toURI());
		Assert.assertTrue(srcFile.exists());

		File mp4File = FileHelper.createTempFile("test", ".mp4");
		if (!mp4File.delete() && mp4File.exists()) {
			throw new IOException("Unable to delete file");
		}
		FileHelper.copy(srcFile, mp4File);
		Film film = createTestFilm();

		IMP4Manager ap = new MP4Manager();
		ap.updateFilm(mp4File, film,null);

		List<Atom> atoms = ap.listAtoms(mp4File);
		Collections.sort(atoms, new Comparator<Atom>() {
			@Override
			public int compare(Atom o1, Atom o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		Assert.assertEquals(7,atoms.size());

		Assert.assertEquals("SciFi",atoms.get(0).getValue());
		Assert.assertEquals("catg",atoms.get(0).getName());
		Assert.assertEquals("iTunes Cover",atoms.get(1).getValue());
		Assert.assertEquals("covr",atoms.get(1).getName());
		Assert.assertEquals("A test description",atoms.get(2).getValue());
		Assert.assertEquals("desc",atoms.get(2).getName());
		Assert.assertEquals("Movie",((AtomStik)atoms.get(3)).getTypedValue().getDescription());
		Assert.assertEquals("0",((AtomStik)atoms.get(3)).getTypedValue().getId());
		Assert.assertEquals("stik",atoms.get(3).getName());
		Assert.assertEquals("2005",atoms.get(4).getValue());
		Assert.assertEquals("©day",atoms.get(4).getName());
		Assert.assertEquals("SciFi",atoms.get(5).getValue());
		Assert.assertEquals("©gen",atoms.get(5).getName());
		Assert.assertEquals("Test film name",atoms.get(6).getValue());
		Assert.assertEquals("©nam",atoms.get(6).getName());
	}

	private Film createTestFilm() throws Exception {
		Film film = new Film("123");
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
		show.setName("Test Show Name");
		List<String>genres = new ArrayList<String>();
		genres.add("SciFi");
		genres.add("Drama");
		show.setGenres(genres);
		Season season = new Season(show,1);
		Episode episode = new Episode(3,season);
		episode.setDate(DF.parse("10-11-2005"));
		episode.setEpisodeId("34567");
		episode.setRating(new Rating(5.4F,345));
		episode.setSummary("This is a test show summary");
		episode.setTitle("Test Episode");

		return episode;
	}
}

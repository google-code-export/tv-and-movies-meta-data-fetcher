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
import org.stanwood.media.store.mp4.mp4v2.MP4v2Manager;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link MP4v2Manager} class.
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
		Assert.assertEquals("Summary: [desc=This is a test show summary]",atoms.get(1).toString());
		Assert.assertEquals("Media type: [stik=10]",atoms.get(2).toString());
		Assert.assertEquals("Episode ID: [tven=103]",atoms.get(3).toString());
		Assert.assertEquals("TV episode number: [tves=3]",atoms.get(4).toString());
		Assert.assertEquals("TV show name: [tvsh=Test Show Name]",atoms.get(5).toString());
		Assert.assertEquals("TV season number: [tvsn=1]",atoms.get(6).toString());
		Assert.assertEquals("Release year: [©day=Thu Nov 10 00:00:00 GMT 2005]",atoms.get(7).toString());
		Assert.assertEquals("Genre: [©gen=SciFi]",atoms.get(8).toString());
		Assert.assertEquals("Title: [©nam=Test Episode]",atoms.get(9).toString());
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
		IMP4Manager ap = createMP4Manager();
		MP4ITunesStore.updateEpsiode(ap,mp4File, episode);

		List<IAtom> atoms = ap.listAtoms(mp4File);
		Assert.assertEquals(10,atoms.size());
//		Assert.assertEquals("TV Show",((AtomStik)atoms.get(0)).getTypedValue().getDescription());
//		Assert.assertEquals("10",((AtomStik)atoms.get(0)).getTypedValue().getId());
		Assert.assertEquals("stik=10",atoms.get(0).toString());
		Assert.assertEquals("stik",atoms.get(0).toString());
		Assert.assertEquals("tven=34567",atoms.get(1).toString());
		Assert.assertEquals("tvsh=Test Show Name",atoms.get(2).toString());
		Assert.assertEquals("tvsn=1",atoms.get(3).toString());
		Assert.assertEquals("tves=3",atoms.get(4).toString());
		Assert.assertEquals("©day=2005",atoms.get(5).toString());
		Assert.assertEquals("©nam=Test Episode",atoms.get(6).toString());
		Assert.assertEquals("desc=This is a test show summary",atoms.get(7).toString());
		Assert.assertEquals("©gen=SciFi",atoms.get(8).toString());
		Assert.assertEquals("catg=SciFi",atoms.get(9).toString());
	}

	protected IMP4Manager createMP4Manager() throws MP4Exception {
		MP4v2Manager manager = new MP4v2Manager();
		manager.init();
		return manager;
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

		IMP4Manager ap = createMP4Manager();
		MP4ITunesStore.updateFilm(ap,mp4File, film,null);

		List<IAtom> atoms = ap.listAtoms(mp4File);
		Collections.sort(atoms, new Comparator<IAtom>() {
			@Override
			public int compare(IAtom o1, IAtom o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		Assert.assertEquals(7,atoms.size());

		Assert.assertEquals("catg=SciFi",atoms.get(0).toString());
//		Assert.assertEquals("Artwork of type COVERART_JPEG and size 9495",atoms.get(1).getValue());
		Assert.assertTrue(atoms.get(1).toString().startsWith("covr=Artwork of type COVERART_JPEG and size "));
		Assert.assertEquals("desc=A test description",atoms.get(2).toString());
		Assert.assertEquals("stik=0",atoms.get(3).toString());
		Assert.assertEquals("©day=2005",atoms.get(4).toString());
		Assert.assertEquals("©gen=SciFi",atoms.get(5).toString());
		Assert.assertEquals("©nam=Test film name",atoms.get(6).toString());
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

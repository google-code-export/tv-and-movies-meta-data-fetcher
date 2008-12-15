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
package org.stanwood.media.store.mp4;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.stanwood.media.FileHelper;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;

/**
 * Used to test the {@link AtomicParsley} class.
 */
public class TestAtomicParsley extends TestCase {

	private SimpleDateFormat DF = new SimpleDateFormat("dd-MM-yyyy");
	
	/** 
	 * Used to test reading atoms when the MP4 file has no atoms
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testNoAtomsFound() throws Exception {		
		AtomicParsley.updateImages = false;
		File apCmd = new File("/usr/local/bin/AtomicParsley");
//		File apCmd = new File("c:\\AtomicParsley-win32-0.9.0\\AtomicParsley.exe");
		assertTrue("Check atomic parsley command can be found",apCmd.exists());

		URL url = Data.class.getResource("a_video.mp4");
		File mp4File = new File(url.toURI());
		assertTrue(mp4File.exists());		
		
		AtomicParsley ap = new AtomicParsley(apCmd);
		List<Atom> atoms = ap.listAttoms(mp4File);
		
		assertEquals(0,atoms.size());	
	}
	
	/** 
	 * Used to test that the episode details can be written to the MP4 file.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testWriteEpsiode() throws Exception {
		AtomicParsley.updateImages = false;
		File apCmd = new File("/usr/local/bin/AtomicParsley");
//		File apCmd = new File("c:\\AtomicParsley-win32-0.9.0\\AtomicParsley.exe");
		assertTrue("Check atomic parsley command can be found",apCmd.exists());

		URL url = Data.class.getResource("a_video.mp4");
		File srcFile = new File(url.toURI());
		assertTrue(srcFile.exists());
		
		File mp4File = File.createTempFile("test", ".mp4");
		mp4File.delete();
		FileHelper.copy(srcFile, mp4File);
		mp4File.deleteOnExit();
		Episode episode = createTestEpisode();
		AtomicParsley ap = new AtomicParsley(apCmd);
		ap.updateEpsiode(mp4File, episode);
		
		List<Atom> atoms = ap.listAttoms(mp4File);
		System.out.println("Output: " +ap.getOutputStream());
		assertEquals(10,atoms.size());
		assertEquals("TV Show",atoms.get(0).getValue());
		assertEquals("stik",atoms.get(0).getName());
		assertEquals("103",atoms.get(1).getValue());
		assertEquals("tven",atoms.get(1).getName());
		assertEquals("Test Show Name",atoms.get(2).getValue());
		assertEquals("tvsh",atoms.get(2).getName());
		assertEquals("1",atoms.get(3).getValue());
		assertEquals("tvsn",atoms.get(3).getName());
		assertEquals("3",atoms.get(4).getValue());
		assertEquals("tves",atoms.get(4).getName());
		assertEquals("Thu Nov 10 00:00:00 GMT 2005",atoms.get(5).getValue());
		assertEquals("©day",atoms.get(5).getName());
		assertEquals("Test Episode",atoms.get(6).getValue());
		assertEquals("©nam",atoms.get(6).getName());
		assertEquals("This is a test show summary",atoms.get(7).getValue());
		assertEquals("desc",atoms.get(7).getName());
		assertEquals("SciFi",atoms.get(8).getValue());
		assertEquals("©gen",atoms.get(8).getName());
		assertEquals("SciFi",atoms.get(9).getValue());
		assertEquals("catg",atoms.get(9).getName());			
	}
	
	/**
	 * Used to test that a film meta data can be written to a .mp4 file
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testWriteFilm() throws Exception {
		AtomicParsley.updateImages = false;
		File apCmd = new File("/usr/local/bin/AtomicParsley");
//		File apCmd = new File("c:\\AtomicParsley-win32-0.9.0\\AtomicParsley.exe");
		assertTrue("Check atomic parsley command can be found",apCmd.exists());

		URL url = Data.class.getResource("a_video.mp4");
		File srcFile = new File(url.toURI());
		assertTrue(srcFile.exists());
		
		File mp4File = File.createTempFile("test", ".mp4");
		mp4File.delete();
		FileHelper.copy(srcFile, mp4File);
		mp4File.deleteOnExit();
		Film film = createTestFilm();
		
		AtomicParsley ap = new AtomicParsley(apCmd);
		ap.updateFilm(mp4File, film);
		
		List<Atom> atoms = ap.listAttoms(mp4File);
		System.out.println("Output: " +ap.getOutputStream());
		assertEquals(6,atoms.size());
		assertEquals("Movie",atoms.get(0).getValue());
		assertEquals("stik",atoms.get(0).getName());		
		assertEquals("Thu Nov 10 00:00:00 GMT 2005",atoms.get(1).getValue());
		assertEquals("©day",atoms.get(1).getName());
		assertEquals("Test film name",atoms.get(2).getValue());
		assertEquals("©nam",atoms.get(2).getName());
		assertEquals("A test summary",atoms.get(3).getValue());
		assertEquals("desc",atoms.get(3).getName());
		assertEquals("SciFi",atoms.get(4).getValue());
		assertEquals("©gen",atoms.get(4).getName());
		assertEquals("SciFi",atoms.get(5).getValue());
		assertEquals("catg",atoms.get(5).getName());	
	}
	
	private Film createTestFilm() throws Exception {
		Film film = new Film(123);
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
		List<Link> directors = new ArrayList<Link>();
		directors.add(new Link("Bryan Singer","http://www.imdb.com/name/nm0001741/"));
		film.setDirectors(directors);
		film.setFilmUrl(new URL("http://www.imdb.com/title/tt0114814/"));
		List<String>genres = new ArrayList<String>();
		genres.add("SciFi");
		genres.add("Drama");
		film.setGenres(genres);
		List<Link> guestStars = new ArrayList<Link>();
		guestStars.add(new Link("Stephen Baldwin","http://www.imdb.com/name/nm0000286/"));
		guestStars.add(new Link("Gabriel Byrne","http://www.imdb.com/name/nm0000321/"));
		guestStars.add(new Link("Benicio Del Toro","http://www.imdb.com/name/nm0001125/"));
		guestStars.add(new Link("Kevin Pollak","http://www.imdb.com/name/nm0001629/"));
		film.setGuestStars(guestStars);
		film.setRating(8.4F);
		film.setSourceId("imdb");
		film.setSummary("A test summary");
		film.setTitle("Test film name");
		List<Link>writers = new ArrayList<Link>();
		writers.add(new Link("Christopher McQuarrie","http://www.imdb.com/name/nm0003160/"));
		film.setWriters(writers);
		return film;
	}
	
	private Episode createTestEpisode() throws Exception {		
		Show show = new Show(123);
		show.setName("Test Show Name");
		List<String>genres = new ArrayList<String>();
		genres.add("SciFi");
		genres.add("Drama");
		show.setGenres(genres);
		Season season = new Season(show,1);		
		Episode episode = new Episode(3,season);
		episode.setDate(DF.parse("10-11-2005"));
		episode.setEpisodeId(34567);
		episode.setProductionCode("prod103");
		episode.setRating(5.4F);
		episode.setSiteId("103");
		episode.setSummary("This is a test show summary");
		episode.setTitle("Test Episode");
		
		return episode;
	} 
}

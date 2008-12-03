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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.FileHelper;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.testdata.Data;

import junit.framework.TestCase;

public class TestAtomicParsley extends TestCase {

	private SimpleDateFormat DF = new SimpleDateFormat("dd-MM-yyyy");
	
	public void testNoAtomsFound() throws Exception {
//		File apCmd = new File("/usr/local/bin/AtomicParsley");
		File apCmd = new File("c:\\AtomicParsley-win32-0.9.0\\AtomicParsley.exe");
		assertTrue(apCmd.exists());

		URL url = Data.class.getResource("a_video.mp4");
		File mp4File = new File(url.toURI());
		assertTrue(mp4File.exists());		
		
		AtomicParsley ap = new AtomicParsley(apCmd);
		List<Atom> atoms = ap.listAttoms(mp4File);
		
		assertEquals(0,atoms.size());	
	}
	
	public void testWriteEpsiode() throws Exception {
//		File apCmd = new File("/usr/local/bin/AtomicParsley");
		File apCmd = new File("c:\\AtomicParsley-win32-0.9.0\\AtomicParsley.exe");
		assertTrue(apCmd.exists());

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

	private Episode createTestEpisode() throws ParseException {
		Show show = new Show(new File("/tmp/blah"),123);
		show.setName("Test Show Name");
		List<String>genres = new ArrayList<String>();
		genres.add("SciFi");
		genres.add("Drama");
		show.setGenres(genres);
		Season season = new Season(show,1);		
		Episode episode = new Episode(3,season);
		episode.setAirDate(DF.parse("10-11-2005"));
		episode.setEpisodeId(34567);
		episode.setProductionCode("prod103");
		episode.setRating(5.4F);
		episode.setSiteId("103");
		episode.setSummary("This is a test show summary");
		episode.setTitle("Test Episode");
		
		return episode;
	} 
}

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
package org.stanwood.media.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.stanwood.media.FileHelper;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.testdata.Data;

import au.id.jericho.lib.html.Source;

/**
 * Used to test the {@link TagChimpSource} class.
 */
public class TestTagChimpSource extends TestCase {

	private final static String FILM_ID_IRON_MAN = "iron-man-17";
	
	/**
	 * Used to test the searching of films
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testSearch() throws Exception {
		TagChimpSource source = getSource(FILM_ID_IRON_MAN);
		File dir = FileHelper.createTmpDir("films");
		try {
			File tmpFile = new File(dir,"Iron man.avi");						
			SearchResult result = source.searchForVideoId(Mode.FILM,tmpFile);
			assertEquals("iron-man-17",result.getId());
			assertEquals("tagChimp",result.getSourceId());			
		}
		finally {
			FileHelper.deleteDir(dir);
		}	
	}
	
	/**
	 * Test that film details are correctly read from the source
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testIronManFilm() throws Exception {
		TagChimpSource source = getSource(FILM_ID_IRON_MAN);
		Film film = source.getFilm( FILM_ID_IRON_MAN);
		assertEquals("Check id",FILM_ID_IRON_MAN,film.getId());
		assertEquals("Check title","Iron Man",film.getTitle().trim());
		assertEquals("Check summary","You know you're going to get a different kind of superhero when you cast Robert Downey Jr. in the lead role. And Iron Man is different, in welcome ways. Cleverly updated from Marvel Comics' longstanding series, Iron Man puts billionaire industrialist Tony",film.getSummary());
		assertNull("Check rating",film.getRating());
		assertEquals("Check the release date","Fri May 09 00:00:00 BST 2008",film.getDate().toString());
		StringBuilder expectedDesc = new StringBuilder();
		expectedDesc.append("The movie begins with Tony Stark (Robert Downey Jr.) visiting soldiers on duty ");
		expectedDesc.append("in the Middle East. He is joking with some members of a convoy who seem to be ");
		expectedDesc.append("genuinely amused by his persona. Suddenly, the convoy is attacked. The soldiers ");
		expectedDesc.append("fight to defend themselves but are quickly killed. Stark flees when a bomb ");
		expectedDesc.append("(bearing the logo of his company, \"Stark Industries\") explodes, severely ");
		expectedDesc.append("wounding Tony's chest. Tony is captured and recorded by a group of terrorists. ");
		expectedDesc.append("A flashback sequence reveals Tony's history as a child prodigy before taking over his father's technology company at age 21. Colonel James Rhodes (Terrence Howard) attends a ceremony to present Tony Stark with an award for his work, but Stark is not in attendance. Tony's right-hand man (and his father's former partner) Obadiah Stane (Jeff Bridges) accepts the award in Tony's honor. Rhody later finds Tony partying in a casino. On his way out, a reporter named Christine (Leslie Bibb) approaches Stark with some questions regarding the ethics of his weapons business. Stark deflects her questions with some swift quips and the two end up spending the night together. Next morning, Christine is awakened by a voice on a computer monitor. It's JARVIS, the artificial intelligence program responsible for running Tony's house. Christine is greeted by Tony's assistant, \"Pepper\" Potts (Gwyneth Paltrow) as she leaves the house. Pepper helps Tony catch up on some business before Tony heads out to the airport where his plane is kept. In flight, Tony talks with Rhody. Rhody is unhappy about Tony's lax attitude, and Tony tries to get his old friend to relax. Before long they are drunk and leering at the stewardesses. Tony arrives at a military outpost in the Middle East to demonstrate his company's latest project - the Jericho, a super-missile system. After the demonstration, Tony gets a phone call from Obadiah and they are both pleased that the demonstration went well. Tony goes off with the convoy that is soon attacked by terrorists. Much later, Tony regains consciousness in a cave. His chest is hooked up to a strange device. Another captive, named Yinsen (Shaun Tomb), explains that he operated on Stark but was unable to remove all the shrapnel fragments from the bomb blast. Yinsen created a device - essentially a battery-powered magnet - that will keep the remaining fragments out of Tony's heart. The terrorists who captured Tony & Yinsen enter the room. Yinsen translates; they want Tony to build them a Jericho missile. Tony refuses, so they begin to torture Stark.");
		assertEquals("Check film description",expectedDesc.toString(),film.getDescription());
		
		List<String>genres = film.getGenres();
		assertEquals(1,genres.size());
		assertEquals("Sci-Fi & Fantasy",genres.get(0));				
		
		List<Link>directors = film.getDirectors();
		assertEquals(1,directors.size());
		assertEquals("Jon Favreau",directors.get(0).getTitle());
		assertEquals("",directors.get(0).getURL());
		
		List<Link>writers = film.getWriters();
		assertEquals(2,writers.size());
		assertEquals("Mark Fergus",writers.get(0).getTitle());
		assertEquals("",writers.get(0).getURL());
		assertEquals("Hawk Ostby",writers.get(1).getTitle());
		assertEquals("",writers.get(1).getURL());
		
		List<Certification>certs = film.getCertifications();
		assertEquals(1,certs.size());
		assertEquals("USA",certs.get(0).getCountry());
		assertEquals("PG-13",certs.get(0).getCertification());
				
		List<Chapter>chapters = film.getChapters();
		assertEquals("Check chapter number", 1,chapters.get(0).getNumber());
		assertEquals("Check chapter name", "Start",chapters.get(0).getName());
		assertEquals("Check chapter number", 2,chapters.get(1).getNumber());
		assertEquals("Check chapter name", "Las Vegas",chapters.get(1).getName());
		assertEquals("Check chapter number", 3,chapters.get(2).getNumber());
		assertEquals("Check chapter name", "Puttering",chapters.get(2).getName());
		assertEquals("Check chapter number", 4,chapters.get(3).getNumber());
		assertEquals("Check chapter name", "Survived",chapters.get(3).getName());
		assertEquals("Check chapter number", 5,chapters.get(4).getNumber());
		assertEquals("Check chapter name", "The Plans",chapters.get(4).getName());
		assertEquals("Check chapter number", 6,chapters.get(5).getNumber());
		assertEquals("Check chapter name", "What Progress",chapters.get(5).getName());
		assertEquals("Check chapter number", 7,chapters.get(6).getNumber());
		assertEquals("Check chapter name", "Home",chapters.get(6).getName());
		assertEquals("Check chapter number", 8,chapters.get(7).getNumber());
		assertEquals("Check chapter name", "Pundits",chapters.get(7).getName());
		assertEquals("Check chapter number", 9,chapters.get(8).getNumber());
		assertEquals("Check chapter name", "Industry",chapters.get(8).getName());
		assertEquals("Check chapter number", 10,chapters.get(9).getNumber());
		assertEquals("Check chapter name", "Morning After",chapters.get(9).getName());
		assertEquals("Check chapter number", 11,chapters.get(10).getNumber());
		assertEquals("Check chapter name", "New Grids",chapters.get(10).getName());
		assertEquals("Check chapter number", 12,chapters.get(11).getNumber());
		assertEquals("Check chapter name", "Allies",chapters.get(11).getName());
		assertEquals("Check chapter number", 13,chapters.get(12).getNumber());
		assertEquals("Check chapter name", "Pepper:Accomplice",chapters.get(12).getName());
		assertEquals("Check chapter number", 14,chapters.get(13).getNumber());
		assertEquals("Check chapter name", "Old Inventions",chapters.get(13).getName());
		assertEquals("Check chapter number", 15,chapters.get(14).getNumber());
		assertEquals("Check chapter name", "Underpowered",chapters.get(14).getName());
		assertEquals("Check chapter number", 16,chapters.get(15).getNumber());
		assertEquals("Check chapter name", "Official Statement",chapters.get(15).getName());
		assertEquals("Check chapter number", 17,chapters.get(16).getNumber());
		assertEquals("Check chapter name", "End Credits",chapters.get(16).getName());
	}

	private TagChimpSource getSource(final String filmId) {
		TagChimpSource.fetchPosters = false;
		TagChimpSource source = new TagChimpSource() {
			@Override
			Source getSource(URL url) throws IOException {						
				String strUrl = url.toExternalForm();				
				if (strUrl.equals("http://www.tagchimp.com/movies/"+filmId+"/")) {
					String file = "tagchimp-"+filmId+".html";
					return new Source(Data.class.getResource(file));									
				}						
				else if (strUrl.contains("/search/index.php?searchterm=")) {
					return new Source(Data.class.getResource("tagchimp-search-ironman.html"));
				}
				return null;
			}			
		};
		return source;
	}
}
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
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link TagChimpSource} class.
 */
public class TestTagChimpSource {

	private static final Pattern TAGCHIMP_FILM_PATTERN = Pattern.compile(".*tagchimp\\.com.*id\\=(.+?)");
	private static final Pattern TAGCHIMP_SEARCH_PATTERN = Pattern.compile(".*tagchimp\\.com.*title\\=(.+?)");
	private static final String FILM_ID_IRON_MAN = "39752";

	private final DateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");

	/**
	 * Used to test the searching of films
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testSearch() throws Exception {
		TagChimpSource source = getSource();

		File dir = FileHelper.createTmpDir("films");
		try {
			File tmpFile = new File(dir,"Iron man.avi");
			MediaDirConfig config = new MediaDirConfig();
			config.setMediaDir(tmpFile.getAbsoluteFile());
			config.setMode(Mode.FILM);
			SearchResult result = source.searchMedia("Iron Man", Mode.FILM, null);
			Assert.assertEquals("39752",result.getId());
			Assert.assertEquals("tagChimp",result.getSourceId());
			Assert.assertEquals("http://www.tagchimp.com/ape/search.php?token=11151451274D8F94339E891&type=lookup&id=39752",result.getUrl());
		}
		finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Test that film details are correctly read from the source
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testIronManFilm() throws Exception {
		TagChimpSource source = getSource();
		Film film = source.getFilm( FILM_ID_IRON_MAN,new URL("http://www.tagchimp.com/ape/search.php?token=11151451274D8F94339E891&type=lookup&id=39752"),null);
		Assert.assertEquals("Check id",FILM_ID_IRON_MAN,film.getId());
		Assert.assertEquals("Check title","Iron Man",film.getTitle().trim());
		Assert.assertEquals("Check summary","When wealthy industrialist Tony Stark (Robert Downey Jr.) is forced to build an armored suit after a life-threatening incident, he ultimately decides to use its technology to fight against evil.",film.getSummary());
		Assert.assertNull("Check rating",film.getRating());
		Assert.assertEquals("Check the release date","00:00:00 2008-05-09",df.format(film.getDate()));
		Assert.assertEquals("Check image url","http://www.tagchimp.com/covers/large/39752.jpg",film.getImageURL().toExternalForm());
		StringBuilder expectedDesc = new StringBuilder();
		expectedDesc.append("The movie begins with Tony Stark (Robert Downey Jr.) visiting soldiers on duty ");
		expectedDesc.append("in the Middle East. He is joking with some members of a convoy who seem to be ");
		expectedDesc.append("genuinely amused by his persona. Suddenly, the convoy is attacked. The soldiers ");
		expectedDesc.append("fight to defend themselves but are quickly killed. Stark flees when a bomb ");
		expectedDesc.append("(bearing the logo of his company, \"Stark Industries\") explodes, severely ");
		expectedDesc.append("wounding Tony's chest. Tony is captured and recorded by a group of terrorists. ");
		expectedDesc.append("A flashback sequence reveals Tony's history as a child prodigy before taking over his father's technology company at age 21. Colonel James Rhodes (Terrence Howard) attends a ceremony to present Tony Stark with an award for his work, but Stark is not in attendance. Tony's right-hand man (and his father's former partner) Obadiah Stane (Jeff Bridges) accepts the award in Tony's honor. Rhody later finds Tony partying in a casino. On his way out, a reporter named Christine (Leslie Bibb) approaches Stark with some questions regarding the ethics of his weapons business. Stark deflects her questions with some swift quips and the two end up spending the night together. Next morning, Christine is awakened by a voice on a computer monitor. It's JARVIS, the artificial intelligence program responsible for running Tony's house. Christine is greeted by Tony's assistant, \"Pepper\" Potts (Gwyneth Paltrow) as she leaves the house. Pepper helps Tony catch up on some business before Tony heads out to the airport where his plane is kept. In flight, Tony talks with Rhody. Rhody is unhappy about Tony's lax attitude, and Tony tries to get his old friend to relax. Before long they are drunk and leering at the stewardesses. Tony arrives at a military outpost in the Middle East to demonstrate his company's latest project - the Jericho, a super-missile system. After the demonstration, Tony gets a phone call from Obadiah and they are both pleased that the demonstration went well. Tony goes off with the convoy that is soon attacked by terrorists. Much later, Tony regains consciousness in a cave. His chest is hooked up to a strange device. Another captive, named Yinsen (Shaun Tomb), explains that he operated on Stark but was unable to remove all the shrapnel fragments from the bomb blast. Yinsen created a device - essentially a battery-powered magnet - that will keep the remaining fragments out of Tony's heart. The terrorists who captured Tony & Yinsen enter the room. Yinsen translates; they want Tony to build them a Jericho missile. Tony refuses, so they begin to torture Stark.");
		Assert.assertEquals("Check film description",expectedDesc.toString(),film.getDescription());

		List<String>genres = film.getGenres();
		Assert.assertEquals(1,genres.size());
		Assert.assertEquals("Sci-Fi & Fantasy",genres.get(0));

		List<String>directors = film.getDirectors();
		Assert.assertEquals(1,directors.size());
		Assert.assertEquals("Jon Favreau",directors.get(0));

		List<String>writers = film.getWriters();
		Assert.assertEquals(2,writers.size());
		Assert.assertEquals("Mark Fergus",writers.get(0));
		Assert.assertEquals("Hawk Ostby",writers.get(1));

		List<Certification>certs = film.getCertifications();
		Assert.assertEquals(1,certs.size());
		Assert.assertEquals("mpaa",certs.get(0).getType());
		Assert.assertEquals("PG-13",certs.get(0).getCertification());

		List<Chapter>chapters = film.getChapters();
		Assert.assertEquals("Check chapter number", 1,chapters.get(0).getNumber());
		Assert.assertEquals("Check chapter name", "Start",chapters.get(0).getName());
		Assert.assertEquals("Check chapter number", 2,chapters.get(1).getNumber());
		Assert.assertEquals("Check chapter name", "Las Vegas",chapters.get(1).getName());
		Assert.assertEquals("Check chapter number", 3,chapters.get(2).getNumber());
		Assert.assertEquals("Check chapter name", "Puttering",chapters.get(2).getName());
		Assert.assertEquals("Check chapter number", 4,chapters.get(3).getNumber());
		Assert.assertEquals("Check chapter name", "Survived",chapters.get(3).getName());
		Assert.assertEquals("Check chapter number", 5,chapters.get(4).getNumber());
		Assert.assertEquals("Check chapter name", "The Plans",chapters.get(4).getName());
		Assert.assertEquals("Check chapter number", 6,chapters.get(5).getNumber());
		Assert.assertEquals("Check chapter name", "What Progress",chapters.get(5).getName());
		Assert.assertEquals("Check chapter number", 7,chapters.get(6).getNumber());
		Assert.assertEquals("Check chapter name", "Home",chapters.get(6).getName());
		Assert.assertEquals("Check chapter number", 8,chapters.get(7).getNumber());
		Assert.assertEquals("Check chapter name", "Pundits",chapters.get(7).getName());
		Assert.assertEquals("Check chapter number", 9,chapters.get(8).getNumber());
		Assert.assertEquals("Check chapter name", "Industry",chapters.get(8).getName());
		Assert.assertEquals("Check chapter number", 10,chapters.get(9).getNumber());
		Assert.assertEquals("Check chapter name", "Morning After",chapters.get(9).getName());
		Assert.assertEquals("Check chapter number", 11,chapters.get(10).getNumber());
		Assert.assertEquals("Check chapter name", "New Grids",chapters.get(10).getName());
		Assert.assertEquals("Check chapter number", 12,chapters.get(11).getNumber());
		Assert.assertEquals("Check chapter name", "Allies",chapters.get(11).getName());
		Assert.assertEquals("Check chapter number", 13,chapters.get(12).getNumber());
		Assert.assertEquals("Check chapter name", "Pepper:Accomplice",chapters.get(12).getName());
		Assert.assertEquals("Check chapter number", 14,chapters.get(13).getNumber());
		Assert.assertEquals("Check chapter name", "Old Inventions",chapters.get(13).getName());
		Assert.assertEquals("Check chapter number", 15,chapters.get(14).getNumber());
		Assert.assertEquals("Check chapter name", "Underpowered",chapters.get(14).getName());
		Assert.assertEquals("Check chapter number", 16,chapters.get(15).getNumber());
		Assert.assertEquals("Check chapter name", "Official Statement",chapters.get(15).getName());
		Assert.assertEquals("Check chapter number", 17,chapters.get(16).getNumber());
		Assert.assertEquals("Check chapter name", "End Credits",chapters.get(16).getName());
	}

	private TagChimpSource getSource() {
		TagChimpSource source = new TagChimpSource() {
			@Override
			InputStream getSource(URL url) throws IOException {
				String strUrl = url.toExternalForm();
				System.out.println("Fetching URL: " + strUrl);
				Matcher m = TAGCHIMP_SEARCH_PATTERN.matcher(strUrl);
				if (m.matches()) {
					return Data.class.getResourceAsStream("tagchimp-search-"+getSearchName(m.group(1))+".html");
				}
				m = TAGCHIMP_FILM_PATTERN.matcher(strUrl);
				if (m.matches()) {
					return Data.class.getResourceAsStream("tagchimp-film-"+m.group(1)+".html");
				}
				throw new IOException("Unable to find test data for url: " + url);
			}
		};
		return source;
	}

	private String getSearchName(String value) {
		value = value.toLowerCase();
		value = value.replaceAll("[ |+]", "-");
		return value;
	}
}

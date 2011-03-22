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
package org.stanwood.media.testdata;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;

/**
 * This class in used to make it easier to find all the test resources. They can be found
 * relative to this class.
 */
public class Data {

	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/** Film Source id to use in test data */
	public final static String TEST_FILM_SOURCE_ID = "xbmc-metadata.themoviedb.org";
	/** TV Source id to use in test data */
	public final static String TEST_TV_SOURCE_ID = "xbmc-metadata.tvdb.com";

	/** A test show id */
	public final static String SHOW_ID_EUREKA = "58448";
	/** A test show id */
	public static final String SHOW_ID_HEROES = "17552";

	/**
	 * Used to create test data for a film bject
	 * @return The film test object
	 * @throws Exception Thrown if their is a problem creating the film
	 */
	public static Film createFilm() throws Exception {
		Film film = new Film("114814");
		film.setImageURL(new URL("http://test/image.jpg"));
		film.setTitle("The Usual Suspects");
		List<String> genres = new ArrayList<String>();
		genres.add("Crime");
		genres.add("Drama");
		genres.add("Mystery");
		genres.add("Thriller");
		film.setGenres(genres);

		film.setPreferredGenre("Drama");
		film.setCountry("USA");
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
		certifications.add(new Certification("PG-12","Japan"));
		certifications.add(new Certification("16","Netherlands"));
		certifications.add(new Certification("R18","New Zealand"));
		certifications.add(new Certification("15","Norway"));
		certifications.add(new Certification("M/16","Portugal"));
		certifications.add(new Certification("M18","Singapore"));
		certifications.add(new Certification("PG (cut)","Singapore"));
		certifications.add(new Certification("18","South Korea"));
		certifications.add(new Certification("18","Spain"));
		certifications.add(new Certification("15","Sweden"));
		certifications.add(new Certification("18","UK"));
		certifications.add(new Certification("R","USA"));
		film.setCertifications(certifications);
		film.setDate(df.parse("1995-08-25"));
		film.setDirectors(createStringList(new String[] {"Bryan Singer"}));
		film.setFilmUrl(new URL("http://www.imdb.com/title/tt0114814/"));
		List<Actor> guestStars = new ArrayList<Actor>();
		guestStars.add(new Actor("Stephen Baldwin","Michael McManus"));
		guestStars.add(new Actor("Gabriel Byrne","Dean Keaton"));
		guestStars.add(new Actor("Benicio Del Toro","Fred Fenster"));
		guestStars.add(new Actor("Kevin Pollak","Todd Hockney"));
		guestStars.add(new Actor("Kevin Spacey","Roger 'Verbal' Kint"));
		guestStars.add(new Actor("Chazz Palminteri","Dave Kujan, US Customs"));
		guestStars.add(new Actor("Pete Postlethwaite","Kobayashi"));
		guestStars.add(new Actor("Giancarlo Esposito","Jack Baer, FBI"));
		guestStars.add(new Actor("Suzy Amis","Edie Finneran"));
		guestStars.add(new Actor("Dan Hedaya","Sgt. Jeffrey 'Jeff' Rabin"));
		guestStars.add(new Actor("Paul Bartel","Smuggler"));
		guestStars.add(new Actor("Carl Bressler","Saul Berg"));
		guestStars.add(new Actor("Phillip Simon","Fortier"));
		guestStars.add(new Actor("Jack Shearer","Renault"));
		guestStars.add(new Actor("Christine Estabrook","Dr. Plummer"));
		film.setActors(guestStars);
		film.setRating(new Rating(8.7F,35));
		film.setSourceId(TEST_FILM_SOURCE_ID);
		film.setSummary("A boat has been destroyed, criminals are dead, and the key to this mystery lies with the only survivor and his twisted, convoluted story beginning with five career crooks in a seemingly random police lineup.");
		film.setDescription("Test description of the film");

		film.setWriters(createStringList(new String[] {"Christopher McQuarrie"}));

		film.addChapter(new Chapter("The start",1));
		film.addChapter(new Chapter("The end",3));
		film.addChapter(new Chapter("Second Chapter",2));

		return film;
	}

	/**
	 * Used to create a test show that somewhat resembles eureka
	 * @param eurekaDir The directory where the show is located
	 * @return The episodes of the show
	 * @throws IOException Throw if their is a problem creating shows files
	 * @throws ParseException Thrown if their is a problem parsing a date
	 */
	public static List<EpisodeData> createEurekaShow(File eurekaDir) throws IOException, ParseException {
		List<EpisodeData>result = new ArrayList<EpisodeData>();

		Show show = new Show(SHOW_ID_EUREKA);
		show.setSourceId(TEST_TV_SOURCE_ID);
		show.setImageURL(new URL("http://image.com.com/tv/images/b.gif"));
		StringBuilder summary = new StringBuilder();
		summary.append("Small town. Big secret.\n");
		summary.append("\n");
		summary.append("A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand.\n");
		summary.append("\n");
		summary.append("Eureka is produced by NBC Universal Cable Studio and filmed in Vancouver, British Columbia, Canada.\n");
		show.setLongSummary(summary.toString());
		show.setName("Eureka");
		show.setShortSummary("Small town. Big secret. A car accident leads U.S. Marshal Jack Carter into the top-secret Pacific Northwest town of Eureka. For decades, the United States government has relocated the world's geniuses to Eureka, a town where innovation and chaos have lived hand in hand. Eureka is produced by NBC...");
		show.setShowURL(new URL("http://www.tv.com/show/58448/summary.html"));
		List<String> genres = new ArrayList<String>();
		genres.add("SCIFI");
		genres.add("Drama");
		show.setGenres(genres);

		Season season = new Season(show,1);
		season.setURL(new URL("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_listings.html?season=1"));

		File episodeFile = new File(eurekaDir,"1x01 - blah");
		if (!episodeFile.createNewFile() && episodeFile.exists()) {
			throw new IOException("Unable to create file: " + episodeFile);
		}

		Episode episode1 = new Episode(1,season);
		episode1.setDate(df.parse("2006-10-10"));
		episode1.setSpecial(false);
		episode1.setSummary("A car accident leads U.S. Marshal Jack Carter into the unique Pacific Northwest town of Eureka.");
		episode1.setUrl(new URL("http://www.tv.com/eureka/pilot/episode/784857/summary.html"));
		episode1.setTitle("Pilot");
		episode1.setRating(new Rating(1,1));
		episode1.setDirectors(createStringList(new String[] {"Harry"}));
		episode1.setWriters(createStringList(new String[]{"Write a lot"}));
		episode1.setActors(createActorsList(new Actor[]{new Actor("sally","betty"),new Actor("Cedric","steve")}));
		episode1.setEpisodeId("784857");
		episode1.setImageURL(new URL("http://blah/image.jpg"));
		result.add(new EpisodeData(episode1,episodeFile));

		episodeFile = new File(eurekaDir,"1x02 - blah");
		if (!episodeFile.createNewFile() && episodeFile.exists()) {
			throw new IOException("Unable to create file: " + episodeFile);
		}
		Episode episode2 = new Episode(2,season);
		episode2.setDate(df.parse("2006-10-11"));
		episode2.setSpecial(false);
		episode2.setSummary("Carter and the other citizens of Eureka attend the funeral of Susan and Walter Perkins. Much to their surprise, Susan makes a return to Eureka as a woman who is very much alive!");
		episode2.setUrl(new URL("http://www.tv.com/eureka/many-happy-returns/episode/800578/summary.html"));
		episode2.setTitle("Many Happy Returns");
		episode2.setRating(new Rating(9.5F,2355));
		episode2.setEpisodeId("800578");
		result.add(new EpisodeData(episode2,episodeFile));

		season = new Season(show,2);
		season.setURL(new URL("http://www.tv.com/show/"+SHOW_ID_EUREKA+"/episode_listings.html?season=2"));

		episodeFile = new File(eurekaDir,"2x13 - blah");
		if (!episodeFile.createNewFile() && episodeFile.exists()) {
			throw new IOException("Unable to create file: " + episodeFile);
		}
		episode1 = new Episode(2,season);
		episode1.setDate(df.parse("2007-7-10"));
		episode1.setSpecial(false);
		episode1.setSummary("Reaccustoming to the timeline restored in \"Once in a Lifetime\", Sheriff Carter investigates a series of sudden deaths.");
		episode1.setUrl(new URL("http://www.tv.com/eureka/phoenix-rising/episode/1038982/summary.html"));
		episode1.setTitle("Phoenix Rising");
		episode1.setEpisodeId("800578");
		episode1.setRating(new Rating(0.4F,12354));
		result.add(new EpisodeData(episode1,episodeFile));

		episodeFile = new File(eurekaDir,"000 - blah");
		if (!episodeFile.createNewFile() && episodeFile.exists()) {
			throw new IOException("Unable to create file: " + episodeFile);
		}
		Episode special1 = new Episode(0,season);
		special1.setDate(df.parse("2007-7-09"));
		special1.setSpecial(true);
		special1.setSummary("Before the third season premiere, a brief recap of Seasons 1 and 2 and interviews with the cast at the premiere party is shown.");
		special1.setUrl(new URL("http://www.tv.com/heroes/heroes-countdown-to-the-premiere/episode/1228258/summary.html"));
		special1.setTitle("Countdown to the Premiere");
		special1.setRating(new Rating(0.4F,3000));
		special1.setEpisodeId("800578");
		special1.setDirectors(createStringList(new String[]{"JP"}));
		special1.setWriters(createStringList(new String[]{"Write a lot","Write a little"}));
		List<Actor> actors = new ArrayList<Actor>();
		actors.add(new Actor("bob","fred"));
		actors.add(new Actor("Write a little","blah"));
		special1.setActors(actors);
		result.add(new EpisodeData(special1,episodeFile));

		return result;
	}

	/**
	 * Used to create a actor list from an array
	 * @param values The actors array
	 * @return The actors list
	 */
	public static List<Actor> createActorsList(Actor values[]) {
		List<Actor> list = new ArrayList<Actor>() ;
		for (Actor s : values) {
			list.add(s);
		}
		return list;
	}

	/**
	 * Used to create a list of strings from an array
	 * @param values The array
	 * @return The list
	 */
	public static List<String> createStringList(String values[]) {
		List<String> list = new ArrayList<String>() ;
		for (String s : values) {
			list.add(s);
		}
		return list;
	}

	/**
	 * Used to create a test show that somewhat resembles heroes
	 * @param heroesDir The directory where the show is located
	 * @return The episodes of the show
	 * @throws IOException Throw if their is a problem creating shows files
	 * @throws ParseException Thrown if their is a problem parsing a date
	 */
	public static List<EpisodeData> createHeroesShow(File heroesDir) throws IOException, ParseException {
		List<EpisodeData>result = new ArrayList<EpisodeData>();
		Show show = new Show(SHOW_ID_HEROES);
		show.setSourceId(TEST_TV_SOURCE_ID);
		show.setImageURL(new URL("http://image.com.com/tv/images/content_headers/program_new/17552.jpg"));
		show.getExtraInfo().put("url","http://sdfsdfsdf/sdfsd/fsdfsd/");

		StringBuilder summary = new StringBuilder();
		summary.append("Heroes is a serial saga about people all over the world discovering that they have superpowers and trying to deal with how this change affects their lives. Some of the superheroes who will be introduced to the viewing audience include Peter Petrelli, an almost 30-something male nurse who suspects he might be able to fly, Isaac Mendez, a 28-year-old junkie who has the ability to paint images of the future when he is high, Niki Sanders, a 33-year-old Las Vegas showgirl who begins seeing strange things in mirrors, Hiro Nakamura, a 24-year-old Japanese comic-book geek who literally makes time stand still, D.L. Hawkins, a 31-year-old inmate who can walk through walls, Matt Parkman, a beat cop who can hear other people's thoughts, and Claire Bennet, a 17-year-old cheerleader who defies death at every turn. As the viewing audience is discovering the nature of each hero's powers, the heroes themselves are discovering what having superpowers means to them as well as the larger picture of where their superpowers come from. Tune in each week to see how these heroes are drawn together by their common interest of evading the series' antagonist who wants to harvest their super-DNA for himself. Their ultimate destiny is nothing less than saving the world! The series will star Greg Grunberg (Alias), Leonard Roberts (Buffy the Vampire Slayer), Milo Ventimiglia (Gilmore Girls), and Hayden Panettiere (Ally McBeal, Guiding Light). Tim Kring (Crossing Jordan, Chicago Hope) is the series' creator. The pilot is set to be directed by Dave Semel (American Dreams, Buffy, the Vampire Slayer, Beverly Hills, 90210). Heroes will be produced by NBC/Universal/Tailwind. Summary revised with help from: space-cowboy");
		show.setLongSummary(summary.toString());
		show.setName("Heroes");
		show.setShortSummary("Heroes is a serial saga about people all over the world discovering that they have superpowers and trying to deal with how this change affects their lives. Some of the superheroes who will be introduced to the viewing audience include Peter Petrelli, an almost 30-something male nurse who suspect...");
		show.setShowURL(new URL("http://www.tv.com/heroes/show/17552/summary.html"));
		List<String> genres = new ArrayList<String>();
		genres.add("SCIFI");
		genres.add("Drama");
		show.setGenres(genres);

		File episodeFile = new File(heroesDir,"1x01 - hero");
		if (!episodeFile.createNewFile() && !episodeFile.exists()) {
			throw new IOException("Unable to create file: " + episodeFile.getAbsolutePath());
		}

		Season season = new Season(show,1);
		season.setURL(new URL("http://www.tv.com/show/"+SHOW_ID_HEROES+"/episode_listings.html?season=1"));

		Episode episode1 = new Episode(1,season);
		episode1.setDate(df.parse("2006-11-11"));
		episode1.setSpecial(false);
		episode1.setSummary("After a look into the future, Nathan's shooter is revealed. Matt chases him and winds up in a desert. Hiro receives an important message from his father. Sylar visits Claire. Maya gives Mohinder an idea for his research. Nathan recovers and gets a visit from Linderman.");
		episode1.setUrl(new URL("http://www.tv.com/eureka/pilot/episode/784857/summary.html"));
		episode1.setTitle("Heroe, Pilot");
		episode1.setRating(new Rating(2,2345));
		episode1.setDirectors(createStringList(new String[]{"Whoever"}));
		episode1.setWriters(createStringList(new String[]{"Write a lot"}));
		episode1.setActors(createActorsList(new Actor[]{new Actor("sally","betty"),new Actor("Cedric","steve")}));
		episode1.setEpisodeId("1181337");
		result.add(new EpisodeData(episode1,episodeFile));

		return result;
	}

}

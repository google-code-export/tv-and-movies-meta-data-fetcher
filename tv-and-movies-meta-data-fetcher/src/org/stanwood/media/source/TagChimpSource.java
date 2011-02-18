package org.stanwood.media.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.search.SearchHelper;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Source;

/**
 * This class is a source used to retrieve information about films from {@link "www.tagchimp.com"}. This source has the
 * option parameter "regexpToReplace". This is used when searching for a film via the film's filename. The parameter is
 * a regular expression, that when found in the filename, is removed. Use the method <code>{@link #setParameter(String, String)}</code>
 * to set the regular expression.
 */
public class TagChimpSource implements ISource {

	private final static Log log = LogFactory.getLog(TagChimpSource.class);

	private static final SimpleDateFormat RELEASE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/** The ID of the the source */
	public static final String SOURCE_ID = "tagChimp";

	private static final String BASE_URL = "http://www.tagchimp.com";
	private static final String MOVIES_URL = "/tc/$filmId$/";
	private Pattern CHAPTER_PATTERN = Pattern.compile("chapter (\\d+):");
	private Pattern SEARCH_PATTERN = Pattern.compile("/tc/(\\d+)/");

	private String regexpToReplace = null;

	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 *
	 * @param filmId The id of the film
	 * @param url The URL to use when looking up film details
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Film getFilm(String filmId,URL url) throws SourceException, MalformedURLException, IOException {
		//TODO is this needed? We now have a url in the params
		url = new URL(getFilmURL(filmId));
		Film film = new Film(filmId);
		film.setFilmUrl(url);
		film.setSourceId(SOURCE_ID);
		Source source = getSource(film.getFilmUrl());
		if (source == null) {
			throw new SourceException("Unable to find film with id: " + filmId);
		}
		parseFilm(source, film);
		// if (fetchPosters) {
		// FindFilmPosters posterFinder = new FindFilmPosters();
		// film.setImageURL(posterFinder.findViaMoviePoster(film));
		// }

		return film;
	}

	@SuppressWarnings("unchecked")
	private void parseFilm(Source source, Film film) throws MalformedURLException {
		List<Element> trs = source.findAllElements(HTMLElementName.TR);
		for (Element tr : trs) {
			List<Element> tds = tr.getChildElements();
			if (tds.size() == 2 || tds.size() == 3) {
				int index = 0;
				if (tds.size()==3) {
					index++;
				}
				String fieldName = tds.get(index).getTextExtractor().toString();
				@SuppressWarnings("rawtypes")
				List elements = tds.get(index+1).getChildElements();
				if (elements.size()>0) {
					Element sub = (Element) elements.get(0);
					String fieldValue = null;
					if (sub.getName().equals(HTMLElementName.INPUT)) {
						fieldValue = sub.getAttributeValue("value");
					} else if (sub.getName().equals(HTMLElementName.SELECT)) {
						List<Element> opts = sub.getChildElements();
						for (Element opt : opts) {
							if (opt.getAttributeValue("selected") != null
									&& opt.getAttributeValue("selected").equals("selected")) {
								fieldValue = opt.getAttributeValue("value");
							}
						}
					} else if (sub.getName().equals(HTMLElementName.TEXTAREA)) {
						fieldName = sub.getAttributeValue("name");
						fieldValue = sub.getTextExtractor().toString();
					}

					if (fieldValue != null) {

						handleField(film, fieldName, SearchHelper.decodeHtmlEntities(fieldValue).trim());
					}
				}
			}
		}
		List<Element> imgs = source.findAllElements(HTMLElementName.IMG);
		for (Element img : imgs) {
			String src = img.getAttributeValue("src");
			if (src != null && src.startsWith("/covers/large/")) {
				film.setImageURL(new URL(BASE_URL + src));
			}
		}
	}

	private void handleField(Film film, String name, String value) {
		name = name.toLowerCase();
		if (name.equals("movie title")) {
			film.setTitle(value);
		} else if (name.equals("release date")) {
			try {
				film.setDate(RELEASE_DATE_FORMAT.parse(value));
			} catch (ParseException e) {
				log.error("Unable to parse date '" + value + "' of film with id '" + film.getId() + "'");
			}
		} else if (name.equals("director")) {
			List<String> directors = new ArrayList<String>();
			for (String director : value.split(",")) {
				directors.add(director.trim());
			}
			film.setDirectors(directors);
		} else if (name.equals("producer")) {

		} else if (name.equals("screenwriter")) {
			List<String> writers = new ArrayList<String>();
			for (String writer : value.split(",")) {
				writers.add(writer.trim());
			}
			film.setWriters(writers);
		} else if (name.equals("copyright")) {

		} else if (name.equals("rating")) {
			List<Certification> certs = new ArrayList<Certification>();
			certs.add(new Certification(value, "USA"));
			film.setCertifications(certs);
		} else if (name.equals("genre")) {
			List<String> genres = new ArrayList<String>();
			genres.add(value);
			film.setGenres(genres);
			film.setPreferredGenre(value);
		} else if (name.equals("artist")) {
			List<Actor> guestStars = new ArrayList<Actor>();
			for (String artist : value.split(",")) {
				guestStars.add(new Actor(artist.trim(),""));
			}
			film.setActors(guestStars);
		} else if (name.equals("short_description")) {
			film.setSummary(value);
		} else if (name.equals("long_description")) {
			film.setDescription(value);
		} else {
			Matcher m = CHAPTER_PATTERN.matcher(name);
			if (m.matches()) {
				addChapter(film, Integer.parseInt(m.group(1)), value);
			}
		}
	}

	private void addChapter(Film film, int number, String name) {
		film.addChapter(new Chapter(name, number));
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param show The show the season belongs to.
	 * @param seasonNum The number of the season to read
	 */
	@Override
	public Season getSeason(Show show, int seasonNum) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param url String url of the show
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(String showId,URL url) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param specialNumber The number of the special episode to read
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber) {
		return null;
	}

	/**
	 * Get the id of the source.
	 *
	 * @return The id of the source
	 */
	@Override
	public String getSourceId() {
		return SOURCE_ID;
	}

	/**
	 * This will search the IMDB site for the film. It uses the last segment of the file name, converts it to lower
	 * case, tidies up the name and performs the search.
	 *
	 * @param filmFile The file the film is located in
	 * @param mode The mode that the search operation should be performed in
	 * @return Always returns null
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SearchResult searchForVideoId(File rootMediaDir,Mode mode,File filmFile,String renamePattern) throws SourceException, MalformedURLException,
			IOException {
		if (mode != Mode.FILM) {
			return null;
		}
		String query = SearchHelper.getQuery(filmFile, regexpToReplace).toLowerCase();
		Source source = getSource(new URL(getSearchUrl(query.replaceAll(" ", "+"))));
		List<Element> divs = source.findAllElements(HTMLElementName.DIV);
		for (Element div : divs) {
			if (div.getAttributeValue("id") != null && div.getAttributeValue("id").equals("main_mid")) {
					List<Element> links= new ArrayList<Element>();
					ParseHelper.findAllElements(links,div, HTMLElementName.A,true,null);
					for (Element link : links) {
						String url = link.getAttributeValue("href");
						String title = link.getTextExtractor().toString();
						if (url != null) {
							Matcher m = SEARCH_PATTERN.matcher(url);
							if (m.matches()) {
								String id = m.group(1);
								if (SearchHelper.normalizeQuery(title).contains(SearchHelper.normalizeQuery(query))) {
									SearchResult result = new SearchResult(id, SOURCE_ID,BASE_URL+url);
									return result;
								}
							}
						}
//					}
				}
			}
		}

		return null;
	}

	private String getSearchUrl(String query) {
		return BASE_URL + "/search/index.php?s=" + query + "&search.x=0&search.y=0&kind=mo1";
	}

	/**
	 * <p>Used to set source parameters. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * <p>Supported parameters:
	 * <ul>
	 * 	<li>RegexpToReplace -The parameter is a regular expression, that when found in the filename, is removed.</li>
	 * </ul>
	 * </p>
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public void setParameter(String key,String value) throws SourceException {
		if (key.equalsIgnoreCase("RegexpToReplace")) {
			regexpToReplace = value;
		}
		throw new SourceException("Unsupported parameter '" +key+"' on source '"+getClass().getName()+"'");
	}

	/**
	 * <p>Used to get the value of a source parameter. If the key is not supported by this source, then a {@link SourceException} is thrown.</p>
	 * <p>Supported parameters:
	 * <ul>
	 * 	<li>RegexpToReplace -The parameter is a regular expression, that when found in the filename, is removed.</li>
	 * </ul>
	 * </p>
	 * @param key The key of the parameter
	 * @return The value of the parameter
	 * @throws SourceException Throw if the key is not supported by this source.
	 */
	@Override
	public String getParameter(String key) throws SourceException {
		if (key.equalsIgnoreCase("RegexpToReplace")) {
			return regexpToReplace;
		}
		throw new SourceException("Unsupported parameter '" +key+"' on source '"+getClass().getName()+"'");
	}

	/* package for test */Source getSource(URL url) throws IOException {
		return new Source(url);
	}

	private final static String getFilmURL(String filmId) {
		String strFilmId = filmId;
		while (strFilmId.length() < 7) {
			strFilmId = "0" + strFilmId;
		}

		return BASE_URL + MOVIES_URL.replaceAll("\\$filmId\\$", strFilmId);
	}

	/**
	 * This method can be used to get a URL from a nfo file.
	 * @param mode The mode that the YRL is been looked up in
	 * @param file The NFO file
	 * @return The URL, or null if one could not be found
	 * @throws SourceException Thrown if their are any problems
	 */
	@Override
	public URL getUrlFromNFOFile(Mode mode,File file) throws SourceException {
		return null;
	}

}

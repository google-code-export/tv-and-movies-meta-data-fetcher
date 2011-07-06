package org.stanwood.media.source;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.IVideoActors;
import org.stanwood.media.model.IVideoGenre;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.xbmc.StreamProcessor;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.Stream;
import org.stanwood.media.xml.IterableNodeList;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.stanwood.media.xml.XMLParserNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class is a source used to retrieve information about films from {@link "www.tagchimp.com"}.
 */
public class TagChimpSource extends XMLParser implements ISource {

	private static final SimpleDateFormat RELEASE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/** The ID of the the source */
	public static final String SOURCE_ID = "tagChimp";

	private static final String TAG_CHIMP_TOKEN = "11151451274D8F94339E891";

	/**
	 * This will get a film from the source. If the film can't be found, then it will return null.
	 *
	 * @param filmId The id of the film
	 * @param url The URL to use when looking up film details
	 * @param file The film file if looking up a files details, or NULL
	 * @return The film, or null if it can't be found
	 * @throws SourceException Thrown if their is a problem retrieving the data
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a I/O related problem.
	 */
	@Override
	public Film getFilm(String filmId,URL url,File file) throws SourceException, MalformedURLException, IOException {
		return parseFilm(filmId,url,file);
	}

	private Film parseFilm(final String filmId,final URL url,final File file) throws IOException, SourceException {
		final Film film = new Film(filmId);
		film.setFilmUrl(url);
		film.setSourceId(getSourceId());

		StreamProcessor processor = new StreamProcessor(getStreamToURL(url),"text/xml") { //$NON-NLS-1$
			@Override
			public void processContents(String contents) throws SourceException {
				try {
	    			Document doc = parse(contents, null);
	    			film.setDate(RELEASE_DATE_FORMAT.parse(getStringFromXML(doc, "/items/movie/movieTags/info/releaseDate/text()"))); //$NON-NLS-1$
	    			film.setDescription(stripLineBreaks(getStringFromXML(doc, "/items/movie/movieTags/info/longDescription/text()")," ")); //$NON-NLS-1$
	    			film.setId(filmId);
	    			film.setTitle(getStringFromXML(doc, "/items/movie/movieTags/info/movieTitle/text()")); //$NON-NLS-1$
	    			film.setSummary(stripLineBreaks(getStringFromXML(doc, "/items/movie/movieTags/info/shortDescription/text()")," ")); //$NON-NLS-1$
//	    			film.setRating(new Rating(getFloatFromXML(doc, "details/rating/text()"),getIntegerFromXML(doc, "details/votes/text()")));
//	    			film.setCountry(getStringFromXML(doc, "details/country/text()"));
	    			film.setImageURL(new URL(getStringFromXML(doc, "/items/movie/movieTags/coverArtLarge/text()"))); //$NON-NLS-1$

	    			parseDirectors(film, doc);
	    			parseActors(film, doc);
	    			parseWriters(film, doc);

	    			parseGenres(film,doc);
	    			parseCertification(film,doc);
	    			parseChapters(film,doc);


				}
				catch (XMLParserException e) {
					throw new SourceException("Unable to parse show details",e);
				}
				catch (MalformedURLException e) {
					throw new SourceException("Unable to parse show details",e);
				} catch (ParseException e) {
					throw new SourceException("Unable to parse show details",e);
				}
			}

		};
		processor.handleStream();


		return film;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 * @param file The film file if looking up a files details, or NULL
	 */
	@Override
	public Episode getEpisode(Season season, int episodeNum,File file) {
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
	 * @param file The media file if looking up a files details, or NULL
	 * @param url String url of the show
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(String showId,URL url,File file) {
		return null;
	}

	/**
	 * This always returns null as this source does not support reading episodes.
	 *
	 * @param season The season the episode belongs to.
	 * @param specialNumber The number of the special episode to read
	 * @param file The film file if looking up a files details, or NULL
	 */
	@Override
	public Episode getSpecial(Season season, int specialNumber,File file) {
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

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(final String name,final String year, final Mode mode, final Integer part) throws SourceException {
		if (mode != Mode.FILM) {
			return null;
		}

		final List<SearchResult>lockedResults = new ArrayList<SearchResult>();
		final List<SearchResult>unlockedResults = new ArrayList<SearchResult>();
		try {
			URL url = getSearchUrl(name);
			StreamProcessor processor = new StreamProcessor(getStreamToURL(url),"text/xml") { //$NON-NLS-1$
				@Override
				public void processContents(String contents) throws SourceException {
					try {
						Document doc = parse(contents, null);
						IterableNodeList entities = selectNodeList(doc, "/items/movie"); //$NON-NLS-1$
						for (Node n : entities) {
							String id = getStringFromXML(n, "tagChimpID/text()"); //$NON-NLS-1$
							String locked = getStringFromXML(n, "locked/text()"); //$NON-NLS-1$
							SearchResult result = new SearchResult(id, getSourceId(), getFilmUrl(id).toExternalForm(), null);
							result.setTitle(getStringFromXML(n,"movieTags/info/movieTitle/text()")); //$NON-NLS-1$
							if (locked.equals("yes")) { //$NON-NLS-1$
								lockedResults.add(result);
							}
							else {
								unlockedResults.add(result);
							}
						}
					}
					catch (XMLParserException e) {
						throw new SourceException("Unale to get show results",e);
					} catch (MalformedURLException e) {
						throw new SourceException("Unale to get show results",e);
					}
				}
			};
			processor.handleStream();
		} catch (MalformedURLException e) {
			throw new SourceException("Unable to search for show",e);
		} catch (IOException e) {
			throw new SourceException("Unable to search for show",e);
		}

		final List<SearchResult>results = new ArrayList<SearchResult>();
		results.addAll(lockedResults);
		results.addAll(unlockedResults);
		if (results.size()>0) {
			for (SearchResult result : results) {
				if (result.getTitle().equalsIgnoreCase(name)) {
					return result;
				}
			}
			return results.get(0);
		}
		else {
			return null;

		}
	}

	private void parseGenres(final IVideoGenre video, Document doc) throws XMLParserException {
		List<String> genres = new ArrayList<String>();
		for (Node genre : selectNodeList(doc, "//genre/text()")) { //$NON-NLS-1$
			genres.add(genre.getTextContent());
		}
		video.setGenres(genres);
	}

	private void parseWriters(final IVideo viode, Document doc)
	throws XMLParserException {
		List<String> writers = new ArrayList<String>();
		for (Node writer : selectNodeList(doc, "/items/movie/movieTags/info/screenwriters/screenwriter/text()")) { //$NON-NLS-1$
			writers.add(writer.getTextContent());
		}
		viode.setWriters(writers);
	}

	private void parseDirectors(final IVideo video, Document doc)
		throws XMLParserException {
		List<String> directors = new ArrayList<String>();
		for (Node director : selectNodeList(doc, "/items/movie/movieTags/info/directors/director/text()")) { //$NON-NLS-1$
			directors.add(director.getTextContent());
		}
		video.setDirectors(directors);
	}

	private void parseActors(final IVideoActors video, Document doc)
		throws XMLParserException {
		List<Actor> actors = new ArrayList<Actor>();
		try {
			for (Node actor : selectNodeList(doc, "/items/movie/movieTags/info/cast/actor/text()")) { //$NON-NLS-1$
				actors.add(new Actor(actor.getNodeValue(),""));
			}
		}
		catch (XMLParserNotFoundException e) {
			// Ignore, no actors
		}
		video.setActors(actors);
	}

	protected void parseChapters(Film film, Document doc) throws XMLParserException {
		List<Chapter>chapters = new ArrayList<Chapter>();
		for (Node chapterNode : selectNodeList(doc, "/items/movie/movieChapters/chapter")) { //$NON-NLS-1$
			chapters.add(new Chapter(getStringFromXML(chapterNode, "chapterTitle/text()"),getIntegerFromXML(chapterNode, "chapterNumber/text()"))); //$NON-NLS-1$
		}
		film.setChapters(chapters);
	}

	protected void parseCertification(Film film, Document doc) throws XMLParserException {
		String type = "mpaa";
		String cert = getStringFromXML(doc, "/items/movie/movieTags/info/rating/text()"); //$NON-NLS-1$
		List<Certification>certs = new ArrayList<Certification>();
		certs.add(new Certification(cert, type));
		film.setCertifications(certs);
	}

	private URL getSearchUrl(String query) throws MalformedURLException, UnsupportedEncodingException {
		query = URLEncoder.encode(query,"UTF-8");
		return new URL("http://www.tagchimp.com/ape/search.php?token="+TAG_CHIMP_TOKEN+"&type=search&totalChapters=X&title="+query); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private URL getFilmUrl(String id) throws MalformedURLException {
		return new URL("http://www.tagchimp.com/ape/search.php?token="+TAG_CHIMP_TOKEN+"&type=lookup&id="+id);  //$NON-NLS-1$//$NON-NLS-2$
	}

	/* package for test */Stream getSource(URL url) throws IOException {
		return FileHelper.getInputStream(url);
	}

	/**
	 * Used to download a URL to a stream
	 * @param url The URL to download
	 * @return The stream
	 * @throws IOException Thrown if their are IO problems
	 * @throws SourceException Thrown if their are any other problems
	 */
	public Stream getStreamToURL(URL url) throws IOException, SourceException {
		Stream stream = getSource(url);
		if (stream==null) {
			throw new SourceException("Unable to get resource: " + url);
		}
		return stream;
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
		throw new SourceException(MessageFormat.format("Unsupported parameter '{0}' on source '{1}'",key,getClass().getName()));
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
		throw new SourceException(MessageFormat.format("Unsupported parameter '{0}' on source '{1}'",key,getClass().getName()));
	}

	/** {@inheritDoc} */
	@Override
	public void setMediaDirConfig(MediaDirectory dir) throws SourceException {

	}

	private static String stripLineBreaks(String string, String replaceWith) {
	      int len = string.length();
	      StringBuffer buffer = new StringBuffer(len);
	      for (int i = 0; i < len; i++) {
	          char c = string.charAt(i);

	          // skip \n, \r, \r\n
	          switch (c) {
	              case '\n':
	              case '\r': // do lookahead
	                  if (i + 1 < len && string.charAt(i + 1) == '\n') {
	                      i++;
	                  }

	                  buffer.append(replaceWith);
	                  break;
	              default:
	                  buffer.append(c);
	          }
	      }

	      return buffer.toString().replaceAll(replaceWith+replaceWith, replaceWith);
	  }


}

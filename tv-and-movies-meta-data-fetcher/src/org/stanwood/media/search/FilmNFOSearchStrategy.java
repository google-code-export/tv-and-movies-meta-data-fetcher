package org.stanwood.media.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Film;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCSource;

/**
 * This search strategy looks information about films if they are in or under a directory contains a .NFO
 * file that describes them.
 */
public class FilmNFOSearchStrategy implements ISearchStrategy {

	private final static Pattern PATTERN_IMDB_URL = Pattern.compile(".*www\\.imdb\\..*/(tt\\d+).*");
	private final static Log log = LogFactory.getLog(FilmNFOSearchStrategy.class);
	private final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
	private final static Pattern PATTERN_PART_FOLDER = Pattern.compile("^CD(\\d+)$");


	/**
	 * Look up the film file details using the NFO file if it can be found
	 * @param mediaFile The media file that is been processed
	 * @param rootMediaDir The root media directory
	 * @param renamePattern The pattern that is been used to rename media files
	 * @param mediaDir The media directory
	 * @return The search details
	 */

	@Override
	public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
		File nfoFile = findNFOfile(rootMediaDir,mediaFile.getParentFile());
		if (nfoFile!=null) {

			// Read the IMDB from the NFO file
			String imdbId = getIMDBIDFromFile(nfoFile);
			if (imdbId!=null) {
				// Look the film information up on the IMDB website
				ISource source = mediaDir.getSource("xbmc-metadata.imdb.com");
				if (source instanceof XBMCSource) {
					XBMCSource xbmcSource = (XBMCSource)source;
					try {
						Film film = xbmcSource.getFilm(imdbId, new URL("http://www.imdb.com/title/"+imdbId+"/"), mediaFile);
						if (film!=null) {
							String year = null;
							if (film.getDate()!=null) {
								year = YEAR_FORMAT.format(film.getDate());
							}


							Integer part = getPart(nfoFile,mediaFile);

							SearchDetails details = new SearchDetails(film.getTitle(),year,part);
							return details;
						}
					} catch (SourceException e) {
						log.error("Unable to look up NFO file details on IMDB.com",e);
					} catch (MalformedURLException e) {
						log.error("Unable to look up NFO file details on IMDB.com",e);
					} catch (IOException e) {
						log.error("Unable to look up NFO file details on IMDB.com",e);
					}
				}
			}
		}
		return null;
	}

	private Integer getPart(File nfoFile,File mediaFile) {
		File nfoDir = nfoFile.getParentFile();
		File parentDir = mediaFile.getParentFile();
		if (!parentDir.equals(nfoDir)) {
			Matcher m = PATTERN_PART_FOLDER.matcher(parentDir.getName());
			if (m.matches()) {
				try {
					Integer part = Integer.parseInt(m.group(1));
					return part;
				}
				catch (NumberFormatException e) {
					// Ignore as we could not work out the part
				}
			}
		}
		Integer part = SearchHelper.extractPart(new StringBuilder(mediaFile.getName()));
		return part;
	}

	private File findNFOfile(File rootMediaDir,File parentDir) {
		while (!parentDir.equals(rootMediaDir)) {

			// Now check that their is one nfo file in the directory
			File files[] = parentDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String arg1) {
					return StringUtils.endsWithIgnoreCase(arg1, ".nfo");
				}
			});
			if (files.length==1) {
				 return files[0];
			}
			parentDir = parentDir.getParentFile();
		}
		return null;
	}

	private String getIMDBIDFromFile(File nfoFile) {
		FileReader fr = null;
		BufferedReader in = null;
		try {
			fr = new FileReader(nfoFile);
			in = new BufferedReader(fr);
			String str;
			while ((str = in.readLine()) != null) {
				Matcher m = PATTERN_IMDB_URL.matcher(str);
				if (m.matches()) {
					return m.group(1);
				}
			}
		} catch (IOException e) {
			log.error("Unable to read nfo file: " + nfoFile,e);
		}
		finally {
			try {
			if (in!=null) {
				in.close();
			}
			if (fr!=null) {
				fr.close();
			}
			}
			catch (IOException e) {
				log.error("Unable to close file streams",e);
			}
		}
		return null;
	}
}
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Film;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.xbmc.XBMCSource;

public class FilmNFOSearchStrategy implements ISearchStrategy {

	private final static Pattern PATTERN_IMDB_URL = Pattern.compile(".*www\\.imdb\\..*/(tt\\d+).*");
	private final static Log log = LogFactory.getLog(FilmNFOSearchStrategy.class);
	private final static DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

	@Override
	public SearchDetails getSearch(File mediaFile, File rootMediaDir, String renamePattern,MediaDirectory mediaDir) {
		File nfoDir = mediaFile.getParentFile();
		// Check that the parent of the media file is not the rootMediaDir
		if (!rootMediaDir.equals(nfoDir)) {
			// Now check that their is one nfo file in the directory
			File files[] = nfoDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.toLowerCase().endsWith(".nfo");
				}
			});
			if (files.length==1) {
				File nfoFile = files[0];
				// Read the IMDB from the NFO file
				String imdbId = getIMDBIDFromFile(nfoFile);
				if (imdbId!=null) {
					// Look the film information up on the IMDB website
					ISource source = mediaDir.getSource("xbmc-metadata.imdb.com");
					if (source instanceof XBMCSource) {
						XBMCSource xbmcSource = (XBMCSource)source;
						try {
							Film film = xbmcSource.getFilm(imdbId, new URL("http://www.imdb.com/title/"+imdbId+"/"), mediaFile);
							String year = null;
							if (film.getDate()!=null) {
								year = YEAR_FORMAT.format(film.getDate());
							}

							StringBuilder fileName= new StringBuilder(mediaFile.getName());
							Integer part = SearchHelper.extractPart(fileName);

							SearchDetails details = new SearchDetails(film.getTitle(),year,part);
							return details;
						} catch (SourceException e) {
							e.printException();
							log.error("Unable to look up NFO file details on IMDB.com",e);
						} catch (MalformedURLException e) {
							log.error("Unable to look up NFO file details on IMDB.com",e);
						} catch (IOException e) {
							log.error("Unable to look up NFO file details on IMDB.com",e);
						}
					}
				}
			}
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
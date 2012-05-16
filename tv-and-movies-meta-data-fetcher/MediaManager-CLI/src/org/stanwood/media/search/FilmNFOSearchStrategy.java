package org.stanwood.media.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.model.Film;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.xbmc.XBMCSource;

/**
 * This search strategy looks information about films if they are in or under a directory contains a .NFO
 * file that describes them.
 */
public class FilmNFOSearchStrategy implements ISearchStrategy {

	private final static Pattern PATTERN_IMDB_URL = Pattern.compile(".*www\\.imdb\\..*/(tt\\d+).*"); //$NON-NLS-1$
	private final static Log log = LogFactory.getLog(FilmNFOSearchStrategy.class);
	private final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
	private final static Pattern PATTERN_PART_FOLDER = Pattern.compile("^CD(\\d+)$"); //$NON-NLS-1$

	/**
	 * Used to get parse the details used to perform a search
	 * @param mediaFile The media file that is been looked up
	 * @param mediaDir The media directory the media file is in
	 * @return The search details, or null if they could not be found
	 */
	public SearchDetails getSearch(File mediaFile, MediaDirectory mediaDir) {
		File nfoFile = NFOSearchHelper.findNFOfile(mediaDir,mediaFile);
		if (nfoFile!=null) {

			// Read the IMDB from the NFO file
			String imdbId = getIMDBIDFromFile(nfoFile);
			if (imdbId!=null) {
				// Look the film information up on the IMDB website
				ExtensionInfo<? extends ISource> info = mediaDir.getController().getSourceInfo(XBMCSource.class.getName()+"#metadata.imdb.com"); //$NON-NLS-1$
				if (info != null) {
					try {
						XBMCSource xbmcSource = (XBMCSource)info.getAnyExtension(mediaDir.getMediaDirConfig());
						log.info(MessageFormat.format(Messages.getString("FilmNFOSearchStrategy.LookingUpInfo"),mediaFile)); //$NON-NLS-1$
						Film film = xbmcSource.getFilm(imdbId, new URL("http://www.imdb.com/title/"+imdbId+"/"), mediaFile); //$NON-NLS-1$ //$NON-NLS-2$
						if (film!=null) {
							String year = null;
							if (film.getDate()!=null) {
								year = YEAR_FORMAT.format(film.getDate());
							}


							Integer part = getPart(nfoFile,mediaFile);

							SearchDetails details = new SearchDetails(film.getTitle(),year,part);
							return details;
						}
					} catch (MalformedURLException e) {
						log.error(Messages.getString("FilmNFOSearchStrategy.UNABLE_TO_LOOKUP_NFO_DETIALS"),e); //$NON-NLS-1$
					} catch (IOException e) {
						log.error(Messages.getString("FilmNFOSearchStrategy.UNABLE_TO_LOOKUP_NFO_DETIALS"),e); //$NON-NLS-1$
					} catch (ExtensionException e) {
						log.error(Messages.getString("FilmNFOSearchStrategy.UNABLE_TO_LOOKUP_NFO_DETIALS"),e); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

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
		return getSearch(mediaFile,mediaDir);
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
			log.error(MessageFormat.format(Messages.getString("FilmNFOSearchStrategy.UNABLE_READ_NFO"),nfoFile) ,e); //$NON-NLS-1$
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
				log.error(Messages.getString("FilmNFOSearchStrategy.UNABLE_CLOSE_STREAMS"),e); //$NON-NLS-1$
			}
		}
		return null;
	}


}
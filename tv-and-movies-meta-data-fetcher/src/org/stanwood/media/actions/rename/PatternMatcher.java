package org.stanwood.media.actions.rename;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.MediaDirConfig;

/**
 * This class is used to translate media directory patterns into filenames with the media
 */
public class PatternMatcher {

	private final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy"); //$NON-NLS-1$

	private String normalizeText(String text) {
		if (text==null) {
			return null;
		}
		text = text.replaceAll(":|/","-"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("!",".");  //$NON-NLS-1$//$NON-NLS-2$
		return text;
	}

	/**
	 * Get a file name for a film pattern
	 * @param dirConfig The media directory configuration where the film is to be located
	 * @param pattern The pattern
	 * @param film The film information
	 * @param ext The film extension
	 * @param part The part number of the film
	 * @return The filename
	 * @throws PatternException thrown if their is a problem
	 */
	public String getNewFilmName(MediaDirConfig dirConfig,String pattern,final Film film,final String ext,final Integer part) throws PatternException {
		PatternProcessor processor = new PatternProcessor() {
			@Override
			protected String processName(String name) {
				String value = processFilmName(name,film,ext,part);
				return value;
			}
		};

		return processor.doit(dirConfig,pattern);
	}

	private String processFilmName(String newName, Film film, String ext,Integer part) {
		newName = newName.replaceAll(Token.ID.getFull(), normalizeText(film.getId()));
		newName = newName.replaceAll(Token.PERCENT.getFull(), "%"); //$NON-NLS-1$
		newName = newName.replaceAll(Token.TITLE.getFull(), normalizeText(film.getTitle()));
		if (film.getSummary()!=null) {
			newName = newName.replaceAll(Token.SUMMARY.getFull(), normalizeText(film.getSummary()));
		}
		if (film.getImageURL()!=null) {
			newName = newName.replaceAll(Token.IMAGE.getFull(), normalizeText(film.getImageURL().toExternalForm()));
		}
		if (part!=null) {
			newName = newName.replaceAll(Token.PART.getFull(), String.valueOf(part));
		}
		newName = newName.replaceAll(Token.EXT.getFull(), normalizeText(ext));
		if (film.getDate()!=null) {
			newName = newName.replaceAll(Token.YEAR.getFull(),YEAR_FORMAT.format(film.getDate()));
		}
		return newName;
	}

	private String processTVShowName(String newName,Show show,Season season,Episode episode,String ext) {
		newName = newName.replaceAll(Token.ID.getFull(), normalizeText(show.getShowId()));
		newName = newName.replaceAll(Token.SEASON.getFull(), String.valueOf(season.getSeasonNumber()));
		String episodeNum = String.valueOf(episode.getEpisodeNumber());
		if (episodeNum.length()==1) {
			episodeNum = "0" +episodeNum; //$NON-NLS-1$
		}

		newName = newName.replaceAll(Token.EPISODE.getFull(), episodeNum);
		newName = newName.replaceAll(Token.PERCENT.getFull(), "%"); //$NON-NLS-1$
		newName = newName.replaceAll(Token.SHOW_NAME.getFull(), normalizeText(show.getName()));
		if (show.getShortSummary()!=null) {
			newName = newName.replaceAll(Token.SUMMARY.getFull(), normalizeText(show.getShortSummary()));
		}
		if (show.getImageURL()!=null) {
			newName = newName.replaceAll(Token.IMAGE.getFull(), normalizeText(show.getImageURL().toExternalForm()));
		}
		newName = newName.replaceAll(Token.TITLE.getFull(), normalizeText(episode.getTitle()));
		newName = newName.replaceAll(Token.EXT.getFull(), normalizeText(ext));
		if (episode.getDate()!=null) {
			newName = newName.replaceAll(Token.YEAR.getFull(),YEAR_FORMAT.format(episode.getDate()));
		}
		return newName;
	}

	/**
	 * Get a file name for a TV show pattern
	 * @param dirConfig The media directory configuration where the episode is to be located
	 * @param pattern The pattern
	 * @param episode The Episode information
	 * @param ext The film extension
	 * @return The filename
	 * @throws PatternException thrown if their is a problem
	 */
	public String getNewTVShowName(MediaDirConfig dirConfig,String pattern,final Episode episode,final String ext) throws PatternException {
		PatternProcessor processor = new PatternProcessor() {
			@Override
			protected String processName(String name) {
				Season season = episode.getSeason();
				Show show = season.getShow();
				String value = processTVShowName(name,show,season,episode,ext);
				return value;
			}
		};

		return processor.doit(dirConfig,pattern);
	}

	/**
	 * Used to check a pattern is valid
	 * @param pattern The pattern
	 * @return true if valid, otherwise false
	 */
	public static boolean validPattern(String pattern) {

		for (Token token : Token.values()) {
			pattern = pattern.replaceAll(token.getFull(), ""); //$NON-NLS-1$
		}
		return !pattern.contains("%"); //$NON-NLS-1$
	}

	private static abstract class PatternProcessor {

		public String doit(MediaDirConfig dirConfig,String pattern) throws PatternException {
			boolean inBrace = false;
			StringBuilder result = new StringBuilder();
			StringBuilder newName = new StringBuilder();
			for (int i=0;i<pattern.length();i++) {
				char c = pattern.charAt(i);
				if (c=='{') {
					inBrace = true;
					result.append(processName(newName.toString()));
					newName = new StringBuilder();
				}
				else if (c=='}') {
					inBrace = false;
					String value = processName(newName.toString());
					if (containsPattern(value)) {
						// Don't append it as it still contains patterns
					}
					else {
						 result.append(value);
					}
					newName = new StringBuilder();
				}
				else {
					newName.append(c);
				}
			}
			if (inBrace) {
				throw new PatternException("Mismatched braces in pattern "+pattern);
			}
			else {
				result.append(processName(newName.toString()));
			}

			return result.toString();

		}

		protected abstract String processName(String string);

		private boolean containsPattern(String newName) {
			for (Token token : Token.values() ) {
				if (newName.indexOf(token.getFull())!=-1) {
					return true;
				}
			}
			return false;
		}



	}
}

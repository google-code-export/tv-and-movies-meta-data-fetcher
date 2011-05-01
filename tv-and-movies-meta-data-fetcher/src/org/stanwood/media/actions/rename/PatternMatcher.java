package org.stanwood.media.actions.rename;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.MediaDirConfig;

public class PatternMatcher {

	/** the token for "show name" */
	public static final String TOKEN_SHOW_NAME = "%n";
	/** the token for "episode number" */
	public static final String TOKEN_EPISODE = "%e";
	/** the token for "season number" */
	public static final String TOKEN_SEASON = "%s";
	/** the token for "extension" */
	public static final String TOKEN_EXT = "%x";
	/** the token for "episode or film title" */
	public static final String TOKEN_TITLE = "%t";
	/** add a % char */
	public static final String TOKEN_PERCENT = "%%";
	/** the token for "show Id" */
	public static final String TOKEN_ID = "%h";
	/** the token for "part number" */
	public static final String TOKEN_PART = "%p";
	/** the token for the "year" */
	public static final String TOKEN_YEAR = "%y";

	private final static String TOKENS[] = {TOKEN_SHOW_NAME,TOKEN_EPISODE,TOKEN_SEASON,TOKEN_EXT,TOKEN_TITLE,TOKEN_PERCENT,TOKEN_ID,TOKEN_PART,TOKEN_YEAR};

	private final static DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

	private String normalizeText(String text) {
		text = text.replaceAll(":|/","-");
		text = text.replaceAll("!",".");
		return text;
	}

	public File getNewFilmName(MediaDirConfig dirConfig,final Film film,final String ext,final Integer part) throws PatternException {
		PatternProcessor processor = new PatternProcessor() {
			@Override
			protected String processName(String name) {
				String value = processFilmName(name,film,ext,part);
				return value;
			}
		};

		return processor.doit(dirConfig);
	}


	private String processFilmName(String newName, Film film, String ext,Integer part) {
		newName = newName.replaceAll(TOKEN_ID, normalizeText(film.getId()));
		newName = newName.replaceAll(TOKEN_PERCENT, "%");
		newName = newName.replaceAll(TOKEN_TITLE, normalizeText(film.getTitle()));
		if (part!=null) {
			newName = newName.replaceAll(TOKEN_PART, String.valueOf(part));
		}
		newName = newName.replaceAll(TOKEN_EXT, normalizeText(ext));
		if (film.getDate()!=null) {
			newName = newName.replaceAll(TOKEN_YEAR,YEAR_FORMAT.format(film.getDate()));
		}
		return newName;
	}

	private String processTVShowName(String newName,Show show,Season season,Episode episode,String ext) {
		newName = newName.replaceAll(TOKEN_ID, normalizeText(show.getShowId()));
		newName = newName.replaceAll(TOKEN_SEASON, String.valueOf(season.getSeasonNumber()));
		String episodeNum = String.valueOf(episode.getEpisodeNumber());
		if (episodeNum.length()==1) {
			episodeNum = "0" +episodeNum;
		}

		newName = newName.replaceAll(TOKEN_EPISODE, episodeNum);
		newName = newName.replaceAll(TOKEN_PERCENT, "%");
		newName = newName.replaceAll(TOKEN_SHOW_NAME, normalizeText(show.getName()));
		newName = newName.replaceAll(TOKEN_TITLE, normalizeText(episode.getTitle()));
		newName = newName.replaceAll(TOKEN_EXT, normalizeText(ext));
		if (episode.getDate()!=null) {
			newName = newName.replaceAll(TOKEN_YEAR,YEAR_FORMAT.format(episode.getDate()));
		}
		return newName;
	}

	public File getNewTVShowName(MediaDirConfig dirConfig,final Show show,final Season season,final Episode episode,final String ext) throws PatternException {
		PatternProcessor processor = new PatternProcessor() {
			@Override
			protected String processName(String name) {
				String value = processTVShowName(name,show,season,episode,ext);
				return value;
			}
		};

		return processor.doit(dirConfig);
	}

	/**
	 * Used to check a pattern is valid
	 * @param pattern The pattern
	 * @return true if valid, otherwise false
	 */
	public static boolean validPattern(String pattern) {

		for (String token : TOKENS) {
			pattern = pattern.replaceAll(token, "");
		}
		return !pattern.contains("%");
	}


	private static abstract class PatternProcessor {

		public File doit(MediaDirConfig dirConfig) throws PatternException {
			String pattern = dirConfig.getPattern();
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

			File path = getPath(dirConfig,result.toString());
			return path;
		}

		protected abstract String processName(String string);

		private boolean containsPattern(String newName) {
			for (String token : TOKENS ) {
				if (newName.indexOf(token)!=-1) {
					return true;
				}
			}
			return false;
		}

		private File getPath(MediaDirConfig dirConfig,String newName) {
			File dir = dirConfig.getMediaDir();
			StringTokenizer tok = new StringTokenizer(newName,""+File.separatorChar);
			while (tok.hasMoreTokens()) {
				dir = new File(dir,tok.nextToken());
			}
			return dir;
		}

	}
}

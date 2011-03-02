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
package org.stanwood.media.renamer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;

/**
 * This class is used to handle the renaming of files. It provides a main method
 * so that the Renamer class can be access from the command line.
 */
public class Main extends AbstractLauncher {

	@SuppressWarnings("unused")
	private final static Log log = LogFactory.getLog(Main.class);

	private final static String DEFAULT_TV_FILE_PATTERN = "%s %e - %t.%x";
	private final static String DEFAULT_FILM_FILE_PATTERN = "%t.%x";
	private final static String VALID_EXTS[] = new String[] { "avi","mkv","mov","jpg","mpg","mp4","m4a","m4v","srt","sub","divx" };

	private final static String HELP_OPTION = "h";
	private final static String SHOWID_OPTION = "s";
	private final static String ROOT_MEDIA_DIR_OPTION = "d";
	private final static String RENAME_PATTERN = "p";
	private final static String SOURCE_ID_OPTION = "o";
	private final static String REFRESH_STORE_OPTION = "r";
	private final static String MODE_OPTION = "m";
	private final static String RECURSIVE_OPTION = "R";
	private static final List<Option> OPTIONS;

	private String showId = null;
	private String sourceId = null;
	private File rootMediaDirectory = new File(System.getProperty("user.dir"));
	private String pattern = null;
	private boolean refresh = false;

	private Mode mode = null;
	/* package for test */ static IExitHandler exitHandler = null;

	private static boolean recursive = false;

	static {
		OPTIONS = new ArrayList<Option>();
		Option o = new Option(SHOWID_OPTION,"showid",true,"The ID of the show. If not present, then it will search for the show id.");
		o.setArgName("showid");
		OPTIONS.add(o);

		o = new Option(ROOT_MEDIA_DIR_OPTION, "dir",true,"The directory to look for media. If not present use the current directory.");
		OPTIONS.add(o);
		o = new Option(RENAME_PATTERN, "pattern",true,"The pattern used to rename files. Defaults to \"%s %e - %t.%x\" if not present.");
		OPTIONS.add(o);
		o = new Option(SOURCE_ID_OPTION, "source",true,"The id if the source too look up meta data in. Defaults too tvcom if not present.");
		OPTIONS.add(o);
		o = new Option(REFRESH_STORE_OPTION, "refresh",false,"If this option is present, it will make the stores get regenerated from source.");
		OPTIONS.add(o);
		o = new Option(MODE_OPTION,"mode",true,"The mode that the tool will work in. Either FILM or TV.");
		OPTIONS.add(o);
		o = new Option(RECURSIVE_OPTION,"recursive",false,"Also process subdirectories");
		OPTIONS.add(o);
	}

	/**
	 * The main method used to rename files. It looks for files in a directory,
	 * works out the correct episode numbers and season numbers, then renames then
	 * too the correct name.
	 *
	 * The following command line syntax is passed too this method:
	 * <pre>
	 * media-renamer [-h| [OPTIONS]...]
	 *
     * -d, --dir         The directory to look for media. If not present use the current directory
     * -s, --showid      The ID of the show that episodes in the media directory belong to. Only usable with TV mode.
     * -p, --pattern     The pattern used to rename files. Defaults to "%s %e - %t.%x" if not present.
     * -o, --source      The ID of the source that should be used to lookup information.
     * -r, --refresh     If this option is present, it will make the stores get regenerated from source.
     * -c, --config_file The location of the configuration file. If not present, attempts to load it from /etc/mediafetcher-conf.xml
     * -m, --mode        The mode that the tool will work in. Either FILM or TV.
     * -l, --log_config  The log configuration mode [<INFO>|<DEBUG>|<log4j configuration file>], defaults to INFO
     * -h, --help        Show the help
	 * </pre>
	 *
	 * The pattern is used to work out what the format of the renamed file should be. See
	 * @see org.stanwood.media.renamer.Renamer for more information on the pattern.
	 *
	 * When looking up files to rename, it needs to work out the episode and season number
	 * of each file. See @see org.stanwood.media.renamer.FileNameParser for more information
	 * on the patterns it matches todo this.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			exitHandler = new DefaultExitHandler();
		}

		Main ca = new Main(exitHandler);
		ca.launch(args);
	}


	private Main(IExitHandler exitHandler) {
		super("media-renamer",OPTIONS,exitHandler);
	}


	/**
	 * This does the actual work of the tool.
	 * @return true if successful, otherwise false.
	 */
	@Override
	protected boolean run() {
		Renamer renamer = new Renamer(getController(),showId,mode, rootMediaDirectory, pattern,VALID_EXTS,refresh,recursive);
		try {
			renamer.tidyShowNames();
			return true;
		} catch (MalformedURLException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		} catch (SourceException e) {
			log.error(e.getMessage(),e);
		} catch (StoreException e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

	/**
	 * Used to check the CLI options are valid
	 * @param cmd The CLI options
	 * @return true if valid, otherwise false.
	 */
	@Override
	protected boolean processOptions(CommandLine cmd) {
		showId = null;
		sourceId = null;
		rootMediaDirectory = new File(System.getProperty("user.dir"));
		pattern = null;
		refresh = false;
		mode = null;

		try {
			if (cmd.hasOption(MODE_OPTION) && cmd.getOptionValue(MODE_OPTION)!=null) {
				String cliMode = cmd.getOptionValue(MODE_OPTION);
				if (cliMode.toLowerCase().equals("film")) {
					mode = Mode.FILM;
				}
				else if (cliMode.toLowerCase().equals("tv")) {
					mode = Mode.TV_SHOW;
				}
				else {
					fatal("Unkown rename mode: " + cliMode);
					return false;
				}
			}


			if (cmd.hasOption(SOURCE_ID_OPTION) && cmd.getOptionValue(SOURCE_ID_OPTION)!=null) {
				sourceId = cmd.getOptionValue(SOURCE_ID_OPTION);
			}

			if (cmd.hasOption(ROOT_MEDIA_DIR_OPTION) && cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION) != null) {
				File dir = new File(cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION));
				if (dir.isDirectory() && dir.canWrite()) {
					rootMediaDirectory = dir;
				} else {
					fatal("Show directory must be a writable directory");
					return false;
				}
			}
			if (rootMediaDirectory==null || !rootMediaDirectory.exists()) {
				fatal("Show directory '" + rootMediaDirectory +"' does not exist.");
				return false;
			}

			if (cmd.hasOption(SHOWID_OPTION)
					&& cmd.getOptionValue(SHOWID_OPTION) != null) {
				if (mode==Mode.FILM) {
					fatal("Show id is not a valid option when used with Film mode");
					return false;
				}
				try {
					showId = cmd.getOptionValue(SHOWID_OPTION);
				} catch (NumberFormatException e) {
					fatal("Invalid command line parameters");
					return false;
				}
			}

			if (cmd.hasOption(RENAME_PATTERN) && cmd.getOptionValue(RENAME_PATTERN) != null) {
				pattern = cmd.getOptionValue(RENAME_PATTERN);
			}

			refresh = (cmd.hasOption(REFRESH_STORE_OPTION));
			recursive = (cmd.hasOption(RECURSIVE_OPTION));

			if (mode == null) {
				if (cmd.hasOption(SHOWID_OPTION)) {
					mode = Mode.TV_SHOW;
				}
				else {
					mode = getDefaultMode();
				}
			}

			if (showId==null) {
				if (mode==Mode.TV_SHOW) {
					info("No id given, will search for id");
				}
			}
			else {
				info("Using show id " + sourceId+":" + showId);
			}

			if (sourceId==null) {
				sourceId = getController().getDefaultSourceID(mode);
			}

			if (pattern==null) {
				if (mode==Mode.TV_SHOW) {
					pattern = DEFAULT_TV_FILE_PATTERN;
				}
				else {
					pattern = DEFAULT_FILM_FILE_PATTERN;
				}
			}

			return true;
		}
		catch (SourceException e) {
			log.error(e.getMessage(),e);
			return false;
		}
	}

	private Mode getDefaultMode() {
		StringTokenizer tok = new StringTokenizer(rootMediaDirectory.getAbsolutePath(),File.separator);
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken().toLowerCase();
			if (token.equals("films") || token.equals("movies")) {
				return Mode.FILM;
			}
		}
		return Mode.TV_SHOW;
	}
}

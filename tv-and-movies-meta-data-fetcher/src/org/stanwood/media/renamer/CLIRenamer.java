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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;

/**
 * This class is used to handle the renaming of files. It provides a main method
 * so that the Renamer class can be access from the command line.
 */
public class CLIRenamer extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIRenamer.class);

	private final static String VALID_EXTS[] = new String[] { "avi","mkv","mov","jpg","mpg","mp4","m4a","m4v","srt","sub","divx" };

	private final static String ROOT_MEDIA_DIR_OPTION = "d";
	private final static String REFRESH_STORE_OPTION = "r";
	private final static String RECURSIVE_OPTION = "R";
	private static final List<Option> OPTIONS;

	private boolean refresh = false;
	private boolean recursive = false;
	private MediaDirConfig rootMediaDirConfig = null;

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;


	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(ROOT_MEDIA_DIR_OPTION, "dir",true,"The directory to look for media. If not present use the current directory.");
		o.setRequired(true);
		o.setArgName("directory");
		OPTIONS.add(o);

		o = new Option(REFRESH_STORE_OPTION, "refresh",false,"If this option is present, it will make the stores get regenerated from source.");
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
			setExitHandler(new DefaultExitHandler());
		}

		CLIRenamer ca = new CLIRenamer(exitHandler);
		ca.launch(args);
	}


	private CLIRenamer(IExitHandler exitHandler) {
		super("media-renamer",OPTIONS,exitHandler,stdout,stderr);
	}


	/**
	 * This does the actual work of the tool.
	 * @return true if successful, otherwise false.
	 */
	@Override
	protected boolean run() {
		Renamer renamer = new Renamer(getController(),rootMediaDirConfig,VALID_EXTS,refresh,recursive);
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
		refresh = false;
		rootMediaDirConfig = null;

		if (cmd.hasOption(ROOT_MEDIA_DIR_OPTION) && cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION) != null) {
			File dir = new File(cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION));
			if (dir.isDirectory() && dir.canWrite()) {
				try {
					rootMediaDirConfig = getController().init(dir);
				} catch (ConfigException e) {
					fatal(e);
					return false;
				}
			} else {
				fatal("Media directory must be a writable directory");
				return false;
			}
			if (rootMediaDirConfig==null || !rootMediaDirConfig.getMediaDir().exists()) {
				fatal("Media directory '" + dir +"' does not exist.");
				return false;
			}
		}

		refresh = (cmd.hasOption(REFRESH_STORE_OPTION));
		recursive = (cmd.hasOption(RECURSIVE_OPTION));

		return true;
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}

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
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.stanwood.media.model.Mode;
import org.stanwood.media.renamer.logging.LogSetupHelper;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.IMDBSource;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.TVCOMSource;
import org.stanwood.media.store.StoreException;

/**
 * This class is used to handle the renaming of files. It provides a main method
 * so that the Renamer class can be access from the command line.
 */
public class Main {

	private final static String DEFAULT_TV_FILE_PATTERN = "%s %e - %t.%x";
	private final static String DEFAULT_FILM_FILE_PATTERN = "%t.%x";
	private final static String VALID_EXTS[] = new String[] { "avi","mkv","mov","jpg","mpg","mp4","m4a","m4v" };

	private final static String HELP_OPTION = "h"; 
	private final static String SHOWID_OPTION = "s";
	private final static String SHOW_DIR_OPTION = "d";
	private final static String RENAME_PATTERN = "p";
	private final static String SOURCE_ID_OPTION = "o";
	private final static String REFRESH_STORE_OPTION = "r";
	private final static String CONFIG_FILE_OPTION = "c";
	private final static String MODE_OPTION = "m";
	private final static String LOG_CONFIG_OPTION = "l";
	private static final Options OPTIONS;

	private static String showId = null;
	private static String sourceId = null;
	private static File showDirectory = new File(System.getProperty("user.dir"));
	private static String pattern = null;
	private static boolean refresh = false;
	private static File configFile = new File(File.separator+"etc"+File.separator+"mediafetcher-conf.xml");
	private static Mode mode = null;
	/* package for test */ static IExitHandler exitHandler = null;
	/* package for test */ static boolean doInit = true;
	
	
	static {
		OPTIONS = new Options();
		OPTIONS.addOption(new Option(HELP_OPTION,"help",false,"Show the help"));
		OPTIONS.addOption(new Option(SHOWID_OPTION, "showid", true, "The ID of the show. If not present, then it will search for the show id."));
		OPTIONS.addOption(new Option(SHOW_DIR_OPTION, "dir",true,"The directory to look for media. If not present use the current directory."));
		OPTIONS.addOption(new Option(RENAME_PATTERN, "pattern",true,"The pattern used to rename files. Defaults to \"%s %e - %t.%x\" if not present."));
		OPTIONS.addOption(new Option(SOURCE_ID_OPTION, "source",true,"The id if the source too look up meta data in. Defaults too tvcom if not present."));
		OPTIONS.addOption(new Option(REFRESH_STORE_OPTION, "refresh",false,"If this option is present, it will make the stores get regenerated from source."));
		OPTIONS.addOption(new Option(CONFIG_FILE_OPTION,"config_file",true,"The location of the config file. If not present, attempts to load it from /etc/mediafetcher-conf.xml"));
		OPTIONS.addOption(new Option(MODE_OPTION,"mode",true,"The mode that the tool will work in. Either FILM or TV."));
		OPTIONS.addOption(new Option(LOG_CONFIG_OPTION,"log_config",true,"The log config mode [<INFO>|<DEBUG>|<log4j config file>]"));
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
     * -c, --config_file The location of the config file. If not present, attempts to load it from /etc/mediafetcher-conf.xml
     * -m, --mode        The mode that the tool will work in. Either FILM or TV.
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
		showId = null;
		sourceId = null;
		showDirectory = new File(System.getProperty("user.dir"));
		pattern = null;
		refresh = false;
		configFile = new File(File.separator+"etc"+File.separator+"mediafetcher-conf.xml");
		mode = null;
		
		if (exitHandler==null) {
			exitHandler = new IExitHandler() {
				@Override
				public void exit(int exitCode) {
					System.exit(exitCode);
				}
				
			};
		}
		
		// create Options object
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(OPTIONS, args);

			if (cmd.hasOption(HELP_OPTION)) {
				displayHelp();
				doExit(0);
				return;
			} else if (processOptions(cmd)) {
				if (run()) {
					doExit(0);
					return;
				}
				else {
					doExit(0);
					return;
				}
			} else {
				fatal("Invalid command line parameters");
				return;
			}
		} catch (ParseException e1) {
			fatal(e1.getMessage());
			return;
		} catch (ConfigException e) {
			fatal(e.getMessage());
			return;
		}
	}

	private static boolean run() {
		Renamer renamer = new Renamer(showId,mode, showDirectory, pattern,VALID_EXTS,refresh);
		try {
			renamer.tidyShowNames();
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SourceException e) {
			e.printStackTrace();
		} catch (StoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean processOptions(CommandLine cmd) throws ConfigException {
		String logConfig = null;
		if (cmd.hasOption(LOG_CONFIG_OPTION)) {
			logConfig = cmd.getOptionValue(LOG_CONFIG_OPTION);
		}
		if (!initLogging(logConfig)) {
			return false;
		}
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
				
		if (cmd.hasOption(CONFIG_FILE_OPTION) && cmd.getOptionValue(CONFIG_FILE_OPTION) != null) {
			configFile = new File(cmd.getOptionValue(CONFIG_FILE_OPTION)); 
		}
		
		if (configFile==null || !configFile.exists()) {
			warn("Unable to find config file '" +configFile+"' so using defaults.");
			if (doInit) {
				Controller.initWithDefaults();
			}
		}
		else {
			ConfigReader reader = new ConfigReader(configFile);
			reader.parse();
			if (doInit) {
				Controller.initFromConfigFile(reader);
			}
		}
		
		if (cmd.hasOption(SOURCE_ID_OPTION) && cmd.getOptionValue(SOURCE_ID_OPTION)!=null) { 
			sourceId = cmd.getOptionValue(SOURCE_ID_OPTION);
		}		
		
		if (cmd.hasOption(SHOW_DIR_OPTION)
				&& cmd.getOptionValue(SHOW_DIR_OPTION) != null) {
			File dir = new File(cmd.getOptionValue(SHOW_DIR_OPTION));
			if (dir.isDirectory() && dir.canWrite()) {
				showDirectory = dir;
			} else {
				fatal("Show directory must be a writable directory");
				return false;
			}					
		}
		if (showDirectory==null || !showDirectory.exists()) {
			fatal("Show directory '" + showDirectory +"' does not exist.");
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
			if (mode==Mode.TV_SHOW) {
				sourceId = TVCOMSource.SOURCE_ID;
			}
			else {
				sourceId = IMDBSource.SOURCE_ID;
			}
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

	private static boolean initLogging(String logConfig) {
		if (logConfig!=null) {			
			if (logConfig.toLowerCase().equals("info")) {
				LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
			}
			else if (logConfig.toLowerCase().equals("debug")) {
				LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
			}
			else {
				File logConfigFile = new File(logConfig);
				if (logConfigFile.exists()) {
					LogSetupHelper.initLogingFromConfigFile(logConfigFile);
				}
				else {
					fatal("Unable to find log configuraion file " + logConfigFile.getAbsolutePath());
					return false;
				}
				
			}
		}
		else {
			LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		}
		
		return true;
	}

	private static Mode getDefaultMode() {
		StringTokenizer tok = new StringTokenizer(showDirectory.getAbsolutePath(),File.separator);		
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken().toLowerCase();
			if (token.equals("films") || token.equals("movies")) {
				return Mode.FILM;
			}			
		}
		return Mode.TV_SHOW;
	}

	

	@SuppressWarnings("unchecked")
	private static void displayHelp() {
		info("media-renamer [-"+HELP_OPTION+"|-"+SHOWID_OPTION+"=<showid> [OPTIONS]...]\n");		
		
		for (Option option : (Collection<Option>)OPTIONS.getOptions()) {
			String opt = "-"+option.getOpt()+", --" + option.getLongOpt();
			while (opt.length()<15) {
				opt+=" ";
			}
			info(opt+ option.getDescription());
		}		
	}

	/**
	 * This will exit the application
	 * @param code The exit code
	 */
	public static void doExit(int code) {
		exitHandler.exit(code);
	}

	private static void warn(String msg) {
		System.out.println("WARN: "+msg);
	}
	
	private static void fatal(String msg) {
		System.err.println("FATAL: "+msg);
		displayHelp();
		doExit(1);		
	}
	
	private static void info(String msg) {
		System.out.println(msg);
	}
	
}

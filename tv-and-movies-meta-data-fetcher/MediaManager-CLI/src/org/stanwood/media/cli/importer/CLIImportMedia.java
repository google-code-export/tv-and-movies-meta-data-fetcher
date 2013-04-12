package org.stanwood.media.cli.importer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.cli.manager.Messages;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.server.commands.ICommandLogger;
import org.stanwood.media.server.commands.ImportMediaCommand;
import org.stanwood.media.server.commands.XBMCUpdateAddonsCommand;
import org.stanwood.media.setup.ConfigException;

/**
 * <p>
 * The mm-import-media command is used import media form watched media directories.
 * </p>
 * <p>
 * It has the following usage:
 * <code>
 * usage: mm-import-media [-a] [-c <file>] [-d] [-e] [-h] [-l <info|debug|file>] [-t] [-u] [-v]
 *
 * --version, -v                      Display the version
 * --dontUseDefaults, -d              Don't use default media directories.
 * --noupdate, -u                     If this option is present, then the XBMC addons won''t be updated
 * --deleteNonMedia, -e               Delete files are that are not media files (use with care)
 * --test, -t                         If this option is present, then no changes are performed.
 * --config_file, -c <file>           The location of the config file. If not present, attempts to load it from ~/.mediaManager/mediamanager-conf.xml or /etc/mediamanager-conf.xml
 * --actions, -a                      Execute actions on new media files
 * --log_config, -l <info|debug|file> The log config mode [<INFO>|<DEBUG>|<log4j config file>]
 * --help, -h                         Show the help
 * </code>
 */
public class CLIImportMedia extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIImportMedia.class);

	private final static String TEST_OPTION = "t"; //$NON-NLS-1$
	private final static String USE_DEFAULT_OPTION = "d"; //$NON-NLS-1$
	private final static String DELETE_NON_MEDIA_OPTION = "e"; //$NON-NLS-1$
	private final static String ACTIONS_OPTION = "a"; //$NON-NLS-1$
	private static final List<Option> OPTIONS;
	private static final String NOUPDATE_OPTION = "u"; //$NON-NLS-1$

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;

	private XBMCUpdateAddonsCommand updateCommand;
	private ImportMediaCommand importMediaCommand;

	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(TEST_OPTION,"test",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_TEST_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_NOUPDATE_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(USE_DEFAULT_OPTION,"dontUseDefaults",false,Messages.getString("CLIImportMedia.DONT_USE_DEFAULT_MEDIA_DIRS")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(DELETE_NON_MEDIA_OPTION,"deleteNonMedia",false,Messages.getString("CLIImportMedia.DELETE_NON_MEDIA")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(ACTIONS_OPTION,"actions",false,Messages.getString("CLIImportMedia.EXECUTE_ACTIONS")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);
	}

	/**
	 * The entry point to the application. For details see the class documentation.
	 *
	 * @param args The arguments.
	 */

	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIImportMedia ca = new CLIImportMedia(exitHandler);
		ca.launch(args);
	}


	private CLIImportMedia(IExitHandler exitHandler) {
		super("mm-import-media",OPTIONS,exitHandler,stdout,stderr); //$NON-NLS-1$
	}

	/**
	 * This does the actual work of the tool.
	 * @return true if successful, otherwise false.
	 */
	@Override
	protected boolean run() {
		ICommandLogger logger = new CLICommandLogger(log);
		if (updateCommand!=null) {
			if (updateCommand.execute(logger, new NullProgressMonitor())==null) {
				return false;
			}
		}
		return importMediaCommand.execute(logger, new NullProgressMonitor())==null;
	}


	/**
	 * Used to check the CLI options are valid
	 * @param cmd The CLI options
	 * @return true if valid, otherwise false.
	 */
	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {
		try {
			getController().init(cmd.hasOption(TEST_OPTION));
		} catch (ConfigException e) {
			fatal(e);
			return false;
		}

		updateCommand = new XBMCUpdateAddonsCommand(getController());
		if (cmd.hasOption(NOUPDATE_OPTION)) {
			updateCommand = null;
		}
		importMediaCommand = new ImportMediaCommand(getController());
		if (cmd.hasOption(USE_DEFAULT_OPTION)) {
			importMediaCommand.setUseDefaults(false);
		}

		if (cmd.hasOption(DELETE_NON_MEDIA_OPTION)) {
			importMediaCommand.setDeleteNonMedia(true);
		}

		if (cmd.hasOption(ACTIONS_OPTION)) {
			importMediaCommand.setExecuteActions(true);
		}
		return true;
	}

	protected String getPrintArguments() {
		return Messages.getString("CLICopyToMediaDir.MEDIA_FILES"); //$NON-NLS-1$
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}

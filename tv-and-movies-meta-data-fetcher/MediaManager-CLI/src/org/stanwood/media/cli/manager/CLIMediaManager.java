package org.stanwood.media.cli.manager;

import java.io.File;
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
import org.stanwood.media.cli.importer.CLICommandLogger;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.server.commands.ICommandLogger;
import org.stanwood.media.server.commands.ManageMediaCommand;
import org.stanwood.media.server.commands.XBMCUpdateAddonsCommand;
import org.stanwood.media.setup.ConfigException;

/**
 * <p>
 * This is a command line launcher that is used to managed a media directory. It reads
 * the configuration file to work out which sources, stores and actions are to be used
 * with media directory. Then the actions are performed on the media directory.
 * </p>
 * <p>
 * It has the following usage:
 * <code>
 *  usage: mm-manager [-c <file>] -d <directory> [-h] [-l <info|debug|file>] [-t] [-u] [-v]
 *
 *  --version, -v                 Display the version
 *  --noupdate, -u                If this option is present, then the XBMC addons won't be updated
 *  --dir, -d <directory>         The directory to look for media. If not present use the current directory.
 *  --test, -t                    If this option is present, then no changes are performed.
 *  --config_file, -c <info|debug|file>
 *                                The location of the config file. If not present, attempts to load it from /etc/mediamanager-conf.xml
 *  --log_config, -l <file>       The log config mode [<INFO>|<DEBUG>|<log4j config file>]
 *  --help, -h                    Show the help
 * </code>
 * </p>
 */
public class CLIMediaManager extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIMediaManager.class);

	private final static String ROOT_MEDIA_DIR_OPTION = "d"; //$NON-NLS-1$
	private final static String TEST_OPTION = "t"; //$NON-NLS-1$

	private static final List<Option> OPTIONS;

	private static final String NOUPDATE_OPTION = "u"; //$NON-NLS-1$

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;

	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(ROOT_MEDIA_DIR_OPTION, "dir",true,Messages.getString("CLIMediaManager.CLI_MEDIA_DIR_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(true);
		o.setArgName("directory"); //$NON-NLS-1$
		OPTIONS.add(o);

		o = new Option(TEST_OPTION,"test",false,Messages.getString("CLIMediaManager.CLI_TEST_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,Messages.getString("CLIMediaManager.CLI_NOUPDATE_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);
	}

	/**
	 * The entry point
	 * <p>
	 * It has the following usage:
	 * <code>
	 *  usage: mm-manager [-c <info|debug|file>] -d <directory> [-h] [-l <file>] [-t] [-u]
	 *
	 *  --noupdate, -u                If this option is present, then the XBMC addons won't be updated
	 *  --dir, -d <directory>         The directory to look for media. If not present use the current directory.
	 *  --test, -t                    If this option is present, then no changes are performed.
	 *  --config_file, -c <info|debug|file>
	 *                                The location of the config file. If not present, attempts to load it from /etc/mediamanager-conf.xml
	 *  --log_config, -l <file>       The log config mode [<INFO>|<DEBUG>|<log4j config file>]
	 *  --help, -h                    Show the help
	 * </code>
	 * </p>
	 * @param args The arguments
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIMediaManager ca = new CLIMediaManager(exitHandler);
		ca.launch(args);
	}



	private XBMCUpdateAddonsCommand updateCommand;

	private ManageMediaCommand manageMediaCommand;


	private CLIMediaManager(IExitHandler exitHandler) {
		super("mm-manager",OPTIONS,exitHandler,stdout,stderr); //$NON-NLS-1$
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
		return manageMediaCommand.execute(logger, new NullProgressMonitor())!=null;
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
		manageMediaCommand = new ManageMediaCommand(getController());
		if (cmd.hasOption(ROOT_MEDIA_DIR_OPTION) && cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION) != null) {
			File dir = new File(cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION));
			List<File>mediaDirs = new ArrayList<File>();
			mediaDirs.add(dir);
			manageMediaCommand.setMediaDirectories(mediaDirs);
		}

		return true;
	}



	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}

package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.cli.ICLICommand;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.cli.importer.CLICommandLogger;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.server.commands.XBMCInstallAddonsCommand;

/**
 * This is a sub command of the {@link CLIManageAddons} command. It is used
 * to install xmbc addons. It takes the addons to install as arguments. It has the
 * following usage:
 * <code>
 * usage: mm-xbmc [--global-options] install [--command-options] <addon id>...
 *
 * Command Options:
 *   --help, -h                    Show the help
 * </code>
 */
public class InstallCommand extends AbstractXBMCSubCommand {

	private final static Log log = LogFactory.getLog(InstallCommand.class);

	private final static String NAME = "install"; //$NON-NLS-1$
	private final static String DESCRIPTION = Messages.getString("InstallCommand.INSTALL_NEW"); //$NON-NLS-1$
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	private XBMCInstallAddonsCommand command;

	/**
	 * The constructor
	 * @param rootCommand The root command
	 * @param exitHandler The exit handler
	 * @param stdout The output stream
	 * @param stderr The error stream
	 */
	public InstallCommand(ICLICommand rootCommand,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(rootCommand,NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		return command.execute(new CLICommandLogger(log), new NullProgressMonitor())!=null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {
		command = new XBMCInstallAddonsCommand(getController());
		command.setAddons(cmd.getArgList());
		return true;
	}

	@Override
	protected String getPrintArguments() {
		return " "+Messages.getString("InstallCommand.PLUGINS"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}

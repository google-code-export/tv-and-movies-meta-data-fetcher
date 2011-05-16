package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.stanwood.media.cli.ICLICommand;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.AddonDetails;
import org.stanwood.media.util.TextTable;
import org.stanwood.media.util.Version;

/**
 * This is a sub command of the {@link CLIManageAddons} command. It is used
 * to list XBMC addons. It has the following usage:
 * <code>
 * usage: mm-xbmc [--global-options] list [--command-options]
 *
 * Command Options:
 *   --help, -h                    Show the help
 * </code>
 */
public class ListCommand extends AbstractXBMCSubCommand {

	private final static String NAME = "list";
	private final static String DESCRIPTION = "lists the installed XBMC addons";
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	/**
	 * The constructor
	 * @param rootCommand The root command
	 * @param exitHandler The exit handler
	 * @param stdout The output stream
	 * @param stderr The error stream
	 */
	public ListCommand(ICLICommand rootCommand,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(rootCommand,NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		try {
			PrintStream stdout = getStdout();
			info("XBMC Addon list:");
			TextTable table = new TextTable(new String[] {"ID","Status","Installed Version","Avaliable Version"});
			for (AddonDetails ad : getUpdater().listAddons(getConsole())) {
				String installedVer = displayVersion(ad.getInstalledVersion());
				String avaliableVer = displayVersion(ad.getAvaliableVersion());
				table.addRow(new String[]{ad.getId(),ad.getStatus().getDisplayName(),installedVer,avaliableVer});
			}
			StringBuilder buffer = new StringBuilder();
			table.printTable(buffer);
			stdout.print(buffer.toString());
		} catch (XBMCUpdaterException e) {
			fatal(e);
			return false;
		}
		return true;
	}

	private String displayVersion(Version version) {
		if (version==null) {
			return "";
		}

		return version.toString();
	}

	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {
		return checkNoArgs(cmd);
	}
}

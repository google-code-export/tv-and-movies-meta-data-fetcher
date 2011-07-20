package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.stanwood.media.cli.ICLICommand;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.AddonDetails;

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

	private final static String NAME = "install"; //$NON-NLS-1$
	private final static String DESCRIPTION = Messages.getString("InstallCommand.INSTALL_NEW"); //$NON-NLS-1$
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	private Set<String> plugins;

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
		try {
			if (plugins.size()>0) {
				getUpdater().installAddons(getConsole(), plugins);
			}
			else {
				fatal(Messages.getString("InstallCommand.MISSING_ARG")); //$NON-NLS-1$
				return false;
			}
		} catch (XBMCException e) {
			fatal(e.getMessage());
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {
		plugins = new HashSet<String>();
		try {
			Set<AddonDetails> addons = getUpdater().listAddons(getConsole());
			for (String arg : (List<String>)cmd.getArgList()) {
				if (!checkAddon(plugins,arg,addons)) {
					return false;
				}
			}
		} catch (XBMCUpdaterException e) {
			fatal(e.getMessage());
			return false;
		}

		return true;
	}

	private boolean checkAddon(Set<String>plugins,String addonId, Set<AddonDetails> addons) {
		for (AddonDetails addon : addons) {
			if (addon.getId().equals(addonId)) {
				switch (addon.getStatus()) {
					case INSTALLED:
						fatal(MessageFormat.format(Messages.getString("InstallCommand.ADDON_ALREADY_INSTALLED"),addonId)); //$NON-NLS-1$
						return false;
					case NOT_INSTALLED:
						plugins.add(addonId);
						return true;
					case OUT_OF_DATE:
						fatal(MessageFormat.format(Messages.getString("InstallCommand.ADDON_ALREADY_INSTALLED"),addonId)); //$NON-NLS-1$
						return false;
				}
			}
		}
		fatal(MessageFormat.format(Messages.getString("InstallCommand.UNABLE_FIND_ADDON"),addonId)); //$NON-NLS-1$
		return false;
	}

	@Override
	protected String getPrintArguments() {
		return " "+Messages.getString("InstallCommand.PLUGINS"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}

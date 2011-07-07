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
 * to update xmbc addons. It takes the addons to update as arguments. If none
 * are given, then all addons are updated. It also checks the addons they
 * depend on to see if they need updating. It has the following usage:
 * <code>
 * usage: mm-xbmc [--global-options] update [--command-options] [<addon id>...]
 *
 * Command Options:
 *   --help, -h                    Show the help
 * </code>
 */
public class UpdateCommand extends AbstractXBMCSubCommand {

	private final static String NAME = "update"; //$NON-NLS-1$
	private final static String DESCRIPTION = Messages.getString("UpdateCommand.DESC"); //$NON-NLS-1$
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	private Set<String> plugins;
	private boolean pluginsSpecified;

	/**
	 * The constructor
	 * @param rootCommand The root command
	 * @param exitHandler The exit handler
	 * @param stdout The output stream
	 * @param stderr The error stream
	 */
	public UpdateCommand(ICLICommand rootCommand,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(rootCommand,NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		try {
			if (pluginsSpecified) {
				getUpdater().update(getConsole(),plugins);
			}
			else {
				getUpdater().update(getConsole());
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
		pluginsSpecified = cmd.getArgList() != null && cmd.getArgList().size()>0;
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
						warn(MessageFormat.format(Messages.getString("UpdateCommand.INSTALLED_MSG"),addonId)); //$NON-NLS-1$
						return true;
					case NOT_INSTALLED:
						fatal(MessageFormat.format(Messages.getString("UpdateCommand.NOT_INSTALLED_MSG"),addonId)); //$NON-NLS-1$
						return false;
					case OUT_OF_DATE:
						plugins.add(addonId);
						return true;
				}
			}
		}
		fatal(MessageFormat.format(Messages.getString("UpdateCommand.UNABLE_FIND_ADDON"),addonId)); //$NON-NLS-1$
		return false;
	}

	@Override
	protected String getPrintArguments() {
		return " "+Messages.getString("UpdateCommand.PLUGINS"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

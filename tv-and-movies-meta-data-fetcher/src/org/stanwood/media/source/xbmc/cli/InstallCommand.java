package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
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

public class InstallCommand extends AbstractXBMCSubCommand {

	private final static String NAME = "install";
	private final static String DESCRIPTION = "Install a new XBMC addon";
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	private Set<String> plugins;

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
				fatal("Missing argument, no plugins specified");
				return false;
			}
		} catch (XBMCException e) {
			fatal(e.getMessage());
			return false;
		}
		return true;
	}

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
						fatal("Addon '"+addonId+"' is already installed, so unable to install it. Try update instead.");
						return false;
					case NOT_INSTALLED:
						plugins.add(addonId);
						return true;
					case OUT_OF_DATE:
						fatal("Addon '"+addonId+"' is already installed, so unable to install it. Try update instead.");
						return false;
				}
			}
		}
		fatal("Unable to find addon: " +addonId);
		return false;
	}

}
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

public class UpdateCommand extends AbstractXBMCSubCommand {

	private final static String NAME = "update";
	private final static String DESCRIPTION = "Update the installed XBMC addons to the latest versions";
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	private Set<String> plugins;

	public UpdateCommand(ICLICommand rootCommand,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(rootCommand,NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		try {
			if (plugins.size()>0) {
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
						warn("Addon '"+addonId+"' is already uptodate, so will not be updated");
						return true;
					case NOT_INSTALLED:
						fatal("Addon '"+addonId+"' is not installed, so unable to update it");
						return false;
					case OUT_OF_DATE:
						plugins.add(addonId);
						return true;
				}
			}
		}
		fatal("Unable to find addon: " +addonId);
		return false;
	}

}

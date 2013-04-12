/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.server.commands;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stanwood.media.Controller;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.cli.Messages;
import org.stanwood.media.source.xbmc.updater.AddonDetails;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.source.xbmc.updater.IXBMCUpdater;

/**
 * This command is used to uninstall XBMC addons for the XBMC source
 */
public class XBMCRemoveAddonsCommand extends AbstractServerCommand<EmptyResult> {

	private IXBMCUpdater updater;
	private List<String> addons;

	/**
	 * The constructor
	 * @param controller The controller
	 */
	public XBMCRemoveAddonsCommand(Controller controller) {
		super(controller);
		updater = controller.getXBMCAddonManager().getUpdater();
	}

	protected IXBMCUpdater getUpdater() {
		return updater;
	}

	/** {@inheritDoc} */
	@Override
	public EmptyResult execute(final ICommandLogger logger,IProgressMonitor monitor) {
		IConsole console = new IConsole() {
			@Override
			public void error(String error) {
				logger.error(error);
			}

			@Override
			public void info(String info) {
				logger.info(info);
			}

		};

		try {
			Set<String> plugins = new HashSet<String>();
			Set<AddonDetails> addons = getUpdater().listAddons(console);
			for (String arg : this.addons) {
				if (!checkAddon(logger,plugins,arg,addons)) {
					return null;
				}
			}

			if (plugins.size()>0) {
				getUpdater().uninstallAddons(console, plugins);
			}
			else {
				logger.error(Messages.getString("RemoveCommand.MISSING_ARG")); //$NON-NLS-1$
				return null;
			}
			getController().reloadSources();
		} catch (XBMCException e) {
			logger.error(e.getMessage());
			return null;
		} catch (ConfigException e) {
			logger.error(e.getMessage());
			return null;
		}
		return new EmptyResult();

	}

	/**
	 * Set the list of addons to uninstall using their addon ID from XBMC
	 * @param addons The addon ID's to uninstall
	 */
	@param(name="addons",description="List of addons to be uninstalled")
	public void setAddons(List<String> addons) {
		this.addons = addons;
	}

	private boolean checkAddon(ICommandLogger logger,Set<String>plugins,String addonId, Set<AddonDetails> addons) {
		for (AddonDetails addon : addons) {
			if (addon.getId().equals(addonId)) {
				switch (addon.getStatus()) {
					case INSTALLED:
						plugins.add(addonId);
						return true;
					case NOT_INSTALLED:
						logger.error(MessageFormat.format(Messages.getString("RemoveCommand.ADDON_NOT_INSTALLED"),addonId)); //$NON-NLS-1$
						return false;
					case OUT_OF_DATE:
						plugins.add(addonId);
						return true;
				}
			}
		}
		logger.error(MessageFormat.format(Messages.getString("RemoveCommand.UNABLE_FIND_ADDON"),addonId)); //$NON-NLS-1$
		return false;
	}

}

/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
import java.util.Set;

import org.stanwood.media.Controller;
import org.stanwood.media.cli.manager.Messages;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.IConsole;

/**
 * This command is used to update addons
 */
public class XBMCUpdateAddonsCommand extends AbstractServerCommand<EmptyResult> {

	private Set<String> addons;

	/**
	 * The constructor
	 * @param controller The controller
	 */
	public XBMCUpdateAddonsCommand(Controller controller) {
		super(controller);
	}

	/** {@inheritDoc} */
	@Override
	public EmptyResult execute(final ICommandLogger logger,IProgressMonitor monitor) {
		if (!getController().isTestRun()) {
			try {
				logger.info(Messages.getString("CLICopyToMediaDir.CHECKING_UPTODATE")); //$NON-NLS-1$
				IConsole console = new IConsole() {
					@Override
					public void error(String error) {
						logger.info(error);
					}

					@Override
					public void info(String info) {
						logger.info(info);
					}
				};
				int count;
				if (addons ==null) {
					count = getController().getXBMCAddonManager().getUpdater().update(console);
				}
				else {
					count = getController().getXBMCAddonManager().getUpdater().update(console,addons);
				}
				if (count>0 ) {
					logger.info(MessageFormat.format(Messages.getString("CLICopyToMediaDir.DOWNLOAD_INSTALL_UPDATE"),count)); //$NON-NLS-1$
				}
				getController().reloadSources();
			} catch (XBMCUpdaterException e) {
				logger.error(Messages.getString("CLICopyToMediaDir.UNABLE_TO_UPDATE"),e); //$NON-NLS-1$
			} catch (XBMCException e) {
				logger.error(Messages.getString("CLICopyToMediaDir.UNABLE_TO_UPDATE"),e); //$NON-NLS-1$
			} catch (ConfigException e) {
				logger.error(Messages.getString("CLICopyToMediaDir.UNABLE_TO_UPDATE"),e); //$NON-NLS-1$
			}
		}
		return new EmptyResult();
	}

	/**
	 * Used to set addons to update. If none are given, then all addons are updated
	 * @param addons set of addons to update
	 */
	@param(name="addons",description="The addons to update. If none are given, then all addons are updated")
	public void setAddons(Set<String> addons) {
		this.addons = addons;
	}

}

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

import java.util.Set;

import org.stanwood.media.Controller;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.cli.Messages;
import org.stanwood.media.source.xbmc.updater.AddonDetails;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.source.xbmc.updater.IXBMCUpdater;

/**
 * A command to list installed addons
 */
public class XBMCListAddonsCommand extends AbstractServerCommand<XBMCListAddonsResult> {

	private IXBMCUpdater updater;

	/**
	 * The constructor
	 * @param controller The controller
	 */
	public XBMCListAddonsCommand(Controller controller) {
		super(controller);
		updater = controller.getXBMCAddonManager().getUpdater();
	}

	protected IXBMCUpdater getUpdater() {
		return updater;
	}

	/** {@inheritDoc} */
	@Override
	public XBMCListAddonsResult execute(final ICommandLogger logger,IProgressMonitor monitor) {
		try {
			logger.info(Messages.getString("ListCommand.ADDON_LIST")); //$NON-NLS-1$
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
			Set<AddonDetails> addons = getUpdater().listAddons(console);
			return new XBMCListAddonsResult(addons);
		} catch (XBMCUpdaterException e) {
			logger.error(Messages.getString("CLICopyToMediaDir.UNABLE_TO_UPDATE"),e); //$NON-NLS-1$

		}
		return null;
	}

}

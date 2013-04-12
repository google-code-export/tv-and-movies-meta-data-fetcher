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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.ActionPerformer;
import org.stanwood.media.cli.manager.Messages;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigException;


public class ManageMediaCommand extends AbstractServerCommand<EmptyResult> {

	private List<File> mediaDirs;

	public ManageMediaCommand(Controller controller) {
		super(controller);
	}

	/**
	 * Uses to set a list of media directories to process. Default is all of them.
	 * @param mediaDirs A list of media directories to process. Default is all of them.
	 */
	@param(name="mediaDirectories",description="A list of media directories to process. Default is all of them.")
	public void setMediaDirectories(List<File> mediaDirs) {
		this.mediaDirs = mediaDirs;
	}

	@Override
	public EmptyResult execute(ICommandLogger logger, IProgressMonitor monitor) {
		try  {
			List<MediaDirectory> mediaDirs = getMediaDirs(logger);
			if (mediaDirs==null) {
				return null;
			}

			for (MediaDirectory rootMediaDir : mediaDirs) {
				ActionPerformer renamer = new ActionPerformer(getController(),rootMediaDir.getActions(),rootMediaDir,rootMediaDir.getMediaDirConfig().getExtensions());
				renamer.performActions(new NullProgressMonitor());
			}
			return new EmptyResult();
		} catch (ActionException e) {
			logger.error(e.getMessage(),e);
		} catch (ConfigException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	private List<MediaDirectory> getMediaDirs(ICommandLogger logger) {
		List<MediaDirectory>mediaDirs = new ArrayList<MediaDirectory>();
		if (this.mediaDirs!=null) {
			for (File dir : this.mediaDirs) {
				MediaDirectory mediaDir = getMediaDirectory(logger,dir);
				if (mediaDir==null) {
					return null;
				}
				mediaDirs.add(mediaDir);
			}
		}
		else {
			for (File dir : getController().getMediaDirectories()) {
				MediaDirectory mediaDir = getMediaDirectory(logger,dir);
				if (mediaDir==null) {
					return null;
				}
				mediaDirs.add(mediaDir);
			}
		}
		return mediaDirs;
	}

	private MediaDirectory getMediaDirectory(ICommandLogger logger,File dir) {
		MediaDirectory rootMediaDir = null;
		if (dir.isDirectory()) {
			try {
				rootMediaDir = getController().getMediaDirectory(dir);
			} catch (ConfigException e) {
				logger.error(e.getMessage(),e);
				return null;
			}
		} else {
			logger.error(MessageFormat.format(Messages.getString("CLIMediaManager.MEDIA_DIR_NOT_WRITEABLE"),dir)); //$NON-NLS-1$
			return null;
		}
		if (rootMediaDir==null || !rootMediaDir.getMediaDirConfig().getMediaDir().exists()) {
			logger.error(MessageFormat.format(Messages.getString("CLIMediaManager.MEDIA_DIR_NOT_EXIST"),dir)); //$NON-NLS-1$
			return null;
		}
		return rootMediaDir;
	}
}

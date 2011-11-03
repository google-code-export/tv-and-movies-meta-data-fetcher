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
package org.stanwood.media.cli.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.FileNameParser;
import org.stanwood.media.actions.rename.ParsedFileName;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;

public class MediaSearcher {

	private Controller controller;
	private List<MediaDirectory> mediaDirs;

	public MediaSearcher(Controller controller) throws ConfigException {
		this.controller = controller;
		mediaDirs = new ArrayList<MediaDirectory>();
		for (File mediaDirLoc :  controller.getMediaDirectories()) {
			MediaDirectory mediaDir = controller.getMediaDirectory(mediaDirLoc);
			if (mediaDir.getMediaDirConfig().getMode()==Mode.TV_SHOW) {
				mediaDirs.add(0,mediaDir);
			}
			else {
				mediaDirs.add(mediaDir);
			}
		}
	}

	public SearchResult lookupMedia(File mediaFile) throws ConfigException, SourceException, StoreException, MalformedURLException, IOException {
		Mode mode = Mode.TV_SHOW;
		ParsedFileName parsed = FileNameParser.parse(mediaFile);
		if (parsed==null) {
			mode = Mode.FILM;
		}
		for (MediaDirectory mediaDir : mediaDirs) {
			if (mediaDir.getMediaDirConfig().getMode()==mode) {
				SearchResult result = mediaDir.searchForVideoId(mediaFile);
				if (result!=null) {
					return result;
				}
			}
		}
		return null;
	}
}

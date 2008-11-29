/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Episode;
import org.stanwood.media.util.AbstractExecutable;

public class AtomicParsley extends AbstractExecutable {

	private final static DateFormat YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy");
	
	private File atomicParsleyApp = null;
	
	public AtomicParsley(File app) {
		atomicParsleyApp = app;
	}
	
	public void listAttoms(File mp4File) throws IOException, InterruptedException {
		List<String> args = new ArrayList<String>();
		args.add(atomicParsleyApp.getAbsolutePath());
		args.add(mp4File.getAbsolutePath());
		args.add("-t");
		args.add("+");
		execute(args);
	}
	
	public void updateEpsiode(File mp4File, Episode episode) throws IOException, InterruptedException {
		List<String> args = new ArrayList<String>();
		args.add(atomicParsleyApp.getAbsolutePath());
		args.add(mp4File.getAbsolutePath());
		args.add("--metaEnema");
		args.add("--freefree");
		args.add("--overWrite");
		args.add("--stik");
		args.add("\"TV Show\"");
		args.add("--TVShowName");
		args.add(episode.getSeason().getShow().getName());
		args.add("--TVSeasonNum");
		args.add(String.valueOf(episode.getSeason().getSeasonNumber()));
		args.add("--TVEpisodeNum");
		args.add(String.valueOf(episode.getEpisodeNumber()));
		args.add("--year");
		//TODO use the full date
		args.add(YEAR_DATE_FORMAT.format(episode.getAirDate()));
		args.add("--title");
		args.add(episode.getTitle());
		args.add("--description");
		args.add("\""+episode.getSummary()+"\"");
		if (episode.getSeason().getShow().getGenres().size()>0) {
			args.add("--genre");
			args.add(episode.getSeason().getShow().getGenres().get(0));
		}
		execute(args);
		
	}
}

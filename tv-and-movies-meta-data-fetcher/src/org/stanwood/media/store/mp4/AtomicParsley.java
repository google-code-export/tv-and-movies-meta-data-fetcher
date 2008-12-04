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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.Episode;
import org.stanwood.media.util.AbstractExecutable;

public class AtomicParsley extends AbstractExecutable {
	private static Map<String, String> nameToParam;
	// private final static DateFormat YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy");
	private final static Pattern ATOM_PATTERN = Pattern.compile("Atom \"(.*)\" contains\\: (.*)");

	private File atomicParsleyApp = null;

	static {
		nameToParam = new HashMap<String, String>();

		nameToParam.put("©ART", "--artist");
		nameToParam.put("©nam", "--title");
		nameToParam.put("©alb", "--album");
		nameToParam.put("©gen", "--genre");
//		nameToParam.put("", "--tracknum");
//		nameToParam.put("", "--disk");
		nameToParam.put("©cmt", "--comment");
		nameToParam.put("©day", "--year");
		nameToParam.put("©lyr", "--lyrics");
		nameToParam.put("©wrt", "--composer");
		nameToParam.put("cprt", "--copyright");
		nameToParam.put("©grp", "--grouping");
		nameToParam.put("covr", "--artwork");
		nameToParam.put("tmpo", "--bpm");
		nameToParam.put("aART", "--albumArtist");
		nameToParam.put("cpil", "--compilation");
//		nameToParam.put("", "--advisory");
		nameToParam.put("stik", "--stik");
		nameToParam.put("desc", "--description");
		nameToParam.put("tvnn", "--TVNetwork");
		nameToParam.put("tvsh", "--TVShowName");
		nameToParam.put("tven", "--TVEpisode");
		nameToParam.put("tvsn", "--TVSeasonNum");
		nameToParam.put("tves", "--TVEpisodeNum");
//		nameToParam.put("", "--podcastFlag");
		nameToParam.put("catg", "--category");
//		nameToParam.put("", "--keyword");
		nameToParam.put("purl", "--podcastURL");
		nameToParam.put("egid", "--podcastGUID");
		nameToParam.put("purd", "--purchaseDate");
		nameToParam.put("©too", "--encodingTool");
//		nameToParam.put("", "--gapless");
	}
	
	public AtomicParsley(File app) {
		atomicParsleyApp = app;
	}

	public List<Atom> listAttoms(File mp4File) throws IOException, InterruptedException {
		List<String> args = new ArrayList<String>();
		args.add(atomicParsleyApp.getAbsolutePath());
		args.add(mp4File.getAbsolutePath());
		args.add("-t");
		args.add("+");
		execute(args);

		List<Atom> atoms = new ArrayList<Atom>();
		String lines[] = getOutputStream().split("\n");
		for (String line : lines) {
			Matcher m = ATOM_PATTERN.matcher(line);
			if (m.matches()) {
				Atom atom = new Atom(m.group(1),m.group(2));
				atoms.add(atom);
			}
		}
		return atoms;
	}

	public void update(File mp4File, List<Atom>atoms) throws AtomicParsleyException {
		List<String> args = new ArrayList<String>();
		args.add(atomicParsleyApp.getAbsolutePath());
		args.add(mp4File.getAbsolutePath());
		args.add("--metaEnema");
		args.add("--freefree");
		args.add("--overWrite");
		
		for (Atom atom: atoms) {
			String param = nameToParam.get( atom.getName());
			if (param==null) {
				throw new AtomicParsleyException("Unkown attom " + atom.getName());
			}
			args.add(param);
			args.add(atom.getValue());
		}
		try {
			execute(args);
		} catch (IOException e) {
			throw new AtomicParsleyException(e.getMessage(),e);
		} catch (InterruptedException e) {
			throw new AtomicParsleyException(e.getMessage(),e);
		}
	}
	
	public void updateEpsiode(File mp4File, Episode episode) throws AtomicParsleyException  {
		List<Atom> atoms = new ArrayList<Atom>();
		atoms.add(new Atom("stik","TV Show"));
		atoms.add(new Atom("tven",episode.getEpisodeSiteId()));
		atoms.add(new Atom("tvsh",episode.getSeason().getShow().getName()));
		atoms.add(new Atom("tvsn",String.valueOf(episode.getSeason().getSeasonNumber())));
		atoms.add(new Atom("tves",String.valueOf(episode.getEpisodeNumber())));
		atoms.add(new Atom("©day",episode.getAirDate().toString()));
		atoms.add(new Atom("©nam",episode.getTitle()));
		atoms.add(new Atom("desc",episode.getSummary()));
		
		if (episode.getSeason().getShow().getGenres().size() > 0) {
			atoms.add(new Atom("©gen",episode.getSeason().getShow().getGenres().get(0)));
			atoms.add(new Atom("catg",episode.getSeason().getShow().getGenres().get(0)));
		}
		update(mp4File,atoms);
	}
}

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.util.AbstractExecutable;

/**
 * This class is a wrapper the the atomic parsley application {@link "http://atomicparsley.sourceforge.net/"}
 * It is used to store and retrieve atoms to a MP4 file.
 */
public class AtomicParsley extends AbstractExecutable {
	
	/** Used to disable the update of images within MP4 files for tests */
	public static boolean updateImages = true;
	
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
	
	/**
	 * Used to create a instance of the Atomic Parsley command wrapper.
	 * @param app A file object that points to the location of the Atomic Parsley command
	 */
	public AtomicParsley(File app) {
		atomicParsleyApp = app;
	}

	/**
	 * Used to get a list of atoms in the MP4 file.
	 * @param mp4File The MP4 file
	 * @return A list of atoms
	 * @throws IOException Thrown if their is a I/O problem
	 * @throws InterruptedException Thrown if a thread is interrupted
	 */
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
	
	/**
	 * Used to remove all artwork from the .mp4 file
	 * @param mp4File The mp4 file
	 * @throws AtomicParsleyException Thrown if their is a problem updating the atoms
	 */
	public void removeAllArtwork(File mp4File) throws AtomicParsleyException {
		List<String> args = new ArrayList<String>();
		args.add(atomicParsleyApp.getAbsolutePath());
		args.add(mp4File.getAbsolutePath());
		args.add("--artwork");
		args.add("REMOVE_ALL");
		try {
			execute(args);
		} catch (IOException e) {
			throw new AtomicParsleyException(e.getMessage(),e);
		} catch (InterruptedException e) {
			throw new AtomicParsleyException(e.getMessage(),e);
		}	
	}

	/**
	 * Used to add atoms to a MP4 file
	 * @param mp4File The MP4 file
	 * @param atoms A list of atoms
	 * @throws AtomicParsleyException Thrown if their is a problem updating the atoms 
	 */
	public void update(File mp4File, List<Atom>atoms) throws AtomicParsleyException {
		try {
			List<Atom> newAtoms = new ArrayList<Atom>(atoms);
			List<Atom> atomsAlreadyInFile = this.listAttoms(mp4File);			
			
			List<String> args = new ArrayList<String>();
			args.add(atomicParsleyApp.getAbsolutePath());
			args.add(mp4File.getAbsolutePath());
//			args.add("--metaEnema"); 
			args.add("--freefree");
			args.add("--overWrite");
			
			boolean found = false;
			for (Atom atom: newAtoms) {
				// Check if this atom as already been set on this file
				if (!atomsAlreadyInFile.contains(atom)) {
					found = true;
					String param = nameToParam.get( atom.getName());
					if (param==null) {
						throw new AtomicParsleyException("Unkown attom '" + atom.getName() + "' with value '" + atom.getValue());
					}
					args.add(param);
					args.add(atom.getValue());
				}
			}
		
			if (found) {
				// If their is already artwork, then remove it
				if (hasAtomWithName("covr",atomsAlreadyInFile)) {
					args.add(1,"--artwork");
					args.add(2,"REMOVE_ALL");					
				}
								
				System.out.println("Updating mp4/m4v file '"+mp4File.getName()+"' with new metadata");
				execute(args);
			}
			else {
				System.out.println("No new metadata to add to mp4/m4v file '"+mp4File.getName()+"'");
			}
		} catch (IOException e) {
			throw new AtomicParsleyException(e.getMessage(),e);
		} catch (InterruptedException e) {
			throw new AtomicParsleyException(e.getMessage(),e);
		}
	}
	
	
	private boolean hasAtomWithName(String name,List<Atom> atoms) {
		for (Atom atom : atoms) {
			if (atom.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a TV Show episode
	 * @param mp4File The MP4 file
	 * @param episode The episode details
	 * @throws AtomicParsleyException Thrown if their is a problem updating the atoms 
	 */
	public void updateEpsiode(File mp4File, Episode episode) throws AtomicParsleyException  {
		List<Atom> atoms = new ArrayList<Atom>();
		atoms.add(new Atom("stik","TV Show"));
		atoms.add(new Atom("tven",episode.getEpisodeSiteId()));
		atoms.add(new Atom("tvsh",episode.getSeason().getShow().getName()));
		atoms.add(new Atom("tvsn",String.valueOf(episode.getSeason().getSeasonNumber())));
		atoms.add(new Atom("tves",String.valueOf(episode.getEpisodeNumber())));
		atoms.add(new Atom("©day",episode.getDate().toString()));
		atoms.add(new Atom("©nam",episode.getTitle()));
		atoms.add(new Atom("desc",episode.getSummary()));
		
		if (episode.getSeason().getShow().getGenres().size() > 0) {
			atoms.add(new Atom("©gen",episode.getSeason().getShow().getGenres().get(0)));
			atoms.add(new Atom("catg",episode.getSeason().getShow().getGenres().get(0)));
		}
			
		update(mp4File,atoms);
	}

	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a Film. It also removes any artwork before
	 * adding the Film atoms and artwork.
	 * @param mp4File The MP4 file
	 * @param film The film details
	 * @throws AtomicParsleyException Thrown if their is a problem updating the atoms 
	 */
	public void updateFilm(File mp4File, Film film) throws AtomicParsleyException {		
		List<Atom> atoms = new ArrayList<Atom>();
		atoms.add(new Atom("stik","Movie"));
		atoms.add(new Atom("©day",film.getDate().toString()));
		atoms.add(new Atom("©nam",film.getTitle()));
		atoms.add(new Atom("desc",film.getSummary()));
		if (film.getImageURL()!=null) {
			File artwork;
			try {
				artwork = downloadToTempFile(film.getImageURL());
				atoms.add(new Atom("covr",artwork.getAbsolutePath()));
			} catch (IOException e) {
				System.err.println("Unable to download artwork from " + film.getImageURL().toExternalForm());
				e.printStackTrace();
			}			
		}
		
		if (film.getGenres().size() > 0) {
			atoms.add(new Atom("©gen",film.getGenres().get(0)));
			atoms.add(new Atom("catg",film.getGenres().get(0)));
		}
		update(mp4File,atoms);
	}
	
	private File downloadToTempFile(URL url) throws IOException {
		File file = File.createTempFile("artwork",".jpg");
		file.delete();
		OutputStream out = null;
		URLConnection conn = null;
		InputStream  in = null;
		try {			
			out = new BufferedOutputStream(
				new FileOutputStream(file));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}			
			file.deleteOnExit();
			return file;					
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}

	}
}
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
package org.stanwood.media.renamer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.source.TVCOMSource;
import org.stanwood.media.store.StoreException;


/**
 * This class is used to rename show episodes in a directory to the correct name.
 * The pattern given is the format of the name.
 * The following are valid values:
 * <pre>
 *  %h - show Id
 * 	%s - series number  
 *  %e - episode number
 *  %% - add a % char
 *  %n - show name
 *  %t - episode title  
 *  %x - extension (avi, mkv....)
 * </pre> 
 */
public class Renamer {

	private long showId;
	private File showDirectory;
	private String pattern;
	private String[] exts;
	private boolean refresh;

	/**
	 * Constructor used to create a instance of the class
	 * @param showId The id of the show to rename
	 * @param showDirectory The directory it is located in
	 * @param pattern The pattern to use while renaming
	 * @param exts The extensions to search for
	 * @param refresh If true, then don't read from the stores
	 */
	public Renamer(long showId,File showDirectory,String pattern,String exts[], boolean refresh) {
		this.showId = showId;
		this.showDirectory = showDirectory;
		this.pattern = pattern;
		this.exts = exts;
		this.refresh = refresh;
	}
	
	/**
	 * This will cause the renaming to happen.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a IO problem. For example, not been
	 *                     about to read from the disc.
	 * @throws SourceException Thrown if their is a problem reading from the source
	 * @throws StoreException Thrown is their is a problem with a store
	 */
	public void tidyShowNames() throws MalformedURLException, IOException, SourceException, StoreException {
		File files[] = showDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {				
				if (file.isDirectory()) {					
					for (String ext : exts) {
						if (name.endsWith("."+ext)) {
							return true;
						}
					}
				}
				return false;
			}			
		});
		
		
		
		for (File file : files ) {
			Show show = Controller.getInstance().getShow(file,TVCOMSource.SOURCE_ID,showDirectory, showId,refresh);		
			if (show == null) {
				fail("Unable to find show details");						
			}
			String oldFileName = file.getName(); 
			ParsedFileName data =  FileNameParser.parse(oldFileName);
			if (data==null) {
				System.err.println("Unable to workout the season and/or episode number of '" + file.getName()+"'");
			}
			else {
				
				Season season = Controller.getInstance().getSeason(file,show,data.getSeason(),refresh);
				if (season==null) {
					System.err.println("Unable to find season for file : " + file.getAbsolutePath());
					continue;
				}
				Episode episode = Controller.getInstance().getEpisode(file,season,data.getEpisode(),refresh);
				if (episode==null) {
					System.err.println("Unable to find epsiode for file : " + file.getAbsolutePath());
					continue;
				}
				String ext = oldFileName.substring(oldFileName.length()-3);
				String newName = getNewName(show,season,episode,ext);
				
				File newFile = new File(file.getParentFile(),newName);
				if (file.equals(newFile)) {
					System.out.println("File '" + oldFileName+"' already has the correct name.");
				}
				else {					
					if (newFile.exists()) {
						System.err.println("Unable rename '"+oldFileName+"' file too '"+newFile.getName()+"' as it already exists.");					
					}
					else {
						System.out.println("Renaming '" + oldFileName + "' -> '" + newName+"'");
						
						File oldFile = new File(file.getAbsolutePath());
						if (file.renameTo(newFile)) {
							Controller.getInstance().renamedFile(oldFile,newFile);	
						}
						else {
							System.err.println("Failed to rename '"+oldFileName+"' file too '"+newFile.getName()+"'.");
						}
					}
				}
			}
		}		
	}

	private void fail(String msg) {
		System.err.println(msg);
		System.exit(1);		
	}

	private String getNewName(Show show,Season season, Episode episode,String ext) {
		String newName = pattern;
		newName = newName.replaceAll("%h", String.valueOf(showId));
		newName = newName.replaceAll("%s", String.valueOf(season.getSeasonNumber()));
		String episodeNum = String.valueOf(episode.getEpisodeNumber());
		if (episodeNum.length()==1) {
			episodeNum = "0" +episodeNum;
		}
		
		newName = newName.replaceAll("%e", episodeNum);
		newName = newName.replaceAll("%%", "%");
		newName = newName.replaceAll("%n", show.getName());
		newName = newName.replaceAll("%t", episode.getTitle());
		newName = newName.replaceAll("%x", ext);
		return newName;
	}
}

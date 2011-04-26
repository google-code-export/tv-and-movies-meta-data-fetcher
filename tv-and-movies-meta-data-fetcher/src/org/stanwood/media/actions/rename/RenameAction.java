package org.stanwood.media.actions.rename;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;

/**
 * This action is used to rename media files based on a pattern, media data that can be found in source/stores and
 * the mode.
 */
public class RenameAction implements IAction {

	private final static Log log = LogFactory.getLog(RenameAction.class);

	/**
	 * The key of the refresh parameter for this action.
	 */
	public static final String PARAM_KEY_REFRESH = "refresh";

	private boolean refresh = false;

	/**
	 * Perform the rename action of the file files
	 * @param files the media files
	 * @throws ActionException Thrown if their is a problem renaming the files
	 */
	@Override
	public void perform(MediaDirectory dir,List<File> files) throws ActionException {
		Iterator<File> it =files.iterator();
		List<File>newFiles = new ArrayList<File>();
		while (it.hasNext()) {
			File file = it.next();
			try {
				if (dir.getMediaDirConfig().getMode() == Mode.TV_SHOW) {
					File newFile = renameTVShow(dir,file);
					if (!file.equals(newFile)) {
						it.remove();
						newFiles.add(newFile);
					}
				} else if (dir.getMediaDirConfig().getMode() == Mode.FILM) {
					File newFile = renameFilm(dir,file);
					if (!file.equals(newFile)) {
						it.remove();
						newFiles.add(newFile);
					}
				} else {
					throw new ActionException("Unknown rename mode: " + dir.getMediaDirConfig().getMode() );
				}
			}
			catch (PatternException e) {
				throw new ActionException("Unable to rename file " +file,e);
			} catch (MalformedURLException e) {
				throw new ActionException("Unable to rename file " +file,e);
			} catch (SourceException e) {
				throw new ActionException("Unable to rename file " +file,e);
			} catch (IOException e) {
				throw new ActionException("Unable to rename file " +file,e);
			} catch (StoreException e) {
				throw new ActionException("Unable to rename file " +file,e);
			}
		}
		for (File newFile : newFiles) {
			files.add(newFile);
		}
	}

	private File renameFilm(MediaDirectory dir,File file) throws MalformedURLException, SourceException, IOException, StoreException, PatternException {
		SearchResult result = searchForId(dir,file);
		if (result==null) {
			log.error("Unable to find film id for file '"+file.getName()+"'");
			return null;
		}

		String oldFileName = file.getName();

		Film film = dir.getFilm(dir.getMediaDirConfig().getMediaDir(), file,result,refresh);
		if (film==null) {
			log.error("Unable to find film with id  '" + result.getId() +"' and source '"+result.getSourceId()+"'");
			return null;
		}

		String ext = oldFileName.substring(oldFileName.lastIndexOf('.')+1);
		PatternMatcher pm = new PatternMatcher();
		File newName = pm.getNewFilmName(dir.getMediaDirConfig(),film, ext,result.getPart());

		doRename(dir,file, newName,film);
		return newName;
	}

	private File renameTVShow(MediaDirectory dir,File file) throws MalformedURLException, SourceException, IOException, StoreException, PatternException {
		SearchResult result = searchForId(dir,file);
		if (result==null) {
			log.error("Unable to find show id");
			return null;
		}

		Show show =  dir.getShow(dir.getMediaDirConfig().getMediaDir(),file,result,refresh);
		if (show == null) {
			log.fatal("Unable to find show details");
			return null;
		}
		String oldFileName = file.getName();
		ParsedFileName data =  FileNameParser.parse(dir.getMediaDirConfig(),file);
		if (data==null) {
			log.error("Unable to workout the season and/or episode number of '" + file.getName()+"'");
		}
		else {
			Season season = dir.getSeason(dir.getMediaDirConfig().getMediaDir(),file, show, data.getSeason(), refresh);
			if (season == null) {
				log.error("Unable to find season for file : " + file.getAbsolutePath());
			} else {
				Episode episode = dir.getEpisode(dir.getMediaDirConfig().getMediaDir(),file, season, data.getEpisode(), refresh);
				if (episode == null) {
					log.error("Unable to find episode for file : " + file.getAbsolutePath());
				} else {
					String ext = oldFileName.substring(oldFileName.length() - 3);
					PatternMatcher pm = new PatternMatcher();
					File newName = pm.getNewTVShowName(dir.getMediaDirConfig(),show, season, episode, ext);

					doRename(dir,file, newName,episode);
					file = newName;
				}
			}
		}
		return file;
	}

	private SearchResult searchForId(MediaDirectory dir,File file) throws MalformedURLException, SourceException, StoreException, IOException
	{
		SearchResult result;
		result = dir.searchForVideoId(dir.getMediaDirConfig(),file);
		return result;

	}

	private void doRename(MediaDirectory dir,File file, File newFile,IVideo video) throws StoreException {
		// Remove characters from filenames that windows and linux don't like
		if (file.equals(newFile)) {
			log.info("File '" + file.getAbsolutePath()+"' already has the correct name.");
		}
		else {
			if (newFile.exists()) {
				log.error("Unable rename '"+file.getAbsolutePath()+"' file too '"+newFile.getAbsolutePath()+"' as it already exists.");
			}
			else {
				if (!newFile.getParentFile().exists()) {
					if (!newFile.getParentFile().mkdirs() || !newFile.getParentFile().exists()) {
						log.error("Unable to create directories: " + newFile.getParentFile().getAbsolutePath());
					}
				}
				log.info("Renaming '" + file.getAbsolutePath() + "' -> '" + newFile.getAbsolutePath()+"'");

				File oldFile = new File(file.getAbsolutePath());
				if (file.renameTo(newFile)) {
					for (VideoFile vf : video.getFiles()) {
						if (vf.getLocation().equals(file)) {
							vf.setLocation(newFile);
							if (vf.getOrginalLocation()==null) {
								vf.setOrginalLocation(file);
							}
						}
					}
					dir.renamedFile(dir.getMediaDirConfig().getMediaDir(),oldFile,newFile);
				}
				else {
					log.error("Failed to rename '"+file.getAbsolutePath()+"' file too '"+newFile.getName()+"'.");
				}
			}
		}
	}

	/**
	 * <p>Used to set the value of a parameter for this action.</p>
	 * <p>This action supports the following parameters
	 * <ul>
	 * <li>refresh - if true, will only read from sources. Stores will then be refreshed</li>
	 * </ul>
	 * </p>
	 */
	@Override
	public void setParameter(String key,String value) throws ActionException {
		if (key.equalsIgnoreCase(PARAM_KEY_REFRESH)) {
			refresh = Boolean.parseBoolean(value);
		}
		else {
			throw new ActionException("Unsupported parameter "+key);
		}
	}

}

package org.stanwood.media.actions.rename;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.FileHelper;

/**
 * This action is used to rename media files based on a pattern, media data that can be found in source/stores and
 * the mode.
 */
public class RenameAction extends AbstractAction {

	private final static Log log = LogFactory.getLog(RenameAction.class);

	/**
	 * The key of the refresh parameter for this action.
	 */
	public static final String PARAM_KEY_REFRESH = "refresh";
	public static final String PARAM_KEY_PRUNE_EMPTY_FOLDERS = "pruneEmptyFolders";

	private boolean refresh = false;

	private boolean pruneEmptyFolders = false;

	/**
	 * Perform the rename action of the file files
	 * @param files the media files
	 * @throws ActionException Thrown if their is a problem renaming the files
	 */
	@Override
	public void perform(MediaDirectory dir,File file,IActionEventHandler eventHandler) throws ActionException {
		try {
			if (dir.getMediaDirConfig().getMode() == Mode.TV_SHOW) {
				renameTVShow(dir,file,eventHandler);
			} else if (dir.getMediaDirConfig().getMode() == Mode.FILM) {
				renameFilm(dir,file,eventHandler);
			} else {
				throw new ActionException("Unknown rename mode: " + dir.getMediaDirConfig().getMode() );
			}
		}
		catch (PatternException e) {
			throw new ActionException("Unable to rename file " +file,e);
		} catch (MalformedURLException e) {
			throw new ActionException("Unable to rename file " +file,e);
		} catch (SourceException e) {
			log.error("Unable to rename file '" + file,e);
		} catch (IOException e) {
			throw new ActionException("Unable to rename file " +file,e);
		} catch (StoreException e) {
			throw new ActionException("Unable to rename file " +file,e);
		}
	}

	private File renameFilm(MediaDirectory dir,File file,IActionEventHandler eventHandler) throws MalformedURLException, SourceException, IOException, StoreException, PatternException, ActionException {

		SearchResult result = searchForId(dir,file);
		if (result==null) {
			log.error("Unable to find film id for file '"+file.getName()+"'");
			return null;
		}


		Film film = dir.getFilm(dir.getMediaDirConfig().getMediaDir(), file,result,refresh);
		if (film==null) {
			log.error("Unable to find film with id  '" + result.getId() +"' and source '"+result.getSourceId()+"'");
			return null;
		}
		if (result.getPart()!=null && !isTestMode()) {
			boolean found = false;
			for (VideoFile vf : film.getFiles()) {
				if (vf.getPart()!=null && vf.getPart().equals(result.getPart()) && vf.getLocation().equals(file)) {
					found = true;
					break;
				}
			}
			if (!found) {
				for (IStore store : dir.getStores()) {
					store.cacheFilm(dir.getMediaDirConfig().getMediaDir(), file, film, result.getPart());
				}
			}
		}

		String oldFileName = file.getName();
		String ext = oldFileName.substring(oldFileName.lastIndexOf('.')+1);
		PatternMatcher pm = new PatternMatcher();
		File newName = pm.getNewFilmName(dir.getMediaDirConfig(),film, ext,result.getPart());

		doRename(dir,file, newName,film,eventHandler);
		return newName;
	}

	private File renameTVShow(MediaDirectory dir,File file,IActionEventHandler eventHandler) throws MalformedURLException, SourceException, IOException, StoreException, PatternException, ActionException {
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

					doRename(dir,file, newName,episode,eventHandler);
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

	private void doRename(MediaDirectory dir,File file, File newFile,IVideo video,IActionEventHandler eventHandler) throws StoreException, ActionException {
		// Remove characters from filenames that windows and linux don't like
		if (file.equals(newFile)) {
			log.info("File '" + file.getAbsolutePath()+"' already has the correct name.");
		}
		else {
			if (newFile.exists()) {
				log.error("Unable to rename '"+file.getAbsolutePath()+"' file to '"+newFile.getAbsolutePath()+"' as it already exists.");
			}
			else {
				if (!isTestMode()) {
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
						eventHandler.sendEventRenamedFile(oldFile, newFile);
					}
					else {
						log.error("Failed to rename '"+file.getAbsolutePath()+"' file too '"+newFile.getName()+"'.");
					}
				}
				else {
					log.info("Not Renaming '" + file.getAbsolutePath() + "' -> '" + newFile.getAbsolutePath()+"' as it's a test run");
				}
			}
		}
	}

	/**
	 * <p>Used to set the value of a parameter for this action.</p>
	 * <p>This action supports the following parameters
	 * <ul>
	 * <li>refresh - if true, will only read from sources. Stores will then be refreshed</li>
	 * <li>pruneEmptyFolders - If true, then after renaming, empty folders will be deleted</li>
	 * </ul>
	 * </p>
	 */
	@Override
	public void setParameter(String key,String value) throws ActionException {
		if (key.equalsIgnoreCase(PARAM_KEY_REFRESH)) {
			refresh = Boolean.parseBoolean(value);
		}
		else if (key.equalsIgnoreCase(PARAM_KEY_PRUNE_EMPTY_FOLDERS)) {
			pruneEmptyFolders = Boolean.parseBoolean(value);
		}
		else {
			throw new ActionException("Unsupported parameter "+key);
		}
	}

	@Override
	public void performOnDirectory(MediaDirectory mediaDir, File dir,IActionEventHandler actionEventHandler) throws ActionException {
		if (pruneEmptyFolders) {
			List<File> files = FileHelper.listFiles(dir);
			if (files.size()==0) {
				try {
					if (!isTestMode()) {
						log.info("Empty directory '"+dir.getAbsolutePath()+"' not deleted as in test mode");
					}
					else {
						FileHelper.delete(dir);
						log.info("Deleted empty directory '"+dir.getAbsolutePath()+"'");
					}

				} catch (IOException e) {
					throw new ActionException("Unable to delete empty directory '"+dir.getAbsolutePath()+"'",e);
				}
			}
		}
	}



}

package org.stanwood.media.actions.podcast;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.actions.rename.PatternException;
import org.stanwood.media.actions.rename.PatternMatcher;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.util.FileHelper;

public class PodCastAction extends AbstractAction {

	private final static String PARAM_MEDIA_DIR_URL = "mediaDirURL";
	private final static String PARAM_NUMBER_ENTRIES = "numberEntries";
	private final static String PARAM_FILE_LOCATION = "fileLocation";
	private final static String PARAM_RESTRICT_PATTERN = "restrictPattern";

	private SortedSet<IFeedFile>feedFiles = null;
	private int numEntries = 20;
	private MediaDirectory dir;
	private String fileLocation;
	private String mediaDirUrl;
	private String restricted;
	private PatternMatcher pm = new PatternMatcher();

	@Override
	public void init(MediaDirectory dir) {
		this.dir = dir;
		feedFiles = new TreeSet<IFeedFile>(new Comparator<IFeedFile>() {
			@Override
			public int compare(IFeedFile o1, IFeedFile o2) {
				return o1.getLastModified().compareTo(o2.getLastModified());
			}
		});
	}

	@Override
	public void perform(MediaDirectory dir, Episode episode, File file,IActionEventHandler actionEventHandler) throws ActionException {
		try {
			if (restricted!=null && !file.getAbsolutePath().startsWith(pm.getNewTVShowName(dir.getMediaDirConfig(), restricted, episode,  FileHelper.getExtension(file)).getAbsolutePath())) {
				return;
			}
			perform(dir,(IVideo)episode,file,actionEventHandler);
		} catch (PatternException e) {
			throw new ActionException("Unable to calculate the '"+PARAM_RESTRICT_PATTERN+"' pattern",e);
		}


	}

	@Override
	public void perform(MediaDirectory dir, Film film, File file, Integer part,IActionEventHandler actionEventHandler) throws ActionException {
		try {
			if (restricted!=null && !file.getAbsolutePath().startsWith(pm.getNewFilmName(dir.getMediaDirConfig(), restricted, film,  FileHelper.getExtension(file),part).getAbsolutePath())) {
				return;
			}
			perform(dir,film,file,actionEventHandler);
		} catch (PatternException e) {
			throw new ActionException("Unable to calculate the '"+PARAM_RESTRICT_PATTERN+"' pattern",e);
		}
	}

	private void perform(MediaDirectory dir, IVideo video, File file,IActionEventHandler actionEventHandler) throws ActionException {
		//TODO Check if file is a certian media dir
		feedFiles.add(FeedFileFactory.createFile(file,dir.getMediaDirConfig(),video,mediaDirUrl ));
		if (feedFiles.size()>numEntries) {
			Iterator<IFeedFile> it = feedFiles.iterator();
			it.next();
			it.remove();
		}
	}

	@Override
	public void finished(MediaDirectory dir) throws ActionException {
		RSSFeed rssFeed = new RSSFeed(getFeedFile());
		rssFeed.createNewFeed();
		for (IFeedFile file : feedFiles) {
			rssFeed.addEntry(file);
		}
		try {
			rssFeed.write();
		} catch (Exception e) {
			throw new ActionException("Unable to write pod case",e);
		}
	}

	private File getFeedFile() {
		String loc = fileLocation;
		return new File(dir.getMediaDirConfig().getMediaDir(),loc);
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		if (key.equalsIgnoreCase(PARAM_NUMBER_ENTRIES)) {
			try {
				numEntries = Integer.parseInt(value);
				return;
			}
			catch (NumberFormatException e) {
				throw new ActionException("Invalid number '"+value+"' for parameter '"+key+"'");
			}
		}
		else if (key.equalsIgnoreCase(PARAM_MEDIA_DIR_URL)) {
			mediaDirUrl = value;
			return;
		}
		else if (key.equalsIgnoreCase(PARAM_FILE_LOCATION)) {
			fileLocation = value;
			return;
		}
		else if (key.equalsIgnoreCase(PARAM_RESTRICT_PATTERN)) {
			restricted = value;
			return;
		}
		throw new ActionException("Unsupported parameter '"+key+"'");
	}




}

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
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;

public class PodCastAction extends AbstractAction {

	private final static String PARAM_URL_LOCATION = "urlLocation";
	private final static String PARAM_NUMBER_ENTRIES = "numberEntries";
	private final static String PARAM_FILE_LOCATION = "fileLocation";
	private final static String PARAM_MATCH_PATTERN = "pattern";

	private SortedSet<IFeedFile>feedFiles = null;
	private int numEntries = 20;
	private MediaDirectory dir;
	private String fileLocation;

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
	public void perform(MediaDirectory dir, Episode episode, File file,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,(IVideo)episode,file,actionEventHandler);

	}

	@Override
	public void perform(MediaDirectory dir, Film film, File file, Integer part,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,film,file,actionEventHandler);
	}

	private void perform(MediaDirectory dir, IVideo video, File file,IActionEventHandler actionEventHandler) throws ActionException {
		//TODO Check if file is a certian media dir
		feedFiles.add(FeedFileFactory.createFile(file,video ));
		if (feedFiles.size()>numEntries) {
			Iterator<IFeedFile> it = feedFiles.iterator();
			it.next();
			it.remove();
		}
	}

	@Override
	public void finished(MediaDirectory dir) throws ActionException {
		RSSFeed rssFeed = new RSSFeed(getFeedFile());
		for (IFeedFile file : feedFiles) {
			rssFeed.addEntry(file);
		}
	}

	private File getFeedFile() {
		return null;
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
		if (key.equals(PARAM_FILE_LOCATION)) {
			fileLocation = value;
		}
		throw new ActionException("Unsupported parameter '"+key+"'");
	}




}

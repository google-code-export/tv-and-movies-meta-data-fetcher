package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private final static Log log = LogFactory.getLog(PodCastAction.class);

	private final static String PARAM_MEDIA_DIR_URL = "mediaDirURL";
	private final static String PARAM_NUMBER_ENTRIES = "numberEntries";
	private final static String PARAM_FILE_LOCATION = "fileLocation";
	private final static String PARAM_RESTRICT_PATTERN = "restrictPattern";
	private final static String PARAM_EXTENSIONS_KEY = "extensions";
	private final static String PARAM_FEED_TITLE_KEY = "feedTitle";
	private final static String PARAM_FEED_DESCRIPTION_KEY = "feedDescription";

	private List<IFeedFile>feedFiles = null;
	private int numEntries = 20;
	private MediaDirectory dir;
	private String fileLocation;
	private String mediaDirUrl;
	private String restricted;
	private List<String> extensions;
	private PatternMatcher pm = new PatternMatcher();
	private String feedDescription = "";
	private String feedTitle = "";

	@Override
	public void init(MediaDirectory dir) throws ActionException {
		if (log.isDebugEnabled()) {
			log.debug("Init PodCast Action");
		}
		this.dir = dir;
		feedFiles = new ArrayList<IFeedFile>();

		try {
			File feedFile = getFeedFile();

			if (feedFile.exists()) {
				RSSFeed rssFeed = new RSSFeed(feedFile,mediaDirUrl,dir.getMediaDirConfig() );
			}
		}
		catch (Exception e) {
			throw new ActionException("Unable unable to parse RSS feed",e);
		}
	}

	@Override
	public void perform(MediaDirectory dir, Episode episode, File mediaFile,IActionEventHandler actionEventHandler) throws ActionException {
		String ext = FileHelper.getExtension(mediaFile);
		if (extensions!=null) {
			if (!extensions.contains(ext)) {
				return;
			}
		}
		try {
			if (restricted!=null && !mediaFile.getAbsolutePath().startsWith(dir.getPath(pm.getNewTVShowName(dir.getMediaDirConfig(), restricted, episode,  ext)).getAbsolutePath())) {
				return;
			}
			perform(dir,(IVideo)episode,mediaFile,actionEventHandler);
		} catch (PatternException e) {
			throw new ActionException("Unable to calculate the '"+PARAM_RESTRICT_PATTERN+"' pattern",e);
		}


	}

	@Override
	public void perform(MediaDirectory dir, Film film, File mediaFile, Integer part,IActionEventHandler actionEventHandler) throws ActionException {
		String ext = FileHelper.getExtension(mediaFile);
		if (extensions!=null) {
			if (!extensions.contains(ext)) {
				return;
			}
		}
		try {
			if (restricted!=null && !mediaFile.getAbsolutePath().startsWith(dir.getPath(pm.getNewFilmName(dir.getMediaDirConfig(), restricted, film,  ext,part)).getAbsolutePath())) {
				return;
			}
			perform(dir,film,mediaFile,actionEventHandler);
		} catch (PatternException e) {
			throw new ActionException("Unable to calculate the '"+PARAM_RESTRICT_PATTERN+"' pattern",e);
		}
	}

	private void perform(MediaDirectory dir, IVideo video, File file,IActionEventHandler actionEventHandler) throws ActionException {

		IFeedFile feedFile = FeedFileFactory.createFile(file,dir.getMediaDirConfig(),video,mediaDirUrl );

		int index = Collections.binarySearch(feedFiles,feedFile,new Comparator<IFeedFile>() {
			@Override
			public int compare(IFeedFile o1, IFeedFile o2) {
				return o1.getLastModified().compareTo(o2.getLastModified());
			}
		});
		if (index < 0) {
		    feedFiles.add(-index-1,feedFile);
		}

		if (feedFiles.size()>numEntries) {
			Iterator<IFeedFile> it = feedFiles.iterator();
			it.next();
			it.remove();
		}
	}

	@Override
	public void finished(MediaDirectory dir) throws ActionException {
		File feedFile = getFeedFile();
		try {
			RSSFeed rssFeed = new RSSFeed(feedFile,mediaDirUrl,dir.getMediaDirConfig());
			rssFeed.createNewFeed();
			rssFeed.setTitle(feedTitle);
			rssFeed.setDescription(feedDescription);
			rssFeed.setLink(new URL(mediaDirUrl));

			for (IFeedFile file : feedFiles) {
				rssFeed.addEntry(file);
			}

			rssFeed.write();
		} catch (Exception e) {
			throw new ActionException("Unable to write pod case",e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Written RSS feed: " + feedFile);
		}
	}

	private File getFeedFile() {
		String loc = fileLocation;
		return dir.getPath(loc);
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		if (key.equalsIgnoreCase(PARAM_NUMBER_ENTRIES)) {
			try {
				numEntries = Integer.parseInt(value);
			}
			catch (NumberFormatException e) {
				throw new ActionException("Invalid number '"+value+"' for parameter '"+key+"'");
			}
		}
		else if (key.equalsIgnoreCase(PARAM_MEDIA_DIR_URL)) {
			mediaDirUrl = value;
		}
		else if (key.equalsIgnoreCase(PARAM_FILE_LOCATION)) {
			fileLocation = value;
		}
		else if (key.equalsIgnoreCase(PARAM_RESTRICT_PATTERN)) {
			restricted = value;
		}
		else if (key.equalsIgnoreCase(PARAM_EXTENSIONS_KEY)) {
			StringTokenizer tok = new StringTokenizer(value,",");
			this.extensions = new ArrayList<String>();
			while (tok.hasMoreTokens()) {
				extensions.add(tok.nextToken());
			}
		}
		else if (key.equalsIgnoreCase(PARAM_FEED_TITLE_KEY)) {
			feedTitle = value;
		}
		else if (key.equalsIgnoreCase(PARAM_FEED_DESCRIPTION_KEY)) {
			feedDescription = value;
		}
		else {
			throw new ActionException("Unsupported parameter '"+key+"'");
		}
	}




}

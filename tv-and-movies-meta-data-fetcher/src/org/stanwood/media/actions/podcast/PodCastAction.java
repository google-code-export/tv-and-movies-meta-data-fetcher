package org.stanwood.media.actions.podcast;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.util.FileHelper;

/**
 * <p>
 * This action is used create a pod cast of media that it finds. It will
 * add order the  most recent media files by the date they were last modified.
 * </p>
 * <p>This action supports the following parameters:
 * <ul>
 * <li>mediaDirURL - This is a required parameter that specifies the URL used to find the root media directory.</li>
 * <li>fileLocation - This is a required parameter that specifies the location of the RSS feed relative to the root of the media directory. It can contain standard rename patterns with the value.</li>
 * <li>numberEntries - The maximum number of entries in the feed. The default if not set is unlimited.</li>
 * <li>extensions - A comma separated list of media file extensions to accept.</li>
 * <li>restrictPattern - This can be used to restrict the media files. It can contain standard rename patterns with the value.</li>
 * <li>feedTitle - Used to give a title to the RSS feed</li>
 * <li>feedDescription - Used to give a description to the RSS feed</li>
 * </ul>
 * </p>
 * <p>
 * Parameters can also have variable in them. The following variables cane be used:
 * <ul>
 * <li>$HOME - The current users home directory.</li>
 * <li>$MEDIAFILE_NAME - The name part of the current media file been processed. So after the last file seperator, until it finds the extension.</li>
 * <li>$MEDIAFILE_EXT - The extension of the current media file been processed.</li>
 * <li>$MEDIAFILE_DIR - The directory the current media file is in.</li>
 * <li>$MEDIAFILE - The full path of the current media file.</li>
 * </ul>
 * </p>
 */
public class PodCastAction extends AbstractAction {

	private final static Log log = LogFactory.getLog(PodCastAction.class);

	private final static String PARAM_MEDIA_DIR_URL = "mediaDirURL";
	private final static String PARAM_NUMBER_ENTRIES = "numberEntries";
	private final static String PARAM_FILE_LOCATION = "fileLocation";
	private final static String PARAM_RESTRICT_PATTERN = "restrictPattern";
	private final static String PARAM_EXTENSIONS_KEY = "extensions";
	private final static String PARAM_FEED_TITLE_KEY = "feedTitle";
	private final static String PARAM_FEED_DESCRIPTION_KEY = "feedDescription";

	private SortedSet<IFeedFile>feedFiles = null;
	private Integer numEntries = null;
	private MediaDirectory dir;
	private String fileLocation;
	private String mediaDirUrl;
	private String restricted;
	private List<String> extensions;

	private String feedDescription = "";
	private String feedTitle = "";
	private File currentFeedFile = null;

	/**
	 * Used to setup the action and parse the podcast if it already exists
	 * @param dir The media directory
	 * @throws ActionException Thrown if their are any problems
	 */
	@Override
	public void init(MediaDirectory dir) throws ActionException {
		createNewFeedFiles();
		validateParameters();
		if (log.isDebugEnabled()) {
			log.debug("Init PodCast Action");
		}
		this.dir = dir;
	}

	private void createNewFeedFiles() {
		feedFiles = new TreeSet<IFeedFile>(new Comparator<IFeedFile>() {
			@Override
			public int compare(IFeedFile o1, IFeedFile o2) {
				return o1.compareTo(o2);
			}
		});
	}

	protected void parseFeed(IVideo video,File mediaFile,Integer part) throws ActionException {
		File feedFile = getFeedFile(video,mediaFile,part);
		try {
			if (currentFeedFile !=null && feedFile.equals(currentFeedFile)) {
				return ;
			}
			if (currentFeedFile!=null) {
				writeFeed(currentFeedFile);
			}
			if (feedFile.exists()) {
				RSSFeed rssFeed = new RSSFeed(feedFile,mediaDirUrl,this.dir.getMediaDirConfig() );
				rssFeed.parse();
				for (IFeedFile ff : rssFeed.getEntries()) {
					if (!containsFeedFile(ff)) {
						feedFiles.add(ff);
					}
				}
			}

			currentFeedFile = feedFile;
		}
		catch (Exception e) {
			throw new ActionException("Unable to parse RSS feed: " + feedFile ,e);
		}
	}

	private boolean containsFeedFile(IFeedFile ff1) {
		for (IFeedFile ff2 : feedFiles) {
			if (ff2.hashCode()==ff1.hashCode()) {
				return true;
			}
		}
		return false;
	}

	private void removeIfFound(IFeedFile ff) {
		Iterator<IFeedFile>it = feedFiles.iterator();
		while (it.hasNext()) {
			if (it.next().hashCode()==ff.hashCode()) {
				it.remove();
			}
		}
	}

	protected void writeFeed(File feedFile) throws ActionException {
		if (isTestMode()) {
			log.info("Podcast feed not written as in test mode: " + feedFile);
			return;
		}
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
			createNewFeedFiles();
			log.info("Written Podcast feed: " + feedFile);
		} catch (Exception e) {
			throw new ActionException("Unable to write pod case",e);
		}
	}

	private void validateParameters() throws ActionException {
		List<String>missingParams = new ArrayList<String>();
		if (fileLocation==null) {
			missingParams.add(PARAM_FILE_LOCATION);
		}
		if (mediaDirUrl==null) {
			missingParams.add(PARAM_MEDIA_DIR_URL);
		}
		if (missingParams.size()>0) {
			StringBuilder buffer = new StringBuilder("Missing required parameters for PodCastAction: ");
			for (int i=0;i<missingParams.size();i++) {
				if (i>0) {
					buffer.append(", ");
				}
				buffer.append("'"+missingParams.get(i)+"'");
			}
			throw new ActionException(buffer.toString());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void perform(MediaDirectory dir, Episode episode, File mediaFile,IActionEventHandler actionEventHandler) throws ActionException {

		String ext = FileHelper.getExtension(mediaFile);
		if (extensions!=null) {
			if (!extensions.contains(ext)) {
				return;
			}
		}
		if (restricted!=null) {
			String r = resolvePatterns(dir,restricted,episode,mediaFile,null);
			if (!mediaFile.getAbsolutePath().startsWith(dir.getPath(r).getAbsolutePath())) {
				return;
			}
		}
		parseFeed(episode,mediaFile,null);
		perform(dir,(IVideo)episode,mediaFile,actionEventHandler);
	}

	/** {@inheritDoc} */
	@Override
	public void perform(MediaDirectory dir, Film film, File mediaFile, Integer part,IActionEventHandler actionEventHandler) throws ActionException {

		String ext = FileHelper.getExtension(mediaFile);
		if (extensions!=null) {
			if (!extensions.contains(ext)) {
				return;
			}
		}
		if (restricted!=null && !mediaFile.getAbsolutePath().startsWith(dir.getPath(resolvePatterns(dir,restricted, film,mediaFile,part)).getAbsolutePath())) {
			return;
		}
		parseFeed(film,mediaFile,part);
		perform(dir,film,mediaFile,actionEventHandler);
	}

	private void perform(MediaDirectory dir, IVideo video, File file,IActionEventHandler actionEventHandler) throws ActionException {
		IFeedFile feedFile = FeedFileFactory.createFile(file,dir.getMediaDirConfig(),video,mediaDirUrl );
		addFileToList(feedFile);
	}

	protected void addFileToList(IFeedFile feedFile) {
		removeIfFound(feedFile);
		feedFiles.add(feedFile);

		if (numEntries!=null && feedFiles.size()>numEntries) {
			Iterator<IFeedFile> it = feedFiles.iterator();
			it.next();
			it.remove();
		}
	}

	/**
	 * Used to write the podcast
	 * @param dir The media directory
	 * @throws ActionException Thrown if their are any problems
	 */
	@Override
	public void finished(MediaDirectory dir) throws ActionException {
		if (currentFeedFile!=null) {
			writeFeed(currentFeedFile);
		}
	}



	private File getFeedFile(IVideo video,File mediaFile,Integer part) throws ActionException {
		String loc = resolvePatterns(dir,fileLocation, video, mediaFile, part);
		return dir.getPath(loc);
	}

	/**
	 * <p>Used to set parameters on the action</p>
	 * <p>
	 * <ul>
	 * <li>mediaDirURL - This is a required parameter that specifies the URL used to find the root media directory.</li>
	 * <li>fileLocation - This is a required parameter that specifies the location of the RSS feed relative to the root of the media directory. It can contain standard rename patterns with the value.</li>
	 * <li>numberEntries - The maximum number of entries in the feed. The default if not set is unlimited.</li>
	 * <li>extensions - A comma separated list of media file extensions to accept.</li>
	 * <li>restrictPattern - This can be used to restrict the media files. It can contain standard rename patterns with the value.</li>
	 * <li>feedTitle - Used to give a title to the RSS feed</li>
	 * <li>feedDescription - Used to give a description to the RSS feed</li>
	 * </ul>
	 * </p>
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 * @throws ActionException Thrown if their is a problem setting the parameter
	 */
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

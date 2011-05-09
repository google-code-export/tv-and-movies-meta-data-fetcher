package org.stanwood.media.actions.podcast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndImageImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * This class is used to create and update RSS feeds used to store the pod cast details
 */
public class RSSFeed {

	private final static Log log = LogFactory.getLog(RSSFeed.class);

	private final static String FEED_TYPE = "rss_2.0";

	private SyndFeed feed;
	private File feedFile;

	/**
	 * The constructor used to create a instance of the RSSFeed object
	 * @param feedFile The file used to store the pod cast RSS feed
	 */
	public RSSFeed(File feedFile) {
		this.feedFile = feedFile;
	}

	/**
	 * Used to parse the RSS feed
	 * @throws IOException Thrown if their is a problem reading the RSS feed
	 * @throws FeedException Thrown if their is a problem parsing the RSS feed
	 */
	public void parse() throws IOException, FeedException {
		SyndFeedInput input = new SyndFeedInput();
		feed = input.build(feedFile);
	}

	/**
	 * Used to create a new feed
	 */
	public void createNewFeed() {
		log.info("Creating new rss feed " + feedFile);
		feed = new SyndFeedImpl();
		feed.setFeedType(FEED_TYPE);
	}

	/**
	 * Used to set the feeds title
	 * @param title The title of the feed
	 */
	public void setTitle(String title) {
		if (title != null) {
			feed.setTitle(title);
		}
	}

	/**
	 * Used to set the feeds link
	 * @param link The link to the feed
	 */
	public void setLink(URL link) {
		if (link != null) {
			feed.setLink(link.toExternalForm());
		}
	}

	/**
	 * Used to set the feeds description
	 * @param description A plain text description of the feeds contents
	 */
	public void setDescription(String description) {
		if (description != null) {
			feed.setDescription(description);
		}
	}

	/**
	 * Used to set the feeds artwork
	 * @param imageUrl A URL to the feeds cover artwork
	 */
	public void setArtwork(URL imageUrl) {
		if (imageUrl != null) {
			SyndImage image = new SyndImageImpl();
			image.setUrl(imageUrl.toExternalForm());
			if (feed.getTitle()!=null) {
				image.setTitle(feed.getTitle());
			}
			else {
				image.setTitle("No Title");
			}
			feed.setImage(image);
		}
	}

	/**
	 * Write the RSS feed to a file
	 * @throws IOException Thrown if their is a problem reading the RSS feed
	 * @throws FeedException Thrown if their is a problem parsing the RSS feed
	 */
	public void write() throws IOException, FeedException {
		if (feed==null) {
			throw new FeedException("parse() or createNewFeed() must be called before writing the feed");
		}
		SyndFeedOutput output = new SyndFeedOutput();
		if (feed.getTitle()==null) {
			throw new FeedException("Unable to write feed, feed has no title");
		}
		if (feed.getDescription()==null) {
			throw new FeedException("Unable to write feed, feed has no description");
		}
		if (feed.getLink()==null) {
			throw new FeedException("Unable to write feed, feed has no link");
		}
		output.output(feed, feedFile);
	}

	/**
	 * Used to add a pod cast entry to the RSS feed
	 * @param title The title of the entry
	 * @param link The link to the entry
	 * @param publishDate The date the entry was published
	 * @param plainDescription The plain text description of the entry
	 * @param author The author of the entry
	 * @param audioFile The audio file
	 */
	@SuppressWarnings("unchecked")
	public void addEntry(String title, URL link, Date publishDate, String plainDescription, String author,IFeedFile audioFile) {
		SyndEntry entry = new SyndEntryImpl();
		if (title != null) {
			entry.setTitle(title);
		}

		entry.setLink(link.toExternalForm());
		entry.setPublishedDate(publishDate);

		List<SyndEnclosure> es = new ArrayList<SyndEnclosure>();
		SyndEnclosure e = new SyndEnclosureImpl();
		e.setType(audioFile.getContentType());
		e.setUrl(link.toExternalForm());
		e.setLength(audioFile.getFile().length());
		es.add(e);
		entry.setEnclosures(es);

		if (plainDescription != null) {
			SyndContent description = new SyndContentImpl();
			description.setType("text/plain");
			description.setValue(plainDescription);
			entry.setDescription(description);
		}
		entry.setAuthor(author);

		feed.getEntries().add(0, entry);
		log.info("Entry added to rss feed" + feedFile);
	}

	/**
	 * Used to set the maximum number of entries in the feed. If their are more
	 * entries in the feed than the max entries, then they are removed. Also the
	 * associated files are deleted.
	 * @param maxEntries Maximum number of entries
	 * @param rssDir The directory containing the RSS directory
	 */
	@SuppressWarnings("unchecked")
	public void setMaxEntries(long maxEntries, File rssDir) {
		Iterator<SyndEntry> it = feed.getEntries().iterator();
		int count = 0;
		while (it.hasNext()) {
			SyndEntry entry = it.next();
			count++;
			if (count > maxEntries) {
				String link = entry.getLink();
				String fileName = link.substring(link.lastIndexOf('/') + 1);
				File entryFile = new File(rssDir, fileName);
				if (entryFile.exists()) {
					if (!entryFile.delete()) {
						log.error("Unable to delete file : " + entryFile);
					}
				}

				it.remove();
				log.info("Old entry " + link + " removed from feed");
			}
		}
	}
}

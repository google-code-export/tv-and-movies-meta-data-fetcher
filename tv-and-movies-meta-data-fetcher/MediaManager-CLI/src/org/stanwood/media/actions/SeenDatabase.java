package org.stanwood.media.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.xml.IterableNodeList;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to maintain a list of files that have been seen by the actions
 * in media directories
 */
public class SeenDatabase extends XMLParser {

	private Map<File,SortedSet<SeenEntry>> entries = new HashMap<File,SortedSet<SeenEntry>>();
	private File seenFile;

	/**
	 * The constructor
	 * @param configDir The configuration directory
	 */
	public SeenDatabase(File configDir) {
		seenFile = new File(configDir,"seenFiles.xml"); //$NON-NLS-1$
	}

	/**
	 * Used to work out if a file has been seen already
	 * @param mediaDirectory The media directory the file lives in
	 * @param file The file
	 * @return True if seen, otherwise false
	 */
	public boolean isSeen(File mediaDirectory,File file) {
		Set<SeenEntry>entryList = entries.get(mediaDirectory);
		if (entryList!=null) {
			for (SeenEntry entry : entryList) {
				if (entry.getFileName().equals(file.getAbsolutePath())) {
					if (file.lastModified()==entry.getLastModified()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Used to mark a file as seen after actions have seen it
	 * @param mediaDirectory The media directory the file lives in
	 * @param file The file
	 */
	public void markAsSeen(File mediaDirectory,File file) {
		long lastModified = file.lastModified();
		String path = file.getAbsolutePath();
		markAsSeen(mediaDirectory, lastModified, path);
	}

	private void markAsSeen(File mediaDirectory,  long lastModified,
			String path) {
		SortedSet<SeenEntry>entryList = entries.get(mediaDirectory);
		if (entryList==null) {
			entryList = new TreeSet<SeenEntry>(new Comparator<SeenEntry>() {
				@Override
				public int compare(SeenEntry o1, SeenEntry o2) {
					return o1.getFileName().compareTo(o2.getFileName());
				}
			});
			entries.put(mediaDirectory, entryList);
		}

		Iterator<SeenEntry>it = entryList.iterator();
		while (it.hasNext()) {
			SeenEntry entry = it.next();
			if (entry.getFileName().equals(path)) {
				it.remove();
			}
		}

		SeenEntry entry = new SeenEntry();
		entry.setFileName(path);
		entry.setLastModified(lastModified);
		entryList.add(entry);
	}

	/**
	 * Used to write the database to disc
	 * @param parentMonitor Parent progress monitor
	 * @throws FileNotFoundException Thrown if their is a problem
	 */
	public void write(IProgressMonitor parentMonitor) throws FileNotFoundException {
		PrintStream ps = null;
		try {
			ps = new PrintStream(seenFile);
			ps.println("<seen>"); //$NON-NLS-1$
			Set<Entry<File, SortedSet<SeenEntry>>> entriesSet = entries.entrySet();
			parentMonitor.beginTask(Messages.getString("SeenDatabase.WRITING_SEEN_DB"), entriesSet.size()); //$NON-NLS-1$
			for (Entry<File,SortedSet<SeenEntry>> e : entriesSet) {
				ps.println("  <mediaDir dir=\""+e.getKey()+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
				for (SeenEntry entry : e.getValue()) {
					if (new File(entry.getFileName()).exists()) {
						ps.println("    <file path=\""+StringEscapeUtils.escapeXml(entry.getFileName())+"\" lastModified=\""+entry.getLastModified()+"\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				ps.println("  </mediaDir>"); //$NON-NLS-1$
				parentMonitor.worked(1);
			}
			ps.println("</seen>"); //$NON-NLS-1$
			parentMonitor.done();
		}
		finally {
			if (ps!=null) {
				ps.close();
			}
		}
	}

	/**
	 * Used to read the database from disk
	 * @param parentMonitor Progress monitor
	 * @throws FileNotFoundException Thrown if their is a problem
	 * @throws XMLParserException Thrown if possible to parse file
	 *
	 */
	public void read(IProgressMonitor progress) throws FileNotFoundException, XMLParserException {
		entries = new HashMap<File,SortedSet<SeenEntry>>();
		if (seenFile.exists()) {
			Document doc = XMLParser.parse(seenFile, null);
			IterableNodeList nodes = selectNodeList(doc, "seen/mediaDir"); //$NON-NLS-1$
			progress.beginTask(Messages.getString("SeenDatabase.READING_SEEN_DB"), nodes.getLength()); //$NON-NLS-1$
			for (Node mediaDirNode : nodes) {
				Element mediaDirEl = (Element)mediaDirNode;
				File mediaDir = new File(mediaDirEl.getAttribute("dir")); //$NON-NLS-1$
				for (Node fileNode : selectNodeList(mediaDirEl,"file")) { //$NON-NLS-1$
					Element fileElement = (Element)fileNode;
					String path = fileElement.getAttribute("path"); //$NON-NLS-1$
					long lastModified = Long.parseLong(fileElement.getAttribute("lastModified")); //$NON-NLS-1$
					markAsSeen(mediaDir, lastModified, path);
				}
				progress.worked(1);
			}
		}
		progress.done();
	}

	/**
	 * Used to notify the seen database when a file has been renamed
	 * @param mediaDirectory The media directory the file is located in
	 * @param oldFile The old filename
	 * @param newFile The new filename
	 */
	public void renamedFile(File mediaDirectory, File oldFile, File newFile) {
		removeFile(mediaDirectory, oldFile);
		markAsSeen(mediaDirectory, newFile);
	}

	/**
	 * Used to remove files from the Seen database
	 * @param mediaDirectory The media directory of the file to remove
	 * @param file The file to remove
	 */
	public void removeFile(File mediaDirectory, File file) {
		Set<SeenEntry>entryList = entries.get(mediaDirectory);
		if (entryList!=null) {
			Iterator<SeenEntry>it = entryList.iterator();
			while (it.hasNext()) {
				SeenEntry entry = it.next();
				if (entry.getFileName().equals(file.getAbsolutePath())) {
					it.remove();
				}
			}
		}
	}
}

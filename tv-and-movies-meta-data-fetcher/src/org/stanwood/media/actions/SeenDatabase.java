package org.stanwood.media.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	private Map<File,Set<SeenEntry>> entries = new HashMap<File,Set<SeenEntry>>();
	private File seenFile;

	/**
	 * The constructor
	 * @param configDir The configuration directory
	 */
	public SeenDatabase(File configDir) {
		seenFile = new File(configDir,"seenFiles.xml");
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
		Set<SeenEntry>entryList = entries.get(mediaDirectory);
		if (entryList==null) {
			entryList = new HashSet<SeenEntry>();
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
	 * @throws FileNotFoundException Thrown if their is a problem
	 */
	public void write() throws FileNotFoundException {
		PrintStream ps = null;
		try {
			ps = new PrintStream(seenFile);
			ps.println("<seen>");
			for (Entry<File,Set<SeenEntry>> e : entries.entrySet()) {
				ps.println("  <mediaDir dir=\""+e.getKey()+"\">");
				for (SeenEntry entry : e.getValue()) {
					ps.println("    <file path=\""+entry.getFileName()+"\" lastModified=\""+entry.getLastModified()+"\"/>");
				}
				ps.println("  </mediaDir>");
			}
			ps.println("</seen>");
		}
		finally {
			if (ps!=null) {
				ps.close();
			}
		}
	}

	/**
	 * Used to read the database from disk
	 * @throws FileNotFoundException Thrown if their is a problem
	 * @throws XMLParserException Thrown if possible to parse file
	 */
	public void read() throws FileNotFoundException, XMLParserException {
		entries = new HashMap<File,Set<SeenEntry>>();
		if (seenFile.exists()) {
			Document doc = XMLParser.parse(seenFile, null);
			for (Node mediaDirNode : selectNodeList(doc, "seen/mediaDir")) {
				Element mediaDirEl = (Element)mediaDirNode;
				File mediaDir = new File(mediaDirEl.getAttribute("dir"));
				for (Node fileNode : selectNodeList(mediaDirEl,"file")) {
					Element fileElement = (Element)fileNode;
					String path = fileElement.getAttribute("path");
					long lastModified = Long.parseLong(fileElement.getAttribute("lastModified"));
					markAsSeen(mediaDir, lastModified, path);
				}
			}
		}
	}
}

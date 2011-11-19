package org.stanwood.media.store.mp4.mp4v2cli;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.stanwood.media.jna.NativeHelper;
import org.stanwood.media.logging.LoggerOutputStream;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4AtomKey;
import org.stanwood.media.store.mp4.MP4AtomKeyType;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.MP4ITunesStore;

/**
 * A new MP4 manager based on the MP4v2 command line tools {@link "http://code.google.com/p/mp4v2/"}.
 */
public class MP4v2CLIManager implements IMP4Manager {

	private final static Log log = LogFactory.getLog(MP4v2CLIManager.class);

	private final static Pattern TAG_LIST_PATTERN = Pattern.compile("^ (.+?)\\: (.+)$"); //$NON-NLS-1$
	private final static Pattern ART_LIST_PATTERN = Pattern.compile("^ +(\\d+) +(\\d+) +([\\dabcdef]+) +(.*?) +(.*)$"); //$NON-NLS-1$
	private final static Pattern RANGE_PATTERN = Pattern.compile("(\\d+) of (\\d+)",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	private static final Pattern FILE_LIST_PATTERN = Pattern.compile("^(.*?) +(.*?) +(.*?) +(.*)$",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	private String mp4artPath = null;
	private String mp4infoPath = null;
	private String mp4tagsPath = null;
	private String mp4filePath = null;

	private boolean extended = false;

	/** {@inheritDoc} */
	@Override
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception {
		if (!mp4File.exists()) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_FIND_MP4_FILE"),mp4File)); //$NON-NLS-1$
		}
		List<IAtom> atoms = new ArrayList<IAtom>();
		String output = getCommandOutput(true,false,true,mp4infoPath,mp4File);


		BufferedReader reader = null;
		try {
			reader =new BufferedReader(new StringReader(output));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = TAG_LIST_PATTERN.matcher(line);
				if (m.matches()) {
					String name = m.group(1);
					String value = m.group(2);
					MP4AtomKey key = nameToAtomKey(name);
					if (key!=null) {
						if (isArtwork(key)) {
							parseArtwork(atoms,mp4File);
						}
						else {
	 						IAtom atom = parseAtom(key,value);
							if (atom!=null) {
								atoms.add(atom);
							}
						}
					}
				}
			}

		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.CANT_LIST_ATOMS"),mp4File),e); //$NON-NLS-1$
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(Messages.getString("MP4v2CLIManager.UNABLE_CLOSE_STREAM"),e); //$NON-NLS-1$
				}
			}
		}

		return atoms;
	}

	private void parseArtwork(List<IAtom> atoms, File mp4File) throws MP4Exception {
		String output = getCommandOutput(true,false,true,mp4artPath,"--list",mp4File); //$NON-NLS-1$

		BufferedReader reader = null;
		try {
			reader =new BufferedReader(new StringReader(output));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = ART_LIST_PATTERN.matcher(line);
				if (m.matches()) {
					int index = Integer.parseInt(m.group(1));
					int size = Integer.parseInt(m.group(2));
					String type = m.group(4);
					MP4ArtworkType artType = convertArtRawType(type);
					atoms.add(new MP4v2CLIAtomArtworkSummary(MP4AtomKey.ARTWORK,index,size,artType));
				}
			}
		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.CANT_READ_ARTWORK_DETAILS"),mp4File),e); //$NON-NLS-1$
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(Messages.getString("MP4v2CLIManager.UNABLE_CLOSE_STREAM"),e); //$NON-NLS-1$
				}
			}
		}
	}

	private MP4ArtworkType convertArtRawType(String type) {
		if (type.equalsIgnoreCase("jpeg")) { //$NON-NLS-1$
			return MP4ArtworkType.MP4_ART_JPEG;
		}
		else if (type.equalsIgnoreCase("png")) { //$NON-NLS-1$
			return MP4ArtworkType.MP4_ART_PNG;
		}
		else if (type.equalsIgnoreCase("gif")) { //$NON-NLS-1$
			return MP4ArtworkType.MP4_ART_GIF;
		}
		else if (type.equalsIgnoreCase("bmp")) { //$NON-NLS-1$
			return MP4ArtworkType.MP4_ART_BMP;
		}
		return  MP4ArtworkType.MP4_ART_UNDEFINED;
	}

	private IAtom parseAtom(MP4AtomKey key, String value) throws MP4Exception {
		IAtom atom = null;
		if (isBoolean(key)) {
			int ivalue = 0;
			if (value.equalsIgnoreCase("yes")) { //$NON-NLS-1$
				ivalue = 1;
			}
			atom = createAtom(key, ivalue);
		}
		else if (key.getType()==MP4AtomKeyType.Enum) {
			if (key == MP4AtomKey.MEDIA_TYPE) {
				int ivalue = -1;
				if (value.equalsIgnoreCase("Old Movie")) { //$NON-NLS-1$
					ivalue = 0;
				}
				else if (value.equalsIgnoreCase("Normal")) { //$NON-NLS-1$
					ivalue = 1;
				}
				else if (value.equalsIgnoreCase("Audio Book")) { //$NON-NLS-1$
					ivalue = 2;
				}
				else if (value.equalsIgnoreCase("Music Video")) { //$NON-NLS-1$
					ivalue = 6;
				}
				else if (value.equalsIgnoreCase("Movie")) { //$NON-NLS-1$
					ivalue = 6;
				}
				else if (value.equalsIgnoreCase("TV Show")) { //$NON-NLS-1$
					ivalue = 10;
				}
				else if (value.equalsIgnoreCase("Booklet")) { //$NON-NLS-1$
					ivalue = 11;
				}
				else if (value.equalsIgnoreCase("Ringtone")) { //$NON-NLS-1$
					ivalue = 14;
				}
				atom = createAtom(key, ivalue);
			}
			else if (key == MP4AtomKey.RATING) {
				int ivalue = -1;
				if (value.equalsIgnoreCase("None")) { //$NON-NLS-1$
					ivalue = 0;
				}
				else if (value.equalsIgnoreCase("Clean")) { //$NON-NLS-1$
					ivalue = 2;
				}
				else if (value.equalsIgnoreCase("Explicit")) { //$NON-NLS-1$
					ivalue = 4;
				}
				atom = createAtom(key, ivalue);
			}
			else {
				atom = createAtom(key, value);
			}
		}
		else if (isRange(key)) {
			Matcher m = RANGE_PATTERN.matcher(value);
			if (m.matches()) {
				atom = createAtom(key, Short.parseShort(m.group(1)),Short.parseShort(m.group(2)));
			}
			else {
				throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_PARSE_RANGE"),value)); //$NON-NLS-1$
			}
		}
		else if (isArtwork(key)) {
			// Handle else where
		}
		else {
			atom = createAtom(key, value);
		}
		return atom;
	}

	private boolean isArtwork(MP4AtomKey key) {
		return key.getType()==MP4AtomKeyType.Artwork;
	}

	private boolean isRange(MP4AtomKey key) {
		return key.getType()==MP4AtomKeyType.Range;
	}

	private boolean isBoolean(MP4AtomKey key) {
		return key.getType()==MP4AtomKeyType.Boolean;
	}

	private MP4AtomKey nameToAtomKey(String name) {
		if (name.equals("Name")) { //$NON-NLS-1$
			return MP4AtomKey.NAME;
		}
		if (name.equals("Release Date")) { //$NON-NLS-1$
			return MP4AtomKey.RELEASE_DATE;
		}
		if (name.equals("Disk")) { //$NON-NLS-1$
			return MP4AtomKey.DISK_NUMBER;
		}
		if (name.equals("TV Show")) { //$NON-NLS-1$
			return MP4AtomKey.TV_SHOW_NAME;
		}
		if (name.equals("TV Episode Number")) { //$NON-NLS-1$
			return MP4AtomKey.TV_EPISODE_ID;
		}
		if (name.equals("TV Season")) { //$NON-NLS-1$
			return MP4AtomKey.TV_SEASON;
		}
		if (name.equals("TV Episode")) { //$NON-NLS-1$
			return MP4AtomKey.TV_EPISODE;
		}
		if (name.equals("Short Description")) { //$NON-NLS-1$
			return MP4AtomKey.DESCRIPTION_SHORT;
		}
		if (name.equals("Long Description")) { //$NON-NLS-1$
			return MP4AtomKey.DESCRIPTION_LONG;
		}
		if (name.equals("Artist")) { //$NON-NLS-1$
			return MP4AtomKey.ARTIST;
		}
		if(name.equals("Cover Art pieces")) { //$NON-NLS-1$
			return MP4AtomKey.ARTWORK;
		}
		if (name.equals("Encoded with")) { //$NON-NLS-1$
			return MP4AtomKey.ENCODING_TOOL;
		}
		if(name.equals("Media Type")) { //$NON-NLS-1$
			return MP4AtomKey.MEDIA_TYPE;
		}
		if(name.equals("Content Rating")) { //$NON-NLS-1$
			return MP4AtomKey.RATING;
		}
		if (name.equals("Genre")) { //$NON-NLS-1$
			return MP4AtomKey.GENRE_USER_DEFINED;
		}
		if (name.equals("Category")) { //$NON-NLS-1$
			return MP4AtomKey.CATEGORY;
		}
		if (name.equals("HD Video")) { //$NON-NLS-1$
			return MP4AtomKey.HD;
		}
		if (name.equals("Album")) { //$NON-NLS-1$
			return MP4AtomKey.ALBUM;
		}
		if (name.equals("Album Artist")) {
			return MP4AtomKey.ALBUM_ARTIST;
		}
		if (name.equals("Sort Album")) {
			return MP4AtomKey.SORT_ALBUM;
		}
		if (name.equals("Track")) {
			return MP4AtomKey.TRACK_NUMBER;
		}
		return null;
	}


	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name, String value) {
		return new MP4v2CLIAtomString(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name, int value) {
		return new MP4v2CLIAtomInteger(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name,short number, short total) {
		return new MP4v2CLIAtomRange(name,number,total );
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name,MP4ArtworkType type, int size, byte data[]) {
		return new MP4v2CLIAtomArtwork(this,name,type,size,data);
	}

	/**
	 * This checks that the stores system commands can be found before the store is used.
	 * @throws MP4Exception Thrown if their is a problem locating the commands
	 */
	@Override
	public void init(File nativeDir) throws MP4Exception {
		if (mp4infoPath == null) {
			mp4infoPath = NativeHelper.getNativeApplication(nativeDir,"mp4info"); //$NON-NLS-1$
		}
		if (mp4artPath == null) {
			mp4artPath = NativeHelper.getNativeApplication(nativeDir,"mp4art"); //$NON-NLS-1$
		}
		if (mp4tagsPath == null) {
			mp4tagsPath = NativeHelper.getNativeApplication(nativeDir,"mp4tags"); //$NON-NLS-1$
		}
		if (mp4filePath == null) {
			mp4filePath = NativeHelper.getNativeApplication(nativeDir,"mp4file"); //$NON-NLS-1$
		}
		boolean errors = false;
		if (!checkCommand(mp4infoPath)) {
			log.error(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_EXECUTE_CMD"),mp4infoPath)); //$NON-NLS-1$
			errors = true;
		}
		if (!checkCommand(mp4artPath)) {
			log.error(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_EXECUTE_CMD"),mp4artPath)); //$NON-NLS-1$
			errors = true;
		}
		if (!checkCommand(mp4filePath)) {
			log.error(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_EXECUTE_CMD"),mp4filePath)); //$NON-NLS-1$
			errors = true;
		}
		if (!checkTagsCommand()) {
			log.error(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_EXECUTE_CMD"),mp4tagsPath)); //$NON-NLS-1$
			errors = true;
		}
		else if (!extended) {
			log.warn(MessageFormat.format(Messages.getString("MP4v2CLIManager.WRONG_MP4V2_VERSION"),MP4ITunesStore.class.getName())); //$NON-NLS-1$
		}
		if (errors) {
			throw new MP4Exception(Messages.getString("MP4v2CLIManager.SYSTEM_CMD_NOT_FOUND")); //$NON-NLS-1$
		}
	}

	private boolean checkCommand(String cmd) {
		try {
			boolean capture = !log.isDebugEnabled();
			getCommandOutput(capture,capture,false,cmd);
		}
		catch (MP4Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Command failed",e); //$NON-NLS-1$
			}
			return false;
		}
		return true;
	}

	private boolean checkTagsCommand() {
		try {
			String output = getCommandOutput(true,true,false,mp4tagsPath);
			if (output.contains("-category") && output.contains("-longdesc") && output.contains("-rating")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				extended = true;
			}
		}
		catch (MP4Exception e) {
			return false;
		}
		return true;
	}

	String getCommandOutput(boolean captureStdout,boolean captureStderr,boolean failOnExitCode,String command,Object ... args) throws MP4Exception {
		CommandLine cmdLine= new CommandLine(command);
		for (Object arg : args) {
			if (arg instanceof File) {
				cmdLine.addArgument(((File)arg).getAbsolutePath(),false);
			}
			else if (arg instanceof String) {
				cmdLine.addArgument((String)arg,false);
			}
		}
		Executor exec = new DefaultExecutor();
		exec.setExitValues(new int[] {0,1,2,3,4,5,6,7,8,9});

		try {
			ByteArrayOutputStream capture = new ByteArrayOutputStream();
			OutputStream out = new LoggerOutputStream(Level.INFO);
			if (captureStdout) {
				out = capture;
			}
			OutputStream err = new LoggerOutputStream(Level.ERROR);
			if (captureStderr) {
				err = capture;
			}
			exec.setStreamHandler(new PumpStreamHandler(out,err));
			int exitCode = exec.execute(cmdLine);
			if (failOnExitCode && exitCode!=0) {
				throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.NON_ZERO_EXIT_CODE"),exitCode,cmdLine.toString())); //$NON-NLS-1$
			}
			return capture.toString();
		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.UNABLE_EXECUTE_SYS_CMD") ,cmdLine.toString()),e); //$NON-NLS-1$
		}
	}

	/**
	 * <p>Used to set the managers parameters.</p>
	 * <p>This manager has following optional parameters:
	 * 	<ul>
	 * 		<li>mp4art - The path to the mp4art command</li>
	 *      <li>mp4info - The path to the mp4info command</li>
	 *      <li>mp4tags - The path to the mp4tags command</li>
	 *      <li>mp4file - The path to the mp4file command</li>
	 *  </ul>
	 * </p>
	 * @param key The name of the parameter
	 * @param value The value of the parameter
	 */
	@Override
	public void setParameter(String key, String value) {
		if (key.equalsIgnoreCase("mp4art")){ //$NON-NLS-1$
			mp4artPath = value;
		}
		else if (key.equalsIgnoreCase("mp4info")) { //$NON-NLS-1$
			mp4infoPath = value;
		}
		else if (key.equalsIgnoreCase("mp4tags")) { //$NON-NLS-1$
			mp4tagsPath = value;
		}
		else if (key.equalsIgnoreCase("mp4file")) { //$NON-NLS-1$
			mp4filePath = value;
		}
	}

	private boolean hasArtwrokAtom(List<IAtom> atoms) {
		for (IAtom atom : atoms) {
			if (atom.getName().equals("covr")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		if (log.isDebugEnabled()) {
			log.debug("Upadting MP4 file '" + mp4File+"' with "+atoms.size()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		checkAppleListItemBoxExists(mp4File);
		if (hasArtwrokAtom(atoms)) {
			try {
				getCommandOutput(true,true,true,mp4artPath, "--remove","--art-any",mp4File); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch (MP4Exception e) {
				if (e.getMessage().contains("non zero exit code")) { //$NON-NLS-1$
					// This is ok as their was no art
				}
			}
		}

		List<Object>args = new ArrayList<Object>();
		for (IAtom atom : atoms) {
			((AbstractCLIMP4v2Atom)atom).writeAtom(mp4File,extended,args);

		}
		args.add(mp4File);
		getCommandOutput(true,false,true,mp4tagsPath,args.toArray(new Object[args.size()]));

		if (log.isDebugEnabled()) {
			log.debug("MP4 modified '" + mp4File+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void checkAppleListItemBoxExists(File mp4File) throws MP4Exception {
		String output = getCommandOutput(true,false,true,mp4filePath, "--list",mp4File); //$NON-NLS-1$
		BufferedReader reader = null;
		try {
			reader =new BufferedReader(new StringReader(output));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = FILE_LIST_PATTERN.matcher(line);
				if (m.matches()) {
					if (m.group(2).contains("mp42")) { //$NON-NLS-1$
						return;
					}
				}
			}
		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.CANT_READ_TYPE_FILE"),mp4File),e); //$NON-NLS-1$
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Unable to close stream",e); //$NON-NLS-1$
				}
			}
		}
		throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIManager.NOT_CORRECT_VERSION"),mp4File)); //$NON-NLS-1$
	}

	String getMP4ArtCommand() {
		return mp4artPath;
	}

}

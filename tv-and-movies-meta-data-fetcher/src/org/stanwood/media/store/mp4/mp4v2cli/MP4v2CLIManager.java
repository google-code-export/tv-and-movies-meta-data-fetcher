package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
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
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.MP4ITunesStore;

/**
 * A new MP4 manager based on the MP4v2 command line tools {@link "http://code.google.com/p/mp4v2/"}.
 */
public class MP4v2CLIManager implements IMP4Manager {

	private final static Log log = LogFactory.getLog(MP4v2CLIManager.class);

	private final static Pattern TAG_LIST_PATTERN = Pattern.compile("^ (.+?)\\: (.+)$");
	private final static Pattern ART_LIST_PATTERN = Pattern.compile("^ +(\\d+) +(\\d+) +([\\dabcdef]+) +(.*?) +(.*)$");
	private final static Pattern RANGE_PATTERN = Pattern.compile("(\\d+) of (\\d+)",Pattern.CASE_INSENSITIVE);

	private static final Pattern FILE_LIST_PATTERN = Pattern.compile("^(.*?) +(.*?) +(.*?) +(.*)$",Pattern.CASE_INSENSITIVE);

	private String ATOM_BOOLEAN_KEYS[] = new String[] {"hdvd"};
//	private String ATOM_STRING_KEYS[] = new String[] {"©nam","©day","tvsh","desc","ldes","©ART","©too","©gen","catg","tven"};
	private String ATOM_NUMBER_KEYS[] = new String[] {"stik","rtng","tvsn","tves"};
	private String ATOM_RANGE_KEYS[] = new String[] {"disk"};
	private String ATOM_ARTWORK_KEYS[] = new String[] {"covr"};

	private String mp4artPath = null;
	private String mp4infoPath = null;
	private String mp4tagsPath = null;
	private String mp4filePath = null;

	private boolean extended = false;

	/** {@inheritDoc} */
	@Override
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception {
		if (!mp4File.exists()) {
			throw new MP4Exception("Unable to find mp4 file: " + mp4File);
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
					String key = nameToAtomKey(name);
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
			throw new MP4Exception("Can't list mp4 file atoms: " + mp4File,e);
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Unable to close stream",e);
				}
			}
		}

		return atoms;
	}

	private void parseArtwork(List<IAtom> atoms, File mp4File) throws MP4Exception {
		String output = getCommandOutput(true,false,true,mp4artPath,"--list",mp4File);

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
					atoms.add(new MP4v2CLIAtomArtworkSummary("covr",index,size,artType));
				}
			}
		} catch (IOException e) {
			throw new MP4Exception("Can't read mp4 file artwork details: " + mp4File,e);
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Unable to close stream",e);
				}
			}
		}
	}

	private MP4ArtworkType convertArtRawType(String type) {
		if (type.equalsIgnoreCase("jpeg")) {
			return MP4ArtworkType.MP4_ART_JPEG;
		}
		else if (type.equalsIgnoreCase("png")) {
			return MP4ArtworkType.MP4_ART_PNG;
		}
		else if (type.equalsIgnoreCase("gif")) {
			return MP4ArtworkType.MP4_ART_GIF;
		}
		else if (type.equalsIgnoreCase("bmp")) {
			return MP4ArtworkType.MP4_ART_BMP;
		}
		return  MP4ArtworkType.MP4_ART_UNDEFINED;
	}

	private IAtom parseAtom(String key, String value) throws MP4Exception {
		IAtom atom = null;
		if (isBoolean(key)) {
			int ivalue = 0;
			if (value.equalsIgnoreCase("yes")) {
				ivalue = 1;
			}
			atom = createAtom(key, ivalue);
		}
		else if (isNumber(key)) {
			if (key.equals("stik")) {
				int ivalue = -1;
				if (value.equalsIgnoreCase("Old Movie")) {
					ivalue = 0;
				}
				else if (value.equalsIgnoreCase("Normal")) {
					ivalue = 1;
				}
				else if (value.equalsIgnoreCase("Audio Book")) {
					ivalue = 2;
				}
				else if (value.equalsIgnoreCase("Music Video")) {
					ivalue = 6;
				}
				else if (value.equalsIgnoreCase("Movie")) {
					ivalue = 6;
				}
				else if (value.equalsIgnoreCase("TV Show")) {
					ivalue = 10;
				}
				else if (value.equalsIgnoreCase("Booklet")) {
					ivalue = 11;
				}
				else if (value.equalsIgnoreCase("Ringtone")) {
					ivalue = 14;
				}
				atom = createAtom(key, ivalue);
			}
			else if (key.equals("rtng")) {
				int ivalue = -1;
				if (value.equalsIgnoreCase("None")) {
					ivalue = 0;
				}
				else if (value.equalsIgnoreCase("Clean")) {
					ivalue = 2;
				}
				else if (value.equalsIgnoreCase("Explicit")) {
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
				throw new MP4Exception("Unable to parse range from '"+value+"'");
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

	private boolean isArtwork(String key) {
		for (String k : ATOM_ARTWORK_KEYS) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRange(String key) {
		for (String k : ATOM_RANGE_KEYS) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean isNumber(String key) {
		for (String k : ATOM_NUMBER_KEYS) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBoolean(String key) {
		for (String k : ATOM_BOOLEAN_KEYS) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}

	private String nameToAtomKey(String name) {
		if (name.equals("Name")) {
			return "©nam";
		}
		if (name.equals("Release Date")) {
			return "©day";
		}
		if (name.equals("Disk")) {
			return "disk";
		}
		if (name.equals("TV Show")) {
			return "tvsh";
		}
		if (name.equals("TV Episode Number")) {
			return "tven";
		}
		if (name.equals("TV Season")) {
			return "tvsn";
		}
		if (name.equals("TV Episode")) {
			return "tves";
		}
		if (name.equals("Short Description")) {
			return "desc";
		}
		if (name.equals("Long Description")) {
			return "ldes";
		}
		if (name.equals("Artist")) {
			return "©ART";
		}
		if(name.equals("Cover Art pieces")) {
			return "covr";
		}
		if (name.equals("Encoded with")) {
			return "©too";
		}
		if(name.equals("Media Type")) {
			return "stik";
		}
		if(name.equals("Content Rating")) {
			return "rtng";
		}
		if (name.equals("Genre")) {
			return "©gen";
		}
		if (name.equals("Category")) {
			return "catg";
		}
		if (name.equals("HD Video")) {
			return "hdvd";
		}
		return null;
	}


	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, String value) {
		return new MP4v2CLIAtomString(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, int value) {
		return new MP4v2CLIAtomInteger(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name,short number, short total) {
		return new MP4v2CLIAtomRange(name,number,total );
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name,MP4ArtworkType type, int size, byte data[]) {
		return new MP4v2CLIAtomArtwork(this,name,type,size,data);
	}

	/**
	 * This checks that the stores system commands can be found before the store is used.
	 * @throws MP4Exception Thrown if their is a problem locating the commands
	 */
	@Override
	public void init(File nativeDir) throws MP4Exception {
		if (mp4infoPath == null) {
			mp4infoPath = NativeHelper.getNativeApplication(nativeDir,"mp4info");
		}
		if (mp4artPath == null) {
			mp4artPath = NativeHelper.getNativeApplication(nativeDir,"mp4art");
		}
		if (mp4tagsPath == null) {
			mp4tagsPath = NativeHelper.getNativeApplication(nativeDir,"mp4tags");
		}
		if (mp4filePath == null) {
			mp4filePath = NativeHelper.getNativeApplication(nativeDir,"mp4file");
		}
		boolean errors = false;
		if (!checkCommand(mp4infoPath)) {
			log.error("Unable to find command "+mp4infoPath+".");
			errors = true;
		}
		if (!checkCommand(mp4artPath)) {
			log.error("Unable to find command "+mp4artPath+".");
			errors = true;
		}
		if (!checkCommand(mp4filePath)) {
			log.error("Unable to find command "+mp4filePath+".");
			errors = true;
		}
		if (!checkTagsCommand()) {
			log.error("Unable to find command "+mp4tagsPath+".");
			errors = true;
		}
		if (!extended) {
			log.warn("The found version of 'mp4tags' application does not support setting some mp4 " +
					 "box types. This only a limited set of meta data can be written to mp4/m4v " +
					 "files. The documentation for the '"+MP4ITunesStore.class.getName()+"' gives details " +
					 "on downloading a newer version and using that instead.");
		}
		if (errors) {
			throw new MP4Exception("One or more of the mp4v2 system commands could not be found.");
		}
	}

	private boolean checkCommand(String cmd) {
		try {
			getCommandOutput(true,true,false,cmd);
		}
		catch (MP4Exception e) {
			return false;
		}
		return true;
	}

	private boolean checkTagsCommand() {
		try {
			String output = getCommandOutput(true,true,false,mp4tagsPath);
			if (output.contains("-category") && output.contains("-longdesc") && output.contains("-rating")) {
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
		exec.setExitValues(new int[] {0,1,2,3});

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
				throw new MP4Exception("System command returned a non zero exit code '"+exitCode+"' :"+cmdLine.toString());
			}
			return capture.toString();
		} catch (IOException e) {
			throw new MP4Exception("Unable to execute system command: " + cmdLine.toString(),e);
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
		if (key.equalsIgnoreCase("mp4art")){
			mp4artPath = value;
		}
		else if (key.equalsIgnoreCase("mp4info")) {
			mp4infoPath = value;
		}
		else if (key.equalsIgnoreCase("mp4tags")) {
			mp4tagsPath = value;
		}
		else if (key.equalsIgnoreCase("mp4file")) {
			mp4filePath = value;
		}
	}

	private boolean hasArtwrokAtom(List<IAtom> atoms) {
		for (IAtom atom : atoms) {
			if (atom.getName().equals("covr")) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		if (log.isDebugEnabled()) {
			log.debug("Upadting MP4 file '" + mp4File+"' with "+atoms.size());
		}
		checkAppleListItemBoxExists(mp4File);
		if (hasArtwrokAtom(atoms)) {
			try {
				getCommandOutput(true,false,true,mp4artPath, "--remove","--art-any",mp4File);
			}
			catch (MP4Exception e) {
				if (e.getMessage().contains("non zero exit code")) {
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
			log.debug("MP4 modified '" + mp4File+"'");
		}
	}

	private void checkAppleListItemBoxExists(File mp4File) throws MP4Exception {
		String output = getCommandOutput(true,false,true,mp4filePath, "--list",mp4File);
		BufferedReader reader = null;
		try {
			reader =new BufferedReader(new StringReader(output));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = FILE_LIST_PATTERN.matcher(line);
				if (m.matches()) {
					if (m.group(2).contains("mp42")) {
						return;
					}
				}
			}
		} catch (IOException e) {
			throw new MP4Exception("Can't read mp4 file type: " + mp4File,e);
		}
		finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Unable to close stream",e);
				}
			}
		}
		throw new MP4Exception("MP4 File '"+mp4File+"' does not have apple metadata container box, so don't know how to update it");
	}

	String getMP4ArtCommand() {
		return mp4artPath;
	}

}

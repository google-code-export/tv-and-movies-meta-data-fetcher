package org.stanwood.media.store.mp4.atomicparsley;

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
import org.stanwood.media.util.FileHelper;

public class MP4AtomicParsleyManager implements IMP4Manager {

	private final static Log log = LogFactory.getLog(MP4AtomicParsleyManager.class);
	private final static Pattern TAG_LIST_PATTERN = Pattern.compile("^Atom \"(.+?)\" contains\\: (.+)$"); //$NON-NLS-1$
	private final static Pattern RANGE_PATTERN = Pattern.compile("(\\d+) of (\\d+)",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
	private final static Pattern ARTWORK_PATTERN = Pattern.compile("(\\d+) .*of artwork"); //$NON-NLS-1$
	private static final Pattern DNS_PATTERN = Pattern.compile("^Atom \"----\" \\[(.+?);(.+?)\\] contains\\: (.+)$"); //$NON-NLS-1$;

	private String apCmdPath;
	private boolean extended = false;


	/**
	 * This checks that the stores system commands can be found before the store is used.
	 * @throws MP4Exception Thrown if their is a problem locating the commands
	 */
	@Override
	public void init(File nativeDir) throws MP4Exception {
		if (apCmdPath == null) {
			apCmdPath = NativeHelper.getNativeApplication(nativeDir,"AtomicParsley");
		}
		boolean errors = false;
		if (!checkCommand(apCmdPath)) {
			log.error(MessageFormat.format("Unable to execute command {0}",apCmdPath));
			errors = true;
		}
		if (!checkCommandVersion()) {
			log.error(MessageFormat.format("Unable to find or execute command ''{0}''.",apCmdPath));
			errors = true;
		}
		else if (!extended) {
			log.warn(MessageFormat.format("The found version of ''AtomicParsley'' application does not support setting some mp4 box types. This means only a limited set of meta data can be written to mp4/m4v files. The documentation for the ''{0}'' gives details on downloading a newer version and using that instead.",MP4ITunesStore.class.getName())); //$NON-NLS-1$
		}
		if (errors) {
			throw new MP4Exception("Required system command not found");
		}
	}

	@Override
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception {
		if (!mp4File.exists()) {
			throw new MP4Exception(MessageFormat.format("Unable to find mp4 file {0}",mp4File));
		}
		List<IAtom> atoms = new ArrayList<IAtom>();
		String output = getCommandOutput(true,false,true,apCmdPath,mp4File,"-t","+"); //$NON-NLS-1$ //$NON-NLS-2$

		BufferedReader reader = null;
		try {
			reader =new BufferedReader(new StringReader(output));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = TAG_LIST_PATTERN.matcher(line);
				if (m.matches()) {
					String name = m.group(1);
					String value = m.group(2);
					MP4AtomKey key = MP4AtomKey.fromKey(name);
					if (key!=null) {
 						IAtom atom = parseAtom(key,value);
						if (atom!=null) {
							atoms.add(atom);
						}
					}
				}
				else {
					Matcher m1 = DNS_PATTERN.matcher(line);
					if (m1.matches()) {
						String value = m1.group(3);
						MP4AtomKey key = MP4AtomKey.fromRDNS(m1.group(2), m1.group(1));
						if (key!=null) {
	 						IAtom atom = parseAtom(key,value);
							if (atom!=null) {
								atoms.add(atom);
							}
						}
					}
				}
			}

		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format("Unable to list MP4 file atoms for file ''{0}''",mp4File),e);
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

	private boolean hasArtwrokAtom(List<IAtom> atoms) {
		for (IAtom atom : atoms) {
			if (atom.getKey() == MP4AtomKey.ARTWORK) {
				return true;
			}
		}
		return false;
	}

	private IAtom parseAtom(MP4AtomKey key, String value) throws MP4Exception {
		IAtom atom = null;
		if (key.getType() == MP4AtomKeyType.Boolean) {
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
		else if (key.getType() == MP4AtomKeyType.Range) {
			Matcher m = RANGE_PATTERN.matcher(value);
			if (m.matches()) {
				atom = createAtom(key, Short.parseShort(m.group(1)),Short.parseShort(m.group(2)));
			}
			else {
				try {
					atom = createAtom(key, Short.parseShort(value),(short)0);
				}
				catch (NumberFormatException e) {
					throw new MP4Exception(MessageFormat.format("Unable to parse range ''{0}''",value),e);
				}
			}
		}
		else if (key.getType() == MP4AtomKeyType.Artwork) {
			Matcher m = ARTWORK_PATTERN.matcher(value);
			if (m.matches()) {
				atom = new APAtomArtworkSummary(key, Integer.parseInt(m.group(1)));
			}
			else {
				throw new MP4Exception(MessageFormat.format("Unable to parse artwork summary from ''{0}''",value));
			}
		}
		else {
			atom = createAtom(key, value);
		}
		return atom;
	}

	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		if (log.isDebugEnabled()) {
			log.debug("Upadting MP4 file '" + mp4File+"' with "+atoms.size()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		List<Object>args = new ArrayList<Object>();
		args.add(mp4File);
		args.add("--output"); //$NON-NLS-1$
		File tempFile;
		try {
			tempFile = FileHelper.createTempFile("tempmedia", ".mp4"); //$NON-NLS-1$ //$NON-NLS-2$
			args.add(tempFile);
		} catch (IOException e) {
			throw new MP4Exception("Unable to create temp file",e);
		}
		if (hasArtwrokAtom(atoms)) {
			args.add("--artwork"); //$NON-NLS-1$
			args.add("REMOVE_ALL"); //$NON-NLS-1$
		}
		for (IAtom atom : atoms) {
			((AbstractAPAtom)atom).writeAtom(mp4File,extended,args);

		}
		getCommandOutput(true,false,true,apCmdPath,args.toArray(new Object[args.size()]));

		if (tempFile.exists() && tempFile.length()>0)  {
			try {
				FileHelper.delete(mp4File);
				FileHelper.move(tempFile, mp4File);
			}
			catch (IOException e) {
				throw new MP4Exception(MessageFormat.format("Unable to move file ''{0}'' to ''{1}''",tempFile,mp4File),e);
			}

		}
		else {
			throw new MP4Exception(MessageFormat.format("Unable to update MP4 metadata of file ''{0}''",mp4File));
		}

		for (IAtom atom : atoms) {
			((AbstractAPAtom)atom).cleanup();
		}

		if (log.isDebugEnabled()) {
			log.debug("MP4 modified '" + mp4File+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name, String value) {
		return new APAtomString(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name, int value) {
		return new APAtomInteger(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name,short number, short total) {
		return new APAtomRange(name,number,total );
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name,MP4ArtworkType type, int size, byte data[]) {
		return new APAtomArtwork(name, type, size, data);
	}
	/**
	 * <p>Used to set the managers parameters.</p>
	 * <p>This manager has following optional parameters:
	 * 	<ul>
	 * 		<li>atomicparsley - The path to the AtomicParsley command</li>
	 *  </ul>
	 * </p>
	 * @param key The name of the parameter
	 * @param value The value of the parameter
	 */
	@Override
	public void setParameter(String key, String value) {
		if (key.equalsIgnoreCase("atomicparsley")){ //$NON-NLS-1$
			apCmdPath = value;
		}
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
		if (log.isDebugEnabled()) {
			log.debug("About to execute: " + cmdLine.toString()); //$NON-NLS-1$
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

				throw new MP4Exception(MessageFormat.format("System command returned a non zero exit code ''{0}'': {1}",exitCode,cmdLine.toString()));
			}
			return capture.toString();
		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format("Unable to execute system command: {0}" ,cmdLine.toString()),e);
		}
	}

	private boolean checkCommandVersion() {
		try {
			String output = getCommandOutput(true,true,false,apCmdPath);
			if (output.contains("--longdesc")) { //$NON-NLS-1$
				extended = true;
			}
		}
		catch (MP4Exception e) {
			return false;
		}
		return true;
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

}

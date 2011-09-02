package org.stanwood.media.actions.command;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Level;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.extensions.ParameterType;
import org.stanwood.media.logging.LoggerOutputStream;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.util.FileHelper;

/**
 * <p>This action is used execute a system command upon media files and directories</p>
 * <p>This action supports the following parameters:
 * <ul>
 * <li>commandOnFile - A command to execute on finding acceptable media files</li>
 * <li>commandOnDirectory - A command to execute on finding acceptable directories within the media directory</li>
 * <li>extensions - A comma separated list of media file extensions to accept</li>
 * <li>newFile - If this command creates a new file, then the name should be in this parameter</li>
 * <li>deletedFile - If this command deletes a new file, then the name should be in this parameter</li>
 * <li>abortIfFileExists - The name of a file, that if it exists, then this action will not perform</li>
 * </ul>
 * </p>
 * <p>
 * Parameters can also have variable in them. These can be any of the pattern variables, as well as
 * the following special variables:
 * <ul>
 * <li>$NEWFILE - The value of the &apos;newFile&apos; parameter.</li>
 * <li>$DELETEDFILE - The value of the &apos;deletedFile&apos; parameter.</li>
 * <li>$MEDIAFILE_NAME - The name part of the current media file been processed. So after the last file seperator, until it finds the extension.</li>
 * <li>$MEDIAFILE_EXT - The extension of the current media file been processed.</li>
 * <li>$MEDIAFILE_DIR - The directory the current media file is in.</li>
 * <li>$MEDIAFILE - The full path of the current media file.</li>
 * <li>$HOME - The current users home directory.</li>
 * </ul>
 * </p>
 */
public class ExecuteSystemCommandAction extends AbstractAction {

	private final static ParameterType PARAM_CMD_ON_FILE_KEY = new ParameterType("commandOnFile",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_CMD_ON_DIR_KEY = new ParameterType("commandOnDirectory",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_EXTENSIONS_KEY = new ParameterType("extensions",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_NEW_FILE_KEY = new ParameterType("newFile",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_DELETED_FILE_KEY = new ParameterType("deletedFile",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_ABORT_IF_FILE_EXISTS = new ParameterType("abortIfFileExists",String.class,false); //$NON-NLS-1$
	private final static ParameterType PARAM_TYPES[] = {PARAM_CMD_ON_FILE_KEY,PARAM_CMD_ON_DIR_KEY,PARAM_EXTENSIONS_KEY,PARAM_NEW_FILE_KEY,PARAM_DELETED_FILE_KEY,PARAM_ABORT_IF_FILE_EXISTS};

	private String fileCmd;
	private String dirCmd;
	private String newFile;
	private String deletedFile;
	private String abortIfFileExists;
	private List<String> extensions;

	private void perform(MediaDirectory dir, File mediaFile,IActionEventHandler actionEventHandler,IVideo video) throws ActionException {
		if (!isTestMode()) {
			if (extensions!=null) {
				String ext = FileHelper.getExtension(mediaFile);
				if (!extensions.contains(ext)) {
					return;
				}
			}

			executeCommand(fileCmd,mediaFile,dir,video);
			sendEvents(actionEventHandler,mediaFile);
		}
	}

	/**
	 * This will execute the command in the parameter <code><commandOnDirectory</code> on
	 * the directory if the parameter is set.
	 * @param dir The media directory
	 * @param mediaDir The directory the action is to perform on
	 * @param actionEventHandler Used to notify the action performer about changes
	 * @throws ActionException Thrown if their is a problem with the action
	 */
	@Override
	public void performOnDirectory(MediaDirectory dir, File mediaDir, IActionEventHandler actionEventHandler)
			throws ActionException {
		if (!isTestMode()) {
			executeCommand(dirCmd,mediaDir,dir,null);
			sendEvents(actionEventHandler,mediaDir);
		}
	}

	protected void executeCommand(String cmd,File file, MediaDirectory dir, IVideo video) throws ActionException {
		if (cmd!=null) {
			if (abortIfFileExists!=null && new File(replaceVars(abortIfFileExists,file,dir,video)).exists()) {
				return;
			}
			String convertedCmd = replaceVars(cmd,file,dir,video);
			CommandLine cmdLine= parseCommandLine(convertedCmd);
			Executor exec = new DefaultExecutor();
			try {
				exec.setStreamHandler(new PumpStreamHandler(new LoggerOutputStream(Level.INFO), new LoggerOutputStream(Level.ERROR)));
				exec.execute(cmdLine);
			} catch (IOException e) {
				throw new ActionException(MessageFormat.format(Messages.getString("ExecuteSystemCommandAction.UNABLE_TO_EXEC_SYS_COMMAND") ,cmdLine.toString()),e); //$NON-NLS-1$
			}
		}
	}

	private CommandLine parseCommandLine(String cmdLine) {
		List<String> args = ExecParseUtils.splitToWhiteSpaceSeparatedTokens(cmdLine);
		Iterator<String> it = args.iterator();
		CommandLine cmd = new CommandLine(it.next());
		while (it.hasNext()) {
			String arg = it.next();
			cmd.addArgument(arg,false);
		}
		return cmd;
	}

	protected void sendEvents(IActionEventHandler actionEventHandler,File mediaFile)
			throws ActionException {
		if (deletedFile!=null) {
			actionEventHandler.sendEventDeletedFile(new File(replaceVars(deletedFile,mediaFile,null,null)));
		}
		if (newFile!=null) {
			actionEventHandler.sendEventNewFile(new File(replaceVars(newFile,mediaFile,null,null)));
		}
	}

	private String replaceVars(String cmd,File mediaFile,MediaDirectory dir,IVideo video) throws ActionException {
		String s=cmd;
		if (newFile!=null) {
			s = s.replaceAll("\\$NEWFILE",Matcher.quoteReplacement(newFile)); //$NON-NLS-1$
		}
		if (deletedFile!=null) {
			s = s.replaceAll("\\$DELETEDFILE", Matcher.quoteReplacement(deletedFile)); //$NON-NLS-1$
		}

		s = resolvePatterns(dir, s, video, mediaFile, null);
		return s;
	}

	/**
	 * This method is sued to set parameters. This action supports the following parameters:
     * <ul>
	 * <li>commandOnFile - A command to execute on finding acceptable media files</li>
	 * <li>commandOnDirectory - A command to execute on finding acceptable directories within the media directory</li>
	 * <li>extensions - A comma separated list of media file extensions to accept</li>
	 * <li>newFile - If this command creates a new file, then the name should be in this parameter</li>
	 * <li>deletedFile - If this command deletes a new file, then the name should be in this parameter</li>
	 * <li>abortIfFileExists - The name of a file, that if it exists, then this action will not perform</li>
	 * </ul>
	 * @param key The parameter key
	 * @param value The parameter value
	 * @throws ActionException Thrown if a known key is given
	 */
	@Override
	public void setParameter(String key, String value) throws ActionException {
		if (key.equalsIgnoreCase(PARAM_CMD_ON_FILE_KEY.getName())) {
			this.fileCmd = value;
		}
		else if (key.equalsIgnoreCase(PARAM_NEW_FILE_KEY.getName())) {
			this.newFile = value;
		}
		else if (key.equalsIgnoreCase(PARAM_DELETED_FILE_KEY.getName())) {
			this.deletedFile = value;
		}
		else if (key.equalsIgnoreCase(PARAM_EXTENSIONS_KEY.getName())) {
			StringTokenizer tok = new StringTokenizer(value,","); //$NON-NLS-1$
			this.extensions = new ArrayList<String>();
			while (tok.hasMoreTokens()) {
				extensions.add(tok.nextToken());
			}
		}
		else if (key.equalsIgnoreCase(PARAM_CMD_ON_DIR_KEY.getName())) {
			this.dirCmd = value;
		}
		else if (key.equalsIgnoreCase(PARAM_ABORT_IF_FILE_EXISTS.getName())) {
			this.abortIfFileExists = value;
		}
 		else {
			throw new ActionException(MessageFormat.format(Messages.getString("ExecuteSystemCommandAction.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
		}
	}

	/**
	 * This will execute the command in the parameter <code><commandOnFile</code> on
	 * the mediaFile if the parameter is set.
	 * @param episode The episode information
	 * @param mediaFile The media file
	 * @param dir File media directory the files belongs to
	 * @param actionEventHandler Used to notify the action performer about changes
	 * @throws ActionException Thrown if their is a problem with the action
	 */
	@Override
	public void perform(MediaDirectory dir, Episode episode, File mediaFile,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,mediaFile,actionEventHandler,episode);
	}

	/**
	 * This will execute the command in the parameter <code><commandOnFile</code> on
	 * the mediaFile if the parameter is set.
	 * @param film The film information
	 * @param part The part number
	 * @param mediaFile The media file
	 * @param dir File media directory the files belongs to
	 * @param actionEventHandler Used to notify the action performer about changes
	 * @throws ActionException Thrown if their is a problem with the action
	 */
	@Override
	public void perform(MediaDirectory dir, Film film, File mediaFile, Integer part,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,mediaFile,actionEventHandler,film);
	}

	/** {@inheritDoc} */
	@Override
	public ParameterType[] getParameters() {
		return PARAM_TYPES;
	}


}

package org.stanwood.media.actions.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Level;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.logging.LoggerOutputStream;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.util.FileHelper;

public class ExecuteSystemCommandAction extends AbstractAction {

	private final static String PARAM_CMD_KEY = "command";
	private final static String PARAM_EXTENSIONS_KEY = "extensions";
	private final static String PARAM_NEW_FILE_KEY = "newFile";
	private final static String PARAM_DELETED_FILE_KEY = "deletedFile";

	private String cmd;
	private String newFile;
	private String deletedFile;
	private List<String> extensions;

	private void perform(MediaDirectory dir, File mediaFile,IActionEventHandler actionEventHandler) throws ActionException {
		if (!isTestMode()) {
			if (extensions!=null) {
				String ext = FileHelper.getExtension(mediaFile);
				if (!extensions.contains(ext)) {
					return;
				}
			}

			executeCommand(mediaFile);
			sendEvents(actionEventHandler,mediaFile);
		}
	}

	protected void executeCommand(File file) throws ActionException {
		String convertedCmd = replaceVars(cmd,file);
		CommandLine cmdLine= CommandLine.parse(convertedCmd);
		Executor cmd = new DefaultExecutor();
		try {
			cmd.setStreamHandler(new PumpStreamHandler(new LoggerOutputStream(Level.INFO), new LoggerOutputStream(Level.ERROR)));
			cmd.execute(cmdLine);
		} catch (IOException e) {
			throw new ActionException("Unable to execute system command: " + cmdLine.toString());
		}
	}

	protected void sendEvents(IActionEventHandler actionEventHandler,File mediaFile)
			throws ActionException {
		if (deletedFile!=null) {
			actionEventHandler.sendEventDeletedFile(new File(replaceVars(deletedFile,mediaFile)));
		}
		if (newFile!=null) {
			actionEventHandler.sendEventNewFile(new File(replaceVars(newFile,mediaFile)));
		}
	}

	private String replaceVars(String cmd,File mediaFile) {
		String s=cmd;
		s =	s.replaceAll("\\$MEDIAFILE", mediaFile.getAbsolutePath());
		s =	s.replaceAll("\\$MEDIAFILE_NAME", FileHelper.getName(mediaFile));
		s =	s.replaceAll("\\$MEDIAFILE_EXT", FileHelper.getExtension(mediaFile));
		s =	s.replaceAll("\\$MEDIAFILE_DIR", mediaFile.getParent());
		s = s.replaceAll("\\$NEWFILE", newFile);
		s = s.replaceAll("\\$DELETEDFILE", deletedFile);
		return s;
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		if (key.equalsIgnoreCase(PARAM_CMD_KEY)) {
			this.cmd = value;
		}
		else if (key.equalsIgnoreCase(PARAM_NEW_FILE_KEY)) {
			this.newFile = value;
		}
		else if (key.equalsIgnoreCase(PARAM_DELETED_FILE_KEY)) {
			this.deletedFile = value;
		}
		else if (key.equalsIgnoreCase(PARAM_EXTENSIONS_KEY)) {
			StringTokenizer tok = new StringTokenizer(",");
			this.extensions = new ArrayList<String>();
			while (tok.hasMoreTokens()) {
				extensions.add(tok.nextToken());
			}
		}
		else {
			throw new ActionException("Unsupported parameter for action '"+key+"'");
		}
	}

	@Override
	public void perform(MediaDirectory dir, Episode episode, File mediaFile,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,mediaFile,actionEventHandler);
	}

	@Override
	public void perform(MediaDirectory dir, Film film, File mediaFile, Integer part,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,mediaFile,actionEventHandler);
	}

}

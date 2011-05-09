package org.stanwood.media.actions.command;

import java.io.File;
import java.io.IOException;

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

public class ExecuteSystemCommandAction extends AbstractAction {

	private String PARAM_CMD_KEY = "command";
	private String cmd;
	private Object PARAM_NEW_FILE_KEY;
	private Object PARAM_DELETED_FILE_KEY;
	private String newFile;
	private String deletedFile;

	@Override
	public void perform(MediaDirectory dir, File file,IActionEventHandler actionEventHandler) throws ActionException {

		if (!isTestMode()) {
			String convertedCmd = replaceVars(cmd,file);
			CommandLine cmdLine= CommandLine.parse(convertedCmd);
			Executor cmd = new DefaultExecutor();
			try {
				cmd.setStreamHandler(new PumpStreamHandler(new LoggerOutputStream(Level.INFO), new LoggerOutputStream(Level.ERROR)));
				cmd.execute(cmdLine);
			} catch (IOException e) {
				throw new ActionException("Unable to execute system command: " + cmdLine.toString());
			}
			if (deletedFile!=null) {
				actionEventHandler.sendEventDeletedFile(new File(deletedFile));
			}
			if (newFile!=null) {
				actionEventHandler.sendEventNewFile(new File(newFile));
			}
		}
	}

	private String replaceVars(String cmd,File mediaFile) {
		String s = cmd.replaceAll("\\$MEDIAFILE", mediaFile.getAbsolutePath());
		s = cmd.replaceAll("\\$NEWFILE", newFile);
		s = cmd.replaceAll("\\$DELETEDFILE", deletedFile);
		return s;
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		if (key.equals(PARAM_CMD_KEY)) {
			this.cmd = value;
		}
		else if (key.equals(PARAM_NEW_FILE_KEY)) {
			this.newFile = value;
		}
		else if (key.equals(PARAM_DELETED_FILE_KEY)) {
			this.deletedFile = value;
		}
		else {
			throw new ActionException("Unsupported parameter for action '"+key+"'");
		}
	}

}

package org.stanwood.media.actions.permission;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.logging.LoggerOutputStream;

public class ChangePermissionAction extends AbstractAction {

	private final static Log log = LogFactory.getLog(ChangePermissionAction.class);
	private Map<String,String>params = new HashMap<String,String>();

	@Override
	public void perform(MediaDirectory dir, File file,IActionEventHandler eventHandler) throws ActionException {
		if (params.get("group")!=null && params.get("chgrp")==null) {
			throw new ActionException("The parameter 'chgrp' must be set if the parameter 'group' has been set.");
		}
		if (params.get("chown")!=null && params.get("owner")==null) {
			throw new ActionException("The parameter 'chown' must be set if the parameter 'owner' has been set.");
		}

		log.info("Updating permisions of file: " + file.getAbsolutePath());
		if (params.get("group")!=null) {
			executeCommand(params.get("chgrp"),params.get("group"),file.getAbsolutePath());
		}
		if (params.get("group")!=null) {
			executeCommand(params.get("chown"),params.get("owner"),file.getAbsolutePath());
		}
		if (params.get("group")!=null) {
			executeCommand(params.get("chmod"),params.get("permissions"),file.getAbsolutePath());
		}
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		params.put(key,value);
	}

	private void executeCommand(String exec,String ... args) throws ActionException {
		CommandLine cmdLine= getCommand(exec,args);
		Executor cmd = new DefaultExecutor();
		try {
			cmd.setStreamHandler(new PumpStreamHandler(new LoggerOutputStream(Level.INFO), new LoggerOutputStream(Level.ERROR)));
			cmd.execute(cmdLine);
		} catch (IOException e) {
			throw new ActionException("Unable to execute system command: " + cmdLine.toString());
		}
	}

	private CommandLine getCommand(String exec,String ... args) {
		CommandLine cmdLine = new CommandLine(exec);
		cmdLine.addArguments(args);
		return cmdLine;
	}
}

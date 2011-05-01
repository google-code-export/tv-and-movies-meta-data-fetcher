package org.stanwood.media.actions.permission;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.AbstractAction;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.util.AbstractExecutable;

public class ChangePermissionAction extends AbstractAction {

	private final static Log log = LogFactory.getLog(ChangePermissionAction.class);
	private Map<String,String>params = new HashMap<String,String>();

	@Override
	public void perform(MediaDirectory dir, List<File> files) throws ActionException {
		if (params.get("group")!=null && params.get("chgrp")==null) {
			throw new ActionException("The parameter 'chgrp' must be set if the parameter 'group' has been set.");
		}
		if (params.get("chown")!=null && params.get("owner")==null) {
			throw new ActionException("The parameter 'chown' must be set if the parameter 'owner' has been set.");
		}

		for (File file : files) {
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
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		params.put(key,value);
	}

	private void executeCommand(String ... args) throws ActionException {
		AbstractExecutable cmd =  new AbstractExecutable();
		String debug = getCommand(args);
		try {
			cmd.execute(args);
			String errorOutput = cmd.getErrorStream().trim();
			if (errorOutput.length()>0) {
				log.error(errorOutput);
			}
		} catch (IOException e) {
			throw new ActionException("Unable to execute system command: " + debug);
		} catch (InterruptedException e) {
			throw new ActionException("Unable to execute system command: " + debug);
		}
	}

	private String getCommand(String ... args) {
		StringBuilder buffer = new StringBuilder();
		for (String a : args) {
			if (buffer.length()>0) {
				buffer.append(" ");
			}
			buffer.append(a);
		}
		return buffer.toString();
	}
}

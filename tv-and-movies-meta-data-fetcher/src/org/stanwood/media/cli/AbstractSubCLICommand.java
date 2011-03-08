package org.stanwood.media.cli;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;


public abstract class AbstractSubCLICommand extends BaseLauncher implements ICLICommand {

	private String name;
	private String description;
	private IExitHandler exitHandler = null;

	public AbstractSubCLICommand(String name,String description,List<Option> options,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(name,stdout,stderr,exitHandler);
		this.description = description;
	}

	public String getDescription() {
		return description;
	}


	@Override
	protected boolean processOptionsInternal(String args[],CommandLine cmd) {
		return processOptions(args,cmd);
	}

	/**
	 * This is called to validate the tools CLI options. When this is called,
	 * the default options added by {@link AbstractLauncher} will already have been
	 * validated sucesfully.
	 * @param cmd The command line options
	 * @return True, if the command line options verified successfully, otherwise false
	 */
	protected abstract boolean processOptions(String args[],CommandLine cmd);

}

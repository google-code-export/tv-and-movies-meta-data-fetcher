package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.stanwood.media.cli.AbstractSubCLICommand;
import org.stanwood.media.cli.ICLICommand;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.renamer.Controller;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.source.xbmc.updater.IXBMCUpdater;

public abstract class AbstractXBMCSubCommand extends AbstractSubCLICommand {

	private IXBMCUpdater updater;
	private IConsole console;

	public AbstractXBMCSubCommand(ICLICommand rootCommand, String name,
			String description, List<Option> options, IExitHandler exitHandler,
			PrintStream stdout, PrintStream stderr) {
		super(rootCommand, name, description, options, exitHandler, stdout,stderr);
	}

	@Override
	public void init(Controller controller) {
		updater = controller.getXBMCAddonManager().getUpdater();
		console = new IConsole() {
			@Override
			public void error(String error) {
				getStderr().println(error);
			}

			@Override
			public void info(String info) {
				getStdout().println(info);
			}

		};
	}

	protected IConsole getConsole() {
		return console;
	}

	protected IXBMCUpdater getUpdater() {
		return updater;
	}

	protected boolean checkNoArgs(CommandLine cmd) {
		String[] args2 = cmd.getArgs();
		if (args2.length > 0) {
			fatal("Unkown sub-command argument '" + args2[0] + "'");
			return false;
		}
		return true;
	}

}

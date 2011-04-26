package org.stanwood.media.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.Controller;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;

/**
 * This class should be extended by classes that have a main method used to lauch them
 * from the command line. It helps with adding command line parameters and add some default
 * CLI options.
 */
public abstract class AbstractLauncher extends BaseLauncher implements ICLICommand {

	private final static Log log = LogFactory.getLog(AbstractLauncher.class);

	private final static String LOG_CONFIG_OPTION = "l";
	private final static String CONFIG_FILE_OPTION = "c";

	private File configFile = null;
	private Controller controller;

	/** This is used by tests to set a configuration that should be used, if null then a configuration is read
	 * in the usual way. Once the configuration has been used, this is set back to null */
	public static ConfigReader config = null;

	/**
	 * Create a instance of the class
	 * @param name The name of the executable
	 * @param options The options that are to be added to the CLI
	 * @param stdout The standard output stream
	 * @param stderr The standard error stream
	 * @param exitHandler The exit handler
	 */
	public AbstractLauncher(String name,List<Option> options,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(name,stdout,stderr,exitHandler);

		for (Option o : options) {
			addOption(o);
		}

		Option o = new Option(LOG_CONFIG_OPTION,"log_config",true,"The log config mode [<INFO>|<DEBUG>|<log4j config file>]");
		o.setArgName("file");
		addOption(o);

		o = new Option(CONFIG_FILE_OPTION,"config_file",true,"The location of the config file. If not present, attempts to load it from /etc/mediafetcher-conf.xml");
		o.setArgName("info|debug|file");
		addOption(o);
	}

	/**
	 * This is called to validate the tools CLI options. When this is called,
	 * the default options added by {@link AbstractLauncher} will already have been
	 * validated sucesfully.
	 * @param cmd The command line options
	 * @return True, if the command line options verified successfully, otherwise false
	 */
	protected abstract boolean processOptions(String args[],CommandLine cmd);

	@Override
	protected boolean processOptionsInternal(String args[],CommandLine cmd) {
		String logConfig = null;
		if (cmd.hasOption(LOG_CONFIG_OPTION)) {
			logConfig = cmd.getOptionValue(LOG_CONFIG_OPTION,"INFO");
		}
		if (!initLogging(logConfig)) {
			return false;
		}
		if (cmd.hasOption(CONFIG_FILE_OPTION)) {
			configFile = new File(cmd.getOptionValue(CONFIG_FILE_OPTION));
		}
		try {
			processConfig();
			controller = new Controller(config);
		} catch (FileNotFoundException e) {
			fatal(e);
			return false;
		} catch (ConfigException e) {
			fatal(e);
			return false;
		}
		finally {
			config=null;
		}

		return processOptions(args,cmd);
	}

	private boolean processConfig() throws FileNotFoundException, ConfigException {
		if (config==null) {
			if (configFile==null) {
				configFile = new File(ConfigReader.getDefaultConfigDir(),"mediamanager-conf.xml");
				if (log.isDebugEnabled()) {
					log.debug("No config file give, so using default location: " + configFile.getAbsolutePath());
				}
			}

			if (!configFile.exists()) {
				fatal("Unable to find config file '" +configFile+"' so using defaults.");
				return false;
			}
			else  {
				InputStream is = null;
				try {
					is = new FileInputStream(configFile);
					config = new ConfigReader(is);
				}
				finally {
					if (is!=null) {
						try {
							is.close();
						} catch (IOException e) {
							warn("Unable to close stream");
						}
					}
				}
			}
			config.parse();
		}
		return true;
	}

	protected Controller getController() {
		return controller;
	}

	private boolean initLogging(String logConfig) {
		if (logConfig!=null) {
			if (logConfig.toLowerCase().equals("info")) {
				LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
			}
			else if (logConfig.toLowerCase().equals("debug")) {
				LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
			}
			else {
				File logConfigFile = new File(logConfig);
				if (logConfigFile.exists()) {
					LogSetupHelper.initLogingFromConfigFile(logConfigFile);
				}
				else {
					fatal("Unable to find log configuraion file " + logConfigFile.getAbsolutePath());
					return false;
				}

			}
		}
		else {
			LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		}

		return true;
	}

	/**
	 * Called to parse a {@link java.util.Long} option fropm the command line
	 * @param optionValue The command line parameter value
	 * @return The long
	 * @throws ParseException Thrown if it could not be parsed correctly
	 */
	protected long parseLongOption(String optionValue) throws ParseException{
		try {
			return Long.parseLong(optionValue);
		}
		catch (NumberFormatException e) {
			throw new ParseException("Unable to parse number from " + optionValue);
		}
	}
}

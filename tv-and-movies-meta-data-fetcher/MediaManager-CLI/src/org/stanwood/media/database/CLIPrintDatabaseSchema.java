/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.database;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;

/**
 * This is a command line application used to print a database schema for a given dialect
 * usage: mm-print-db-schema [-c <file>] -d <dialect> [-h] [-l <info|debug|file>] [-v]
 *
 * --version, -v                 Display the version
 * --dialect, -d <dialect>       The database dialect
 * --config_file, -c <file>      The location of the config file. If not present, attempts to load it from ~/.mediaManager/mediamanager-conf.xml or /etc/mediamanager-conf.xml
 * --log_config, -l <info|debug|file>
 *                               The log config mode [<INFO>|<DEBUG>|<log4j config file>]
 * --help, -h                    Show the help
 */
public class CLIPrintDatabaseSchema  extends AbstractLauncher {

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;

	private static IExitHandler exitHandler = null;
	private static final List<Option> OPTIONS;

	private static final String DATABASE_DIALECT = "d"; //$NON-NLS-1$

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(DATABASE_DIALECT, "dialect",true,"The database dialect"); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(true);
		o.setArgName("dialect"); //$NON-NLS-1$
		OPTIONS.add(o);
	}

	private String dialect;

	/**
	 * The constructor
	 * @param exitHandler The exit handler, or null to use the default one
	 */
	public CLIPrintDatabaseSchema(IExitHandler exitHandler) {
		super("mm-print-db-schema", OPTIONS, exitHandler, stdout, stderr); //$NON-NLS-1$
	}

	@Override
	protected boolean processOptions(String[] args, CommandLine cmd) {
		dialect = cmd.getOptionValue(DATABASE_DIALECT);
		return true;
	}

	@Override
	protected boolean run() {
		try {
			String schema = DBHelper.getInstance().getSchema(dialect);
			stdout.println(schema);
		} catch (DatabaseException e) {
			fatal(e);
			return false;
		}
		return false;
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}

	/**
	 * The entry point to the application. For details see the class documentation.
	 *
	 * @param args The arguments.
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIPrintDatabaseSchema ca = new CLIPrintDatabaseSchema(exitHandler);
		ca.launch(args);
	}
}

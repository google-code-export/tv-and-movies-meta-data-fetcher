/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.server.commands;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.stanwood.media.util.FileHelper;

/**
 * A command logger that logs the outout to a string builder
 */
@SuppressWarnings("nls")
public class StringBuilderCommandLogger implements ICommandLogger {

	private StringBuilder result = new StringBuilder();

	/** {@inheritDoc} */
	@Override
	public void trace(String message, Throwable t) {
		result.append("TRACE:"+message+FileHelper.LS);
		appendStacktrace(t);
	}

	private void appendStacktrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		result.append(sw.toString()+FileHelper.LS);
	}

	/** {@inheritDoc} */
	@Override
	public void trace(String message) {
		result.append("TRACE:"+message+FileHelper.LS);
	}

	/** {@inheritDoc} */
	@Override
	public void debug(String message, Throwable t) {
		result.append("DEBUG:"+message+FileHelper.LS);
		appendStacktrace(t);
	}

	/** {@inheritDoc} */
	@Override
	public void debug(String message) {
		result.append("DEBUG:"+message+FileHelper.LS);
	}

	/** {@inheritDoc} */
	@Override
	public void error(String message, Throwable t) {
		result.append("ERROR:"+message+FileHelper.LS);
		appendStacktrace(t);
	}

	/** {@inheritDoc} */
	@Override
	public void error(String message) {
		result.append("ERROR:"+message+FileHelper.LS);
	}

	/** {@inheritDoc} */
	@Override
	public void warn(String message) {
		result.append("WARN:"+message+FileHelper.LS);
	}

	/** {@inheritDoc} */
	@Override
	public void info(String message) {
		result.append("INFO:"+message+FileHelper.LS);
	}

	/**
	 * Used to get the logged results
	 * @return the logged results
	 */
	public String getResult() {
		return result.toString();
	}
}

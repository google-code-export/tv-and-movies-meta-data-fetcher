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
package org.stanwood.media.util.ruby;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * This is a helper class used with jruby
 */
public class JRubyHelper {

	/**
	 * Used to print a list of install gems to stdout
	 * @param jruby The jruby scripting engine
	 * @throws ScriptException Thrown if their are any ruby problems
	 */
	@SuppressWarnings("nls")
	public static void listGems(ScriptEngine jruby) throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("require 'rubygems'\n");
		script.append("require 'rubygems/gem_runner'\n");
		script.append("Gem::GemRunner.new.run [\"list\"]\n");
		jruby.eval(script.toString());
	}
}

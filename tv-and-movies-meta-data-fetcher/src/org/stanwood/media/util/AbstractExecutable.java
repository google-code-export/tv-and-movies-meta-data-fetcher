/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AbstractExecutable {

	protected int execute(List<String> args) throws IOException, InterruptedException {
		List<String> newArgs = new ArrayList<String>();
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			newArgs.add("cmd");
			newArgs.add("/C");
		}
		newArgs.addAll(args);
		
		String debugOut = "";
		for (String arg : newArgs) {
			debugOut+=arg+" ";
		}
				
		Process p = Runtime.getRuntime().exec(newArgs.toArray(new String[newArgs.size()]));
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(p.getErrorStream(),"OUTPUT");				
		
		errorGobbler.start();
        outputGobbler.start();
		
		return p.waitFor();
	}
	
}

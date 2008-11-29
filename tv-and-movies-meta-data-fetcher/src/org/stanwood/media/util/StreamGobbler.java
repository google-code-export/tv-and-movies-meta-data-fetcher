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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {
	
	private InputStream is;
	private String type;
	private StringBuffer result;

	public StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
        result= new StringBuffer("");
    }

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while (br.readLine()!=null) {}
			String line = null;
			while ((line = br.readLine()) != null) {
				result.append(line+"\n");
			}
		} catch (IOException ioe) {			
			ioe.printStackTrace();
		}
	}
	
	public String getResult() {
		return result.toString();
	}
}
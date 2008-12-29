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

/**
 * Used to swallow the contents of a stream. This is a thread based
 * class, so {@link #start()} should be called to start the thread. {@link #getResult()} 
 * can be called to get the contents of the swallowed stream.
 */
public class StreamGobbler extends Thread {

	private InputStream is;
	private StringBuilder result;
	private boolean done;

	/**
	 * Creates a instance of the stream gobbler thread
	 * @param is The stream that is to be swallowed
	 */
	public StreamGobbler(InputStream is) {
		this.is = is;
		result = new StringBuilder("");
	}

	/**
	 * This is executed when the thread is started. It will swallow the 
	 * input stream. 
	 */
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			done = false;			
			
			while (!done && (line = br.readLine()) != null) {
				result.append(line + "\n");
			}			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			done = true;
		}
	}

	/**
	 * Used to get the contents of the swallowed stream.
	 * @return The contents of the swallowed stream
	 */
	public String getResult() {
		return result.toString();
	}
	
	/**
	 * This will return true when the string has been gobbled.
	 * @return True if the stream has been completly gobbled, otherwise false.
	 */
	public boolean isDone() {
		return done;
	}
}
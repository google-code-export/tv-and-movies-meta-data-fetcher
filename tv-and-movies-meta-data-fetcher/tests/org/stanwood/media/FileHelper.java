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
package org.stanwood.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a help class that is used to perform operations on files.
 */
public class FileHelper {

	private final static Log log = LogFactory.getLog(FileHelper.class);
	/** A Line seperator property value */
	public final static String LS = System.getProperty("line.separator");

	/**
	 * This will create a temporary directory using the given name.
	 *
	 * @param name The name of the directory to create
	 * @return A file object pointing to the directory that was created
	 * @throws IOException Thrown if their is a problme creating the directory
	 */
	public static File createTmpDir(String name) throws IOException {
		File dir = File.createTempFile(name, "");
		if (!dir.delete()) {
			throw new IOException("Unable to delete file: " + dir.getAbsolutePath());
		}
		if (!dir.mkdir()) {
			throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
		}

		return dir;
	}

	/**
	 * Used to copy a source file to a destination file.
	 *
	 * @param src The source file
	 * @param dst The destination file
	 * @throws IOException Thrown if their is a problem copying the file
	 */
	public static void copy(File src, File dst) throws IOException {
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.error("Unable to close output stream", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error("Unable to close input stream", e);
				}
			}
		}
	}

	/**
	 * Used to copy the contents of a input stream to a destination file.
	 *
	 * @param in The input stream
	 * @param dst The destination file
	 * @throws IOException Thrown if their is a problem copying the file
	 */
	public static void copy(InputStream in, File dst) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.error("Unable to close output stream", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error("Unable to close input stream", e);
				}
			}
		}

	}

	/**
	 * Used to copy the contents of a input stream to a destination file.
	 *
	 * @param in The input stream
	 * @param dst The destination file
	 * @param params Parameters which are replaced with values when the file is copied
	 * @throws IOException Thrown if their is a problem copying the file
	 */
	public static void copy(InputStream in, File dst, Map<String, String> params) throws IOException {
		PrintStream out = null;
		BufferedReader bin = null;
		try {
			out = new PrintStream(new FileOutputStream(dst));
			bin = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = bin.readLine()) != null) {
				for (String key : params.keySet()) {
					line = line.replaceAll("\\$" + key + "\\$", params.get(key));
				}
				out.println(line);
			}

		} finally {
			if (out != null) {
				out.close();
			}
			if (bin != null) {
				bin.close();
			}
		}
	}

	/**
	 * Used to delete a directory and all it's children
	 *
	 * @param dir The directory to delete
	 * @return True if successful, otherwise false
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	/**
	 * Used to display the contents of a file
	 *
	 * @param file The file to display
	 * @param os The output stream to display it to
	 * @throws IOException Thrown if their is a problem reading or displaying the file
	 */
	public static void displayFile(File file, PrintStream os) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		while ((str = in.readLine()) != null) {
			os.println(str);
		}
		in.close();
	}

	/**
	 * Used to read the contents of a file into a string
	 *
	 * @param file The file to read
	 * @return The contents of the file
	 * @throws IOException Thrown if their is a problem reading the file
	 */
	public static String readFileContents(File file) throws IOException {
		StringBuilder results = new StringBuilder();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		while ((str = in.readLine()) != null) {
			results.append(str + LS);
		}
		in.close();
		return results.toString();
	}

	/**
	 * Used to read the contents of a stream into a string
	 *
	 * @param inputStream The input stream
	 * @return The contents of the file
	 * @throws IOException Thrown if their is a problem reading the file
	 */
	public static String readFileContents(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder results = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null) {
			results.append(str + LS);
		}
		in.close();
		return results.toString();
	}
}

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


import java.io.BufferedOutputStream;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a help class that is used to perform operations on files.
 */
public class FileHelper {

	private final static Log log = LogFactory.getLog(FileHelper.class);
	/** A Line separator property value */
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
		if (in==null) {
			throw new NullPointerException("Stream is null");
		}
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
	 * Used to display the contents of a file
	 * @param file The file to display
	 * @param startLine The line to start displaying from
	 * @param endLine The line to finish displaying from
	 * @param os The output stream used to print the file to
	 * @throws IOException Thrown if their is a problem reading the file
	 */
	public static void displayFile(File file,int startLine, int endLine, PrintStream os)throws IOException {
		if (startLine<0) {
			startLine = 0;
		}
		int line = 1;
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		while ((str = in.readLine()) != null) {
			if (line>=startLine && line <=endLine) {
				os.println(line + ": " + str);
			}
			line++;
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
		if (inputStream==null) {
			throw new IOException("Input stream is null, probally unable to find file");
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder results = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null) {
			results.append(str + LS);
		}
		in.close();
		return results.toString();
	}

	/**
	 * Used to list all the files in a directory and it's sub directiories.
	 * @param dir The directory to list the files of
	 * @return The files in the directory
	 */
	public static List<File> listFiles(File dir) {
		List<File>files = new ArrayList<File>();
		listFiles(dir,files);
		Collections.sort(files);
		return files;
	}

	/**
	 * Used to list all the files in a directory and it's sub directiories.
	 * File files are returned as a list of absolute paths.
	 * @param dir The directory to list the files of
	 * @return The files in the directory
	 */
	public static List<String> listFilesAsStrings(File dir) {
		List<String>files = new ArrayList<String>();

		List<File>files2 = listFiles(dir);
		for (File f : files2) {
			files.add(f.getAbsolutePath());
		}

		return files;
	}

	private static void listFiles(File dir, List<File> files) {
		if (dir.isDirectory()) {
			for (File d : dir.listFiles()) {
				listFiles(d,files);
			}
		}
		else {
			files.add(dir);
		}
	}

	/**
	 * Used to add contents to a file
	 * @param file The file to add contetns to
	 * @param contents The contents
	 * @throws IOException Thrown if their is a IO problem
	 */
	public static void appendContentsToFile(File file,StringBuilder contents) throws IOException {
		PrintStream ps = null;
		try {
			FileOutputStream os = new FileOutputStream(file);
			ps = new PrintStream(os);
			ps.print(contents.toString());
		}
		finally {
			ps.close();
		}
	}

	/**
	 * Used to unzip a file to a directory
	 * @param is The input stream containing the file
	 * @param destDir The directory to unzip to
	 * @throws IOException Thrown if their are any problems
	 */
	public static void unzip(InputStream is, File destDir) throws IOException {
		ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(is);
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry())!=null) {
                File file = new File(destDir,entry.getName());
                if (entry.isDirectory()) {
                    if (!file.mkdir() && file.exists()) {
                        throw new IOException("Unable to create directory: " + file); //$NON-NLS-1$
                    }
                }
                else {
                	BufferedOutputStream out = null;
    				try  {
    					int count;
    					byte data[] = new byte[1000];
    					out = new BufferedOutputStream(new FileOutputStream(new File(destDir,entry.getName())),1000);
    					System.out.println("Unzipping " + entry.getName() +" with size " + entry.getSize());
    					while ((count = zis.read(data,0,1000)) != -1)
    		            {
    						out.write(data,0,count);
    		            }
    					out.flush();
    				}
    				finally {
    					if (out!=null) {
    						out.close();
    					}
    				}
                }
            }
        } finally {
            if (zis != null) {
				zis.close();
			}
        }
	}

}

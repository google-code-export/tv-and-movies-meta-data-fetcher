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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
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
	public final static String LS = System.getProperty("line.separator"); //$NON-NLS-1$

	/** Stores the current users home directory */
	public final static File HOME_DIR = new File(System.getProperty("user.home")); //$NON-NLS-1$

	private final static char HEX_DIGITS[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * This will create a temporary directory using the given name.
	 *
	 * @param name The name of the directory to create
	 * @return A file object pointing to the directory that was created
	 * @throws IOException Thrown if their is a problme creating the directory
	 */
	public static File createTmpDir(String name) throws IOException {
		File dir = File.createTempFile(name, ""); //$NON-NLS-1$
		if (!dir.delete()) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_DELETE_FILE"),dir.getAbsolutePath())); //$NON-NLS-1$
		}
		if (!dir.mkdir()) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_CREATE_DIR"),dir.getAbsolutePath())); //$NON-NLS-1$
		}

		return dir;
	}

	/**
	 * Used to copy a source file or a directory to a destination file or directory.
	 *
	 * @param src The source file or directory
	 * @param dst The destination file or directory
	 * @throws IOException Thrown if their is a problem copying the file or directory
	 */
	public static void copy(File src, File dst) throws IOException {
		if (dst.exists()) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_COPY_ALREADY_EXISTS"),src,dst)); //$NON-NLS-1$
		}
		if (src.isDirectory()) {
			if (!dst.mkdir() && !dst.exists()) {
				throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_CREATE_DIR"),dst)); //$NON-NLS-1$
			}
			File[] files = src.listFiles();
			for (File f : files) {
				copy(f,new File(dst,f.getName()));
			}
		}
		else {
			copyFile(src, dst);
		}
	}

	private static void copyFile(File src, File dst)
			throws FileNotFoundException, IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(src);
			copy(in,dst);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(Messages.getString("FileHelper.UNABLE_CLOSE_INPUT_STREAM"), e); //$NON-NLS-1$
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
			throw new NullPointerException("Stream is null"); //$NON-NLS-1$
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
					log.error(Messages.getString("FileHelper.UNABLE_CLOSE_OUTPUT_STREAM"), e); //$NON-NLS-1$
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(Messages.getString("FileHelper.UNABLE_CLOSE_INPUT_STREAM"), e); //$NON-NLS-1$
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
				for (Entry<String,String>e : params.entrySet()) {
					line = line.replaceAll("\\$" + e.getKey() + "\\$", e.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
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
	 * This will copy a file from the web to a destination file on the local system
	 * @param url The url to read from the file from
	 * @param dest The file to be created on the location system
	 * @return A MD5 sum of the file
	 * @throws IOException Thrown if their is a problem reading or wring the file
	 */
	public static String copy(URL url,File dest) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Fetching: " + url); //$NON-NLS-1$
		}
		try {
			OutputStream out = null;
			InputStream is = null;
			try {
				out = new FileOutputStream(dest);
				is = new WebFileInputStream(url);
				MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					out.write(buf, 0, len);
					md.update(buf, 0 , len);
				}

				out.flush();
				return bytesToHexString(md.digest());
			}
			catch (IOException e ) {
				if (dest.exists()) {
					try {
						FileHelper.delete(dest);
					}
					catch (IOException e1) {
						log.error(MessageFormat.format(Messages.getString("FileHelper.UNABLE_DELETE_FILE") ,dest),e1); //$NON-NLS-1$
					}
				}
				throw e;
			}
			finally {
				if (is!=null) {
					try {
						is.close();
					}
					catch (IOException e) {
						log.error(Messages.getString("FileHelper.UNABLE_CLOSE_STREAM"),e); //$NON-NLS-1$
					}
				}
				if (out != null) {
					try {
						out.close();
					}
					catch (IOException e) {
						log.error(Messages.getString("FileHelper.UNABLE_CLOSE_STREAM"),e); //$NON-NLS-1$
					}
				}
			}
		}
		catch (NoSuchAlgorithmException e) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_DOWNLOAD_URL"),url),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to generate a MD5 checksum string for a file
	 * @param file The file
	 * @return The checksum
	 * @throws IOException Thrown if their are any problems
	 */
	public static String getMD5Checksum(File file) throws IOException {
		try {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					md.update(buf, 0 , len);
				}

				return bytesToHexString(md.digest());
			}
			finally {
				if (is!=null) {
					is.close();
				}
			}
		}
		catch (NoSuchAlgorithmException e) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_GET_CHECKSUM_FILE"),file),e); //$NON-NLS-1$
		}
	}


	private static String bytesToHexString(byte data[]) {

        StringBuilder sb = new StringBuilder(data.length * 2);
        for(int buc = 0; buc < data.length; buc++)
        {
            sb.append(HEX_DIGITS[(data[buc] >> 4) & 0x0F]);
            sb.append(HEX_DIGITS[data[buc] & 0x0F]);
        }

        return sb.toString();
	}

	/**
	 * Used to delete a directory and all it's children
	 *
	 * @param dir The directory to delete
	 * @return True if successful, otherwise false
	 */
	private static boolean deleteDir(File dir) {
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
	public static void displayFile(File file,int startLine, int endLine, OutputStream os)throws IOException {
		PrintStream ps = new PrintStream(os);
		if (startLine<0) {
			startLine = 0;
		}
		int line = 1;
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		while ((str = in.readLine()) != null) {
			if (line>=startLine && line <=endLine) {
				ps.println(line + ": " + str); //$NON-NLS-1$
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
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				results.append(str + LS);
			}
		}
		finally {
			if (in!=null) {
				in.close();
			}
		}
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
			throw new IOException(Messages.getString("FileHelper.INPUT_STREAM_IS_NULL")); //$NON-NLS-1$
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder results = new StringBuilder();
			String str;
			while ((str = in.readLine()) != null) {
				results.append(str + LS);
			}
			return results.toString();
		}
		finally {
			if (in!=null) {
				in.close();
			}
		}
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
    					if (log.isDebugEnabled()) {
    						log.debug("Unzipping " + entry.getName() +" with size " + entry.getSize()); //$NON-NLS-1$ //$NON-NLS-2$
    					}
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

	/**
	 * Used to move a directory or file from once location to another
	 * @param from The old name of the file or directory
	 * @param to The new name of the file or directory
	 * @throws IOException Thrown if their are any problems
	 */
	public static void move(File from, File to) throws IOException {
		if (!from.exists()) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_MOVE_FILE_SRC_NOT_FOUND"),from,to)); //$NON-NLS-1$
		}
		if (to.exists()) {
			throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_MOVE_FILE_DEST_ALREADY_EXISTS"),from,to)); //$NON-NLS-1$
		}
		copy(from, to);
		delete(from);
	}

	/**
	 * Used to delete a file or a directory tree. If a directory is given, then all it's contents are also deleted recusrsivly.
	 * @param file The file or directory to delete
	 * @throws IOException Thrown if their are any problems
	 */
	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			FileHelper.deleteDir(file);
		}
		else {
			if (!file.delete() && file.exists()) {
				throw new IOException(MessageFormat.format(Messages.getString("FileHelper.UNABLE_DELETE_FILE"),file)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Used to create a temporary file with the give contents
	 * @param testConfig The contents to put in the file
	 * @return A reference to the file
	 * @throws IOException Thrown if their are any problems
	 */
	public static File createTmpFileWithContents(StringBuilder testConfig)  throws IOException {
		File configFile = File.createTempFile("config", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
		configFile.deleteOnExit();
		FileHelper.appendContentsToFile(configFile, testConfig);
		return configFile;
	}

	/**
	 * Used to get a stream to a URL. If their is a socket timeout exception, then
	 * this method will wait 5 seconds and try again. It does this 3 times before throwing the
	 * exception out if the method
	 * @param url The URL of the stream
	 * @return The stream
	 * @throws IOException Thrown if their are any problems
	 */
	public static Stream getInputStream(URL url) throws IOException {
		SocketTimeoutException e = null;
		for (int tryCount=0;tryCount<3;tryCount++) {
			try {
				WebFileInputStream is = new WebFileInputStream(url);
				String MIME = is.getMIMEType();
				if (MIME.equals("application/zip")) { //$NON-NLS-1$
					return new Stream(new ZipInputStream(is),MIME,is.getCharset(),url.toExternalForm(),url);
				}
				else {
					return new Stream(is,MIME,is.getCharset(),url.toExternalForm(),url);
				}
			}
			catch (SocketTimeoutException e1) {
				log.warn(MessageFormat.format("Timed out fetching URL ''{0}'', going to retry..",url.toExternalForm()));
				if (e==null) {
					e = e1;
				}
				try {
					Thread.sleep(5000); // Sleep for 3 seconds
				} catch (InterruptedException e2) {
					// Ignore
				}
			}
		}
		throw e;
	}

	/**
	 * Used a temporary file that will be deleted when the JVM exits
	 * @param name name of file
	 * @param ext extension of the file
	 * @return The file
	 * @throws IOException Thrown if their is a problem creating the file
	 */
	public static File createTempFile(String name,String ext) throws IOException {
		final File file = File.createTempFile(name, ext);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				if (file.exists()) {
					try {
						FileHelper.delete(file);
					} catch (IOException e) {
						log.error(MessageFormat.format(Messages.getString("FileHelper.UNABLE_DELETE_TEMP_FILE") ,file),e); //$NON-NLS-1$
					}
				}
			}
		});
		return file;
	}

	/**
	 * Used to get the extension of the file
	 * @param file The file
	 * @return The extension
	 */
	public static String getExtension(File file) {
		String fileName = file.getAbsolutePath();
		int pos = fileName.lastIndexOf("."); //$NON-NLS-1$
		if (pos==-1) {
			return ""; //$NON-NLS-1$
		}
		return fileName.substring(pos+1);
	}

	/**
	 * Used to get the name of the file
	 * @param file The file
	 * @return The name
	 */
	public static String getName(File file) {
		String fileName = file.getName();
		int pos = fileName.lastIndexOf("."); //$NON-NLS-1$
		if (pos==-1) {
			return fileName;
		}
		return fileName.substring(0,pos);
	}

	/**
	 * Used to get the current working directory
	 * @return the current working directory
	 */
	public static File getWorkingDirectory() {
		return new File( System.getProperty("user.dir")); //$NON-NLS-1$
	}

	/**
	 * Used to convert relative paths to absolute paths. This will remove .. from the path
	 * @param path The relative path
	 * @return The absolute path
	 */
	public static File resolveRelativePaths(File path) {
		String segments[] = path.getAbsolutePath().split(Pattern.quote(File.separator));
		List<String>newSegments = new ArrayList<String>();
		for (String seg : segments) {
			if (seg.equals("..")) { //$NON-NLS-1$
				newSegments.remove(newSegments.size()-1);
			}
			else {
				newSegments.add(seg);
			}
		}
		File result = null;
		for (String seg : newSegments) {
			if (result == null) {
				result = new File(seg);
			}
			else {
				result = new File(result,seg);
			}
		}
		return result;
	}
}

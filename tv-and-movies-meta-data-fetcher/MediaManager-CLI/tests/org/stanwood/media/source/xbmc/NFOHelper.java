package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

/**
 * This is a help class for tests that need to create NFO files
 */
@SuppressWarnings("nls")
public class NFOHelper {

	/**
	 * Used to create a NFO file
	 * @param nfoFile The file to create
	 * @param url The media URL
	 * @throws IOException Thrown if their are any problems
	 */
	public static void createNFO(File nfoFile,URL url) throws IOException {
		OutputStream os = null;
		PrintStream ps = null;
		try {
			os = new FileOutputStream(nfoFile);
			ps = new PrintStream(os);

			ps.println("");
			ps.println("           "+nfoFile.getParent());
			ps.println("");
			ps.println("        URL: " + url.toExternalForm());
			ps.println("");
			ps.println("Too long of a plot summary to write here");
			ps.println(" go to the above url and read it :)");
			ps.println("");

			ps.flush();
		}
		finally {
			if (os!=null) {
				os.close();
			}
			if (ps!=null) {
				ps.close();
			}
		}
	}
}

package org.stanwood.media.source.xbmc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.WebFile;


public class XBMCWebUpdater implements IXBMCUpdater {

	private final static Log log = LogFactory.getLog(XBMCWebUpdater.class);

	private final static String UPDATE_SITE_URL = "http://mirrors.xbmc.org/addons/dharma/addons.xml";
	private final static String UPDATE_SITE_MD5 = "http://mirrors.xbmc.org/addons/dharma/addons.xml.md5";
	private final static String UPDATE_SITE_DATA_DIR = "http://mirrors.xbmc.org/addons/dharma";

	@Override
	public void update(File addonsDir) throws XBMCUpdaterException {
		try {
			File newAddon = new File(addonsDir,"addon.xml.new");
			File oldAddon = new File(addonsDir,"addon.xml");
			String actualMD5;
			try {
				actualMD5 = downloadFile(new URL(UPDATE_SITE_URL),newAddon);
			}
			catch (MalformedURLException e) {
				throw new XBMCUpdaterException("Unable to update XBMC scrapers, bad URL",e);
			}
			File newAddonMD5 = new File(addonsDir,"addon.md5.new");
			String expectedMD5;
			try {
				downloadFile(new URL(UPDATE_SITE_MD5),newAddonMD5);
				expectedMD5 = FileHelper.readFileContents(newAddonMD5);
			}
			catch (MalformedURLException e) {
				throw new XBMCUpdaterException("Unable to update XBMC scrapers, bad URL",e);
			}

			if (!expectedMD5.equals(actualMD5)) {
				throw new XBMCUpdaterException("Unable to check for XBMC Scraper updates, MD5 checksum failed.");
			}

			List<String>pluginList;
			if (!oldAddon.exists()) {
				pluginList = getDefaultPlugins();
			}
			else {
				if (checkFilesSame(oldAddon,newAddon)) {
					log.info("No XBMC Scraper updates found.");
					return;
				}
			 	pluginList = getDownloadedPlugins();
			}

			File newPluginsDir = new File(addonsDir,"newplugins");
			for (String plugin : pluginList) {
//				if (newVersion avaliable) {
//					donwload to newPluginsDir
//
//					reversivly check all the depended plugins and download them if they are
//					not already downloaded and at the correct version
//				}
			}

			// move all the new downloaded plugins into place of the old ones
			// replace old addon.xml wiht new addon.xml
		}
		catch (IOException e) {
			throw new XBMCUpdaterException("Unable to update XBMC scrapers",e);
		}
	}

	private List<String> getDownloadedPlugins() {
		return null;
	}

	private List<String> getDefaultPlugins() {
		return null;
	}

	private boolean checkFilesSame(File file1, File file2) throws IOException {
		InputStream is1 = null;
		InputStream is2 = null;
		if(file1.length() != file2.length()) {
			return false;
		}

		try {
			is1 = new FileInputStream(file1);
			is2 = new FileInputStream(file2);

			return inputStreamEquals(is1, is2);

		} catch (Exception ei) {
			return false;
		} finally {
			if(is1 != null) {
				is1.close();
			}
			if(is2 != null) {
				is2.close();
			}
		}

	}

	public static boolean inputStreamEquals(InputStream is1, InputStream is2) {
		int bufsize = 1024;
		byte buff1[] = new byte[bufsize];
		byte buff2[] = new byte[bufsize];


		if(is1 == is2) {
			return true;
		}
		if(is1 == null && is2 == null) {
			return true;
		}
		if(is1 == null || is2 == null) {
			return false;
		}
		try {
			int read1 = -1;
			int read2 = -1;

			do {
				int offset1 = 0;
				while (offset1 < bufsize && (read1 = is1.read(buff1, offset1, bufsize-offset1)) >= 0) {
            		offset1 += read1;
        		}

				int offset2 = 0;
				while (offset2 < bufsize && (read2 = is2.read(buff2, offset2, bufsize-offset2)) >= 0) {
					offset2 += read2;
        		}
				if(offset1 != offset2) {
					return false;
				}
				if(offset1 != bufsize) {
					Arrays.fill(buff1, offset1, bufsize, (byte)0);
					Arrays.fill(buff2, offset2, bufsize, (byte)0);
				}
				if(!Arrays.equals(buff1, buff2)) {
					return false;
				}
			} while(read1 >= 0 && read2 >= 0);
			if(read1 < 0 && read2 < 0)
			 {
				return true;	// both at EOF
			}
			return false;

		} catch (IOException ei) {
			return false;
		}
	}

	private String downloadFile(URL url,File to) throws XBMCUpdaterException {
		OutputStream out = null;
		InputStream is = null;
		try {
			out = new FileOutputStream(to);
			WebFile page = new WebFile(url);

//			String MIME = page.getMIMEType();
			byte[] content = (byte[]) page.getContent();
			MessageDigest md = MessageDigest.getInstance("MD5");
			is = new DigestInputStream(new ByteArrayInputStream(content),md);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			FileHelper.copy(is,to);
			return md.toString();
		} catch (IOException e) {
			throw new XBMCUpdaterException("Unable to download file " + url.toExternalForm(),e);
		} catch (NoSuchAlgorithmException e) {
			throw new XBMCUpdaterException("Unable to download file " + url.toExternalForm(),e);
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new XBMCUpdaterException("Unable to close input stream");
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new XBMCUpdaterException("Unable to close output stream");
				}
			}
		}
	}



}

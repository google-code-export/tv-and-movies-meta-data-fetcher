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
package org.stanwood.media.info;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.stanwood.media.collections.LRUMapCache;
import org.stanwood.media.logging.LoggerOutputStream;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.NativeHelper;
import org.stanwood.media.xml.XMLParser;
import org.w3c.dom.Document;
import org.stanwood.media.xml.XMLParserException;


/**
 * Used to find information about a media file that is containted within the
 * file.
 */
public class MediaFileInfoFetcher {

	private final static Log log = LogFactory.getLog(MediaFileInfoFetcher.class);

	private String mediaInfoCmdPath;

	private LRUMapCache<File,IMediaFileInfo> infoCache;


	/**
	 * The constructor
	 * @param nativeDir The native application directory
	 * @throws StanwoodException Thrown if their is a problem finding the native apps
	 */
	public MediaFileInfoFetcher(File nativeDir) throws StanwoodException {
		mediaInfoCmdPath = NativeHelper.getNativeApplication(nativeDir,"mediainfo"); //$NON-NLS-1$
		boolean errors = false;
		if (!checkCommand(mediaInfoCmdPath)) {
			log.error(MessageFormat.format("Unable to execute command {0}",mediaInfoCmdPath));
			errors = true;
		}
		if (errors) {
			throw new StanwoodException("Required system command not found");
		}

		infoCache = new LRUMapCache<File,IMediaFileInfo>(100);
	}

	/**
	 * Used to get information on a media file
	 * @param file The media file
	 * @return The information object. If this is a video file then it will be
	 *         of type {@link IVideoFileInfo}.
	 * @throws StanwoodException Thrown if their are any problems
	 */
	public IMediaFileInfo getInformation(File file) throws StanwoodException {
		IMediaFileInfo info = infoCache.get(file);
		if (info==null) {
			try {
				File infoFile = FileHelper.createTempFile("output", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
				if (!infoFile.delete() && infoFile.exists()) {
					throw new IOException(MessageFormat.format("Unable to delete file {0}", infoFile));
				}
				try {
					getCommandOutput(true,true,true,mediaInfoCmdPath,"--Output=XML","--Full","--LogFile="+infoFile.getAbsolutePath(),file.getAbsolutePath());  //$NON-NLS-1$//$NON-NLS-2$
				}
				catch (StanwoodException e) {
					log.error(MessageFormat.format("Unable to read media information for file ''{0}''", file),e);
					return null;
				}
				try
				{
					Document dom = XMLParser.parse(infoFile, null);
					if (!infoFile.delete() && infoFile.exists()) {
						throw new IOException(MessageFormat.format("Unable to delete file {0}", infoFile));
					}
					info = MediaInfoFactory.createMediaInfo(file,dom);
					infoCache.put(file,info);
				}
				catch (XMLParserException e ) {
					log.error(MessageFormat.format("Unable to get media inforamtion for file {0} as the xml is not valid", infoFile),e);
					return null;
				}
			} catch (IOException e) {
				throw new StanwoodException("Unable to create temp file");
			}
		}
		return info;
	}

	private boolean checkCommand(String cmd) {
		try {
			boolean capture = !log.isDebugEnabled();
			getCommandOutput(capture,capture,false,cmd);
		}
		catch (StanwoodException e) {
			if (log.isDebugEnabled()) {
				log.debug("Command failed",e); //$NON-NLS-1$
			}
			return false;
		}
		return true;
	}

	private String getCommandOutput(boolean captureStdout,boolean captureStderr,boolean failOnExitCode,String command,Object ... args) throws StanwoodException {
		CommandLine cmdLine= new CommandLine(command);
		for (Object arg : args) {
			if (arg instanceof File) {
				cmdLine.addArgument(((File)arg).getAbsolutePath(),false);
			}
			else if (arg instanceof String) {
				cmdLine.addArgument((String)arg,false);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("About to execute: " + cmdLine.toString()); //$NON-NLS-1$
		}
		Executor exec = new DefaultExecutor();
		exec.setExitValues(new int[] {0,1,2,3,4,5,6,7,8,9,255,-1});

		try {
			ByteArrayOutputStream capture = new ByteArrayOutputStream();
			OutputStream out = new LoggerOutputStream(Level.INFO);
			if (captureStdout) {
				out = capture;
			}
			OutputStream err = new LoggerOutputStream(Level.ERROR);
			if (captureStderr) {
				err = capture;
			}
			exec.setStreamHandler(new PumpStreamHandler(out,err));
			int exitCode = exec.execute(cmdLine);
			if (failOnExitCode && exitCode!=0) {
                log.error(capture.toString());
				throw new StanwoodException(MessageFormat.format("System command returned a non zero exit code ''{0}'': {1}",exitCode,cmdLine.toString()));
			}
			return capture.toString();
		} catch (IOException e) {
			throw new StanwoodException(MessageFormat.format("Unable to execute system command: {0}" ,cmdLine.toString()),e);
		}
	}
}

package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.mp4.Mp4TagReader;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/**
 * Used to parse and update MP4 Video files. This makes use of the jaudiotagger library and extends it
 * to support video files.
 */
public class MP4VideoFileReader {

	private final static Log log = LogFactory.getLog(MP4VideoFileReader.class);

	private static final int MINIMUM_SIZE_FOR_VALID_AUDIO_FILE = 150;

	private Mp4InfoReader ir = new Mp4InfoReader();
	private Mp4TagReader tr = new Mp4TagReader();

	protected GenericAudioHeader getEncodingInfo(RandomAccessFile raf)
			throws CannotReadException, IOException {
		return ir.read(raf);
	}

	protected Tag getTag(RandomAccessFile raf) throws CannotReadException,
			IOException {
		return tr.read(raf);
	}

	/**
	 * Reads the given file, and return an AudioFile object containing the Tag
	 * and the encoding information present in the file. If the file has no tag, an
	 * empty one is returned. If the encoding information is not valid , an exception is
	 * thrown.
	 *
	 * @param f The file to read
	 * @return The audio file or video file
	 * @exception IOException Thrown if their is a IO Problem
	 * @exception TagException Thrown if their is a tag Problem
	 * @exception ReadOnlyFileException Thrown when accessing a read only file
	 * @exception InvalidAudioFrameException Thrown if the a audio frame is invalid
	 * @exception CannotReadException If anything went bad during the read of
	 *                this file
	 */
	public AudioFile read(File f) throws CannotReadException, IOException,
			TagException, ReadOnlyFileException, InvalidAudioFrameException {
		if (log.isDebugEnabled()) {
			log.debug(ErrorMessage.GENERAL_READ.getMsg(f.getAbsolutePath()));
		}

		if (!f.canRead()) {
			throw new CannotReadException(ErrorMessage.GENERAL_READ_FAILED_FILE_TOO_SMALL.getMsg(f.getAbsolutePath()));
		}

		if (f.length() <= MINIMUM_SIZE_FOR_VALID_AUDIO_FILE) {
			throw new CannotReadException(ErrorMessage.GENERAL_READ_FAILED_FILE_TOO_SMALL.getMsg(f.getAbsolutePath()));
		}

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");
			raf.seek(0);

			GenericAudioHeader info = getEncodingInfo(raf);
			raf.seek(0);
			Tag tag = getTag(raf);
			return new AudioFile(f, info, tag);
		} catch (CannotReadException cre) {
			throw cre;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO is this masking exceptions, i.e NullBoxIDException get
			// converted to CannotReadException
			throw new CannotReadException("Unable to read mp4 file: "+f.getAbsolutePath(), e);
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (Exception ex) {
				log
						.warn(ErrorMessage.GENERAL_READ_FAILED_UNABLE_TO_CLOSE_RANDOM_ACCESS_FILE
								.getMsg(f.getAbsolutePath()));
			}
		}
	}
}

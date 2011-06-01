package org.stanwood.media.store.mp4.mp4v2.lib;

@SuppressWarnings("all")
public interface MP4v2File {

	/** Verbosity bitmask: all possible details. */
	public final static int MP4_DETAILS_ALL = 0xFFFFFFFF;
	/** Verbosity bit: reasons for errors. */
	public final static int MP4_DETAILS_ERROR = 0x00000001;
	/** Verbosity bit: warnings. */
	public final static int MP4_DETAILS_WARNING = 0x00000002;
	/** Verbosity bit: read operations. */
	public final static int MP4_DETAILS_READ = 0x00000004;
	/** Verbosity bit: write operations. */
	public final static int MP4_DETAILS_WRITE = 0x00000008;
	/** Verbosity bit: find property operations. */
	public final static int MP4_DETAILS_FIND = 0x00000010;
	/** Verbosity bit: per table entry details. */
	public final static int MP4_DETAILS_TABLE = 0x00000020;
	/** Verbosity bit: per sample details. */
	public final static int MP4_DETAILS_SAMPLE = 0x00000040;
	/** Verbosity bit: per RTP hint details. */
	public final static int MP4_DETAILS_HINT = 0x00000080;
	/** Verbosity bit: ISMA details. */
	public final static int MP4_DETAILS_ISMA = 0x00000100;
	/** Verbosity bit: edit details. */
	public final static int MP4_DETAILS_EDIT = 0x00000200;

	/** Set verbosity level for diagnostic information.
	 *
	 * MP4SetVerbosity allows control over the level of diagnostic information
	 * printed out by the library. It can be called at any time. Since much is
	 * done  by  the library when opening an mp4 file, functions such as
	 * MP4Create(), MP4Modify(), and MP4Read() include a parameter for the.
	 *
	 *  @param hFile handle of file for operation.
	 *  @param verbosity bitmask of the following bits or <b>0</b> for none:
	 *  <ul>
	 *      <li>{@link #MP4_DETAILS_ALL}</li>
	 *      <li>{@link #MP4_DETAILS_ERROR}</li>
	 *      <li>{@link #MP4_DETAILS_WARNING}</li>
	 *      <li>{@link #MP4_DETAILS_READ}</li>
	 *      <li>{@link #MP4_DETAILS_WRITE}</li>
	 *      <li>{@link #MP4_DETAILS_FIND}</li>
	 *      <li>{@link #MP4_DETAILS_TABLE}</li>
	 *      <li>{@link #MP4_DETAILS_SAMPLE}</li>
	 *      <li>{@link #MP4_DETAILS_HINT}</li>
	 *      <li>{@link #MP4_DETAILS_ISMA}</li>
	 *      <li>{@link #MP4_DETAILS_EDIT}</li>
	 *  </ul>
	 *  @see MP4GetVerbosity().
	 */
	public void MP4SetVerbosity( int hFile, int verbosity );

	/** Dump mp4 file contents as ASCII.
	 *
	 *  Dump is an invaluable debugging tool in that in can reveal all the details
	 *  of the mp4 control structures. However, the output will not make much sense
	 *  until you familiarize yourself with the mp4 specification (or the Quicktime
	 *  File Format specification).
	 *
	 *  Note that MP4Dump() will not print the individual values of control tables,
	 *  such as the size of each sample, unless the current verbosity value
	 *  includes the flag #MP4_DETAILS_TABLE.
	 *  See MP4SetVerbosity() for how to set this flag.
	 *
	 *  @param hFile handle of file to dump.
	 *  @param pDumpFile dump destination. If NULL stdout will be used.
	 *  @param dumpImplicits prints properties which would not actually be
	 *      written to the mp4 file, but still exist in mp4 control structures.
	 *      ie. they are implicit given the current values of other controlling
	 *      properties.
	 *
	 *  @return <b>true</b> on success, <b>false</b> on failure.
	 *
	 *  @see #MP4SetVerbosity(int, int)
	 */
	boolean MP4Dump(int hFile,String tpDumpFile,boolean dumpImplicits);

	/** Return a textual summary of an mp4 file.
	 *
	 *  MP4FileInfo provides a string that contains a textual summary of the
	 *  contents of an mp4 file. This includes the track id's, the track type,
	 *  and track specific information. For example, for a video track, media
	 *  encoding, image size, frame rate, and bitrate are summarized.
	 *
	 *  Note that the returned string is malloc'ed, so it is the caller's
	 *  responsibility to free() the string. Also note that the returned string
	 *  contains newlines and tabs which may or may not be desirable.
	 *
	 *  The following is an example of the output of MP4Info():
	<code>
	Track  Type   Info
	1      video  MPEG−4 Simple @ L3, 119.625 secs, 1008 kbps, 352x288 @ 24.00 fps
	2      audio  MPEG−4, 119.327 secs, 128 kbps, 44100 Hz
	3      hint   Payload MP4V−ES for track 1
	4      hint   Payload mpeg4−generic for track 2
	5      od     Object Descriptors
	6      scene  BIFS
	</code>
	 *
	 *  @param fileName pathname to mp4 file to summarize.
	 *  @param trackId specifies track to summarize. If the value is
	 *      #MP4_INVALID_TRACK_ID, the summary info is created for all
	 *      tracks in the file.
	 *
	 *  @return On success a malloc'd string containing summary information.
	 *      On failure, <b>NULL</b>.
	 */
	String MP4FileInfo(String fileName,/*MP4TrackId*/int  trackId  );

	/** Close an mp4 file.
	 *  MP4Close closes a previously opened mp4 file. If the file was opened
	 *  writable with MP4Create() or MP4Modify(), then MP4Close() will write
	 *  out all pending information to disk.
	 *
	 *  @param hFile handle of file to close.
	 */
	public void MP4Close(int hFile );

	/** Create a new mp4 file.
	 *
	 *  MP4Create is the first call that should be used when you want to create
	 *  a new, empty mp4 file. It is equivalent to opening a file for writing,
	 *  but also involved with creation of necessary mp4 framework structures.
	 *  ie. invoking MP4Create() followed by MP4Close() will result in a file
	 *  with a non-zero size.
	 *
	 *  @param fileName pathname of the file to be created.
	 *  @param verbosity bitmask of diagnostic details the library
	 *      should print to stdout during its functioning.
	 *  @param flags bitmask that allows the user to set 64-bit values for
	 *      data or time atoms. Valid bits may be any combination of:
	 *          @li #MP4_CREATE_64BIT_DATA
	 *          @li #MP4_CREATE_64BIT_TIME
	 *
	 *  @return On success a handle of the newly created file for use in
	 *      subsequent calls to the library.
	 *      On error, #MP4_INVALID_FILE_HANDLE.
	 *  @see #MP4SetVerbosity(int, int)
	 */
	int MP4Create(String fileName,int verbosity,int flags);

	/** Modify an existing mp4 file.
	 *
	 *  MP4Modify is the first call that should be used when you want to modify
	 *  an existing mp4 file. It is roughly equivalent to opening a file in
	 *  read/write mode.
	 *
	 *  Since modifications to an existing mp4 file can result in a sub−optimal
	 *  file layout, you may want to use MP4Optimize() after you have  modified
	 *  and closed the mp4 file.
	 *
	 *  @param fileName pathname of the file to be modified.
	 *  @param verbosity bitmask of diagnostic details the library
	 *      should print to stdout during its functioning.
	 *  @param flags currently ignored.
	 *
	 *  @return On success a handle of the target file for use in subsequent calls
	 *      to the library.
	 *      On error, #MP4_INVALID_FILE_HANDLE.
	 *
	 *  @see #MP4SetVerbosity(int, int)
	 */
	int MP4Modify(String fileName,int verbosity,int flags);


	/** Optimize the layout of an mp4 file.
	 *
	 *  MP4Optimize reads an existing mp4 file and writes a new version of the
	 *  file with the two important changes:
	 *
	 *  First, the mp4 control information is moved to the beginning of the file.
	 *  (Frequenty it is at the end of the file due to it being constantly
	 *  modified as track samples are added to an mp4 file). This optimization
	 *  is useful in that in allows the mp4 file to be HTTP streamed.
	 *
	 *  Second, the track samples are interleaved so that the samples for a
	 *  particular instant in time are colocated within the file. This
	 *  eliminates disk seeks during playback of the file which results in
	 *  better performance.
	 *
	 *  There are also two important side effects of MP4Optimize():
	 *
	 *  First, any free blocks within the mp4 file are eliminated.
	 *
	 *  Second, as a side effect of the sample interleaving process any media
	 *  data chunks that are not actually referenced by the mp4 control
	 *  structures are deleted. This is useful if you have called MP4DeleteTrack()
	 *  which only deletes the control information for a track, and not the
	 *  actual media data.
	 *
	 *  @param fileName pathname of (existing) file to be optimized.
	 *  @param newFileName pathname of the new optimized file.
	 *      If NULL a temporary file will be used and <b>fileName</b>
	 *      will be over-written upon successful completion.
	 *  @param verbosity bitmask of diagnostic details the library
	 *      should print to stdout during its functioning.
	 *
	 *  @return <b>true</b> on success, <b>false</b> on failure.
	 *
	 *  @see #MP4SetVerbosity(int, int)
	 */
	boolean MP4Optimize(String fileName,String newFileName,int verbosity);

	/** Read an existing mp4 file.
	 *
	 *  MP4Read is the first call that should be used when you want to just
	 *  read an existing mp4 file. It is equivalent to opening a file for
	 *  reading, but in addition the mp4 file is parsed and the control
	 *  information is loaded into memory. Note that actual track samples are not
	 *  read into memory until MP4ReadSample() is called.
	 *
	 *  @param fileName pathname of the file to be read.
	 *  @param verbosity bitmask of diagnostic details the library
	 *      should print to stdout during its functioning.
	 *
	 *  @return On success a handle of the file for use in subsequent calls to
	 *      the library.
	 *      On error, #MP4_INVALID_FILE_HANDLE.
	 * @see #MP4SetVerbosity(int, int)
	 */
	public int MP4Read(String fileName,int verbosity);

	/** Read an existing mp4 file.
	 *
	 *  MP4ReadProvider is the first call that should be used when you want to just
	 *  read an existing mp4 file. It is equivalent to opening a file for
	 *  reading, but in addition the mp4 file is parsed and the control
	 *  information is loaded into memory. Note that actual track samples are not
	 *  read into memory until MP4ReadSample() is called.
	 *
	 *  @param fileName pathname of the file to be read.
	 *  @param verbosity bitmask of diagnostic details the library
	 *      should print to stdout during its functioning.
	 *  @param fileProvider custom implementation of file I/O operations.
	 *      All functions in structure must be implemented.
	 *      The structure is immediately copied internally.
	 *
	 *  @return On success a handle of the file for use in subsequent calls to
	 *      the library.
	 *      On error, #MP4_INVALID_FILE_HANDLE.
	 *
	 *  @see #MP4SetVerbosity(int, int) for <b>verbosity</b> values.
	 */
//	public MP4ReadProvider(String fileName,int verbosity,const MP4FileProvider* fileProvider );
}

package org.stanwood.media.info;


import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.atomicparsley.MP4AtomicParsleyManager;
import org.stanwood.media.util.FileHelper;

/**
 * <p>This is a CLI application used to print media file information.</p>
 * <p>
 * It has the following usage:
 * <code>
 * usage: mm-media-file-info [-h] [-c <file>] [-l <info|debug|file>] <media file>
 *
 *  --version, -v                 Display the version
 *  --config_file, -c <file>      The location of the config file. If not present, attempts to load it from  ~/.mediaManager/mediamanager-conf.xml or /etc/mediamanager-conf.xml
 *  --log_config, -l <info|debug|file>
 *                                The log config mode [<INFO>|<DEBUG>|<log4j config file>]
 *  --help, -h                    Show the help * </code>
 * </p>
 *
 */
public class CLIMediaFileInformation extends AbstractLauncher {

	private static IExitHandler exitHandler = null;

	private File mp4File;

	/**
	 * The constructor
	 * @param exitHandler the exit handler
	 */
	public CLIMediaFileInformation(IExitHandler exitHandler) {
		super("mm-media-file-info",new ArrayList<Option>(),exitHandler, System.out, System.err); //$NON-NLS-1$
	}

	@Override
	protected boolean processOptions(String[] args, CommandLine cmd) {
		if (cmd.getArgs().length==0) {
			fatal(Messages.getString("CLIMediaFileInformation.MISSING_ARG")); //$NON-NLS-1$
			return false;
		}
		else if (cmd.getArgs().length>1) {
			fatal(Messages.getString("CLIMediaFileInformation.MORE_THAN_ONE_ARG")); //$NON-NLS-1$
			return false;
		}
		else {
			mp4File=new File(cmd.getArgs()[0]);
		}

		return true;
	}

	@Override
	protected boolean run() {
		try {
			IMP4Manager mp4Manager = new MP4AtomicParsleyManager();
			mp4Manager.init(getController().getNativeFolder());

			MediaFileInfoFetcher fileInfoFetcher = new MediaFileInfoFetcher(getController().getNativeFolder());
			IMediaFileInfo info = fileInfoFetcher.getInformation(mp4File);
			List<IAtom> atoms = null;
			String ext = FileHelper.getExtension(mp4File);
			if (ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("m4v") || ext.equalsIgnoreCase("m4a")) {   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				info(Messages.getString("CLIMediaFileInformation.READING_ATOMS")); //$NON-NLS-1$
				atoms = mp4Manager.listAtoms(mp4File);
				info(""); //$NON-NLS-1$
			}

			if (info!=null) {
				outputHeader(Messages.getString("CLIMediaFileInformation.FILE_FORMAT")); //$NON-NLS-1$


				info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.FILE_SIZE"), info.getFileSize())); //$NON-NLS-1$
				if (info instanceof IVideoFileInfo) {
					IVideoFileInfo vinfo = (IVideoFileInfo)info;
					info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.DISPLAY_ASPECT_RATIO"), vinfo.getAspectRatio().getDescription())); //$NON-NLS-1$
					info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.FRAME_RATE"), vinfo.getFrameRate())); //$NON-NLS-1$
					info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.RESOLUTION"), vinfo.getWidth()+"x"+vinfo.getHeight()));  //$NON-NLS-1$//$NON-NLS-2$
					if (vinfo.getResolutionFormat()==null) {
						info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.RESOLUTION_FORAMT"),Messages.getString("CLIMediaFileInformation.UKNOWN"))); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else {
						info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.RESOLUTION_FORAMT"),vinfo.getResolutionFormat().getDescription())); //$NON-NLS-1$
					}
					info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.INTERLACED"),vinfo.isInterlaced())); //$NON-NLS-1$
					info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.HIGHDEF"),vinfo.isHighDef())); //$NON-NLS-1$
					info(MessageFormat.format(Messages.getString("CLIMediaFileInformation.WIDESCREEN"),vinfo.isWideScreen())); //$NON-NLS-1$
				}
				else {
					warn(Messages.getString("CLIMediaFileInformation.NOT_A_VIDEO")); //$NON-NLS-1$
				}
			}
			else {
				fatal(Messages.getString("CLIMediaFileInformation.UNABLE_READ_INFO")); //$NON-NLS-1$
				return false;
			}
			info(""); //$NON-NLS-1$
			outputHeader(Messages.getString("CLIMediaFileInformation.MP4_ATOMS")); //$NON-NLS-1$
			if (atoms==null || atoms.size()==0) {
				info(Messages.getString("CLIMediaFileInformation.NO_METADATA")); //$NON-NLS-1$
			}
			else {
				for (IAtom a : atoms) {
					if (a!=null) {
						getStdout().println(a.toString());
					}
				}
			}
		} catch (Exception e) {
			fatal(e);
			return false;
		}
		return true;
	}

	private void outputHeader(String msg) {
		info(msg);
		StringBuilder s = new StringBuilder();
		while (s.length()<msg.length()) {
			s.append("="); //$NON-NLS-1$
		}
		info(s.toString());
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}

	/**
	 * The entry point to the application. For details see the class documentation.
	 *
	 * @param args The arguments.
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIMediaFileInformation ca = new CLIMediaFileInformation(exitHandler);
		ca.launch(args);
	}

	@Override
	protected void printUsage(Options options, PrintStream stdout, PrintStream stderr) {
		stdout.println(Messages.getString("CLIMediaFileInformation.USAGE")+getName()+" [-h] [-c <file>] [-l <info|debug|file>] <media file>");  //$NON-NLS-1$//$NON-NLS-2$
		stdout.println(""); //$NON-NLS-1$
	}

}

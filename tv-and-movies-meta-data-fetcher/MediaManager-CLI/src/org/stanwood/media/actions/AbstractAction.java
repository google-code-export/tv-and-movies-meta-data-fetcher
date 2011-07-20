package org.stanwood.media.actions;

import java.io.File;
import java.text.MessageFormat;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.PatternException;
import org.stanwood.media.actions.rename.PatternMatcher;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.util.FileHelper;

/**
 * Helper class that actions should extends so that they only have to
 * implement action methods that are needed.
 */
public abstract class AbstractAction implements IAction {

	private final static PatternMatcher PM = new PatternMatcher();
	private boolean testMode;

	/** {@inheritDoc} */
	@Override
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isTestMode() {
		return this.testMode;
	}

	/** {@inheritDoc} */
	@Override
	public void performOnDirectory(MediaDirectory dir, File file,IActionEventHandler actionEventHandler) throws ActionException {
	}

	/** {@inheritDoc}
	 */
	@Override
	public void init(MediaDirectory dir) throws ActionException {
	}

	/** {@inheritDoc} */
	@Override
	public void finished(MediaDirectory dir) throws ActionException {

	}

	protected String resolvePatterns(MediaDirectory dir,String input,IVideo video,File mediaFile,Integer part) throws ActionException {
		String s = input;
		s =	s.replaceAll("\\$MEDIAFILE_NAME", FileHelper.getName(mediaFile).replaceAll(" ", "\\\\ ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		s =	s.replaceAll("\\$MEDIAFILE_EXT", FileHelper.getExtension(mediaFile).replaceAll(" ", "\\\\ "));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		s =	s.replaceAll("\\$MEDIAFILE_DIR", mediaFile.getParent().replaceAll(" ", "\\\\ "));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		s =	s.replaceAll("\\$MEDIAFILE", mediaFile.getAbsolutePath().replaceAll(" ", "\\\\ "));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

		if (video!=null && dir!=null) {
			MediaDirConfig dirConfig = dir.getMediaDirConfig();
			try {
				String ext = FileHelper.getExtension(mediaFile);
				if (dirConfig.getMode() == Mode.TV_SHOW) {
					s = PM.getNewTVShowName(dirConfig, s, (Episode)video,  ext);
				}
				else {
					s = PM.getNewFilmName(dirConfig, s, (Film)video,  ext,part);
				}
			} catch (PatternException e) {
				throw new ActionException(MessageFormat.format(Messages.getString("AbstractAction.UNABLE_TO_RESOLVE_PATTERN"),input),e); //$NON-NLS-1$
			}
		}
		return s;

	}
}

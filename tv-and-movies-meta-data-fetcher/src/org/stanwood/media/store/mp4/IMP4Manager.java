package org.stanwood.media.store.mp4;

import java.io.File;
import java.util.List;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;

/**
 * This interface should be implemented by classes that manager mp4 files
 */
public interface IMP4Manager {

	/**
	 * Used to get a list of atoms in the MP4 file.
	 *
	 * @param mp4File The MP4 file
	 * @return A list of atoms
	 * @throws MP4Exception Thrown if their is a problem reading the MP4 file
	 */
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception ;

	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a TV Show episode
	 *
	 * @param mp4File The MP4 file
	 * @param episode The episode details
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public void updateEpsiode(File mp4File, Episode episode) throws MP4Exception;

	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a Film. It also removes any artwork before adding the
	 * Film atoms and artwork.
	 *
	 * @param mp4File The MP4 file
	 * @param film The film details
	 * @param part The part number of the film, or null if it does not have parts
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public void updateFilm(File mp4File, Film film, Integer part) throws MP4Exception;

}

package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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
	  * Used to add atoms to a MP4 file.
	  * @param mp4File The MP4 file
	  * @param atoms The atoms to add to the file
 	  * @throws MP4Exception Thrown if their is a problem updating the atoms
	  */
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception;

	/**
	 * Used to create a atom
	 * @param name The name of the atom
	 * @param value The value of the atom
	 * @return the atom
	 */
	public IAtom createAtom(MP4AtomKey name, String value);

	/**
	 * Used to create a atom
	 * @param name The name of the atom
	 * @param value The value of the atom
	 * @return the atom
	 */
	public IAtom createAtom(MP4AtomKey name, boolean value);

	/**
	 * Used to create a range atom
	 * @param name The name of the atom
	 * @param number The number of items in the rage
	 * @param total The total number possible
	 * @return The atom
	 */
	public IAtom createAtom(MP4AtomKey name,short number, short total);

	/**
	 * Used to create a number atom
	 * @param name The name of the atom
	 * @param value The value of the atom
	 * @return The atom
	 */
	public IAtom createAtom(MP4AtomKey name, int value);

	/**
	 * Used to create a artwork atom
	 * @param name The name of the atom
	 * @param type The artwork type
	 * @param size The size of the artwork
	 * @param data The data in the artwork
	 * @return The atom
	 */
	public IAtom createAtom(MP4AtomKey name, MP4ArtworkType type, int size, byte[] data);


	/**
	 * Used to setup the manager
	 * @param nativeDir The native folder been used or configured. Null if can't be found
	 * @throws MP4Exception Thrown if their is a problem setup up the manager
	 */
	public void init(File nativeDir) throws MP4Exception;

	/**
	 * Used to set parameters on the manager
	 * @param key The key of the parameter
	 * @param value The name of the parameter
	 */
	public void setParameter(String key, String value);

	/**
	 * Used to download the artwork from a URL to a file
	 * @param imageUrl The file URL
	 * @return The file
	 * @throws IOException Thrown if their are any problems downloading the file
	 */
	public File getArtworkFile(URL imageUrl) throws IOException;

}

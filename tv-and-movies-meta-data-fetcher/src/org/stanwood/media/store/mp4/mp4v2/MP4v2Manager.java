package org.stanwood.media.store.mp4.mp4v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.store.mp4.mp4v2.lib.file.MP4v2File;
import org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItemList.ByReference;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4TagArtwork;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags;

/**
 * This is a MP4 Manager that uses the native library mp4v2 to manipulate MP4 files.
 */
public class MP4v2Manager implements IMP4Manager {

	private final static Log log = LogFactory.getLog(MP4v2Manager.class);

	private MP4v2Library lib = null;

	/** The constructor */
	public MP4v2Manager() {
	}

	/** {@inheritDoc}
	 * @throws MP4Exception */
	@Override
	public void init() throws MP4Exception {
		try {
			lib = MP4v2Library.INSTANCE;
		}
		catch (Throwable t) {
			throw new MP4Exception("Unable to load native 'mp4v2' library",t);
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception {
		if (!mp4File.exists()) {
			throw new MP4Exception("Unable to find mp4 file: " + mp4File);
		}

		int fileHandle = lib.MP4Read(mp4File.getAbsolutePath(),MP4v2File.MP4_DETAILS_ERROR);
		if (fileHandle == MP4v2File.MP4_INVALID_FILE_HANDLE) {
			throw new MP4Exception("Unable to open '"+mp4File+"' for reading");
		}

		try {
			MP4Tags tags = lib.MP4TagsAlloc();
			try {
				List<IAtom> atoms = getAtoms(fileHandle, tags);
				return atoms;
			}
			finally {
				lib.MP4TagsFree(tags);
			}
		}
		finally {
			lib.MP4Close(fileHandle);
		}

	}

	protected List<IAtom> getAtoms(int fileHandle, MP4Tags tags) {
		List<IAtom>atoms = new ArrayList<IAtom>();
		lib.MP4TagsFetch(tags, fileHandle);
		if (tags.name!=null) {
			atoms.add(createAtom("©nam", tags.name.getString(0)));
		}
		if (tags.releaseDate!=null) {
			atoms.add(createAtom("©day", tags.releaseDate.getString(0)));
		}
		if (tags.disk!=null) {
			atoms.add(createAtom("disk",tags.disk.index, tags.disk.total));
		}
		if (tags.tvShow!=null) {
			atoms.add(createAtom("tvsh",tags.tvShow.getString(0)));
		}
		if (tags.tvEpisodeID!=null) {
			atoms.add(createAtom("tven",tags.tvEpisodeID.getString(0)));
		}
		if (tags.tvSeason!=null) {
			atoms.add(createAtom("tvsn",tags.tvSeason.getValue()));
		}
		if (tags.tvEpisode!=null) {
			atoms.add(createAtom("tves",tags.tvEpisode.getValue()));
		}
		if (tags.description!=null) {
			atoms.add(createAtom("desc",tags.description.getString(0)));
		}
		if (tags.longDescription!=null) {
			atoms.add(createAtom("ldes",tags.longDescription.getString(0)));
		}
		if( tags.artworkCount >0 ) {
			MP4TagArtwork artworks[] = (MP4TagArtwork [])tags.artwork.toArray(new MP4TagArtwork[tags.artworkCount]);
			for (MP4TagArtwork art : artworks) {
				atoms.add(createAtom("covr",MP4ArtworkType.getForValue(art.type),art.size,art.data.getByteArray(0, art.size)));
			}
		}
		if (tags.encodingTool !=null) {
			atoms.add(createAtom("©too",tags.encodingTool.getString(0)));
		}
		if( tags.mediaType !=null) {
			atoms.add(createAtom("stik",tags.mediaType.getByte(0)));
		}
		if( tags.contentRating !=null) {
			atoms.add(createAtom("rtng",tags.contentRating.getByte(0)));
		}
		if (tags.genre != null) {
			atoms.add(createAtom("©gen",tags.genre.getString(0)));
		}
		if (tags.genreType != null) {
			atoms.add(createAtom("gnre",tags.genreType.getValue()));
		}
		if (tags.category != null) {
			atoms.add(createAtom("catg",tags.category.getString(0)));
		}
		return atoms;
	}

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		if (log.isDebugEnabled()) {
			log.debug("Upadting MP4 file '" + mp4File+"' with "+atoms.size());
		}
		int fileHandle = lib.MP4Modify(mp4File.getAbsolutePath(), MP4v2File.MP4_DETAILS_ERROR, 0);
		if (fileHandle == MP4v2File.MP4_INVALID_FILE_HANDLE) {
			throw new MP4Exception("Unable to open '"+mp4File+"' for modification");
		}
		checkAppleListItemBoxExists(fileHandle,mp4File);
		try {
			writeAppleMetadata(atoms, fileHandle);
		}
		finally {
			lib.MP4Close(fileHandle);
		}
		if (log.isDebugEnabled()) {
			log.debug("MP4 modified '" + mp4File+"'");
		}
	}

	private void checkAppleListItemBoxExists(int fileHandle,File mp4File) throws MP4Exception {
		// TODO find a way of adding a apple container metadata box if it's not found
		ByReference list = lib.MP4ItmfGetItems(fileHandle);
		try {
			if (list.elements==null) {
				throw new MP4Exception("MP4 File '"+mp4File+"' does not have apple metadata container box, so don't know how to update it");
			}
		}
		finally {
//			lib.MP4ItmfItemListFree(list);
		}
	}

	protected void writeAppleMetadata(List<IAtom> atoms, int fileHandle) {
		MP4Tags tags = null;
		try {
			tags = lib.MP4TagsAlloc();
			lib.MP4TagsFetch(tags, fileHandle);
			if (hasArtwrokAtom(atoms)) {
				for (int i=0;i<tags.artworkCount;i++) {
					lib.MP4TagsRemoveArtwork(tags, i);
				}
			}

			for (IAtom atom : atoms) {
				((AbstractMP4v2Atom)atom).writeAtom(tags);

			}
			lib.MP4TagsStore( tags, fileHandle );
		}
		finally {
			if (tags!=null) {
				lib.MP4TagsFree(tags);
				tags = null;
			}
		}
	}

	private boolean hasArtwrokAtom(List<IAtom> atoms) {
		for (IAtom atom : atoms) {
			if (atom.getName().equals("covr")) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, String value) {
		return new MP4v2AtomString(lib,name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, int value) {
		return new MP4v2AtomInteger(lib,name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name,short number, short total) {
		return new MP4v2AtomRange(lib,name,number,total );
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name,MP4ArtworkType type, int size, byte data[]) {
		return new MP4v2AtomArtwork(lib,name,type,size,data);
	}
}

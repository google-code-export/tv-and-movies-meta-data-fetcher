package org.stanwood.media.store.mp4.mp4v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4Tags;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;

/**
 * This is a MP4 Manager that uses the native library mp4v2 to manipulate MP4 files.
 */
public class MP4v2Manager implements IMP4Manager {

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
		List<IAtom>atoms = new ArrayList<IAtom>();
		int fileHandle = lib.MP4Read(mp4File.getAbsolutePath(),0);
//		lib.MP4SetVerbosity(fileHandle,MP4v2File.MP4_DETAILS_ALL);
		try {
			MP4Tags tags = lib.MP4TagsAlloc();
//			MP4Tags tags = new MP4Tags();
			System.out.println("fetching tags: " + fileHandle);
			lib.MP4TagsFetch(tags, fileHandle);
			System.out.println("Fetched tags: ");
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
				atoms.add(createAtom("tvsn",tags.mediaType.getByte(0)));
			}
			if (tags.tvEpisode!=null) {
				atoms.add(createAtom("tves",tags.mediaType.getByte(0)));
			}
			if (tags.description!=null) {
				atoms.add(createAtom("desc",tags.description.getString(0)));
			}
			if (tags.longDescription!=null) {
				atoms.add(createAtom("ldes",tags.longDescription.getString(0)));
			}
//			tags.artwork.
//			if( tags.artworkCount >0 ) {
//				MP4TagArtwork art = tags.artwork;
//				if (art!=null) {
//					createAtom("covr",MP4ArtworkType.getForValue(art.type.getIntValue()),art.size,art.data.getByteArray(0, art.size));
//				}

//			}
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
			lib.MP4TagsFree(tags);
		}
		finally {
			lib.MP4Close(fileHandle);
		}
		return atoms;
	}

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		int fileHandle = lib.MP4Modify(mp4File.getAbsolutePath(), 0, 0);
		try {
			MP4Tags tags = null;
			try {
				tags = lib.MP4TagsAlloc();
				lib.MP4TagsFetch(tags, fileHandle);
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
		finally {
			lib.MP4Close(fileHandle);
		}
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

package org.stanwood.media.store.mp4.isoparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.mp4.AtomNameLookup;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.StikValue;
import org.stanwood.media.store.mp4.isoparser.boxes.AppleDiscNumberBox;
import org.stanwood.media.store.mp4.isoparser.boxes.GenericStringBox;
import org.stanwood.media.util.FileHelper;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.PropertyBoxParserImpl;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.UnknownBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.AbstractAppleMetaDataBox;
import com.coremedia.iso.boxes.apple.AppleCoverBox;
import com.coremedia.iso.boxes.apple.AppleDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;

/**
 * This class is a wrapper the the atomic parsley application {@link "http://atomicparsley.sourceforge.net/"} It is used
 * to store and retrieve atoms to a MP4 file.
 */
public class ISOParserMP4Manager implements IMP4Manager {

	// http://help.mp3tag.de/main_tags.html

	private final static Log log = LogFactory.getLog(ISOParserMP4Manager.class);
	private final static AtomNameLookup nameLookup = new AtomNameLookup();

	/**
	 * Used to get a list of atoms in the MP4 file.
	 *
	 * @param mp4File The MP4 file
	 * @return A list of atoms
	 * @throws MP4Exception Thrown if their is a problem reading the MP4 file
	 */
	@Override
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception {
		try {
			// http://code.google.com/p/mp4parser/
			List<IAtom> atoms = new ArrayList<IAtom>();

			IsoFile isoFile = getIsoFile(mp4File,getProperties());
	        isoFile.parse();
	        AppleItemListBox appleItemListBox = (AppleItemListBox) IsoFileConvenienceHelper.get(isoFile, "moov/udta/meta/ilst");
	        if (appleItemListBox!=null) {
		        List<Box> boxes = appleItemListBox.getBoxes();
				if (boxes !=null) {
			        for (Box box : boxes) {
			        	if (!(box instanceof UnknownBox)) {
 			        		atoms.add(getAtomTextValue(box));
			        	}
			        }
				}
	        }
			return atoms;
		} catch (IOException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		}

	}

	private Properties getProperties() throws MP4Exception {
		InputStream is = null;
		try {
			Properties properties = new Properties();
			is = ISOParserMP4Manager.class.getResourceAsStream("/isoparser-default.properties");
			properties.load(is);
			properties.setProperty("ilst-"+"catg", GenericStringBox.class.getName()+"(type)");
			properties.setProperty("ilst-"+"disk", AppleDiscNumberBox.class.getName()+"()");
			properties.setProperty("meta",MetaBox.class.getName()+"()");
		    return properties;
		}
		catch (IOException e) {
			throw new MP4Exception("Unable to create mp4 parser",e);
		}
		finally {
			try {
				is.close();
			} catch (IOException e) {
				log.error("Unable to close stream",e);
			}
		}

	}

	private IsoFile getIsoFile(final File mp4File,Properties properties) throws MP4Exception {
		try {
		    PropertyBoxParserImpl boxParser = new PropertyBoxParserImpl(properties) {
		    	@Override
				public AbstractBox parseBox(IsoBufferWrapper in, ContainerBox parent, Box lastMovieFragmentBox) throws IOException {
		    		AbstractBox box = super.parseBox(in,parent,lastMovieFragmentBox);
		    		if (box==null) {
		    			throw new IOException("Parse error parsing file: " + mp4File);
		    		}

		    		return box;
		    	}
		    };

			IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapper(mp4File);
			IsoFile isoFile = new IsoFile(isoBufferWrapper,boxParser);
			return isoFile;
		}
		catch (IOException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		}
	}

	private Atom getAtomTextValue(Box box) throws MP4Exception, IOException {
		String type = new String(box.getType(),Charset.forName("ISO-8859-1"));
		if (box instanceof AppleCoverBox ) {
			AppleCoverBox b = ((AppleCoverBox) box);
			AppleDataBox b1 = (AppleDataBox) b.getBoxes().get(0);
			String artType ="UNKNOWN";
			if (b1.getFlags()==0xe) {
				artType = "COVERART_PNG";
			}
			else if (b1.getFlags()==0xd) {
				artType = "COVERART_JPEG";
			}
			Atom a = createAtom(b.getDisplayName(),type,"Artwork of type "+artType+" and size "+ b1.getContent().length);
			return a;
		}
		else if (box instanceof AbstractAppleMetaDataBox ) {
			AbstractAppleMetaDataBox b = (AbstractAppleMetaDataBox)box;

			Atom a = createAtom(b.getDisplayName(),type,b.getValue());
			return a;
		}
		else if (box instanceof GenericStringBox) {
			GenericStringBox b = (GenericStringBox)box;
			Atom a = createAtom(b.getDisplayName(),type,b.getValue());
			return a;
		}
		else  if (box instanceof AppleDiscNumberBox) {
			AppleDiscNumberBox b = (AppleDiscNumberBox)box;
			Atom a = createAtom("disk",b.getDiskNumber(),b.getNumberOfDisks());

			return a;
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Unknown atom type: " + new String(box.getType(),Charset.forName("ISO-8859-1")));
			}
			return createUnkownAtom(type,box);
		}

	}

	static long toLong(byte b) {
        return b < 0 ? b + 256 : b;
    }

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		log.info("Updating metadata in MP4 file: "+mp4File);
		Properties properties = getProperties();
		IsoFile isoFile = getIsoFile(mp4File,properties);
		FileOutputStream os = null;
		try {
			isoFile.parse();
			AppleItemListBox appleItemListBox = findAppleItemListBox(isoFile);
	        if (appleItemListBox!=null) {
		        List<Box> boxes = appleItemListBox.getBoxes();
		        for (Box b : boxes) {
		        	for (IAtom a : atoms) {
		        		String type = new String(b.getType(),Charset.forName("ISO-8859-1"));
		        		String name =a.getName();
		        		if (name.equals(type)) {
		        			appleItemListBox.removeBox(b);
		        			break;
		        		}
		        		if (name.startsWith("©")) {
		        			if (name.substring(1).equals(type.substring(1))) {
			        			appleItemListBox.removeBox(b);
			        			break;
			        		}
		        		}
		        	}
		        }
	        }

			for (IAtom atom : atoms) {
				if (atom.getName().equals("covr")) {
					writeArtworkFiled(isoFile,(Atom)atom,appleItemListBox);
				}
				else {
					writeTextField(isoFile,(Atom)atom,appleItemListBox,properties);
				}
			}

			File newFile = FileHelper.createTempFile("media", ".mp4");
			FileHelper.delete(newFile);
			os = new FileOutputStream(newFile);
			isoFile.getBox(new IsoOutputStream(os));
			FileHelper.delete(mp4File);
			FileHelper.copy(newFile, mp4File);
			FileHelper.delete(newFile);
			if (log.isDebugEnabled()) {
				log.debug("Created mp4 file '"+mp4File.getAbsolutePath()+"' with size " + mp4File.length());
			}
		}
		catch (IOException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		}
		finally {
			try {
				if (os!=null) {
					os.close();
				}
			} catch (IOException e) {
				log.error("Unable to close stream",e);
			}
		}

	}

	private void writeArtworkFiled(IsoFile isoFile, Atom atom,AppleItemListBox appleItemListBox) throws IOException {
		String name = atom.getName();
		if (atom.getName().equals("covr")) {
			AppleCoverBox box = (AppleCoverBox) IsoFileConvenienceHelper.get(appleItemListBox, name);
			if (box!=null) {
				appleItemListBox.removeBox(box);
			}

			box = new AppleCoverBox();
			box.setJpg(readFile(new File(atom.getValue())));
			appleItemListBox.addBox(box);
		}
	}

	private byte[] readFile(File file) throws IOException {
		FileInputStream fin = null;
		try {

			fin = new FileInputStream(file);

			byte fileContent[] = new byte[(int) file.length()];

			fin.read(fileContent);

			return fileContent;
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
	}

	private void writeTextField(IsoFile isoFile, Atom atom,AppleItemListBox appleItemListBox,Properties properties) throws MP4Exception {
		String name = atom.getName();
		Box box = IsoFileConvenienceHelper.get(appleItemListBox, name);
		if (box!=null) {
			appleItemListBox.removeBox(box);
		}
		if (name.startsWith("©")) {
			String newName = "ﾩ"+name.substring(1);
			box = IsoFileConvenienceHelper.get(appleItemListBox, newName);
			if (box!=null) {
				appleItemListBox.removeBox(box);
			}
		}

		box = createBox(atom,properties);
		appleItemListBox.addBox(box);
	}

	protected AppleItemListBox findAppleItemListBox(IsoFile isoFile) {
		AbstractContainerBox moovBox = (AbstractContainerBox)IsoFileConvenienceHelper.get(isoFile,"moov");
		if (moovBox==null) {
			moovBox = new MovieBox();
			isoFile.addBox(moovBox);
		}
		AbstractContainerBox udtaBox = (AbstractContainerBox)IsoFileConvenienceHelper.get(moovBox,"udta");
		if (udtaBox==null) {
			udtaBox = new UserDataBox();
			moovBox.addBox(udtaBox);
		}
		MetaBox metaBox = (MetaBox)IsoFileConvenienceHelper.get(udtaBox,"meta");
		if (metaBox==null) {
			metaBox = new MetaBox();
			udtaBox.addBox(metaBox);
		}
		AppleItemListBox appleItemListBox = (AppleItemListBox)IsoFileConvenienceHelper.get(metaBox,"ilst");
		if (appleItemListBox==null) {
			appleItemListBox = new AppleItemListBox();
			metaBox.addBox(appleItemListBox);
		}
		return appleItemListBox;
	}

	private Box createBox(Atom atom, Properties properties) throws MP4Exception {
		if (atom.getBox()!=null) {
			return atom.getBox();
		}
		String prop = properties.getProperty("ilst-"+atom.getName());
		if (prop==null) {
			throw new MP4Exception("Unable to create MP4 box with name '"+atom.getName()+"'");
		}
		if (prop.contains(GenericStringBox.class.getName())) {
			GenericStringBox box = new GenericStringBox(atom.getName().getBytes());
			box.setValue(atom.getValue());
			return box;
		}
		else {
			if (prop.endsWith("()")) {
				String className = prop.substring(0,prop.length()-2);
				try {
					Class<?> c = Class.forName(className);
					AbstractBox b = (AbstractBox ) c.newInstance();
					atom.updateBoxValue(b);
					return b;
				} catch (ClassNotFoundException e) {
					throw new MP4Exception("Unable to create MP4 box of type '"+className+"'",e);
				} catch (InstantiationException e) {
					throw new MP4Exception("Unable to create MP4 box of type '"+className+"'",e);
				} catch (IllegalAccessException e) {
					throw new MP4Exception("Unable to create MP4 box of type '"+className+"'",e);
				}
			}
		}

		throw new MP4Exception("Unable to create box of type: " +atom.getName() );
	}


	/**
	 * Used to create a atom
	 * @param displayName The display name
	 * @param name The name of the atom
	 * @param value The value of the atom
	 * @return the atom
	 */
	public Atom createAtom(String displayName,String name, String value) {
		if (name.equals("stik")) {
			return new AtomStik(value);
		}
		else if (name.equals("disk")) {
			return new AtomDisk(name,value);
		}
		else {
			if (displayName == null || displayName.equals("")) {
				displayName = getDisplayName(name);
			}
			return new Atom(displayName,name,value);
		}
	}

	private String getDisplayName(String name) {
		return nameLookup.getDisplayName(name);
	}

	/**
	 * Used to create a &quot;stik&quot; atom
	 * @param value The atom value
	 * @return The atom
	 */
	public IAtom createAtom(StikValue value) {
		return createAtom("stik",value.getId());
	}

	/**
	 * Used to create a unknown atom box
	 * @param type The type of the box
	 * @param box The box
	 * @return The atom
	 */
	public Atom createUnkownAtom(String type, Box box) {
		return new AtomUnknown(type,box);
	}

	/**
	 * Used to create a disk number box
	 * @param diskNumber The disk number
	 * @param numberOfDisks The total number of disks
	 * @return The atom
	 */
	@Override
	public Atom createAtom( String name,short diskNumber,short numberOfDisks) {
		return new AtomDisk(name,(byte)diskNumber,(byte)numberOfDisks);
	}

	/** {@inheritDoc} */
	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, String value) {
		return createAtom(null,name, value);
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, int value) {
		return createAtom("Media Type","stik",String.valueOf(value));
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(String name, MP4ArtworkType type, int size,byte[] data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws MP4Exception {
	}

}

package org.stanwood.media.store.mp4.boxes;

import java.nio.charset.Charset;

import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.apple.AppleDataBox;

/**
 * This is a generic atom box for MP4 files that stores string contents
 */
public class GenericStringBox extends AbstractAppleMetaDataBox implements ContainerBox {

	/**
	 * The constructor
	 * @param type The type of the box
	 */
	public GenericStringBox(byte[] type) {
		super(type);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

	/**
	 * Gets the display name
	 */
	@Override
	public String getDisplayName() {
		return "Generic Box " + new String(getType(),Charset.forName("ISO-8859-1"));
    }

}

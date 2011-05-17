package org.stanwood.media.store.mp4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.apple.AppleDataBox;

/**
 * This is a generic atom box for MP4 files that stores string contents
 */
public class GenericStringBox extends AbstractBox implements ContainerBox {

	private AppleDataBox appleDataBox = new AppleDataBox();
	private final static Log log = LogFactory.getLog(GenericStringBox.class);

	/**
	 * The constructor
	 * @param type The type of the box
	 */
	public GenericStringBox(byte[] type) {
		super(type);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

	/**
	 * Get a pretty display name
	 * @return the display name
	 */
    @Override
	public String getDisplayName() {
        return "Generic string box";
    }

    /**
     * Get a list of boxes within this box
     * @return list of boxes
     */
    @Override
	public Box[] getBoxes() {
        return new AbstractBox[]{appleDataBox};
    }

    /**
     * Get a list of boxes within this box
     * @return list of boxes
     */
    @Override
	public <T extends Box> T[] getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    /**
     * Get a list of boxes within this box
     * @return list of boxes
     */
    @SuppressWarnings("unchecked")
	@Override
	public <T extends Box> T[] getBoxes(Class<T> clazz, boolean recursive) {
        //todo recursive?
        if (clazz.isAssignableFrom(appleDataBox.getClass())) {
            T[] returnValue = (T[]) Array.newInstance(clazz, 1);
            returnValue[0] = (T) appleDataBox;
            return returnValue;
        }
        return null;
    }

    /**
     * Used to parse the box
     * @param in The raw box data
     * @param size The size of the data
     * @param boxParser the parser
     * @param lastMovieFragmentBox The last movie fragment box
     */
    @Override
	public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long sp = in.position();
        long dataBoxSize = in.readUInt32();
        String thisShouldBeData = in.readString(4);
        assert "data".equals(thisShouldBeData);
        appleDataBox = new AppleDataBox();
        appleDataBox.parse(in, dataBoxSize - 8, boxParser, lastMovieFragmentBox);
        appleDataBox.setParent(this);
        appleDataBox.offset = sp;
    }

    /**
     * Used to get the size of the content
     * @return the size of the content
     */
    @Override
	protected long getContentSize() {
        return appleDataBox.getSize();
    }

    @Override
	protected void getContent(IsoOutputStream os) throws IOException {
        appleDataBox.getBox(os);
    }

    /**
     * Used to get the number bytes to the first child
     * @return the number bytes to the first child
     */
    @Override
	public long getNumOfBytesToFirstChild() {
        return getSize() - appleDataBox.getSize();
    }

    /**
     * Return a string representation of the box for debug.
     * @return a string representation of the box.
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "appleDataBox=" + getValue() +
                '}';
    }

    static long toLong(byte b) {
        return b < 0 ? b + 256 : b;
    }

    /**
     * Used to set the value of the box
     * @param value the value of the box
     */
    public void setValue(String value) {
        if (appleDataBox.getFlags() == 1) {
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(1);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setContent(Utf8.convert(value));
        } else if (appleDataBox.getFlags() == 21) {
            byte[] content = appleDataBox.getContent();
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(21);
            appleDataBox.setFourBytes(new byte[4]);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IsoOutputStream isoOutputStream = new IsoOutputStream(baos);
            try {
                if (content.length == 1) {
                    isoOutputStream.writeUInt8((Byte.parseByte(value) & 0xFF));
                } else if (content.length == 2) {
                    isoOutputStream.writeUInt16(Integer.parseInt(value));
                } else if (content.length == 4) {
                    isoOutputStream.writeUInt32(Long.parseLong(value));
                } else if (content.length == 8) {
                    isoOutputStream.writeUInt64(Long.parseLong(value));
                } else {
                    throw new Error("The content length within the appleDataBox is neither 1, 2, 4 or 8. I can't handle that!");
                }
            } catch (IOException e) {
                throw new Error(e);
            }
            appleDataBox.setContent(content);
        } else if (appleDataBox.getFlags() == 0) {
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(0);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setContent(hexStringToByteArray(value));

        } else {
            log.warn("Don't know how to handle appleDataBox with flag=" + appleDataBox.getFlags());
        }
    }

    /**
     * Used to get the value of the box
     * @return the value of the box
     */
    public String getValue() {
        if (appleDataBox.getFlags() == 1) {
            return Utf8.convert(appleDataBox.getContent());
        } else if (appleDataBox.getFlags() == 21) {
            byte[] content = appleDataBox.getContent();
            long l = 0;
            int current = 1;
            int length = content.length;
            for (byte b : content) {
                l += toLong(b) << (8 * (length - current++));
            }
            return "" + l;
        } else if (appleDataBox.getFlags() == 0) {
            return String.format("%x", new BigInteger(appleDataBox.getContent()));
        } else {
            return "unknown";
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}

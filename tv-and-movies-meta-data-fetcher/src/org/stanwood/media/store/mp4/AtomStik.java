package org.stanwood.media.store.mp4;

import com.coremedia.iso.boxes.apple.AbstractAppleMetaDataBox;
import com.coremedia.iso.boxes.apple.AppleMediaTypeBox;


public class AtomStik extends Atom {

	public enum Value {
		 MOVIE_OLD("0", "Movie (is now 9)"),
	     MUSIC("1", "Music"),
	     AUDIO_BOOK("2", "Audiobook"),
	     MUSIC_VIDEO("6", "Music Video"),
	     MOVIE("9", "Movie"),
	     TV_SHOW("10", "TV Show"),
	     BOOKLET("11", "Booklet"),
	     RINGTONE("14", "Ringtone");

		 private String id;
		 private String desc;

		 private Value(String id,String desc) {
			 this.id = id;
			 this.desc = desc;
		 }

		private static Value fromDescription(String value) {
			for (Value v : values()) {
				if (v.desc.equals(value)) {
					return v;
				}
			}
			return null;
		}

		public String getDescription() {
			return desc;
		}

		public static Value fromId(String value) {
			for (Value v : values()) {
				if (v.id.equals(value)) {
					return v;
				}
			}
			return null;
		}

		public String getId() {
			return id;
		}
	}

	public AtomStik(String value) {
		super("stik", value);
	}

	public void setTypedValue(Value value) {
		setValue(value.id);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
	}

	@Override
	public String getValue() {
		String value = super.getValue();
		return value;
	}

	public Value getTypedValue() {
		return Value.fromId(getValue());
	}

	@Override
	public void updateBoxValue(AbstractAppleMetaDataBox b) {
		if (b instanceof AppleMediaTypeBox) {
			AppleMediaTypeBox box = (AppleMediaTypeBox)b;
			box.setValue(getValue());
		}
	}
}

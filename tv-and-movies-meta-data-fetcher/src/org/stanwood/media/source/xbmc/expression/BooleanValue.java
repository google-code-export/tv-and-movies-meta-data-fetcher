package org.stanwood.media.source.xbmc.expression;

public class BooleanValue extends Value {

	public BooleanValue(ValueType type, Boolean value) {
		super(type, value);
	}

	public boolean booleanValue() {
		return ((Boolean)getValue()).booleanValue();
	}
}

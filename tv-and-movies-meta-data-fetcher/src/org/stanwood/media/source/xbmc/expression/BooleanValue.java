package org.stanwood.media.source.xbmc.expression;

public class BooleanValue extends Value {

	public BooleanValue(ValueType type, Boolean value) {
		super(type, value);
	}

	public boolean booleanValue() {
		return ((Boolean)getValue()).booleanValue();
	}

	@Override
	public String toString() {
		return String.valueOf(booleanValue());
	}

	@Override
	public Value not() {
		return new BooleanValue(getType(), Boolean.valueOf(!booleanValue()));
	}
}

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

	@Override
	public Value and(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() && ((BooleanValue)value).booleanValue()));
	}

	@Override
	public Value or(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() || ((BooleanValue)value).booleanValue()));
	}

	@Override
	public Value notequals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() != ((BooleanValue)value).booleanValue()));
	}

	@Override
	public Value equals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() == ((BooleanValue)value).booleanValue()));
	}
}

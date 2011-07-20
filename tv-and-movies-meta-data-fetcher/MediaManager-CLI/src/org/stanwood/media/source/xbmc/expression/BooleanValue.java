package org.stanwood.media.source.xbmc.expression;

/**
 * Used to store the value of a boolean value used by the expresion evalutor
 */
public class BooleanValue extends Value {

	/**
	 * Used to create a instance of the class
	 * @param type The type of the value
	 * @param value The raw value
	 */
	public BooleanValue(ValueType type, Boolean value) {
		super(type, value);
	}

	/**
	 * Get the value as a boolean
	 * @return The value
	 */
	public boolean booleanValue() {
		return ((Boolean)getValue()).booleanValue();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return String.valueOf(booleanValue());
	}

	/** {@inheritDoc} */
	@Override
	public Value not() {
		return new BooleanValue(getType(), Boolean.valueOf(!booleanValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value and(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() && ((BooleanValue)value).booleanValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value or(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() || ((BooleanValue)value).booleanValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value notequals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() != ((BooleanValue)value).booleanValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value equals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(booleanValue() == ((BooleanValue)value).booleanValue()));
	}
}

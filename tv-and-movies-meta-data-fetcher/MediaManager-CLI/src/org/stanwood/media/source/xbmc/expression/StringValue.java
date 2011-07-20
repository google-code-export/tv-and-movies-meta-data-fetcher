package org.stanwood.media.source.xbmc.expression;

/**
 * Used to store the value of a string value used by the expression evaluator
 */
public class StringValue extends Value {

	/**
	 * Used to create a instance of the class
	 * @param type The type of the value
	 * @param value The raw value
	 */
	public StringValue(ValueType type, String value) {
		super(type, value);
	}

	/**
	 * Get the value as a boolean
	 * @return The value
	 */
	public String stringValue() {
		return (String)getValue();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return stringValue();
	}

	/** {@inheritDoc} */
	@Override
	public Value notequals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(!stringValue().equals(((StringValue)value).stringValue())));
	}

	/** {@inheritDoc} */
	@Override
	public Value equals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(stringValue().equals(((StringValue)value).stringValue())));
	}
}

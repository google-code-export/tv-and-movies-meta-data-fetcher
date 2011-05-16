package org.stanwood.media.source.xbmc.expression;

/**
 * Used to store integer values for the {@link ExpressionEval}
 */
public class IntegerValue extends Value {

	/**
	 * The constructor
	 * @param type The type
	 * @param value the value
	 */
	public IntegerValue(ValueType type, Integer value) {
		super(type, value);
	}

	/**
	 * Get the value as a int
	 * @return The value as a int
	 */
	public int intValue() {
		return ((Integer)getValue()).intValue();
	}

	/** {@inheritDoc} */
	@Override
	public Value addition(Value value) throws ExpressionParserException {
		return new IntegerValue(getType(),intValue()+((IntegerValue)value).intValue());
	}

	/** {@inheritDoc} */
	@Override
	public Value divide(Value value) throws ExpressionParserException {
		return new IntegerValue(getType(),intValue()/((IntegerValue)value).intValue());
	}

	/** {@inheritDoc} */
	@Override
	public Value multiply(Value value) throws ExpressionParserException {
		return new IntegerValue(getType(),intValue()*((IntegerValue)value).intValue());
	}

	/** {@inheritDoc} */
	@Override
	public Value subtract(Value value) throws ExpressionParserException {
		return new IntegerValue(getType(),intValue()-((IntegerValue)value).intValue());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return String.valueOf(intValue());
	}

	/** {@inheritDoc} */
	@Override
	public Value notequals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(intValue() != ((IntegerValue)value).intValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value equals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(intValue() == ((IntegerValue)value).intValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value greater(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(intValue() > ((IntegerValue)value).intValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value greaterEquals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(intValue() >= ((IntegerValue)value).intValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value less(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(intValue() < ((IntegerValue)value).intValue()));
	}

	/** {@inheritDoc} */
	@Override
	public Value lessEquals(Value value) {
		return new BooleanValue(getType(), Boolean.valueOf(intValue() <= ((IntegerValue)value).intValue()));
	}


}

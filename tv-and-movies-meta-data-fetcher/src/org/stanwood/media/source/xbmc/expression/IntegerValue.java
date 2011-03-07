package org.stanwood.media.source.xbmc.expression;

public class IntegerValue extends Value {

	public IntegerValue(ValueType type, Integer value) {
		super(type, value);
	}

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

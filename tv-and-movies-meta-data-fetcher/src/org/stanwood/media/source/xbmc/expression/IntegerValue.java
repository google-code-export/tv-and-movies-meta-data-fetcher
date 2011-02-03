package org.stanwood.media.source.xbmc.expression;

public class IntegerValue extends Value {

	public IntegerValue(ValueType type, Integer value) {
		super(type, value);
	}

	public int intValue() {
		return ((Integer)getValue()).intValue();
	}

	@Override
	public Value addition(Value value) throws ExpressionParserException {
		return new Value(getType(),intValue()+((IntegerValue)value).intValue());
	}

	@Override
	public Value divide(Value value) throws ExpressionParserException {
		return new Value(getType(),intValue()/((IntegerValue)value).intValue());
	}

	@Override
	public Value multiply(Value value) throws ExpressionParserException {
		return new Value(getType(),intValue()*((IntegerValue)value).intValue());
	}

	@Override
	public Value subtract(Value value) throws ExpressionParserException {
		return new Value(getType(),intValue()-((IntegerValue)value).intValue());
	}
}

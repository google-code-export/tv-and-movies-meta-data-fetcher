package org.stanwood.media.source.xbmc.expression;

public class Value {

	private Object value;
	private ValueType type;

	public Value(ValueType type,Object value) {
		this.type = type;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public ValueType getType() {
		return type;
	}

	public void setType(ValueType type) {
		this.type = type;
	}

	public Value addition(Value value) throws ExpressionParserException {
		throw new ExpressionParserException("Operation '+' unspported on types of " + type);
	}

	public Value divide(Value value) throws ExpressionParserException {
		throw new ExpressionParserException("Operation '/' unspported on types of " + type);
	}

	public Value multiply(Value value) throws ExpressionParserException {
		throw new ExpressionParserException("Operation '*' unspported on types of " + type);
	}

	public Value subtract(Value value) throws ExpressionParserException {
		throw new ExpressionParserException("Operation '-' unspported on types of " + type);
	}

	public Value not() {
		throw new ExpressionParserException("Operation '!' unspported on types of " + type);
	}

	public Value and(Value value) {
		throw new ExpressionParserException("Operation '&&' unspported on types of " + type);
	}

	public Value or(Value value) {
		throw new ExpressionParserException("Operation '||' unspported on types of " + type);
	}

	public Value notequals(Value value) {
		throw new ExpressionParserException("Operation '!=' unspported on types of " + type);
	}

	public Value equals(Value value) {
		throw new ExpressionParserException("Operation '==' unspported on types of " + type);
	}
}


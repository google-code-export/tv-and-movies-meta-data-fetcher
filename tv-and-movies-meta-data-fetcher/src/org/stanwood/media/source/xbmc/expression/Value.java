package org.stanwood.media.source.xbmc.expression;

import java.text.MessageFormat;

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
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","+",type));
	}

	public Value divide(Value value) throws ExpressionParserException {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","/",type));
	}

	public Value multiply(Value value) throws ExpressionParserException {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","*",type));
	}

	public Value subtract(Value value) throws ExpressionParserException {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","-",type));
	}

	public Value not() {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","!",type));
	}

	public Value and(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","&&",type));
	}

	public Value or(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","||",type));
	}

	public Value notequals(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","!=",type));
	}

	public Value equals(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","==",type));
	}

	public Value greater(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'",">",type));
	}

	public Value greaterEquals(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'",">=",type));
	}

	public Value less(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","<",type));
	}

	public Value lessEquals(Value value) {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","<=",type));
	}
}


package org.stanwood.media.source.xbmc.expression;

import java.text.MessageFormat;

/**
 * This class is the base class for Values returned by the expersion evaluator
 */
public class Value {

	private Object value;
	private ValueType type;

	/**
	 * The constructor
	 * @param type The type of value
	 * @param value The raw value
	 */
	public Value(ValueType type,Object value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * Used to get the raw value
	 * @return The raw value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Used to set the raw value
	 * @param value The raw value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Used to get the type of the value
	 * @return The type of the value
	 */
	public ValueType getType() {
		return type;
	}

	/**
	 * Used to set the type of the value
	 * @param type The type of the value
	 */
	public void setType(ValueType type) {
		this.type = type;
	}

	/**
	 * Used to add this value to another value
	 * @param value The value to add this value to
	 * @return The added values
	 * @throws ExpressionParserException Thrown if their is a problem performing the operation.
	 */
	public Value addition(Value value) throws ExpressionParserException {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","+",type));
	}

	/**
	 * Used to divide this value with another value
	 * @param value The value to divide this value by
	 * @return The result of the operation
	 * @throws ExpressionParserException Thrown if their is a problem performing the operation.
	 */
	public Value divide(Value value) throws ExpressionParserException {
		throw new ExpressionParserException(MessageFormat.format("Operation \\'{0}\\' unspported on types of \\'{0}\\'","/",type));
	}

	/**
	 * Used to multiply this value with another value
	 * @param value The value to multiply this value by
	 * @return The result of the operation
	 * @throws ExpressionParserException Thrown if their is a problem performing the operation.
	 */
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


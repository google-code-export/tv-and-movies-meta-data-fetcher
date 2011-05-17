package org.stanwood.media.source.xbmc.expression;

/**
 * Used to help with expression evaluation operations
 */
public class OperationHelper {

	/**
	 * Used to perform a operation on two values
	 * @param op The operation
	 * @param value1 The fist value
	 * @param value2 The second value
	 * @return The result
	 * @throws ExpressionParserException Thrown if their is a expression problem
	 */
	public static Value performOperation(Operation op, Value value1,Value value2) throws ExpressionParserException {
		if (value1.getType()==value2.getType()) {
			switch (op) {
			case ADDITION:
				return value1.addition(value2);
			case DIVIDE:
				return value1.divide(value2);
			case MULTIPLY:
				return value1.multiply(value2);
			case SUBTRACTION:
				return value1.subtract(value2);
			case AND:
				return value1.and(value2);
			case OR:
				return value1.or(value2);
			case EQUALS:
				return value1.equals(value2);
			case NOTEQUALS:
				return value1.notequals(value2);
			case GREATER:
				return value1.greater(value2);
			case GREATER_EQUALS:
				return value1.greaterEquals(value2);
			case LESS:
				return value1.less(value2);
			case LESS_EQUALS:
				return value1.lessEquals(value2);
			}

			throw new ExpressionParserException("Unsupported operation "+ op + " on " + value1.getType() + " types");
		}

		throw new ExpressionParserException("Can only perform operation on values of same type");
	}

	/**
	 * Used to perform a operation on one value
	 * @param op The operation
	 * @param value The value
	 * @return The result
	 * @throws ExpressionParserException Thrown if their is a expression problem
	 */
	public static Value performOperation(Operation op, Value value) {
		switch (op) {
		case NOT:
			return value.not();
		}
		throw new ExpressionParserException("Unsupported operation "+ op + " on " + value.getType() + " types");
	}

}

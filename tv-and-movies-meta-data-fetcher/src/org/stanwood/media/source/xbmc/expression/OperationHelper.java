package org.stanwood.media.source.xbmc.expression;


public class OperationHelper {

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
			}
			throw new ExpressionParserException("Unsupported operation "+ op + " on " + value1.getType() + " types");
		}

		throw new ExpressionParserException("Can only perform operation on values of same type");
	}

	public static Value performOperation(Operation op, Value value) {
		switch (op) {
		case NOT:
			return value.not();
		}
		throw new ExpressionParserException("Unsupported operation "+ op + " on " + value.getType() + " types");
	}

}

package org.stanwood.media.source.xbmc.expression;

/**
 * Used to create values
 */
public class ValueFactory {

	/**
	 * Create a value
	 * @param type The type of value
	 * @param value This is parsed to get the value
	 * @return The value
	 */
	public static Value createValue(ValueType type,String value) {
		if (type==ValueType.INTEGER) {
			return new IntegerValue(type,Integer.parseInt(value));
		}
		else if (type==ValueType.BOOLEAN) {
			return new BooleanValue(type,Boolean.parseBoolean(value));
		}
		throw new RuntimeException("Unsuppported type");
	}


}

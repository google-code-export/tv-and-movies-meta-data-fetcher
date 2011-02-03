package org.stanwood.media.source.xbmc.expression;

public class ValueFactory {

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

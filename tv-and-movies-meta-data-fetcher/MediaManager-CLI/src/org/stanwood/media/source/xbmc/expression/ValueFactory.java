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
			if (value.equals("yes")) { //$NON-NLS-1$
				value = "true"; //$NON-NLS-1$
			}
			else if (value.equals("no")) { //$NON-NLS-1$
				value = "false"; //$NON-NLS-1$
			}
			return new BooleanValue(type,Boolean.parseBoolean(value));
		}
		else if (type==ValueType.STRING) {
			return new StringValue(type,value);
		}
		throw new RuntimeException(Messages.getString("UNSUPPORTED_TYPE0")); //$NON-NLS-1$
	}


}

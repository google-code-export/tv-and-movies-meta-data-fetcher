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


}

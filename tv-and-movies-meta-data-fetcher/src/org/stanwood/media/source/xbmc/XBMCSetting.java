package org.stanwood.media.source.xbmc;

import org.stanwood.media.source.xbmc.expression.Value;

public class XBMCSetting {

	private Value value;

	public XBMCSetting(Value value) {
		super();
		this.value = value;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}



}

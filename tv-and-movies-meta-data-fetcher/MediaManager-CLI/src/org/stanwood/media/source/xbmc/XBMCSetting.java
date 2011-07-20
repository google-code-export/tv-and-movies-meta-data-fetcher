package org.stanwood.media.source.xbmc;

import org.stanwood.media.source.xbmc.expression.Value;

/**
 * Used to store the value of a XBMC setting
 */
public class XBMCSetting {

	private Value value;

	/**
	 * The constructor
	 * @param value The value
	 */
	public XBMCSetting(Value value) {
		super();
		this.value = value;
	}

	/**
	 * Used to get the value
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * Used to set the vlaue
	 * @param value the value
	 */
	public void setValue(Value value) {
		this.value = value;
	}



}

package org.stanwood.media.actions.rename;

import org.stanwood.media.source.xbmc.expression.ValueType;

public enum Token {
	/** the token for "show name" */
	SHOW_NAME('n',"(.*)",ValueType.STRING),
	/** the token for "episode number" */
	EPISODE('e',"(\\d+)",ValueType.INTEGER),
	/** the token for "season number" */
	SEASON('s',"(\\d+)",ValueType.INTEGER),
	/** the token for "extension" */
	EXT('x',"(.*)",ValueType.STRING),
	/** the token for "episode or film title" */
	TITLE('t',"(.*)",ValueType.STRING),
	/** add a % char */
	PERCENT('%',"%",ValueType.STRING),
	/** the token for "show Id" */
	ID('h',"(.*)",ValueType.STRING),
	/** the token for "part number" */
	PART('p',"(\\d+)",ValueType.INTEGER),
	/** the token for the "year" */
	YEAR('y',"(\\d+)",ValueType.INTEGER);

	private char tok;
	private String pattern;
	private ValueType type;

	private Token(char tok,String pattern,ValueType type) {
		this.tok = tok;
		this.pattern = pattern;
		this.type = type;
	}

	public String getFull() {
		return "%"+tok;
	}

	public char getToken() {
		return tok;
	}

	public String getPattern() {
		return pattern;
	}

	public ValueType getType() {
		return type;
	}

	public static Token fromToken(char c) {
		for (Token token : values()) {
			if (token.getToken()==c) {
				return token;
			}
		}
		return null;
	}

	public static Token fromFull(String s) {
		for (Token token : values()) {
			if (token.getFull().equals(s)) {
				return token;
			}
		}
		return null;
	}
}

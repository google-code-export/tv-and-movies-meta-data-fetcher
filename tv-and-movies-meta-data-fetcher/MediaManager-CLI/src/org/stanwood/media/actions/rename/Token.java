package org.stanwood.media.actions.rename;

import java.io.File;

import org.stanwood.media.source.xbmc.expression.ValueType;

/**
 * This enum contains all the tokens that are allowed in a pattern
 */
public enum Token {

	/** the token for "show name" */
	SHOW_NAME('n',"([^\\"+File.separatorChar+"]*?)",ValueType.STRING), //$NON-NLS-1$ //$NON-NLS-2$
	/** the token for "episode number" */
	EPISODE('e',"(\\d+)",ValueType.INTEGER), //$NON-NLS-1$
	/** the token for "season number" */
	SEASON('s',"(\\d+)",ValueType.INTEGER), //$NON-NLS-1$
	/** the token for "extension" */
	EXT('x',"([^\\.\\"+File.separatorChar+"]*?)$",ValueType.STRING), //$NON-NLS-1$ //$NON-NLS-2$
	/** the token for "episode or film title" */
	TITLE('t',"([^\\"+File.separatorChar+"]*?)",ValueType.STRING),  //$NON-NLS-1$//$NON-NLS-2$
	/** add a % char */
	PERCENT('%',"%",ValueType.STRING), //$NON-NLS-1$
	/** the token for "show Id" */
	ID('h',"([^\\"+File.separatorChar+"?]*?)",ValueType.STRING), //$NON-NLS-1$ //$NON-NLS-2$
	/** the token for "part number" */
	PART('p',"(\\d+)",ValueType.INTEGER), //$NON-NLS-1$
	/** the token for the "year" */
	YEAR('y',"(\\d+)",ValueType.INTEGER), //$NON-NLS-1$
	/** the token for the show or film image URL */
	IMAGE('i',"([^\\"+File.separatorChar+"?]*?)",ValueType.STRING), //$NON-NLS-1$ //$NON-NLS-2$
	/** the token for the show or film short summary */
	SUMMARY('u',"([^\\"+File.separatorChar+"?]*?)",ValueType.STRING); //$NON-NLS-1$ //$NON-NLS-2$

	private char tok;
	private String pattern;
	private ValueType type;

	private Token(char tok,String pattern,ValueType type) {
		this.tok = tok;
		this.pattern = pattern;
		this.type = type;
	}

	/**
	 * Get the full token name
	 * @return The full token name
	 */
	public String getFull() {
		return "%"+tok; //$NON-NLS-1$
	}

	/**
	 * Get the token character
	 * @return The token character
	 */
	public char getToken() {
		return tok;
	}

	/**
	 * Get the regexp pattern that matches the token
	 * @return the regexp pattern that matches the token
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Get the type of the token
	 * @return The type of the token
	 */
	public ValueType getType() {
		return type;
	}

	/**
	 * Used to get the token from a character
	 * @param c The token character
	 * @return The token or null if it's not found
	 */
	public static Token fromToken(char c) {
		for (Token token : values()) {
			if (token.getToken()==c) {
				return token;
			}
		}
		return null;
	}

	/**
	 * Used to get the token from it's full name
	 * @param s The full name
	 * @return The token or null if it's not found
	 */
	public static Token fromFull(String s) {
		for (Token token : values()) {
			if (token.getFull().equals(s)) {
				return token;
			}
		}
		return null;
	}
}

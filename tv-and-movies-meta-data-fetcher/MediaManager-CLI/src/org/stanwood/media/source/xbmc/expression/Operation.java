package org.stanwood.media.source.xbmc.expression;

/** Allowed operations in the expression evaluator */
public enum Operation {
	/** the + operation */
	ADDITION,
	/** the - operation */
	SUBTRACTION,
	/** the * operation */
	MULTIPLY,
	/** the / operation */
	DIVIDE,
	/** the ! operation */
	NOT,
	/** the == operation */
	EQUALS,
	/** the != operation */
	NOTEQUALS,
	/** the && operation */
	AND,
	/** the || operation */
	OR,
	/** the < operation */
	LESS,
	/** the <= operation */
	LESS_EQUALS,
	/** the > operation */
	GREATER,
	/** the >= operation */
	GREATER_EQUALS

}

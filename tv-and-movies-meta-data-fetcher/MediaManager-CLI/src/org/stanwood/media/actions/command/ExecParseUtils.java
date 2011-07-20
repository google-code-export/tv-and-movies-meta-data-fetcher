package org.stanwood.media.actions.command;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Used to parse command line parameters
 */
public final class ExecParseUtils {

	/** The whitespace char */
	public static final String WHITESPACE = " "; //$NON-NLS-1$

	/** The quote char */
	public static final String QUOTE_CHAR = "\""; //$NON-NLS-1$

	private ExecParseUtils() {
	}


	/**
	 * Splits the input line string by {@link #WHITESPACE}. Supports quoting the
	 * white-spaces with a {@link #QUOTE_CHAR}. A quote itself can also be
	 * enclosed within #{@link #QUOTE_CHAR}#{@link #QUOTE_CHAR}. More than two
	 * double-quotes in a sequence is not allowed. Nested quotes are not
	 * allowed.<br>
	 * E.g. The string
	 * <code>"arg 1"  arg2<code> will return the tokens <code>arg 1</code>,
	 * <code>arg2</code><br>
	 * The string
	 * <code>""arg 1""  "arg2" arg 3<code> will return the tokens <code>"arg 1"</code>
	 * , <code>arg2</code>,<code>arg</code> and <code>3</code> <br>
	 *
	 * @param input the input to split.
	 * @return a not-null list of tokens
	 */
	public static List<String> splitToWhiteSpaceSeparatedTokens(String input) {
	    if (input == null) {
	        return new ArrayList<String>();
	    }
	    StringTokenizer tokenizer = new StringTokenizer(input.trim(), QUOTE_CHAR + WHITESPACE, true);
	    List<String> tokens = new ArrayList<String>();

	    StringBuilder quotedText = new StringBuilder();

	    while (tokenizer.hasMoreTokens()) {
	        String token = tokenizer.nextToken();
	        if (QUOTE_CHAR.equals(token)) {
	            // if we have a quote, add the next tokens to the quoted text
	            // until the quoting has finished
	            quotedText.append(QUOTE_CHAR);
	            String buffer = quotedText.toString();
	            if (isSingleQuoted(buffer) || isDoubleQuoted(buffer)) {
	                tokens.add(buffer.substring(1, buffer.length() - 1));
	                quotedText = new StringBuilder();
	            }
	        } else if (WHITESPACE.equals(token)) {
	            // a white space, if in quote, add the white space, otherwise
	            // skip it
	            if (quotedText.length() > 0) {
	                quotedText.append(WHITESPACE);
	            }
	        } else {
	            if (quotedText.length() > 0) {
	                quotedText.append(token);
	            } else {
	                tokens.add(token);
	            }
	        }
	    }
	    if (quotedText.length() > 0) {
	        throw new IllegalArgumentException(MessageFormat.format(Messages.getString("INVALID_QUOTING0"),quotedText)); //$NON-NLS-1$
	    }
	    return tokens;
	}

	/**
	 * Tests if the input is enclosed within {@link #QUOTE_CHAR} characters
	 *
	 * @param input a not null String
	 * @return true if the regular expression is matched
	 */
	protected static boolean isSingleQuoted(String input) {
	    if (input == null || input.trim().length() == 0) {
	        return false;
	    }
	    return input.matches("(^" + QUOTE_CHAR + "{1}([^" + QUOTE_CHAR + "]+)" + QUOTE_CHAR + "{1})");    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}

	/**
	 * Tests if the input is enclosed within a double-{@link #QUOTE_CHAR} string
	 *
	 * @param input a not null String
	 * @return true if the regular expression is matched
	 */
	protected static boolean isDoubleQuoted(String input) {
	    if (input == null || input.trim().length() == 0) {
	        return false;
	    }
	    return input.matches("(^" + QUOTE_CHAR + "{2}([^" + QUOTE_CHAR + "]+)" + QUOTE_CHAR + "{2})"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
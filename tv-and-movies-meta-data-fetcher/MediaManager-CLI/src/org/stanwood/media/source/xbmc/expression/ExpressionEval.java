package org.stanwood.media.source.xbmc.expression;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to evaluate expressions
 */
public class ExpressionEval {

	private final static Log log = LogFactory.getLog(ExpressionEval.class);

	private static Map<String,Value> variables = new HashMap<String,Value>();

	/**
	 * Used to get the variables which can be used by expressions. This method can be used to add new variables
	 * @return A map of variables.
	 */
	public Map<String,Value> getVariables() {
		return variables;
	}

	/**
	 * Used to evaluate an expression
	 * @param expression The expression
	 * @return The value it evaluates to
	 * @throws ExpressionParserException Thrown if their are any problems
	 */
	public Value eval(String expression) throws ExpressionParserException {
		if (log.isDebugEnabled()) {
			log.debug("Evaluating expression '"+expression+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			ExpressionLexer lexer = new ExpressionLexer(new ANTLRStringStream(expression));
		    CommonTokenStream tokens = new CommonTokenStream(lexer);
		    ExpressionParser parser = new ExpressionParser(tokens);
		    parser.setVariables(variables);
		    Value value = parser.parse().value;
		    if (log.isDebugEnabled()) {
		    	log.debug("Result :" +value); //$NON-NLS-1$
		    }
		    return value;
		}
		catch (RecognitionException e) {
			throw new ExpressionParserException(MessageFormat.format(Messages.getString("ExpressionEval.UNABLE_PARSE_EXPRESSION"),expression),e); //$NON-NLS-1$
		}
   }
}

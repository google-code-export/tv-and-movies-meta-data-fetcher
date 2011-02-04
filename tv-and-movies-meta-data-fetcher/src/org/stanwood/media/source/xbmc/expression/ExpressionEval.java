package org.stanwood.media.source.xbmc.expression;

import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;


public class ExpressionEval {

	private Map<String,Value> variables = new HashMap<String,Value>();

	public ExpressionEval() {

	}

	public Map<String,Value> getVariables() {
		return variables;
	}

	public Value eval(String expression) throws ExpressionParserException {
		try {
			ExpressionLexer lexer = new ExpressionLexer(new ANTLRStringStream(expression));
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        ExpressionParser parser = new ExpressionParser(tokens);
	        parser.setVariables(variables);
	        return parser.parse().value;
		}
		catch (RecognitionException e) {
			throw new ExpressionParserException("Unable to parse expression '" + expression +"'",e);
		}
   }
}

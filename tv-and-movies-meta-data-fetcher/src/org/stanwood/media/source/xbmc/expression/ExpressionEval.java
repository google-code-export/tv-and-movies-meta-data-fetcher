package org.stanwood.media.source.xbmc.expression;

import java.io.IOException;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.hsqldb.lib.StringInputStream;


public class ExpressionEval {

	public void eval(String expression) throws IOException, RecognitionException {
		ANTLRInputStream input = new ANTLRInputStream(new StringInputStream(expression));
		ExpressionLexer lexer = new ExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        parser.prog();
	}
}

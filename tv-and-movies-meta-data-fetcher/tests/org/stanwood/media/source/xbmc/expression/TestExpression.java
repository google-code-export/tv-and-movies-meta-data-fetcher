package org.stanwood.media.source.xbmc.expression;

import junit.framework.Assert;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.hsqldb.lib.StringInputStream;
import org.junit.Test;

public class TestExpression {

	@Test
	public void testExpressions() throws Exception {
		ExpressionEval eval = new ExpressionEval();

		Value result = eval.eval("4+5");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(Integer.valueOf(9), result.getValue());

		result = eval.eval("10+100");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(Integer.valueOf(110), result.getValue());


		eval.getVariables().put("test", ValueFactory.createValue(ValueType.INTEGER,"10"));
		result = eval.eval("test+12");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(22, result.getValue());

		result = eval.eval("12+test");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(22, result.getValue());
	}

	@Test
	public void testStuff2() throws Exception {
		ANTLRInputStream input = new ANTLRInputStream(new StringInputStream("3*(5+2)"));
        ExprLexer lexer = new ExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);
//        parser.memory.put("test",Integer.valueOf(5));
        System.out.println(parser.expression());
	}


}

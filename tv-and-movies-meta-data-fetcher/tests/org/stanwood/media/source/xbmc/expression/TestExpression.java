package org.stanwood.media.source.xbmc.expression;

import junit.framework.Assert;

import org.junit.Test;

public class TestExpression {

	@Test
	public void testExpressions() throws Exception {
		ExpressionEval eval = new ExpressionEval();

		Value result = eval.eval("4+5");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(9, result.getValue());

		eval.getVariables().put("test", new Value(ValueType.INTEGER,10));
		result = eval.eval("12+test");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(22, result.getValue());
	}
}

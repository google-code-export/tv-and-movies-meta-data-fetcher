package org.stanwood.media.source.xbmc.expression;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Used to test the evaluation of expressions
 */
@SuppressWarnings("nls")
public class TestExpression {

	/**
	 * Test expressions using integers
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testIntExpressions() throws Exception {
		ExpressionEval eval = new ExpressionEval();

		Value result = eval.eval("4+5");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(Integer.valueOf(9), result.getValue());
		Assert.assertEquals("9", result.getValue().toString());
		Assert.assertEquals(9, ((IntegerValue)result).intValue());

		result = eval.eval("10+100");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(Integer.valueOf(110), result.getValue());
		Assert.assertEquals("110", result.getValue().toString());
		Assert.assertEquals(110, ((IntegerValue)result).intValue());

		eval.getVariables().put("test", ValueFactory.createValue(ValueType.INTEGER,"10"));
		result = eval.eval("test+12");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(22, result.getValue());
		Assert.assertEquals("22", result.getValue().toString());

		result = eval.eval("12+test");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(22, result.getValue());
		Assert.assertEquals("22", result.getValue().toString());

		result = eval.eval("12+test == 22");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());
		result = eval.eval("12+test != 400");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());
		result = eval.eval("12+test == 400");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());

		result = eval.eval("12>5");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());
		result = eval.eval("12<5");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());
		result = eval.eval("12<12");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());
		result = eval.eval("12<=12");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());
		result = eval.eval("12>=12");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		result = eval.eval("5*10");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(50, result.getValue());

		result = eval.eval("40/5");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(8, result.getValue());

		result = eval.eval("5-2");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(3, result.getValue());

		result = eval.eval("4-10");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(-6, result.getValue());

		result = eval.eval("3456");
		Assert.assertEquals(ValueType.INTEGER, result.getType());
		Assert.assertEquals(3456, result.getValue());
	}

	/**
	 * Test expressions using booleans
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testBoolExpressions() throws Exception {
		ExpressionEval eval = new ExpressionEval();

		Value result = eval.eval("true");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.BOOLEAN, result.getType());
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());
		Assert.assertEquals("true", result.getValue().toString());
		Assert.assertEquals(true, ((BooleanValue)result).booleanValue());

		result = eval.eval("false");
		Assert.assertNotNull(result);
		Assert.assertEquals(ValueType.BOOLEAN, result.getType());
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());
		Assert.assertEquals("false", result.getValue().toString());
		Assert.assertEquals(false, ((BooleanValue)result).booleanValue());

		result = eval.eval("!false");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		result = eval.eval("false == false");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		result = eval.eval("true == true");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		result = eval.eval("true != false");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		result = eval.eval("false != true");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		result = eval.eval("false == true");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());

		result = eval.eval("true == false");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());

		result = eval.eval("!true");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());

		eval.getVariables().put("dummy", ValueFactory.createValue(ValueType.BOOLEAN,"false"));
		result = eval.eval("!dummy");
		Assert.assertEquals(Boolean.valueOf(true), result.getValue());

		eval.getVariables().put("dummy", ValueFactory.createValue(ValueType.BOOLEAN,"true"));
		result = eval.eval("!dummy");
		Assert.assertEquals(Boolean.valueOf(false), result.getValue());
	}
}

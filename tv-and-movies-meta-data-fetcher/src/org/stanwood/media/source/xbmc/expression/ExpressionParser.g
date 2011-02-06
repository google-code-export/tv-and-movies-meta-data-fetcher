// http://www.codeproject.com/KB/recipes/sota_expression_evaluator.aspx
parser  grammar ExpressionParser;

options {
  tokenVocab=ExpressionLexer;  
  language=Java;
  output=AST;
}

@header {
package org.stanwood.media.source.xbmc.expression;

import java.util.HashMap;
import java.util.Map;
}

@members {
@SuppressWarnings("all")

/** Map Value name to Integer object holding value */
private Map<String,Value> variables;

private Value result = null;

public Value getResult() {
  return result;
}

public Map<String,Value> getVariables() {
  return this.variables; 
}

public void setVariables(Map<String,Value> variables) {
  this.variables = variables;
}
    
}

parse returns [Value value]
    :    exp=unaryExpression { $value = $exp.value;}
    ;

additiveExpression returns [Value value]
    :    m1=multiplyExp      {$value =  $m1.value;} 
        ( PLUS m2=multiplyExp {$value = OperationHelper.performOperation(Operation.ADDITION,$value,$m2.value);} 
        | MINUS m2=multiplyExp {$value = OperationHelper.performOperation(Operation.SUBTRACTION,$value,$m2.value);}
        )*  
    ;

multiplyExp returns [Value value]
    :   a1=atomExp       {$value = $a1.value;}
        ( MULT a2=atomExp {$value = OperationHelper.performOperation(Operation.MULTIPLY,$value,$a2.value);} 
        | DIV a2=atomExp {$value = OperationHelper.performOperation(Operation.DIVIDE,$value,$a2.value);}
        )*  
    ;
    
logicalExpression returns [Value value]
    :    b1=booleanAndExpression  {$value = $b1.value;}
         ( OR b2=booleanAndExpression {$value = OperationHelper.performOperation(Operation.OR,$value,$b2.value);} 
         )*
    ;    
    
booleanAndExpression returns [Value value]
    :    b1=equalityExpression {$value = $b1.value;}
         ( AND b2=equalityExpression {$value = OperationHelper.performOperation(Operation.AND,$value,$b2.value);}
         )*
    ;

equalityExpression returns [Value value]
    :    b1=additiveExpression  {$value = $b1.value;}
         ( EQUALS a2=additiveExpression {$value = OperationHelper.performOperation(Operation.EQUALS,$value,$a2.value);} 
         | NOTEQUALS a2=additiveExpression {$value = OperationHelper.performOperation(Operation.NOTEQUALS,$value,$a2.value);}
         )*
    ;        
    
unaryExpression returns [Value value]
    :   u1=logicalExpression { $value = $u1.value; }      
    |   NOT u1=logicalExpression { $value = OperationHelper.performOperation(Operation.NOT,$u1.value); }
    ;

atomExp returns [Value value]
    :    v=INTEGER               { $value = ValueFactory.createValue(ValueType.INTEGER,$v.text);}
    |    v=BOOLEAN               { $value = ValueFactory.createValue(ValueType.BOOLEAN,$v.text);}             
    |    i=IDENTIFIER            { Value value1 = getVariables().get($i.text);
                                   if (value1==null) {
                                      throw new ExpressionParserException("Unable to find variable "+$i.text);
                                   }
                                   $value = value1; }
    |    LBRACKET exp=logicalExpression RBRACKET {$value = $exp.value;}
    ;

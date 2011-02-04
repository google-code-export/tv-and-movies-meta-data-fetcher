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
    :    exp=additionExp { $value = $exp.value;}
    ;

additionExp returns [Value value]
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
    
//unaryExpression returns [Value value]
//    :   NOT atomExp {$value = OperationHelper.performOperation(Operation.NOT,$value); }
//       ;

atomExp returns [Value value]
    :    v=INTEGER               { $value = ValueFactory.createValue(ValueType.INTEGER,$v.text);}
    |    v=BOOLEAN               { $value = ValueFactory.createValue(ValueType.BOOLEAN,$v.text);}             
    |    i=IDENTIFIER            { $value = getVariables().get($i.text); }
    |    LBRACKET exp=additionExp RBRACKET {$value = $exp.value;}
    ;


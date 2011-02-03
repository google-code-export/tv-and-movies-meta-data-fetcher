grammar Expression;

options {
  language=Java;
}

@header {
package org.stanwood.media.source.xbmc.expression;

import java.util.HashMap;
import java.util.Map;
}

@lexer::header {
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
    :    exp=additionExp {$value = $exp.value;}
    ;

additionExp returns [Value value]
    :    m1=multiplyExp      {$value =  $m1.value;} 
        ( '+' m2=multiplyExp {$value = OperationHelper.performOperation(Operation.ADDITION,$value,$m2.value);} 
        | '-' m2=multiplyExp {$value = OperationHelper.performOperation(Operation.SUBTRACTION,$value,$m2.value);}
        )*  
    ;

multiplyExp returns [Value value]
    :   a1=atomExp       {$value = $a1.value;}
        ( '*' a2=atomExp {$value = OperationHelper.performOperation(Operation.MULTIPLY,$value,$a2.value);} 
        | '/' a2=atomExp {$value = OperationHelper.performOperation(Operation.DIVIDE,$value,$a2.value);}
        )*  
    ;

atomExp returns [Value value]
    :    n=Integer                {$value = ValueFactory.createValue(ValueType.INTEGER,$n.text);}
    |    i=Identifier            { System.out.println($i.text); $value = getVariables().get($i.text); }
    |    '(' exp=additionExp ')' {$value = $exp.value;}
    ;

fragment DIGIT : ('0'..'9') ;

fragment LETTER : ('a'..'z'|'A'..'Z') ;

Identifier         
    :    (LETTER | '_') (LETTER | '_' |DIGIT)*
    ;

Integer
    :    '-'? DIGIT+
    ;
  
WHITESPACE : (' '|'\t')+ { skip(); } ;
NEWLINE : ('\r'|'\n')+ { skip(); } ;
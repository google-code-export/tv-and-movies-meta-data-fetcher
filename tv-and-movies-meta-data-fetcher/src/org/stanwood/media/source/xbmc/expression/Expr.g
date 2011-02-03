// http://www.codeproject.com/KB/recipes/sota_expression_evaluator.aspx

grammar Expr;

options {
//    backtrack = true;
//    memoize=true;
    language=Java;
    output=AST;
//    k=2;
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

addition : INTEGER '+' INTEGER ;

logicalExpression
    :    booleanAndExpression ( OR booleanAndExpression )*
    ;
    
booleanAndExpression
    :    equalityExpression ( AND equalityExpression )*
    ;
    
equalityExpression
    :    relationalExpression ( (EQUALS | NOTEQUALS) relationalExpression)*
    ;
    
relationalExpression
    :    additiveExpression ( (LT | LTEQ | GT | GTEQ) additiveExpression)*
    ;

additiveExpression
    :    multiplicativeExpression ( (PLUS | MINUS) multiplicativeExpression )*
    ;

multiplicativeExpression
    :    unaryExpression (( MULT | DIV | MOD ) unaryExpression)*
    ;

unaryExpression
    :    /*NOT!*/ primaryExpression
       ;

primaryExpression
    :    '(' logicalExpression ')'
    |    value
    ;    
    
OR    :     '||' | 'or';
AND   :     '&&' | 'and';
EQUALS
      :    '=' | '==';
NOTEQUALS
      :    '!=' | '<>';
LT    :    '<';
LTEQ  :    '<=';
GT    :    '>';
GTEQ  :    '>=';
PLUS  :    '+';
MINUS :    '-';
MULT  :    '*';
DIV   :    '/';
MOD   :    '%';
NOT   :    '!' | 'not';  

value    :    INTEGER
    |    FLOAT
    |    STRING
    |     DATETIME
    |    BOOLEAN
    ;  
    
INTEGER
    :    '-'? ('0'..'9')+
    ;
FLOAT
    :    '-'? ('0'..'9')+ '.' ('0'..'9')+
    ;

STRING
     :    '\'' (~ '\'' )* '\''
     ;  
     
DATETIME
     :    '#' (~ '#' )* '#'
     ;      
     
BOOLEAN
    :    'true'
    |    'false'
    ;   
    
expression 
    :     logicalExpression EOF
    ;      
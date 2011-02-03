lexer grammar ExpressionLexer;

@header 
{package org.stanwood.media.source.xbmc.expression;

/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
import java.util.HashMap;
import java.util.Map;
}

@members {
  @Override
  public void reportError(RecognitionException e) {
    throw new ExpressionParserException(e);
  }
}

fragment DIGIT : ('0'..'9') ;

fragment LETTER : ('a'..'z'|'A'..'Z') ;

BOOLEAN
    : ('true' | 'false')
    ;  

IDENTIFIER         
    :    (LETTER | '_') (LETTER | '_' |DIGIT)*
    ; 

INTEGER
    :    DIGIT+
    ;
  
WHITESPACE : (' '|'\t')+ { skip(); } ;
NEWLINE : ('\r'|'\n')+ { skip(); } ;

PLUS  :    '+';
MINUS :    '-';
MULT  :    '*';
DIV   :    '/';
LBRACKET : '(';
RBRACKET : ')';
NOT      : '!';


 

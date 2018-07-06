/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2011-2016 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
grammar PhonexTokenizer;

options {
	output=AST;
	backtrack=true;
}

tokens {
	ARG;
	ARG_LIST;
	BACK_REF;
	BASEEXPR;
	BOUNDARY_MATCHER;
	COMPOUND_MATCHER;
	EXPR;
	FEATURE_SET;
	GROUP;
	MATCHER;
	NAME;
	PHONE_CLASS;
	PLUGIN;
	P_PHONE_CLASS;
	QUANTIFIER;
	SCTYPE;
	STRESS;
}

@header {
package ca.phon.phonex;

import org.apache.commons.lang3.StringEscapeUtils;
}

@lexer::header {
package ca.phon.phonex;

import org.apache.commons.lang3.StringEscapeUtils;
}

@lexer::members {
	
public void reportError(RecognitionException e) {
	throw new PhonexPatternException(e.line, e.charPositionInLine, e);
}

@Override
public void emitErrorMessage(String msg) {
	// do nothing
}

}

rule
	: IDENTIFIER+
	;

FLAGS
	: FORWARDSLASH ('o'|'O')
	;
	
IDENTIFIER
	: LETTER (LETTER | NUMBER)*
	;
	
ESCAPED_PHONE_CLASS
	:	BACKSLASH ('c'|'v'|'g'|'p'|'P'|'w'|'W'|'s')
	;

ESCAPED_PUNCT
	:	BACKSLASH ('.'|'*'|CARET)
	;

ESCAPED_BOUNDARY
	:	BACKSLASH ('b'|'S')
	;

DOLLAR_SIGN
	:	'$'
	;

PERIOD	:	'.'
	;

PIPE
	:	'|'
	;

MINUS
	:	'-'
	;

EQUALS
	:	'='
	;

AMP
	:	'&'
	;

EXC
	:	'!'
	;

OPEN_PAREN
	:	'('
	;

CLOSE_PAREN
	:	')'
	;

OPEN_BRACKET
	:	'['
	;

CLOSE_BRACKET
	:	']'
	;

CARET
	:	'^'
	;

OPEN_BRACE
	:	'{'
	;

CLOSE_BRACE
	:	'}'
	;

COLON
	:	':'
	;

	
//TRIANGULAR_COLON
//	:	'\u02d0'
//	;

SEMICOLON
	:	';'
	;

COMMA
	:	','
	;

	
FORWARDSLASH
	:	'/'
	;

BACKSLASH
	:	'\\'
	;

SINGLE_QUANTIFIER
	:	ZERO_OR_MORE
	|	ONE_OR_MORE
	|	ZERO_OR_ONE
	;

fragment
ZERO_OR_MORE
	:	'*'
	;

fragment
ONE_OR_MORE
	:	'+'
	;

fragment
ZERO_OR_ONE
	:	'?'
	;

NON_CAPTURING_GROUP
	:	'?='
	;

LOOK_BEHIND_GROUP
	:	'?<'
	;

LOOK_AHEAD_GROUP
	:	'?>'
	;
	
GROUP_NAME
	:	IDENTIFIER EQUALS
	;

BOUND_START
	:	'<'
	;

BOUND_END
	:	'>'
	;
	
COMMENT_START
	:	'/*'
	;

COMMENT_END
	:	'*/'
	;
	
EOL_COMMENT_START
	:	FORWARDSLASH FORWARDSLASH
	;

/**
 * TODO - this set should match exactly what is supported
 * through the IPA parser. Only
 */
fragment
LETTER
	:	'a'..'z'
	|	'A'..'Z'
	|	'\u00e6'..'\u03df'
	|	'\u2194'
	;

fragment
NUMBER
	:	'0'..'9'
	;

INT :	NUMBER+
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
		| '\n'
		)
    ;

REGEX_STRING
	:	'\'' ( ESC_SEQ | HEX_CHAR | ~(BACKSLASH|'\'') )* '\''
	;

STRING
    :  '\"' ( ESC_SEQ | HEX_CHAR | ~(BACKSLASH|'\"') )* '\"'
    ;

HEX_CHAR
	:	BACKSLASH 'u' NUMBER NUMBER NUMBER NUMBER
	;
		
SCTYPE
	: COLON MINUS? ( 'A' | 'a' | 'L' | 'l' | 'O' | 'o' | 'N' | 'n' | 'D' | 'd' | 'C' | 'c' | 'R' | 'r' | 'E' | 'e' )
	;
	
PLUGIN
	: COLON ( 'sctype' | 'stress' | 'mdc' | 'diacritic' | 'tone' | 'comb' | 'prefix' | 'suffix' )
	;
	
STRESS_TYPE
	: EXC MINUS? ('1' | '2' | 'A' | 'a' | 'U' | 'u')
	;
	
BACKREF
	: BACKSLASH MINUS? INT
	;

fragment
ESC_SEQ
    :   BACKSLASH ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

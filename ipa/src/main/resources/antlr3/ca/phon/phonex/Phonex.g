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
grammar Phonex;

options {
	output=AST;
	ASTLabelType=CommonTree;
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

@members {

public void reportError(RecognitionException e) {
	throw new PhonexPatternException(e.line, e.charPositionInLine, e);
}

private boolean reverseExpr = false;

}

/**
 * Start
 */
expr
@after {
	if(input.LA(1) != EOF) {
		throw new PhonexPatternException(input.LT(1).getLine(), input.LT(1).getCharPositionInLine(), "Unexpected symbol: " + input.LT(1).getText());
	}
}
	:	baseexpr flags?
	->	^(EXPR baseexpr flags?)
	;

flags
	:	FORWARDSLASH LETTER+
	->	^(FORWARDSLASH LETTER+)
	;

baseexpr
	:	(exprTrees+=exprele)+
	{
		if(reverseExpr) {
			// reverse elements in stream
			stream_exprele = new RewriteRuleSubtreeStream(adaptor,"rule exprele");
			java.util.Collections.reverse(list_exprTrees);
			for(Object tree:list_exprTrees) {
				stream_exprele.add((CommonTree)tree);
			}
		}
	}
	-> ^(BASEEXPR exprele+)
	;

exprele
	:	matcher
	|	group
	|	boundary_matchers
	;

group
	// normal group
	:	OPEN_PAREN baseexpr (PIPE baseexpr)* CLOSE_PAREN quantifier?
	->	^(GROUP baseexpr+ quantifier?)
	// non capturing group
	|	OPEN_PAREN NON_CAPTURING_GROUP baseexpr (PIPE baseexpr)* CLOSE_PAREN quantifier?
	->	^(GROUP["?"] NON_CAPTURING_GROUP baseexpr+ quantifier?)
	// named group
	|	OPEN_PAREN group_name '=' baseexpr (PIPE baseexpr)* CLOSE_PAREN quantifier?
	->	^(GROUP[$group_name.text] baseexpr+ quantifier?)
	// look-behind
	|	OPEN_PAREN LOOK_BEHIND_GROUP { reverseExpr = true; } baseexpr CLOSE_PAREN quantifier?
	{
		reverseExpr = false;
	}
	->	^(GROUP["?<"] NON_CAPTURING_GROUP baseexpr+ quantifier?)
	// look ahead
	|	OPEN_PAREN LOOK_AHEAD_GROUP baseexpr CLOSE_PAREN quantifier?
	->	^(GROUP["?>"] NON_CAPTURING_GROUP baseexpr+ quantifier?)
	;

group_name
	:	LETTER (LETTER | INT)*
	;

matcher
	:	base_matcher plugin_matcher* quantifier?
	->	^(MATCHER base_matcher plugin_matcher* quantifier?)
	|	back_reference plugin_matcher* quantifier?
	->	^(back_reference plugin_matcher* quantifier?)
	;

base_matcher
	:	class_matcher
	|	single_phone_matcher
	|	compound_phone_matcher
	;

compound_phone_matcher
	:	m1=single_phone_matcher '_' m2=single_phone_matcher
	->	^(COMPOUND_MATCHER $m1 $m2)
	;

single_phone_matcher
	:	feature_set_matcher
	|	base_phone_matcher
	|	predefined_phone_classes
	|	regex_matcher
	|	hex_value
	|	escaped_char
	;

hex_value
	:	HEX_CHAR
	->	LETTER[StringEscapeUtils.unescapeJava($HEX_CHAR.text)]
	;

escaped_char
	:	ESCAPED_PUNCT
	->	LETTER[""+$ESCAPED_PUNCT.text.charAt(1)]
	;

class_matcher
	:	OPEN_BRACKET CARET? single_phone_matcher+ CLOSE_BRACKET
	->	^(PHONE_CLASS[($CARET == null ? "" : $CARET.text)] single_phone_matcher+)
	;

plugin_matcher
	:	COLON id=identifier OPEN_PAREN argument_list? CLOSE_PAREN
	->	^(PLUGIN[$id.text] OPEN_PAREN argument_list? CLOSE_PAREN)
	|	COLON MINUS? sctype
	->	^(PLUGIN["sctype"] MINUS? sctype)
	|	AMP single_phone_matcher
	->  ^(PLUGIN["diacritic"] single_phone_matcher)
	|	AMP MINUS? class_matcher
	->  ^(PLUGIN["diacritic"] MINUS? class_matcher)
	|	EXC MINUS? stress_type
	->	^(PLUGIN["stress"] MINUS? stress_type)
	|	TRIANGULAR_COLON
	->	^(PLUGIN["diacritic"] TRIANGULAR_COLON)
	;

argument
	:	STRING
	->	^(ARG STRING)
	;

argument_list
	:	argument ( COMMA argument )*
	->	^(ARG_LIST argument+)
	;

back_reference
	:	BACKSLASH MINUS? INT
	->	^(BACK_REF[$INT] MINUS?)
	;

feature_set_matcher
	:	OPEN_BRACE ( negatable_identifier ( COMMA negatable_identifier )* )? CLOSE_BRACE
	->	^(FEATURE_SET negatable_identifier*)
	;

base_phone_matcher
	:	LETTER
	;

regex_matcher
	:	REGEX_STRING
	;

identifier
	:	LETTER+
	->	^(NAME LETTER+)
	;

negatable_identifier
	:	MINUS? LETTER+
	->	^(NAME MINUS? LETTER+)
	;

quantifier
	:	quant=SINGLE_QUANTIFIER type=SINGLE_QUANTIFIER?
	->	^(QUANTIFIER $quant $type?)
	|	bounded_quantifier SINGLE_QUANTIFIER?
	->	^(QUANTIFIER bounded_quantifier SINGLE_QUANTIFIER?)
	;

bounded_quantifier
	:	BOUND_START x=INT BOUND_END
	->	^(BOUND_START $x)
	|	BOUND_START x=INT COMMA y=INT BOUND_END
	->	^(BOUND_START $x $y)
	|	BOUND_START x=INT COMMA BOUND_END
	->	^(BOUND_START $x INT["0"])
	|	BOUND_START COMMA x=INT BOUND_END
	->	^(BOUND_START INT["0"] $x)
	;

predefined_phone_classes
	:	PERIOD
	->	P_PHONE_CLASS[$PERIOD]
	|	ESCAPED_PHONE_CLASS
	->	P_PHONE_CLASS[$ESCAPED_PHONE_CLASS]
	;

boundary_matchers
	:	CARET
	->	BOUNDARY_MATCHER[$CARET]
	|	DOLLAR_SIGN
	->	BOUNDARY_MATCHER[$DOLLAR_SIGN]
	|	ESCAPED_BOUNDARY
	->	BOUNDARY_MATCHER[$ESCAPED_BOUNDARY]
	;

stress_type
	:	INT
	->	^(STRESS[$INT])
	|	LETTER
	->	^(STRESS[$LETTER])
	;

sctype
	:	LETTER
	->	^(SCTYPE[$LETTER])
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

TRIANGULAR_COLON
	:	'\u02d0'
	;

SEMICOLON
	:	';'
	;

COMMA
	:	','
	;

BACKSLASH
	:	'\\'
	;

FORWARDSLASH
	:	'/'
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

BOUND_START
	:	'<'
	;

BOUND_END
	:	'>'
	;
	
COMMENT
	:	'/*' .* '*/'
	{$channel=HIDDEN;}
	;

EOL_COMMENT
	:	'//' .* '\n'
	{$channel=HIDDEN;}
	;

/**
 * TODO - this set should match exactly what is supported
 * through the IPA parser. Only
 */
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
        ) {$channel=HIDDEN;}
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

fragment
ESC_SEQ
    :   BACKSLASH ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

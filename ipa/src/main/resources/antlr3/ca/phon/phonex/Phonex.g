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
}

@header {
package ca.phon.phonex;

import org.apache.commons.lang3.StringEscapeUtils;
}

@lexer::header {
package ca.phon.phonex;

import org.apache.commons.lang3.StringEscapeUtils;
}

/**
 * Start
 */
expr
	:	exprele+
	->	^(EXPR exprele+)
	;

/**
 * 
 */
exprele	
	:	matcher
	|	group
	|	boundary_matchers
	;

group
	:	OPEN_PAREN exprele+ CLOSE_PAREN quantifier?
	->	^(GROUP exprele+ quantifier?)
	|	OPEN_PAREN NON_CAPTURING_GROUP exprele+ CLOSE_PAREN quantifier?
	->	^(GROUP["?"] NON_CAPTURING_GROUP exprele+ quantifier?)
	|	OPEN_PAREN group_name '=' exprele+ CLOSE_PAREN quantifier?
	->	^(GROUP[$group_name.text] exprele+ quantifier?)
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
	:	OPEN_BRACKET MINUS? single_phone_matcher+ CLOSE_BRACKET
	->	^(PHONE_CLASS[($MINUS == null ? "" : $MINUS.text)] single_phone_matcher+)
	;

plugin_matcher
	:	COLON identifier OPEN_PAREN argument_list? CLOSE_PAREN
	->	^(PLUGIN[$identifier.text] OPEN_PAREN argument_list? CLOSE_PAREN)
	|	COLON negatable_identifier (PIPE negatable_identifier)*
	->	^(PLUGIN["sctype"] negatable_identifier+)
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
	:	BACKSLASH INT
	->	BACK_REF[$INT]
	;

feature_set_matcher
	:	OPEN_BRACE ( negatable_identifier ( COMMA negatable_identifier )* )? CLOSE_BRACE
	->	^(FEATURE_SET negatable_identifier*)
	;
	
base_phone_matcher
	:	LETTER
	;
	
regex_matcher
	:	STRING
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
	
ESCAPED_PHONE_CLASS
	:	BACKSLASH ('c'|'v'|'g'|'w'|'W'|'s')
	;
	
ESCAPED_PUNCT
	:	BACKSLASH ('.'|'*')
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
	
COMMA
	:	','
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

BOUND_START
	:	'<'
	;

BOUND_END
	:	'>'
	;
	
LETTER
	:	'a'..'z'
	|	'A'..'Z'
	|	'\u0250'..'\u02af'
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

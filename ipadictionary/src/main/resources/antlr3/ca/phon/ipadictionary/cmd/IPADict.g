grammar IPADict;
options {
	output=AST;
	ASTLabelType=CommonTree;
}

@header {
package ca.phon.ipadictionary.cmd;
}

@lexer::header {
package ca.phon.ipadictionary.cmd;
}

@members {

private final static java.util.logging.Logger LOGGER = 
	java.util.logging.Logger.getLogger("ca.phon.ipadictionary.cmd.IPADictParser");

/** Override the default getErrorMessage() to 
 * also output to PhonLogger
 */
public String getErrorMessage(RecognitionException re, String[] tokens) {
    String retVal = super.getErrorMessage(re, tokens);
    LOGGER.warning(input.toString() + "(" + 
        re.line + ":" + re.c + ") " + retVal);
    return retVal;
}

}

/**
 * Parser start
 */
expr	:	createExpr
	|	dropExpr
	|	addExpr
	|	importExpr
	|	exportExpr
	|	lookupExpr
	|	removeExpr
	|	removeAllExpr
	|	switchExpr
	|	listExpr
	|	helpExpr
	;

/**
 * Expressions
 */

createExpr	:	CREATE DICT_ID ('=' STRING)? -> ^(CREATE DICT_ID STRING?);

dropExpr	:	DROP DICT_ID -> ^(DROP DICT_ID);

addExpr		:	ADDIPA STRING '=' STRING -> ^(ADDIPA STRING+);

importExpr	:	IMPORT STRING -> ^(IMPORT STRING);

exportExpr	:	EXPORT STRING -> ^(EXPORT STRING);

lookupExpr	:	(LOOKUP)? STRING -> ^(LOOKUP STRING);

removeExpr	:	REMOVE STRING '=' STRING -> ^(REMOVE STRING+);

removeAllExpr	:	REMOVE ALL -> ^(REMOVE ALL);

switchExpr	:	SWITCH DICT_ID -> ^(SWITCH DICT_ID);

listExpr	:	LIST -> LIST;

helpExpr	:	HELP command? -> ^(HELP command?);


/**
 * Tokens 
 */

command	:	CREATE
		|	ADDIPA
		|	IMPORT
		|	LOOKUP
		|	REMOVE
		|	SWITCH
		|	LIST
		|	DROP
		|	EXPORT
		;

CREATE	:	'create';

DROP	:	'drop';

ADDIPA	:	'add';

IMPORT	:	'import';

EXPORT	:	'export';

LOOKUP	:	'lookup';

REMOVE	:	'remove';

SWITCH	:	'use';

LIST	:	'list';

HELP	:	'help';

ALL	:	'all';

STRING
	    : '"' ( '\\' . | ~('\\'|'"') )* '"' // double quoted string
	    ;
	    
DICT_ID	:	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'-')*
	;

WS :
    ( ' '
    | '\t'
    | '\f'
    | '\n'
    ) {  $channel = HIDDEN; };

	    


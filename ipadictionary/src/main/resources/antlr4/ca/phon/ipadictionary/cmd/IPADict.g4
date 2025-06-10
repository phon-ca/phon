grammar IPADict;

@header {
package ca.phon.ipadictionary.cmd;

import ca.phon.ipadictionary.ui.*;
import ca.phon.ipadictionary.exceptions.*;
}

@members {
private IPALookupContext lookupContext;

public void setLookupContext(IPALookupContext context) {
	lookupContext = context;
}

public IPALookupContext getLookupContext() {
	return lookupContext;
}

// die on any error
@Override
public void notifyErrorListeners(org.antlr.v4.runtime.Token offendingToken, String msg, RecognitionException e) {
	throw new IPADictionaryException(e);
}
}

// Parser rules

expr
	:	createExpr
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

createExpr
	:	CREATE DICT_ID (ASSIGN STRING)?
		{
			// try-catch not supported in ANTLR4 actions, use error listeners
		}
	;

dropExpr
	:	DROP DICT_ID
	;

addExpr
	:	ADDIPA STRING ASSIGN STRING
	;

importExpr
	:	IMPORT STRING
	;

exportExpr
	:	EXPORT STRING
	;

lookupExpr
	:	LOOKUP? STRING
	;

removeExpr
	:	REMOVE STRING ASSIGN STRING
	;

removeAllExpr
	:	REMOVE ALL
	;

switchExpr
	:	SWITCH DICT_ID
	;

listExpr
	:	LIST
	;

helpExpr
	:	HELP command?
	;

command
	:	CREATE
	|	ADDIPA
	|	IMPORT
	|	LOOKUP
	|	REMOVE
	|	SWITCH
	|	LIST
	|	DROP
	|	EXPORT
	;

// Lexer rules

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
ASSIGN	:	'=';
STRING
	:	'"' ( '\\' . | ~('\\'|'"') )* '"'
	;
DICT_ID
	:	[a-zA-Z_] [a-zA-Z_0-9-]*
	;
WS
	:	[ \t\f\n\r]+ -> skip
	;

grammar IPADict;
options {
	output=AST;
	ASTLabelType=CommonTree;
}

@header {
package ca.phon.ipadictionary.cmd;

import ca.phon.ipadictionary.ui.*;
import ca.phon.ipadictionary.exceptions.*;
}

@lexer::header {
package ca.phon.ipadictionary.cmd;
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
public void reportError(RecognitionException e) {
	throw new IPADictionaryExecption(e);
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
	catch [RecognitionException re] {
		lookupContext.printHelp("create");
		reportError(re);
	}
	
dropExpr	:	DROP DICT_ID -> ^(DROP DICT_ID);
	catch [RecognitionException re] {
		lookupContext.printHelp("drop");
		reportError(re);
	}

addExpr		:	ADDIPA STRING '=' STRING -> ^(ADDIPA STRING+);
	catch [RecognitionException re] {
		lookupContext.printHelp("add");
		reportError(re);
	}

importExpr	:	IMPORT STRING -> ^(IMPORT STRING);
	catch [RecognitionException re] {
		lookupContext.printHelp("import");
		reportError(re);
	}
	
exportExpr	:	EXPORT STRING -> ^(EXPORT STRING);
	catch [RecognitionException re] {
		lookupContext.printHelp("export");
		reportError(re);
	}

lookupExpr	:	(LOOKUP)? STRING -> ^(LOOKUP STRING);
	catch [RecognitionException re] {
		lookupContext.printHelp("lookup");
		reportError(re);
	}

removeExpr	:	REMOVE STRING '=' STRING -> ^(REMOVE STRING+);
	catch [RecognitionException re] {
		lookupContext.printHelp("remove");
		reportError(re);
	}

removeAllExpr	:	REMOVE ALL -> ^(REMOVE ALL);
	catch [RecognitionException re] {
		lookupContext.printHelp("removeAll");
		reportError(re);
	}

switchExpr	:	SWITCH DICT_ID -> ^(SWITCH DICT_ID);
	catch [RecognitionException re] {
		lookupContext.printHelp("use");
		reportError(re);
	}

listExpr	:	LIST -> LIST;
	catch [RecognitionException re] {
		lookupContext.printHelp("list");
		reportError(re);
	}

helpExpr	:	HELP command? -> ^(HELP command?);
	catch [RecognitionException re] {
		lookupContext.printHelp("help");
		reportError(re);
	}


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

	    


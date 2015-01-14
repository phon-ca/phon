tree grammar IPADictTree;

options
{
        ASTLabelType = CommonTree ;
        tokenVocab = IPADict ;
        output = AST;
}


@header {
package ca.phon.ipadictionary.ui;

import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.cmd.*;
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
	|	switchExpr
	|	removeExpr
	|	removeAllExpr
	|	listExpr
	|	helpExpr
	;

/**
 * Expressions
 */
createExpr	:	^(CREATE langid=DICT_ID langname=STRING?)
	{
		String dictDesc = ($langname == null ? "" : $langname.text);
		if(dictDesc.length() > 0) {
			dictDesc = dictDesc.substring(1, dictDesc.length()-1);
		}
		lookupContext.createDictionary($langid.text, dictDesc);
		lookupContext.switchDictionary($langid.text);
	}
;

dropExpr	:	^(DROP langid=DICT_ID)
	{
			lookupContext.dropDictionary($langid.text);
	}
	;

addExpr		:	^(ADDIPA vals+=STRING+)
	{
		String ortho = $vals.get(0).toString();
		String ipa = $vals.get(1).toString();
		ortho = ortho.substring(1, ortho.length()-1);
		ipa = ipa.substring(1, ipa.length()-1);
		
		lookupContext.addTranscript(ortho, ipa);
	}
	;

importExpr	:	^(IMPORT file=STRING)
	{
		String path = $file.text;
		path = path.substring(1, path.length()-1);
		lookupContext.importData(path);
	}
	;
	
exportExpr	:	^(EXPORT file=STRING)
	{
		String filename = $file.text;
		filename = filename.substring(1, filename.length()-1);
		lookupContext.exportData(filename);
	}
	;

lookupExpr	:	^(LOOKUP ortho=STRING)
	{
		lookupContext.lookup($ortho.text.substring(1, $ortho.text.length()-1));
	}
	;

switchExpr	:	^(SWITCH langid=DICT_ID)
	{
		lookupContext.switchDictionary($langid.text);
	}
	;

removeExpr	:	^(REMOVE vals+=STRING+)
	{
		String ortho = $vals.get(0).toString();
		String ipa = $vals.get(1).toString();
		ortho = ortho.substring(1, ortho.length()-1);
		ipa = ipa.substring(1, ipa.length()-1);

		lookupContext.removeTranscript(ortho, ipa);
	}
	;
	
removeAllExpr	:	^(REMOVE ALL)
	{
		lookupContext.removeAllTranscripts();
	}
	;

listExpr	:	LIST
	{
		lookupContext.list();
	}
	;

helpExpr	
	:	HELP
	{
		lookupContext.printHelp(null);
	}

	|	^(HELP command)
	{
		String cmd =
			$command.cmd;
		lookupContext.printHelp(cmd);
	}
	;

command	returns [String cmd]
		:	CREATE
		{	$cmd = $CREATE.text;	}
		|	ADDIPA
		{	$cmd = $ADDIPA.text;	}
		|	IMPORT
		{	$cmd = $IMPORT.text;	}
		|	LOOKUP
		{	$cmd = $LOOKUP.text;	}
		|	SWITCH
		{	$cmd = $SWITCH.text;	}
		|	REMOVE
		{	$cmd = $REMOVE.text;	}
		|	LIST
		{	$cmd = $LIST.text;		}
		;
	
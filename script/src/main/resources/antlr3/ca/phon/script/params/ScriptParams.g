grammar ScriptParams;
options {
	output=AST;
	ASTLabelType=CommonTree;
}

@header {
package ca.phon.script.params;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
}

@members {
/** The list of script params */
private List<ScriptParam> scriptParams = new ArrayList<ScriptParam>();

public ScriptParam[] getScriptParams() {
	return scriptParams.toArray(new ScriptParam[0]);
}
}

@lexer::header {
package ca.phon.script.params;
}

params
	:	'params' '=' paramDef (',' paramDef)* ';' -> ^('params' paramDef+)
	;
	
paramDef
	:	'{' paramSubDef '}'
	;
	
paramSubDef
	:	stringDef
	|	booleanDef
	|	enumDef
	|	separatorDef
	|	multiBoolDef
	|	labelDef
	;
	
separatorDef
	:	'separator' ',' desc = StringLiteral (',' collapsed = BOOLEAN)?
		{
		SeparatorScriptParam param = new SeparatorScriptParam(StringUtils.strip($desc.text, "\""));
		if($collapsed != null)
		{
			param.setCollapsed(Boolean.parseBoolean($collapsed.text));
		}
		scriptParams.add(param);
		}
	;
	
stringDef
	:	'string' ',' ID ',' def = StringLiteral ',' desc = StringLiteral
		{
		StringScriptParam param = new StringScriptParam($ID.text, 
			StringUtils.strip($desc.text, "\""), StringUtils.strip($def.text, "\""));
		scriptParams.add(param);
		} 
	;
	
booleanDef
	:	'bool' ',' ID ',' def = BOOLEAN ',' lbl = StringLiteral ',' desc = StringLiteral
		{
		BooleanScriptParam param = new BooleanScriptParam($ID.text, StringUtils.strip($lbl.text, "\""),
			StringUtils.strip($desc.text, "\""), new Boolean($def.text));
		scriptParams.add(param);
		}
	;
	
multiBoolDef
	:	'multibool' ',' ids = idList ',' defs = boolList ',' descs = stringList ',' desc = StringLiteral (',' cols = INT)?
		{
		String[] _ids = $ids.text.split("\\|");
		String[] defaults = $defs.text.split("\\|");
		Boolean[] _defs = new Boolean[defaults.length];
		for(int i = 0; i < defaults.length; i++)
			_defs[i] = new Boolean(defaults[i]);
		String[] _descs = $descs.text.split("\\|");
		// strip quotes
		for(int i = 0; i < _descs.length; i++) 
			_descs[i] = StringUtils.strip(_descs[i], "\"");
		MultiboolScriptParam param = 
			new MultiboolScriptParam(_ids, _defs, _descs, StringUtils.strip($desc.text, "\"")
				, ($cols == null ? 2 : new Integer($cols.text)));
		scriptParams.add(param);
		}
	;
	
enumDef
	:	'enum' ',' ID ',' list = stringList ',' def = INT ',' desc = StringLiteral
		{
		String[] opts = $list.text.split("\\|");
		
		// strip quotes
		for(int i = 0; i < opts.length; i++) opts[i] = StringUtils.strip(opts[i], "\"");
		EnumScriptParam param = new EnumScriptParam($ID.text, 
			StringUtils.strip($desc.text, "\""), new Integer($def.text), opts);
		scriptParams.add(param);
		}
	;
	
labelDef
	:	'label'	',' val = StringLiteral ',' desc = StringLiteral
		{
		LabelScriptParam param = new LabelScriptParam(StringUtils.strip($val.text, "\""), 
			StringUtils.strip($desc.text, "\""));
		scriptParams.add(param);
		}
	;
	
idList
	:	ID ('|' ID)*
	;
	
boolList
	:	BOOLEAN ('|' BOOLEAN)*
	;
	
stringList
	:	StringLiteral ('|' StringLiteral)*
	;
	
	
StringLiteral
    	:  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    	;
    	
BOOLEAN
	: 'true' | 'false'
	;

INT
	:	'0'..'9'+
	;
	
ID
	:	LETTER (LETTER|'0'..'9')*
	;
	
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;

fragment
EscapeSequence
    	:   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    	|   UnicodeEscape
    	|   OctalEscape
    	;

fragment
OctalEscape
    	:   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    	|   '\\' ('0'..'7') ('0'..'7')
    	|   '\\' ('0'..'7')
    	;
    
fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;


fragment
UnicodeEscape
	:   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    	;

fragment
LETTER
	:	'A'..'Z'
	|	'a'..'z'
	|	'_'
	;

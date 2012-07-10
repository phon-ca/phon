grammar Pho;

options {
	output=AST;
	ASTLabelType=CommonTree;
	tokenVocab=Pho;
}

@header {
package ca.phon.ipa.xml;
}

/**
 * Start
 */
pho		:  	PHO pw+ PHO_END
      ->  ^(PHO pw+)
      ;
		
pw		:	PW pw_ele+ PW_END
		->	^(PW pw_ele+)
		;
	
pw_ele	:	ph
		|	cp
		|	pause
		|	ss
		|	sb
		;
		
ph		:	PH ph_attr* TEXT PH_END
		->	^(PH ph_attr* TEXT)
		;
		
ph_attr	:	PH_LENGTH
		|	PH_PREFIX
		|	PH_SUFFIX
		|	PH_COMBINING
		|	PH_TONE
		;
		
cp		:	CP CP_LIG? p1=ph p2=ph CP_END
		->	^(CP CP_LIG? $p1 $p2)
		;
		
pause	:	PAUSE PAUSE_LENGTH? PAUSE_END
		->	^(PAUSE PAUSE_LENGTH?)
		;
		
sb		:	SB SB_END
		->	SB
		;
		
ss		:	SS SS_TYPE? SS_END
		->	^(SS SS_TYPE?)
		;
		

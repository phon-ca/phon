tree grammar IPATranscript2Xml;

options {
	tokenVocab=Pho;
	ASTLabelType=CommonTree;
	output=template;
}

@header {
package ca.phon.ipa.xml;
}

@members {

private boolean includeNamespace = false;

public boolean isIncludeNamespace() { return includeNamespace; }

public void setIncludeNamespace(boolean b) { includeNamespace = b; }

private String namespacePrefix = "";

public String getNamespacePrefix() {
  String retVal = "";
  
  if(isIncludeNamespace() && namespacePrefix.length() > 0) {
    retVal = namespacePrefix + ":";
  }
  
  return retVal;
}

public String getNamespaceDecl() {
  String retVal = "";
  
  if(isIncludeNamespace()) {
    retVal = "xmlns";
    if(namespacePrefix.length() > 0)
      retVal += ":" + namespacePrefix;
    retVal += "=\"http://phon.ling.mun.ca/ns/pho\"";
  }
  
  return retVal;
}

public void setNamespacePrefix(String prefix) {
  namespacePrefix = prefix;
}

public String toHexCharList(String val) {
   String retVal = new String();
   
   for(Character c:val.toCharArray()) {
     String cVal = "&#x" + String.format("\%04x", (int)c) + ";";
     retVal += (retVal.length() > 0 ? " " : "") + cVal;
   }
   
   return retVal;
}

}

/**
 * Start
 */
pho		:  ^(PHO (pwlist+=pw)+)
      ->  template(nsdecl={getNamespaceDecl()}, prefix={getNamespacePrefix()}, content={$pwlist})
      <<\<<prefix>pho <nsdecl>\>
      <content; separator="">
\</<prefix>pho\>
>>
      ;
		
pw		: ^(PW (phonelist+=pw_ele)+)
      ->  template(prefix={getNamespacePrefix()}, content={$phonelist})
      <<\<<prefix>pw\>
      <content; separator="">
\</<prefix>pw\>
>>
      ;
	
pw_ele	:  ph
        ->  template(v={$ph.st})
        "<v>"
        |  cp
        ->  template(v={$cp.st})
        "<v>"
        |  pause
        ->  template(v={$pause.st})
        "<v>"
        |  ss
        ->  template(v={$ss.st})
        "<v>"
        |  sb
        ->  template(v={$sb.st})
        "<v>"
        ;
		
ph		:	^(PH (phattrs+=ph_attr)* TEXT)
      ->  template(prefix={getNamespacePrefix()}, attrs={$phattrs}, v={$TEXT.text})
      <<\<<prefix>ph <attrs; separator=" ">\><v>\</<prefix>ph\> >>
      ;
		
ph_attr : PH_LENGTH
        ->  template(v={$PH_LENGTH.text})
        "length=\"<v>\""
		    |	PH_PREFIX
		    ->  template(v={toHexCharList($PH_PREFIX.text)})
		    "prefix=<v>"
		    |	PH_SUFFIX
		    ->  template(v={toHexCharList($PH_SUFFIX.text)})
		    "suffix=\"<v>\""
		    |	PH_COMBINING
		    ->  template(v={toHexCharList($PH_COMBINING.text)})
		    "combining=\"<v>\""
		    |	PH_TONE
		    ->  template(v={toHexCharList($PH_TONE.text)})
		    "tone=\"<v>\""
		    ;
		
cp		:	^(CP CP_LIG? p1=ph p2=ph)
      -> template(prefix={getNamespacePrefix()}, lig={($CP_LIG != null ? $CP_LIG.text : "\u035c")}, ph1={$p1.st}, ph2={$p2.st})
      <<\<<prefix>cp lig="<lig>"\><ph1><ph2>\</<prefix>cp\> >>
      ;
		
pause	:	^(PAUSE PAUSE_LENGTH?)
      ->  template(prefix={getNamespacePrefix()}, len={($PAUSE_LENGTH != null ? $PAUSE_LENGTH.text : "1.0")})
      <<\<<prefix>pause length="<len>"/\> >>
      ;
		
sb		:	SB
      ->  template(prefix={getNamespacePrefix()})
      <<\<<prefix>sb/\> >>
      ;
		
ss		:	^(SS SS_TYPE?)
      -> template(prefix={getNamespacePrefix()}, type={($SS_TYPE != null ? $SS_TYPE.text : "1")})
      <<\<<prefix>ss type="<type>"/\> >>
      ;
		

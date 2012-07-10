tree grammar Xml2IPATranscript;

options {
	tokenVocab=Pho;
	ASTLabelType=CommonTree;
	output=AST;
}

@header {
package ca.phon.ipa.xml;

import ca.phon.ipa.*;
import org.apache.commons.lang3.ArrayUtils;
}

@members {

private final IPAElementFactory factory = new IPAElementFactory();

private IPATranscript transcript;

public IPATranscript getTranscript() {
  if(this.transcript == null) transcript = new IPATranscript();
  return this.transcript;
}

public void setTranscript(IPATranscript transcript) {
  this.transcript = transcript;
}

}

/**
 * Start
 */
pho		:	^(PHO pw+)
      {
        if(getTranscript().size() > 0)
          getTranscript().remove(getTranscript().size()-1);
      }
        ;
		
pw		:	^(PW pw_ele+)
    {
        getTranscript().add(factory.createWordBoundary());
    }
		;
	
pw_ele	
    :	ph
    {
      getTranscript().add($ph.value);
    }
		|	cp
		{
		  getTranscript().add($cp.value);
		}
		|	pause
		{
		  getTranscript().add($pause.value);
		}
		|	ss
		{
		  getTranscript().add($ss.value);
		}
		|	sb
		{
		  getTranscript().add($sb.value);
		}
		;
		
ph returns [Phone value]	
scope {
  Phone currentPhone;
}
@init {
  $ph::currentPhone = factory.createPhone();
}
@after {
  if($cp.size() > 0) {
    if($cp::currentCompoundPhone.getFirstPhone() == null)
      $cp::currentCompoundPhone.setFirstPhone($value);
    else
      $cp::currentCompoundPhone.setSecondPhone($value);
  }
}
    :	^(PH ph_attr* TEXT)
    {
      $ph::currentPhone.setBasePhone($TEXT.text.charAt(0));
      $value = $ph::currentPhone;
    }
		;
		
ph_attr	
    :	PH_LENGTH
    {
      float len = Float.parseFloat($PH_LENGTH.text);
      $ph::currentPhone.setLength(len);
    }
		|	PH_PREFIX
		{
		  char prefix = $PH_PREFIX.text.charAt(0);
		  $ph::currentPhone.setPrefixDiacritic(prefix);
		}
		|	PH_SUFFIX
		{
		  char suffix = $PH_SUFFIX.text.charAt(0);
		  $ph::currentPhone.setSuffixDiacritic(suffix);
		}
		|	PH_COMBINING
		{
		  String txt = $PH_COMBINING.text;
		  if(txt != null) {
		    Character[] cmbChars = ArrayUtils.toObject(txt.toCharArray());
		    $ph::currentPhone.setCombiningDiacritics(cmbChars);
		  }
		}
		|	PH_TONE
		{
		  String txt = $PH_TONE.text;
		  if(txt != null) {
		    Character[] toneChars = ArrayUtils.toObject(txt.toCharArray());
		    $ph::currentPhone.setToneDiacritics(toneChars);
		  }
		}
		;
		
cp returns [CompoundPhone value]
scope {
  CompoundPhone currentCompoundPhone;
}
@init {
  $cp::currentCompoundPhone = factory.createCompoundPhone();
}
    :	^(CP CP_LIG? p1=PH p2=PH)
    {
      char lig = ($CP_LIG != null ? $CP_LIG.text.charAt(0) : '\u035c');
      $cp::currentCompoundPhone.setLigature(lig);
      $value = $cp::currentCompoundPhone;
    }
		;
		
pause returns [Pause value]	
    :	^(PAUSE PAUSE_LENGTH?)
    {
      PauseLength len = ($PAUSE_LENGTH != null ? PauseLength.lengthFromString($PAUSE_LENGTH.text) : PauseLength.SHORT);
      $value = factory.createPause(len);
    }
		;
		
sb returns [SyllableBoundary value]	
    :	SB
    {
      $value = factory.createSyllableBoundary();
    }
		;
		
ss returns [StressMarker value]
    :	^(SS SS_TYPE?)
    {
      StressType type = ($SS_TYPE != null ?
                          ($SS_TYPE.text.equals("1") ? StressType.PRIMARY : StressType.SECONDARY)
                          : StressType.PRIMARY);
      $value = factory.createStress(type);
    }
		;
		

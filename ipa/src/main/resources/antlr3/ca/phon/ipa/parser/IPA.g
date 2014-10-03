/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2011-2014 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
 
 /**
  * Grammar for parsing IPA transcriptions encoded in UTF-8.
  * 
  * Return value is a list of (un-syllabified) ca.phon.phone.Phone objects.
  * This class is meant to be used with a custom PhoneLexer which acts
  * as a TokenSource for this parser.
  */
grammar IPA;

// package and imports for generated parser
@header {
package ca.phon.ipa.parser;

import java.util.Collections;
import java.util.ArrayList;

import ca.phon.ipa.*;
import ca.phon.ipa.parser.exceptions.*;
import ca.phon.ipa.features.*;
import ca.phon.syllable.*;
}

// custom methods for generated parser
@members {
/**
 * Phone factory
 */
private final IPAElementFactory factory = new IPAElementFactory();
}

/* RULES */
/**
 * Start rule for a full IPA transcription
 *
 * Each word is expected to be separated by a space
 */
transcription returns [IPATranscript transcript]
scope {
	IPATranscriptBuilder builder;
}
@init {
	$transcription::builder = new IPATranscriptBuilder();
}
	:	w1=word {if($w1.w != null) { $transcription::builder.append($w1.w);} else { if(input.get(input.index()).getType() != EOF) return transcription(); } } 
		( (word_boundary {$transcription::builder.append($word_boundary.wordBoundary);})+
		w2=word? {if($w2.w != null) { $transcription::builder.append($w2.w);} } )*
	{
		if(input.LA(1) != Token.EOF) {
	  		IPAParserException ipae = new IPAParserException("Failed to process all text");
		
			int myLA = input.LA(1);
			if(myLA == LIGATURE) 
				ipae = new HangingLigatureException("Ligature missing left-hand element.");
			else if(myLA == TONE)
				ipae = new StrayDiacriticException("Stray tone diacritic");
			else if(myLA == LONG || myLA == HALF_LONG)
				ipae = new StrayDiacriticException("Stray length diacritic");
				
			ipae.setPositionInLine(input.LT(1).getCharPositionInLine());
			throw ipae;
		}
		$transcript = $transcription::builder.toIPATranscript();
	}
	;
	
/**
 * A single IPA word
 * 
 * Words may include pauses, but must start with
 * either a stress marker or a phone.
 *
 * @returns a list of ca.phon.phone.Phone objects
 */
word returns [IPATranscript w]
scope {
	IPATranscriptBuilder builder;
	boolean hangingLig;
	boolean makeCompound;
	char hangingLigChar;
}
@init { 
	$word::builder = new IPATranscriptBuilder();
	$word::hangingLig = false;
	$word::makeCompound = false;
}
	:	(we=word_element {
	
			if($we.p != null) { 
				if($word::hangingLig) {
					$word::hangingLig = false;
					if($word::makeCompound) {
						$word::builder.makeCompoundPhone($we.p, $word::hangingLigChar);
					} else {
						$word::builder.append($we.p);
					}
					$word::makeCompound = true;
				} else {
					if($word::makeCompound) {
						$word::builder.makeCompoundPhone($we.p, $word::hangingLigChar);
						$word::makeCompound = false;
					} else {
						$word::builder.append($we.p);
					}
				}
			}
		}  
	
		( COLON sc=sctype {
			SyllabificationInfo sInfo = $we.p.getExtension(SyllabificationInfo.class);
			sInfo.setConstituentType($sc.value);
			sInfo.setDiphthongMember($sc.isDiphthongMember);
		} )? )+
	{
		if($word::makeCompound) {
			final HangingLigatureException hle = new HangingLigatureException("Ligature missing right-hand element");
			int idx = input.LT(1).getCharPositionInLine();
			if(idx < 0) {
				// end of string
				idx = input.toString().length();
			}
			hle.setPositionInLine(idx);
			throw hle;
		}
		$w = $word::builder.toIPATranscript();
	}
	|	p=pause
	{
		$word::builder.append($pause.pause);
		$w = $word::builder.toIPATranscript();
	}
	;
	catch [IllegalStateException e] {
		// this happens when compound phone elements are not phone objects
		final InvalidTokenException ite = new InvalidTokenException("Both elements in a compound phone must be phones");
		ite.setPositionInLine(input.get(input.index()-1).getCharPositionInLine());
		throw ite;
	}
	catch [NoViableAltException e] {
		IPAParserException ipae = new IPAParserException(e);
		
		int myLA = input.LA(1);
		if(myLA == LIGATURE) 
			ipae = new HangingLigatureException("Ligature missing left-hand element.");
		else if(myLA == TONE)
			ipae = new StrayDiacriticException("Stray tone diacritic");
		else if(myLA == LONG || myLA == HALF_LONG)
			ipae = new StrayDiacriticException("Stray length diacritic");
		ipae.setPositionInLine(e.charPositionInLine);
		throw ipae;
	}
	
	
word_element returns [IPAElement p]
	:	stress
	{
		$p = $stress.stressMarker;
	}
	|	phone
	{
		$p = $phone.ele;
	}
	|	syllable_boundary
	{
		$p = $syllable_boundary.syllableBoundary;
	}
	|	word_net_marker
	{
		$p = $word_net_marker.wordnetMarker;
	}
	|	intra_word_pause
	{
		$p = $intra_word_pause.intraWordPause;
	}
	|	phonex_matcher_ref
	{
		$p = $phonex_matcher_ref.phonexMatcherRef;
	}
	;

/**
 * Stress
 */
stress returns [StressMarker stressMarker]
	:	PRIMARY_STRESS
	{
		$stressMarker = factory.createStress(StressType.PRIMARY);
	}
	|	SECONDARY_STRESS
	{
		$stressMarker = factory.createStress(StressType.SECONDARY);
	}
	;
	
intra_word_pause returns [IntraWordPause intraWordPause]
	:	INTRA_WORD_PAUSE
	{
		$intraWordPause = factory.createIntraWordPause();
	}
	;
	
syllable_boundary returns [IPAElement syllableBoundary]
	:	PERIOD
	{
		$syllableBoundary = factory.createSyllableBoundary();
	}
	|	MINOR_GROUP
	{
		$syllableBoundary = factory.createIntonationGroup(IntonationGroupType.MINOR);
	}
	|	MAJOR_GROUP
	{
		$syllableBoundary = factory.createIntonationGroup(IntonationGroupType.MAJOR);
	}
	;
	
word_net_marker returns [IPAElement wordnetMarker]
	:	PLUS
	{
		$wordnetMarker = factory.createCompoundWordMarker();
	}
	|	SANDHI
	{
		$wordnetMarker = factory.createSandhi($SANDHI.text);
	}
	;
	
phonex_matcher_ref returns [IPAElement phonexMatcherRef]
	:	DOLLAR_SIGN DIGIT
	{
		$phonexMatcherRef = factory.createPhonexMatcherReference(Integer.parseInt($DIGIT.text));
	}
	|	DOLLAR_SIGN OPEN_BRACE GROUP_NAME CLOSE_BRACE
	{
		$phonexMatcherRef = factory.createPhonexMatcherReference($GROUP_NAME.text);
	}
	;
	
word_boundary returns [IPAElement wordBoundary]
	:	SPACE
	{
		$wordBoundary = factory.createWordBoundary();
	}
	|	ALIGNMENT
	{
		$wordBoundary = factory.createAlignmentMarker();
	}
	;
	
/**
 * Pauses in words
 */
pause returns [Pause pause]
	:	OPEN_PAREN length=pause_length CLOSE_PAREN
	{
		PauseLength l = $length.length;
		if(l == null) {
			l = PauseLength.SHORT;
		}
		$pause = factory.createPause(l);
	}
	;
	
/**
 * Pause length
 */
pause_length returns [PauseLength length]
	:	PERIOD
	{	
		$length = PauseLength.SHORT;
	}
	|	PERIOD PERIOD
	{
		$length = PauseLength.MEDIUM;
	}
	|	PERIOD PERIOD PERIOD
	{
		$length = PauseLength.LONG;
	}
	;
	
/**
 *
 */
phone returns [IPAElement ele]
	:	p1=single_phone
	{	
		$ele = $p1.phone;
	}
	;
	
base_phone returns [Phone phone]
scope {
	List<Diacritic> cmbDias;
}
@init {
	$base_phone::cmbDias = new ArrayList<Diacritic>();
}
	:	initialToken=(CONSONANT|VOWEL|COVER_SYMBOL|GLIDE) (cd=COMBINING_DIACRITIC {$base_phone::cmbDias.add( factory.createDiacritic( $cd.text.charAt(0) ) );})*
	{
		Diacritic[] combiningDiacritics = new Diacritic[$base_phone::cmbDias.size()];
		for(int i = 0; i < combiningDiacritics.length; i++) {
			combiningDiacritics[i] = $base_phone::cmbDias.get(i);
		}
		
		$phone = factory.createPhone($initialToken.text.charAt(0), combiningDiacritics);
	}
	;
	catch [MismatchedSetException mse] {
		// happens when trying to match a character which cannot be a base-phone
		final InvalidTokenException ite = new InvalidTokenException("Invalid token, expecting one of: consonant, vowel, or cover symbol");
		ite.setPositionInLine(mse.charPositionInLine);
		throw ite;
	}
	
/**
 * Phone + optional COMBINING diacritics and
 * a prefix and/or suffix superscript diacritic.
 */
single_phone returns [Phone phone]
options {
	backtrack=true;
}
	:	ps=prefix_section? p1=base_phone ( id=(LONG|HALF_LONG|SUFFIX_DIACRITIC)? lig=LIGATURE p2=base_phone)?  ss=suffix_section?
	{
		Diacritic[] prefixDiacritics = new Diacritic[0];
		if(ps != null) {
			prefixDiacritics = $ps.diacritics.toArray(prefixDiacritics);
		}
		Diacritic[] suffixDiacritics = new Diacritic[0];
		if(ss != null) {
			suffixDiacritics = $ss.diacritics.toArray(suffixDiacritics);
		}
		
		$phone = $p1.phone;
		if($lig != null) {
			$phone = factory.createCompoundPhone($p1.phone, $p2.phone, $lig.text.charAt(0));
		}
		
		$phone.setPrefixDiacritics(prefixDiacritics);
		$phone.setSuffixDiacritics(suffixDiacritics);
	}
	;
	
prefix_section returns [List<Diacritic> diacritics]
scope {
	List<Diacritic> dias;
}
@init {
	$prefix_section::dias = new ArrayList<Diacritic>();
}
	:	(pd=prefix_diacritic {if(pd != null) $prefix_section::dias.add($pd.diacritic);})+
	{
		$diacritics = $prefix_section::dias;
	}
	;
	
prefix_diacritic returns [Diacritic diacritic]
scope {
	List<Diacritic> dias;
}
@init {
	$prefix_diacritic::dias = new ArrayList<Diacritic>();
}
	:	pd=PREFIX_DIACRITIC (cd=COMBINING_DIACRITIC {$prefix_diacritic::dias.add(factory.createDiacritic($cd.text.charAt(0)));})* lig=LIGATURE?
	{
		final Diacritic[] suffix = new Diacritic[$prefix_diacritic::dias.size()+($lig != null ? 1 : 0)];
		for(int i = 0; i < $prefix_diacritic::dias.size(); i++) {
			suffix[i] = $prefix_diacritic::dias.get(i);
		}
		if($lig != null) {
			suffix[suffix.length-1] = factory.createDiacritic($lig.text.charAt(0));
		}
		
		$diacritic = factory.createDiacritic(new Diacritic[0], $pd.text.charAt(0), suffix);
	}
	|	sd=SUFFIX_DIACRITIC (cd=COMBINING_DIACRITIC {$prefix_diacritic::dias.add(factory.createDiacritic($cd.text.charAt(0)));})* lig=(ROLE_REVERSAL | LIGATURE)
	{
		final Diacritic[] suffix = new Diacritic[$prefix_diacritic::dias.size()+1];
		for(int i = 0; i < $prefix_diacritic::dias.size(); i++) {
			suffix[i] = $prefix_diacritic::dias.get(i);
		}
		suffix[suffix.length-1] = factory.createDiacritic($lig.text.charAt(0));
		
		$diacritic = factory.createDiacritic(new Diacritic[0], $sd.text.charAt(0), suffix);
	}
	;
	catch [MismatchedSetException mse] {
		final ca.phon.ipa.parser.exceptions.StrayDiacriticException sde = new ca.phon.ipa.parser.exceptions.StrayDiacriticException("Stray suffix diacritic, expecting \u0361");
		sde.setPositionInLine(mse.charPositionInLine);
		throw sde;
	}
	
suffix_section returns [List<Diacritic> diacritics]
scope {
	List<Diacritic> dias;
}
@init {
	$suffix_section::dias = new ArrayList<Diacritic>();
}
	:	(sd=suffix_diacritic {if(sd != null) $suffix_section::dias.add($sd.diacritic);})+
	{
		$diacritics = $suffix_section::dias;
	}
	;
	catch [NoViableAltException e] {
		final StrayDiacriticException sde = new StrayDiacriticException("Stray diacritic, expecting \u0361");
		sde.setPositionInLine(e.index);
		throw sde;
	}
	
suffix_diacritic returns [Diacritic diacritic]
scope {
	List<Diacritic> dias;
}
@init {
	$suffix_diacritic::dias = new ArrayList<Diacritic>();
}
	:	lig=LIGATURE? sd=SUFFIX_DIACRITIC (cd=COMBINING_DIACRITIC {$suffix_diacritic::dias.add(factory.createDiacritic($cd.text.charAt(0)));})*
	{
		final Diacritic[] prefix = ($lig != null ? new Diacritic[]{factory.createDiacritic($lig.text.charAt(0))} : new Diacritic[0]);
		final Diacritic[] suffix = new Diacritic[$suffix_diacritic::dias.size()];
		for(int i = 0; i < $suffix_diacritic::dias.size(); i++) {
			suffix[i] = $suffix_diacritic::dias.get(i);
		}
		
		$diacritic = factory.createDiacritic(prefix, $sd.text.charAt(0), suffix);
	}
	|	lig=LIGATURE pd=PREFIX_DIACRITIC (cd=COMBINING_DIACRITIC {$suffix_diacritic::dias.add(factory.createDiacritic($cd.text.charAt(0)));})*
	{
		final Diacritic[] prefix = new Diacritic[]{ factory.createDiacritic($lig.text.charAt(0)) };
		final Diacritic[] suffix = new Diacritic[$suffix_diacritic::dias.size()];
		for(int i = 0; i < $suffix_diacritic::dias.size(); i++) {
			suffix[i] = $suffix_diacritic::dias.get(i);
		}
		
		$diacritic = factory.createDiacritic(prefix, $pd.text.charAt(0), suffix);
	}
	|	pd=PREFIX_DIACRITIC rr=ROLE_REVERSAL (cd=COMBINING_DIACRITIC {$suffix_diacritic::dias.add(factory.createDiacritic($cd.text.charAt(0)));})*
	{
		final Diacritic[] prefix = new Diacritic[0];
		final Diacritic[] suffix = new Diacritic[$suffix_diacritic::dias.size()+1];
		for(int i = 0; i < $suffix_diacritic::dias.size(); i++) {
			suffix[i+1] = $suffix_diacritic::dias.get(i);
		}
		suffix[0] = factory.createDiacritic($rr.text.charAt(0));
		
		$diacritic = factory.createDiacritic(prefix, $pd.text.charAt(0), suffix);
	}
	|	t=TONE (cd=COMBINING_DIACRITIC {$suffix_diacritic::dias.add(factory.createDiacritic($cd.text.charAt(0)));})*
	{
		final Diacritic[] prefix = new Diacritic[0];
		final Diacritic[] suffix = new Diacritic[$suffix_diacritic::dias.size()];
		for(int i = 0; i < $suffix_diacritic::dias.size(); i++) {
			suffix[i] = $suffix_diacritic::dias.get(i);
		}
		
		$diacritic = factory.createDiacritic(prefix, $t.text.charAt(0), suffix);
	}
	|	len=LONG
	{
		$diacritic = factory.createDiacritic($len.text.charAt(0));
	}
	|	len=HALF_LONG
	{
		$diacritic = factory.createDiacritic($len.text.charAt(0));
	}
	;
	catch [NoViableAltException nvae] {
		int myLA = input.LA(1);
		// special case when we have a hanging ligature between complex phones
		if(myLA == LIGATURE) {
			$word::hangingLig = true;
			$word::hangingLigChar = input.LT(1).getText().charAt(0);
			input.consume();
		} else {
			throw nvae;
		}
	}
	
sctype returns [SyllableConstituentType value, boolean isDiphthongMember]
	:	SCTYPE
	{
		$value = SyllableConstituentType.fromString($SCTYPE.text);
		$isDiphthongMember = ($SCTYPE.text.equalsIgnoreCase("D"));
	}
	;
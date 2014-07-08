/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2011 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
import ca.phon.ipa.features.*;
import ca.phon.syllable.*;
}

// custom methods for generated parser
@members {

/**
 * Phone factory
 */
private final IPAElementFactory factory = new IPAElementFactory();

/**
 * List of custom error handlers
 */
private final List<IPAParserErrorHandler> errorHandlers = 
	Collections.synchronizedList(new ArrayList<IPAParserErrorHandler>());
	
/**
 * Add an error handler to the lexer
 * 
 * @param handler
 */
public void addErrorHandler(IPAParserErrorHandler handler) {
	if(!errorHandlers.contains(handler)) {
		errorHandlers.add(handler);
	}
}

/**
 * Remove an error handler from the lexer
 * 
 * @param handler
 */
public void removeErrorHandler(IPAParserErrorHandler handler) {
	errorHandlers.remove(handler);
}

/**
 * Report an error to all handlers
 * 
 * @param ex
 */
private void reportError(IPAParserException ex) {
	for(IPAParserErrorHandler handler:errorHandlers) {
		handler.handleError(ex);
	}
}

@Override
public void reportError(RecognitionException e) {
	IPAParserException ex = new IPAParserException(e);
	ex.setPositionInLine(e.charPositionInLine);
	reportError(ex);
}
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
		(word_boundary {$transcription::builder.appendWordBoundary();}
		w2=word {if($w2.w != null) { $transcription::builder.append($w2.w);} } )*
	{
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
}
@init { 
	$word::builder = new IPATranscriptBuilder(); 
}
	:	(we=word_element {if($we.p != null) $word::builder.append($we.p); }  
		( COLON sc=sctype {
			SyllabificationInfo sInfo = $we.p.getExtension(SyllabificationInfo.class);
			sInfo.setConstituentType($sc.value);
			sInfo.setDiphthongMember($sc.isDiphthongMember);
		} )? )+
	{
		$w = $word::builder.toIPATranscript();
	}
	|	p=pause
	{
		$word::builder.append($pause.pause);
		$w = $word::builder.toIPATranscript();
	}
	;
	catch [EarlyExitException ex] {
		reportError(ex);
		
		if(state.lastErrorIndex != input.index() && input.index() == 0) {
			state.lastErrorIndex = input.index();
			beginResync();
			input.consume();
			endResync();
		} else {
			recover(input, ex);
		}
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
	
word_boundary returns [WordBoundary wordBoundary]
	:	SPACE
	{
		$wordBoundary = factory.createWordBoundary();
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
	:	single_phone
	{	
		$ele = $single_phone.phone;
	}
	|	compound_phone ( COLON sc=sctype )?
	{	
		$ele = $compound_phone.phone;
	}
	;
	catch [NoViableAltException ex] {
		if(state.lastErrorIndex != input.index()) {
			Token t = input.get(input.index());
			if(t.getType() == CONSONANT
            					|| t.getType() == COVER_SYMBOL
            					|| t.getType() == GLIDE
            					|| t.getType() == VOWEL) {            				
				String txt = t.getText();
				char filler = (txt != null && txt.length() > 0 ? txt.charAt(0) : 'X');
				ele = (new IPAElementFactory()).createPhone(filler);
			} else {
				ele = (new IPAElementFactory()).createPhone('X');
			}
		}
		reportError(ex);
		recover(input, ex);
	}
	catch [RecognitionException re] {
		reportError(re);
		recover(input, re);
	}
	
/**
 * A single (non-compound) phone.
 */
single_phone returns [Phone phone]
scope {	
	List<Character> toneDias;
}
@init {
	$single_phone::toneDias = new ArrayList<Character>();
}
	:	base_phone (tc=TONE {$single_phone::toneDias.add($tc.text.charAt(0));})*
	{	
		$phone = $base_phone.phone;
		$phone.setToneDiacritics($single_phone::toneDias.toArray(new Character[0]));
	}
	|	complex_phone (tc=TONE {$single_phone::toneDias.add($tc.text.charAt(0));})*
	{	
		$phone = $complex_phone.phone;
		$phone.setToneDiacritics($single_phone::toneDias.toArray(new Character[0]));
	}
	;
	
/**
 * Phone + optional COMBINING diacritics
 */
base_phone returns [Phone phone]
scope {
	List<Character> cmbDias;
}
@init {
	$base_phone::cmbDias = new ArrayList<Character>();
}
	:	initialToken=(CONSONANT|VOWEL|COVER_SYMBOL|GLIDE) (cd=COMBINING_DIACRITIC {$base_phone::cmbDias.add($cd.text.charAt(0));})* len=phone_length?
	{
		final Character basePhone = $initialToken.text.charAt(0);
		
		final float length = (len != null ? $len.length : 0.0f);
		
		$phone = factory.createPhone(basePhone, $base_phone::cmbDias.toArray(new Character[0]), length);
	}
	;
	
/**
 * Phone + optional COMBINING diacritics and
 * a prefix and/or suffix superscript diacritic.
 */
complex_phone returns [Phone phone]
	:	PREFIX_DIACRITIC base_phone
	{	
		$phone = $base_phone.phone;
		$phone.setPrefixDiacritic($PREFIX_DIACRITIC.text.charAt(0));
	}
	|	SUFFIX_DIACRITIC ROLE_REVERSAL base_phone
	{	
		$phone = $base_phone.phone;
		$phone.setPrefixDiacritic($SUFFIX_DIACRITIC.text.charAt(0));
	}
	|	base_phone SUFFIX_DIACRITIC
	{	
		$phone = $base_phone.phone;
		$phone.setSuffixDiacritic($SUFFIX_DIACRITIC.text.charAt(0));
	}
	|	base_phone PREFIX_DIACRITIC ROLE_REVERSAL
	{	
		$phone = $base_phone.phone;
		$phone.setSuffixDiacritic($PREFIX_DIACRITIC.text.charAt(0));
	}
	|	PREFIX_DIACRITIC base_phone SUFFIX_DIACRITIC
	{	
		$phone = $base_phone.phone;
		$phone.setPrefixDiacritic($PREFIX_DIACRITIC.text.charAt(0));
		$phone.setSuffixDiacritic($SUFFIX_DIACRITIC.text.charAt(0));	
	}
	|	pd1=PREFIX_DIACRITIC base_phone pd2=PREFIX_DIACRITIC ROLE_REVERSAL
	{	
		$phone = $base_phone.phone;
		$phone.setPrefixDiacritic($pd1.text.charAt(0));
		$phone.setSuffixDiacritic($pd2.text.charAt(0));
	}
	|	sd1=SUFFIX_DIACRITIC ROLE_REVERSAL base_phone sd2=SUFFIX_DIACRITIC
	{	
		$phone = $base_phone.phone;
		$phone.setPrefixDiacritic($sd1.text.charAt(0));
		$phone.setSuffixDiacritic($sd2.text.charAt(0));
	}
	|	SUFFIX_DIACRITIC rr1=ROLE_REVERSAL base_phone PREFIX_DIACRITIC rr2=ROLE_REVERSAL
	{	
		$phone = $base_phone.phone;
		$phone.setPrefixDiacritic($SUFFIX_DIACRITIC.text.charAt(0));	
		$phone.setSuffixDiacritic($PREFIX_DIACRITIC.text.charAt(0));
	}
	;
	
/**
 * Compound phones - two single phones connected by 
 * a ligature.
 */
compound_phone returns [CompoundPhone phone]
	:	sp1=single_phone LIGATURE sp2=single_phone
	{
		Phone firstPhone = ($sp1.phone != null ? $sp1.phone : (new IPAElementFactory()).createPhone('X')) ;
		Phone secondPhone = ($sp2.phone != null ? $sp2.phone : (new IPAElementFactory()).createPhone('X')) ;
		Character ligature = $LIGATURE.text.charAt(0);
		$phone = factory.createCompoundPhone(firstPhone, secondPhone, ligature);	
	}
	;
	
/**
 * Rule for matching length diacritics
 */
phone_length returns [Float length]
@init{ $length = 0.0f; }
	:	LONG						// long
	{	
		$length = 1.0f;	
	}
	|	l1=LONG l2=LONG					// x-long
	{	
		$length = 2.0f;	
	}
	|	l1=LONG l2=LONG l3=LONG				// xx-long
	{	
		$length = 3.0f;	
	}
	|	HALF_LONG					// half-long
	{	
		$length = 0.5f;	
	}
	|	LONG HALF_LONG					// long, half-long combo
	{	
		$length = 1.5f;	
	}
	;
	
sctype returns [SyllableConstituentType value, boolean isDiphthongMember]
	:	SCTYPE
	{
		$value = SyllableConstituentType.fromString($SCTYPE.text);
		$isDiphthongMember = ($SCTYPE.text.equalsIgnoreCase("D"));
	}
	;
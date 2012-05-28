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
grammar Phone;

// package and imports for generated parser
@header {
package ca.phon.ipa.phone.parser;

import java.util.Collections;
import java.util.ArrayList;

import ca.phon.ipa.*;
import ca.phon.ipa.featureset.*;
import ca.phon.ipa.phone.*;
}

// custom methods for generated parser
@members {

/**
 * Phone factory
 */
private final PhoneFactory factory = new PhoneFactory();

/**
 * List of custom error handlers
 */
private final List<PhoneParserErrorHandler> errorHandlers = 
	Collections.synchronizedList(new ArrayList<PhoneParserErrorHandler>());
	
/**
 * Add an error handler to the lexer
 * 
 * @param handler
 */
public void addErrorHandler(PhoneParserErrorHandler handler) {
	if(!errorHandlers.contains(handler)) {
		errorHandlers.add(handler);
	}
}

/**
 * Remove an error handler from the lexer
 * 
 * @param handler
 */
public void removeErrorHandler(PhoneParserErrorHandler handler) {
	errorHandlers.remove(handler);
}

/**
 * Report an error to all handlers
 * 
 * @param ex
 */
private void reportError(PhoneParserException ex) {
	for(PhoneParserErrorHandler handler:errorHandlers) {
		handler.handleError(ex);
	}
}

@Override
public void reportError(RecognitionException e) {
	PhoneParserException ex = new PhoneParserException(e);
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
	:	wrds+=word (word_boundary wrds+=word)*
	{
		$transcript = new IPATranscript();
		for(Object wordObj:$wrds) {
			IPATranscript word = IPATranscript.class.cast(wordObj);
			
			if(word != null) {
				if($transcript.size() > 0)
					$transcript.add(factory.createWordBoundary());
				
				$transcript.addAll(word);
			}
		}
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
word returns [IPATranscript word]
	:	eles+=word_element+
	{
		$word = new IPATranscript();
		for(Object obj:$eles) {
			Phone ph = Phone.class.cast(obj);
			$word.add(ph);
		}
	}
	;
	
word_element returns [Phone p]
	:	stress
	{
		$p = $stress.stressMarker;
	}
	|	phone
	{
		$p = $phone.phone;
	}
	|	pause
	{
		$p = $pause.pause;
	}
	|	syllable_boundary
	{
		$p = $syllable_boundary.syllableBoundary;
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
	
syllable_boundary returns [Phone syllableBoundary]
	:	PERIOD
	{
		$syllableBoundary = factory.createSyllableBoundary();
	}
	|	PLUS
	{
		// TODO implement compount words at the IPA level
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
phone returns [Phone phone]
	:	single_phone
	{	
		$phone = $single_phone.phone;
	}
	|	compound_phone
	{	
		$phone = $compound_phone.phone;
	}
	;
	
/**
 * A single (non-compound) phone.
 */
single_phone returns [BasicPhone phone]
	:	base_phone
	{	
		$phone = $base_phone.phone;	
	}
	|	complex_phone
	{	
		$phone = $complex_phone.phone;
	}
	;
	
/**
 * Phone + optional COMBINING diacritics
 */
base_phone returns [BasicPhone phone]
	:	initialToken=(CONSONANT|VOWEL|COVER_SYMBOL|GLIDE) diacritics+=COMBINING_DIACRITIC* len=phone_length?
	{
		Character basePhone = $initialToken.text.charAt(0);
		List<Character> combining = new ArrayList<Character>();
		if($diacritics != null) {
			for(int i = 0; i < $diacritics.size(); i++) {
				Token t = (Token)$diacritics.get(i);
				combining.add(t.getText().charAt(0));
			}
		}
		
		$phone = factory.createPhone(basePhone, combining.toArray(new Character[0]), 0.0f);
	}
	;
	
/**
 * Phone + optional COMBINING diacritics and
 * a prefix and/or suffix superscript diacritic.
 */
complex_phone returns [BasicPhone phone]
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
		Phone firstPhone = $sp1.phone;
		Phone secondPhone = $sp2.phone;
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
	
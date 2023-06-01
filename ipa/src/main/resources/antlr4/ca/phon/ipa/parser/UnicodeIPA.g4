/*
 * Copyright (C) 2012-2021 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
 * Grammar for parsing Unicode IPA transcriptions as a list of
 * discrete IPA elements.
 */
grammar UnicodeIPA;
// Lexer Rules are generated by transforming
// src/main/resources/ca/phon/ipa/parser/ipa.xml using src/main/resources/xml/xslt/ipa2lexer.xsl
// the resulting lexer grammar can be found at target/generated-sources/antlr4/imports/ipa.g4
// This happens during the 'generate-sources' phase of the build - see pom.xml for details
import ipa;

start
	: transcription? EOF
	;

transcription 
	: transcription word_boundary transcription
	| word
	;

word_boundary
	: WS    # WhiteSpace
	;

word
	: word compound_word_marker word    # CompoundWord
	| ipa_element+                      # SimpleWord
	| pause                             # WordPause
	;

ipa_element
	: stress
	| phone sctype?
	| syllable_boundary
	| sandhi
	| intra_word_pause
	| phonex_matcher_ref
	| alignment
	;
	
stress 
	: PRIMARY_STRESS    # PrimaryStress
	| SECONDARY_STRESS  # SecondaryStress
	;

phone
	: phone LIGATURE phone                                              # CompoundPhone
	| prefix_section? base_phone COMBINING_DIACRITIC* suffix_section?   # SinglePhone
	;
	
base_phone
	: CONSONANT 
	| VOWEL 
	| GLIDE 
	| COVER_SYMBOL
	;

prefix_section
	: prefix_diacritic+
	;
	
prefix_diacritic
	: PREFIX_DIACRITIC COMBINING_DIACRITIC*                 # PrefixDiacritic
	| SUFFIX_DIACRITIC ROLE_REVERSAL COMBINING_DIACRITIC*   # PrefixDiacriticRoleReversed
	| SUFFIX_DIACRITIC COMBINING_DIACRITIC* LIGATURE        # PrefixDiacriticLigature
	;

suffix_section
	: suffix_diacritic* phone_length? tone_number?
	;

tone_number
	:   TONE_NUMBER+
	;

suffix_diacritic
	: SUFFIX_DIACRITIC COMBINING_DIACRITIC*                 # SuffixDiacritic
	| PREFIX_DIACRITIC ROLE_REVERSAL COMBINING_DIACRITIC*   # SuffixDiacriticRoleReversed
	| LIGATURE PREFIX_DIACRITIC COMBINING_DIACRITIC*        # SuffixDiacriticLigature
	;

phone_length
	: HALF_LONG
	| LONG
	| LONG HALF_LONG
	| LONG LONG
	| LONG LONG HALF_LONG
	| LONG LONG LONG
	;

syllable_boundary
	: PERIOD        # SyllableBoundary
	| MINOR_GROUP   # MinorGroup
	| MAJOR_GROUP   # MajorGroup
	;

compound_word_marker
	: (PLUS | TILDE)          # CompoundWordMarker
	;

sandhi
	: SANDHI        # SandhiMarker
	;

intra_word_pause
	: INTRA_WORD_PAUSE
	;

pause
	: OPEN_PAREN pause_length CLOSE_PAREN
	;

pause_length
	: PERIOD                    # SimplePause
	| PERIOD PERIOD             # LongPause
	| PERIOD PERIOD PERIOD      # VeryLongPause
	| time_in_minutes_seconds   # NumericPause
	;

time_in_minutes_seconds
    :   (number COLON)? number PERIOD number?
    ;

number
    :   INT+
    ;

/* Other Items */
/**
 * Phonex matcher references are used in find and replace expressions.
 */
phonex_matcher_ref
	: prefix_section? BACKSLASH NUMBER COMBINING_DIACRITIC* suffix_section?         # GroupNumberRef
	| prefix_section? BACKSLASH GROUP_NAME COMBINING_DIACRITIC* suffix_section?    # GroupNameRef
	;

sctype
	: SC_TYPE
	;

alignment
	: ALIGNMENT
	;

INT
    :   [0-9]
    ;

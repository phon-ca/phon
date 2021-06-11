/*
 * Copyright (C) Gregory Hedlund & Yvan Rose
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

grammar Phonex;
import phonexipa;

expr
    :   baseexpr flags?
    ;

flags
    :   FORWARDSLASH LETTER+
    ;

baseexpr
    :   exprele+
    ;

exprele
    :   matcher
    |   group
    |   boundary_matcher
    ;

group
    :   OPEN_PAREN baseexpr (PIPE baseexpr)* CLOSE_PAREN quantifier?                        # CapturingGroup
    |   OPEN_PAREN NON_CAPTURING_GROUP baseexpr (PIPE baseexpr)* CLOSE_PAREN quantifier?    # NonCapturingGroup
    |   OPEN_PAREN group_name EQUAL_SIGN baseexpr (PIPE baseexpr)* CLOSE_PAREN quantifier?  # NamedGroup
    |   OPEN_PAREN LOOK_BEHIND_GROUP baseexpr CLOSE_PAREN quantifier?                       # LookBehindGroup
    |   OPEN_PAREN LOOK_AHEAD_GROUP baseexpr CLOSE_PAREN quantifier?                        # LookAheadGroup
    ;

group_name
    :   LETTER (LETTER | NUMBER)*
    ;

matcher
    :   base_matcher plugin_matcher* quantifier?                                            # SimpleMatcher
    |   back_reference plugin_matcher* quantifier?                                          # BackReference
    |   syllable_matcher syllable_bounds? plugin_matcher* quantifier?                       # SyllableMatcher
    ;

base_matcher
    :   single_phone_matcher                                                                # SinglePhoneMatcher
    |   compound_phone_matcher                                                              # CompoundPhoneMatcher
    ;

syllable_matcher
    :   SYLLABLE_CHAR
    ;

syllable_bounds
    :   FORWARDSLASH sctype FORWARDSLASH                                                    # SingleSyllableBounds
    |   FORWARDSLASH sctype SYLLABLE_BOUNDS_TO sctype FORWARDSLASH                          # FullSyllableBounds
    |   FORWARDSLASH sctype SYLLABLE_BOUNDS_TO FORWARDSLASH                                 # FromSyllableBounds
    |   FORWARDSLASH SYLLABLE_BOUNDS_TO sctype FORWARDSLASH                                 # ToSyllableBounds
    ;

compound_phone_matcher
    :   single_phone_matcher (UNDERSCORE | LIGATURE) single_phone_matcher
    ;

single_phone_matcher
    :   base_phone_matcher
    |   class_matcher
    |   feature_set_matcher
    |   predefined_phone_class
    |   regex_matcher
    |   hex_value
    |   escaped_char
    ;

base_phone_matcher
    :   LETTER
    ;

hex_value
    :   HEX_CHAR
    ;

escaped_char
    :   ESCAPED_PUNCT
    ;

class_matcher
    :   OPEN_BRACKET CARET? single_phone_matcher+ CLOSE_BRACKET
    ;

plugin_matcher
    :   COLON identifier OPEN_PAREN argument_list? CLOSE_PAREN                          # PluginMatcher
    |   COLON MINUS? sctype                                                             # ScTypePluginMatcher
    |   AMP single_phone_matcher                                                        # DiacriticMatcher
    |   EXC MINUS? stress_type                                                          # StressTypeMatcher
    |   LONG                                                                            # LongMatcher
    |   HALF_LONG                                                                       # HalflongMatcher
    ;

argument
    :   QUOTED_STRING
    |   SINGLE_QUOTED_STRING
    ;

argument_list
    :   argument ( COMMA argument)*
    ;

back_reference
    :   BACKSLASH MINUS? NUMBER
    ;

feature_set_matcher
    :   OPEN_BRACE ( negatable_identifier (COMMA negatable_identifier)* )? CLOSE_BRACE
    ;

identifier
    :   LETTER (LETTER | NUMBER)+
    ;

negatable_identifier
    :   MINUS? LETTER (LETTER | NUMBER)*
    ;

quantifier
    :   SINGLE_QUANTIFIER SINGLE_QUANTIFIER?                                            # SingleQuantifier
    |   bounded_quantifier                                                              # BoundedQuantifier
    ;

bounded_quantifier
    :   BOUND_START NUMBER BOUND_END                                                    # ExactBoundedQuantifier
    |   BOUND_START NUMBER COMMA BOUND_END                                              # AtLeastBoundedQuantifier
    |   BOUND_START COMMA NUMBER BOUND_END                                              # AtMostBoundedQuantifier
    |   BOUND_START NUMBER COMMA NUMBER BOUND_END                                       # BetweenBoundedQuantifier
    ;

predefined_phone_class
    :   PERIOD                                                                          # AnyElementClass
    |   ESCAPED_PHONE_CLASS                                                             # EscapedClass
    ;

boundary_matcher
    :   CARET
    |   DOLLAR_SIGN
    |   ESCAPED_BOUNDARY
    ;

stress_type
	:	LETTER | NUMBER
	;

sctype
	:	LETTER
	;

regex_matcher
    :   SINGLE_QUOTED_STRING
    |   QUOTED_STRING
    ;

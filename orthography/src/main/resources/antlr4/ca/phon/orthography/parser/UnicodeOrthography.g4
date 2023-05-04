/**
* ANTLR4 grammer for the CHAT transcription format
*
* This is a parser (non strict) for the main line of the
* <a href="https://talkbank.org/manuals/CHAT.html"/>CHAT Transcription Format</a>
*
* This parser is intended to be used as the parser for the Orthography tier type
* in <a href="https://www.phon.ca">Phon</a>.
*/
grammar UnicodeOrthography;

start
    :   orthography? EOF
    ;

orthography
    :   orthography word_boundary orthography
    |   orthoelement
    ;

orthoelement
    :   orthodata
    |   orthoannotation
    ;

orthodata
    :   linker
    |   complete_word
    |   group
    |   phonetic_group
    |   pause
    |   internal_media
    |   freecode
    |   event
    |   separator
    |   toneMarker
    |   tagMarker
    |   long_feature
    |   nonvocal
    |   terminator
    |   postcode
    |   quotation
    |   replacement
    ;

orthoannotation
    :   marker
    |   error
    |   overlap
    |   group_annotation
    |   duration
    ;

group
    :   LESS_THAN groupcontent GREATER_THAN
    ;

groupcontent
    :   groupcontent word_boundary groupcontent
    |   orthoelement
    ;

phonetic_group
    :   PG_START pgcontent PG_END
    ;

pgcontent
    :   pgcontent word_boundary pgcontent
    |   orthodata
    ;

word_boundary
    :   WS    # WhiteSpace
    ;

linker
    :   PLUS QUOTATION              // quoted utterance next
    |   PLUS CARET                  // quick uptake
    |   PLUS LESS_THAN              // lazy overlap mark
    |   PLUS COMMA                  // self completion
    |   PLUS PLUS                   // other completion
    |   PLUS '\u224b'               // technical break TCU completion
    |   PLUS '\u2248'               // no break TCU completion
    ;

tagMarker
    :   COMMA           // Comma
    |   DOUBLE_DAGGER   // Vocative
    |   DOUBLE_COMMA    // Tag
    ;

freecode
    :   FREECODE
    ;

terminator
    :   (EXCLAMATION|QUESTION|PERIOD)               // BasicTerminator
    |   PLUS PERIOD                                 // BrokenForCoding
    |   PLUS PERIOD PERIOD PERIOD                   // TrailOff
    |   PLUS PERIOD PERIOD QUESTION                 // TrailOffQuestion
    |   PLUS EXCLAMATION QUESTION                   // QuestionExclamation
    |   PLUS FORWARD_SLASH PERIOD                   // Interruption
    |   PLUS FORWARD_SLASH QUESTION                 // InterruptionQuestion
    |   PLUS FORWARD_SLASH FORWARD_SLASH PERIOD     // SelfInterruption
    |   PLUS FORWARD_SLASH FORWARD_SLASH QUESTION   // SelfInterruptionQuestion
    |   PLUS QUOTATION FORWARD_SLASH PERIOD         // QuotationNextLine
    |   PLUS QUOTATION PERIOD                       // QuotationPrecedes
    |   AMP ('\u224b' | '\u2248')                   // TCUContinuation
    ;

complete_word
    :   wordprefix? word langs? wordsuffix?
    ;

wordprefix
    :   ZERO
    |   AMP ( TILDE | MINUS | PLUS )
    ;

word
    :   word wk word    # CompoundWord
    |   wordelement+    # SingleWord
    ;

langs
    :   LANG_PREFIX                         # SecondaryLanguage
    |   LANG_PREFIX COLON language          # SingleLanguage
    |   LANG_PREFIX COLON ambig_lang_list   # AmbiguousLanguages
    |   LANG_PREFIX COLON multi_lang_list   # MultipleLanguages
    ;

ambig_lang_list
    :   ambig_lang_list AMP ambig_lang_list
    |   language
    ;

multi_lang_list
    :   multi_lang_list PLUS multi_lang_list
    |   language
    ;

language
    :   CHAR CHAR CHAR? (MINUS CHAR CHAR? CHAR? CHAR? CHAR? CHAR? CHAR? CHAR?)*
    ;

wordelement
    :   text
    |   ca_element
    |   ca_delimiter
    |   shortening
    |   prosody
    |   overlap_point
    ;

text
    :   (CHAR | APOSTROPHE | MINUS | UNDERSCORE)+
    ;

replacement
    :   REPLACEMENT
    ;

wk
    :   PLUS
    |   TILDE
    ;

ca_element
    :   CA_ELEMENT
    ;

ca_delimiter
    :   CA_DELIMITER
    ;

shortening
    :   OPEN_PAREN text CLOSE_PAREN
    ;

pause
    :   symbolic_pause
    |   numeric_pause
    ;

symbolic_pause
    :   SYMBOLIC_PAUSE
    ;

numeric_pause
    :   OPEN_PAREN time_in_minutes_seconds CLOSE_PAREN
    ;

time_in_minutes_seconds
    :   (digit+ COLON)? digit+ PERIOD digit*
    ;

overlap_point
    :   OVERLAP_POINT digit?
    ;

prosody
    :   COLON           // drawl
    |   CARET           // pause/blocking
    ;

wordsuffix
    :   formtype
    |   wordpos
    |   formtype wordpos
    ;

wordpos
    :   DOLLAR_SIGN CHAR+
    ;

formtype
    :   FORMTYPE
    |   HASH
    ;

internal_media
    :   BULLET mediasegment BULLET
    ;

mediasegment
    :   time_in_minutes_seconds MINUS time_in_minutes_seconds
    ;

separator
    :   COLON
    |   SEMICOLON
    |   CLAUSE_DELIMITER
    |   '\u221e'    // unmarked ending
    |   '\u2261'    // uptake
    ;

toneMarker
    :   TONE_MARKER
    ;

quotation
    :   QUOTATION_START
    |   QUOTATION_END
    ;

event
    :   ZERO                                                # Action
    |   AMP EQUALS id_or_basic_word                         # Happening
    |   AMP STAR id_or_basic_word EQUALS id_or_basic_word   # OtherSpokenEvent
    ;

long_feature
    :   LONG_FEATURE_START id_or_basic_word
    |   LONG_FEATURE_END id_or_basic_word
    ;

nonvocal
    :   LONG_NONVOCAL_START id_or_basic_word CLOSE_BRACE?
    |   LONG_NONVOCAL_END id_or_basic_word
    ;

id_or_basic_word
    :   id_or_basic_word (COLON | UNDERSCORE | MINUS ) id_or_basic_word
    |   CHAR+
    ;

marker
    :   OPEN_BRACKET EXCLAMATION CLOSE_BRACKET                                  // stressing
    |   OPEN_BRACKET EXCLAMATION EXCLAMATION CLOSE_BRACKET                      // contrasive stressing
    |   OPEN_BRACKET QUESTION CLOSE_BRACKET                                    // best guess
    |   OPEN_BRACKET FORWARD_SLASH CLOSE_BRACKET                                // retracing
    |   OPEN_BRACKET FORWARD_SLASH FORWARD_SLASH CLOSE_BRACKET                  // retracing with correction
    |   OPEN_BRACKET FORWARD_SLASH FORWARD_SLASH FORWARD_SLASH CLOSE_BRACKET    // retraction reformulation
    |   OPEN_BRACKET FORWARD_SLASH QUESTION CLOSE_BRACKET                       // retracing unclear
    |   OPEN_BRACKET FORWARD_SLASH MINUS CLOSE_BRACKET                          // false start
    |   MOR_EXCLUDE                                                             // mor exclude
    ;

error
    :   ERROR
    ;

group_annotation
    :   COMMENT             # Comment
    |   ALTERNATIVE         # Alternative
    |   EXPLANATION         # Explanation
    |   PARALINGUISTICS     # Paralinguistics
    ;

overlap
    :   OPEN_BRACKET LESS_THAN digit? CLOSE_BRACKET         // overlap preceeds
    |   OPEN_BRACKET GREATER_THAN digit? CLOSE_BRACKET      // overlap follows
    ;

duration
    :   OPEN_BRACKET HASH WS time_in_minutes_seconds CLOSE_BRACKET
    ;

postcode
    :   POSTCODE_START WS id_or_basic_word CLOSE_BRACKET
    ;

digit
    :   ZERO
    |   ONE_TO_NINE
    ;

// TOKENS
APOSTROPHE
    :   '\''
    ;

STAR
    :   '*'
    ;

BULLET
    :   '\u2022'
    ;

CARET
    :   '^'
    ;

CHAR
    :   [a-zA-Z]
    ;

COLON
    :   ':'
    ;

SEMICOLON
    :   ';'
    ;

CLAUSE_DELIMITER
    :   '[c]'
    ;

MOR_EXCLUDE
    :   '[e]'
    ;

LONG_FEATURE_START
    :   '&{l='
    ;

LONG_FEATURE_END
    :   '&}l='
    ;

LONG_NONVOCAL_START
    :   '&{n='
    ;

LONG_NONVOCAL_END
    :   '&}n='
    ;

COMMA
    :   ','
    ;

DOUBLE_COMMA
    :   '„'
    ;

DOUBLE_DAGGER
    :   '‡'
    ;

FREECODE
    :   OPEN_BRACKET CARET WS ( '\\]' | '\\[' | . )*? CLOSE_BRACKET
    ;

ERROR
    :   OPEN_BRACKET STAR WS ( '\\]' | '\\[' | .)*? CLOSE_BRACKET
    ;


COMMENT
    :   OPEN_BRACKET PERCENT WS ( '\\]' | '\\[' | .)*? CLOSE_BRACKET
    ;

REPLACEMENT
    :   '[:' COLON? WS ( '\\]' | '\\[' | .)*? CLOSE_BRACKET
    ;

ALTERNATIVE
    :   OPEN_BRACKET EQUALS QUESTION ( '\\]' | '\\[' | .)*? CLOSE_BRACKET
    ;

PARALINGUISTICS
    :   OPEN_BRACKET EQUALS EXCLAMATION ( '\\]' | '\\[' | .)*? CLOSE_BRACKET
    ;

EXPLANATION
    :   OPEN_BRACKET EQUALS ( '\\]' | '\\[' | .)*? CLOSE_BRACKET
    ;

POSTCODE_START
    :   '[+'
    ;

PERCENT
    :   '%'
    ;

FORWARD_SLASH
    :   '/'
    ;

QUOTATION
    :   '"'
    ;

LESS_THAN
    :   '<'
    ;

GREATER_THAN
    :   '>'
    ;

OPEN_BRACKET
    :   '['
    ;

CLOSE_BRACKET
    :   ']'
    ;

OPEN_BRACE
    :   '{'
    ;

CLOSE_BRACE
    :   '}'
    ;

EQUALS
    :   '='
    ;

OPEN_PAREN
    :   '('
    ;

CLOSE_PAREN
    :   ')'
    ;

AT
    :   '@'
    ;

ZERO
    :   '0'
    ;

ONE_TO_NINE
    :   '1'..'9'
    ;

AMP
    :   '&'
    ;

TILDE
    :   '~'
    ;

MINUS
    :   '-'
    ;

PLUS
    :   '+'
    ;

DOLLAR_SIGN
    :   '$'
    ;

EXCLAMATION
    :   '!'
    ;

QUESTION
    :   '?'
    ;

PERIOD
    :   '.'
    ;

HASH
    :   '#'
    ;

UNDERSCORE
    :   '_'
    ;

PG_START
    :   '\u2039'
    ;

PG_END
    :   '\u203a'
    ;

QUOTATION_START
    :   '\u201c'
    ;

QUOTATION_END
    :   '\u201d'
    ;

LANG_PREFIX
    :   '@s'
    ;

FORMTYPE
    :   AT ('a'|'b'|'c'|'d'|'e'|'f'|'fp'|'fs'|'g'
           |'i'|'k'|'l'|'n'|'nv'|'o'|'p'|'q'|'sas'
           |'si'|'sl'|'t'|'u'|'x'|'wp'|'z')
    ;

TONE_MARKER
    :   '\u21d7'        // rising to high
    |   '\u2197'        // rising to mid
    |   '\u2192'        // level
    |   '\u2198'        // falling to mid
    |   '\u21d8'        // falling to low
    ;

CA_ELEMENT
    :   '\u2260'        // blocked segments
    |   '\u223e'        // constriction
    |   '\u2219'        // inhalation
    |   '\u1f29'        // laugh in word
    |   '\u2193'        // pitch down
    |   '\u21bb'        // pitch reset
    |   '\u2191'        // pitch up
    |   '\u02c8'        // primary stress
    |   '\u02cc'        // secondary stress
    ;

CA_DELIMITER
    :   '\u264b'        // breathy voice
    |   '\u204e'        // creaky
    |   '\u2206'        // faster
    |   '\u2594'        // high-pitch
    |   '\u25c9'        // louder
    |   '\u2581'        // low-pitch
    |   '\u00a7'        // precise
    |   '\u21ab'        // repeated-segment
    |   '\u222e'        // singing
    |   '\u2207'        // slower
    |   '\u26ea'        // smile voice
    |   '\u00b0'        // softer
    |   '\u2047'        // unsure
    |   '\u222c'        // whisper
    |   '\u03ab'        // yawn
    ;

OVERLAP_POINT
    :   '⌈'
    |   '⌉'
    |   '⌊'
    |   '⌋'
    ;

SYMBOLIC_PAUSE
    :   OPEN_PAREN PERIOD CLOSE_PAREN
    |   OPEN_PAREN PERIOD PERIOD CLOSE_PAREN
    |   OPEN_PAREN PERIOD PERIOD PERIOD CLOSE_PAREN
    ;

WS
    :   [ \t\n]
    ;

grammar UnicodeOrthography;

start
    :   orthography? EOF
    ;

orthography
    :   orthography word_boundary orthography
    |   orthoelement
    ;

orthoelement
    :   linker
    |   complete_word
    |   terminator
    |   tagMarker
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
    :   wordprefix? word wordsuffix?
    ;

wordprefix
    :   ZERO
    |   AMP ( TILDE | MINUS | PLUS )
    ;

word
    :   word wk word    # CompoundWord
    |   wordelement+    # SingleWord
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
    :   (CHAR | APOSTROPHE)+
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
    :   OPEN_PAREN CHAR+ CLOSE_PAREN
    ;

overlap_point
    :   OVERLAP_POINT DIGIT?
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

// tokens
APOSTROPHE
    :   '\''
    ;

CHAR
    :   [a-zA-Z]
    ;

FORWARD_SLASH
    :   '/'
    ;

QUOTATION
    :   '"'
    ;

CARET
    :   '^'
    ;


LESS_THAN
    :   '<'
    ;

COLON
    :   ':'
    ;

COMMA
    :   ','
    ;

DOUBLE_DAGGER
    :   '‡'
    ;

DOUBLE_COMMA
    :   '„'
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

DIGIT
    :   ZERO
    |   '1'..'9'
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

FORMTYPE
    :   AT ('a'|'b'|'c'|'d'|'e'|'f'|'fp'|'fs'|'g'
           |'i'|'k'|'l'|'n'|'nv'|'o'|'p'|'q'|'sas'
           |'si'|'sl'|'t'|'u'|'x'|'wp'|'z')
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

WS
    :   [ \t\n]
    ;

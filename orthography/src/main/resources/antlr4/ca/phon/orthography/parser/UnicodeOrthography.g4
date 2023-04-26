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
    |   word
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

word
    :   wordprefix? wordelement+ wordsuffix?
    ;

wordprefix
    :   ZERO
    |   AMP ( TILDE | MINUS | PLUS )
    ;

wordelement
    :   CHAR
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

WS
    :   [ \t\n]
    ;

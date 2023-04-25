grammar UnicodeOrthography;

start
    :   orthography? EOF
    ;

orthography
    :   orthography word_boundary orthography
    |   orthoelement
    ;

orthoelement
    :   word
    |   terminator
    |   tagMarker
    ;

word_boundary
    :   WS    # WhiteSpace
    ;

tagMarker
    :   COMMA
    |   DOUBLE_DAGGER
    |   DOUBLE_COMMA
    ;

terminator
    :   EXCLAMATION
    |   QUESTION
    |   PERIOD
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

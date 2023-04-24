orthography
    :   orthoelement*
    ;

orthoelement
    :   linker
    |   word
    |   event
    |   terminator
    |   tagMarker
    |   error
    ;

linker
    :   OPEN_PAREN CLOSE_PAREN
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
    |   ca-delimiter
    |   ca-element
    ;

wordsuffix
    :   formtype? ( DOLLAR_SIGN
grammar TierData;

usertier
    :   tierdata* EOF
    ;

tierdata
    :   tierdata WS+ tierdata
    |   element
    ;

element
    :   word
    |   comment
    |   internal_media
    |   link
    ;

word
    :   (CHAR | DIGIT | MINUS | COLON | PERIOD | '[' | ']')+
    ;

comment
    :   BEGIN_COMMENT WS? ('\\]' | .)*? END_BRACKET
    ;

internal_media
    :   BULLET time_in_minutes_seconds (MINUS time_in_minutes_seconds)? BULLET
    ;

label
    :   CHAR (CHAR | DIGIT)*
    ;

link
    :   LINK WS? (label WS)? href END_BRACKET
    ;

href
    :   ('\\]' | .)+?
    ;

time_in_minutes_seconds
    :   (number COLON)? number PERIOD number?
    ;

number
    :   DIGIT+
    ;

DIGIT
    :   '0'..'9'
    ;

COLON
    :   ':'
    ;

PERIOD
    :   '.'
    ;

MINUS
    :   '-'
    ;

WS
    :   [ \t\r\n]+
    ;

BULLET
    :   '\u2022'
    ;

BEGIN_COMMENT
    :   '[%'
    ;

END_BRACKET
    :   ']'
    ;


LINK
    :   '[\uD83D\uDD17'
    ;

CHAR
    :   .
    ;
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
    :   (CHAR | DIGIT | MINUS | COLON | PERIOD | END_COMMENT)+
    ;

comment
    :   BEGIN_COMMENT WS? ('\\]' | .)*? END_COMMENT
    ;

internal_media
    :   BULLET time_in_minutes_seconds MINUS time_in_minutes_seconds BULLET
    ;

label
    :   CHAR (CHAR | DIGIT)*
    ;

link
    :   LINK (label COLON)? .+? LINK
    ;

time_in_minutes_seconds
    :   (number COLON)? number PERIOD number?
    ;

number
    :   DIGIT+
    ;

CHAR
    :   [!"#$%&'()*+,/;<=>?@A-Z[\\^_`a-z{|}~]
    |   [\u00bf-\u024f]
    |   [\u0250-\u02af]
    |   [\u02b0-\u02ff]
    |   [\u0300-\u036f]
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
    :   [ \t\r\n]
    ;

BULLET
    :   '\u2022'
    ;

BEGIN_COMMENT
    :   '[%'
    ;

END_COMMENT
    :   ']'
    ;

LINK
    :   '\uD83D\uDD17'
    ;

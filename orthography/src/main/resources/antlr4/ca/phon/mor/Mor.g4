grammar Mor;

start
    :    mortier? EOF
    ;

mortier
    :   mortier WS mor
    |   mor
    ;

mor
    :   morprefix* morelement translations? morpost*
    ;

morprefix
    :   morelement translations? DOLLAR_SIGN
    ;

morelement
    :   mw
    |   mwc
    |   mt
    ;

mw
    :   (prefix HASH)* pos PIPE stem marker*
    ;

prefix
    :   string
    ;

pos
    :   category (COLON subcategory)*
    ;

category
    :   string
    ;

subcategory
    :   string
    ;

stem
    :   string
    ;

marker
    :   AMP fusionalsuffix
    |   HYPHEN suffix
    |   COLON category
    ;

fusionalsuffix
    :   string
    ;

suffix
    :   string
    ;

translations
    :   EQUALS translation (FORWARD_SLASH translation)*
    ;

translation
    :   string (HYPHEN string)*
    ;

string
    :   STRING
    ;

mwc
    :   (prefix HASH)* pos PIPE (PLUS mw)+
    ;

mt
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
   |   '\u224b'                                    // TechnicalBreakTCUContinuation
   |   '\u2248'                                    // TCUContinuation
   ;

morpost
    :   TILDE morelement translations?
    ;

PLUS
    :   '+'
    ;

PERIOD
    :   '.'
    ;

QUESTION
    :   '?'
    ;

FORWARD_SLASH
    :   '/'
    ;

QUOTATION
    :   '"'
    ;

EXCLAMATION
    :   '!'
    ;

WS
    :   [ \t\r\n]+
    ;

PIPE
    :   '|'
    ;

COLON
    :   ':'
    ;

EQUALS
    :   '='
    ;

AMP
    :   '&'
    ;

HASH
    :   '#'
    ;

HYPHEN
    :   '-'
    ;

TILDE
    :   '~'
    ;

DOLLAR_SIGN
    :   '$'
    ;

STRING
    :   CHAR+
    ;

fragment CHAR
    :   ~('#'|'|'|'&'|'$'|'-'|'='|':'|'~'|'+'|'/'|[ \t\r\n])
    ;

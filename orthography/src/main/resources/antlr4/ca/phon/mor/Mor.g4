grammar Mor;

start
    :    mortier? EOF;

mortier
    :   mortier WS mor
    |   mor
    ;

mor
    :   morelement (morpost)*
    ;

morelement
    :   mw
    |   mt
    ;

mw
    :   (prefix HASH)* pos PIPE stem (marker)*
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
    |   EQUALS translation
    ;

fusionalsuffix
    :   string
    ;

suffix
    :   string
    ;

translation
    :   string
    ;

string
    :   STRING
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
    :   TILDE morelement
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

STRING
    :   TEXT+
    ;

fragment TEXT
    :   ~('#'|'|'|'&'|'-'|'='|':'|'~'|'+'|[ \t\r\n])
    ;

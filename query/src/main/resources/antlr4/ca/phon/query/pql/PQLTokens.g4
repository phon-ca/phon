lexer grammar PQLTokens;

// symbols
BACKSLASH: '\\';

CLOSE_BRACE: '}';

CLOSE_PAREN: ')';

COMMA: ',';

DOT: '.';

HASH: '#';

OPEN_BRACE: '{';

OPEN_PAREN: '(';

RANGE_OP: '..';

STAR: '*';

// ignore whitespace
WS: [ \t\n] -> skip;

// strings
fragment
HEX_LETTER
	:   'a'..'f'
	|   'A'..'F'
	|   '0'..'9'
	;

HEX_CHAR
	:   BACKSLASH 'u' HEX_LETTER HEX_LETTER HEX_LETTER HEX_LETTER
	;

ESC_SEQ
	:   BACKSLASH ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
	;

QUOTED_STRING
	:  '"' ( ESC_SEQ | HEX_CHAR | ~('"') )*? '"'
	;

SLASHED_ESC_SEQ
	:   BACKSLASH ('/')
	;

SLASHED_STRING
	:   '/' ( SLASHED_ESC_SEQ | HEX_CHAR | ~('/') )*? '/'
	;

// tiers
ORTHOGRAPHY: O R T H O G R A P H Y;

IPATARGET: I P A T A R G E T;

IPAACTUAL: I P A A C T U A L;

ALIGNMENT: A L I G N M E N T;

SEGMENT: S E G M E N T;

NOTES: N O T E S;

// words
AND: A N D;

AFTER: A F T E R;

AGE: A G E;

ALIGNED: A L I G N E D;

BEFORE: B E F O R E;

BIRTHDAY: B I R T H D A Y;

BY: B Y;

CONTAINS: C O N T A I N S;

COUNT: C O U N T;

CV: C V;

DATE: D A T E;

EDUCATION: E D U C A T I O N;

EQUALS: E Q U A L S;

FINAL: F I N A L;

FIND: F I N D;

FROM: F R O M;

GREATER: G R E A T E R;

GROUP: G R O U P;

ID: I D;

IN: I N;

INCLUDE: I N C L U D E;

INITIAL: I N I T I A L;

LANGUAGE: L A N G U A G E;

LESS: L E S S;

MEDIAL: M E D I A L;

NAME: N A M E;

NOT: N O T;

NUMBER: N U M B E R;

OR: O R;

PATTERN: P A T T E R N;

PHONES: P H O N E S;

PHONEX: P H O N E X;

POSITION: P O S I T I O N;

PRIMARY: P R I M A R Y;

PROJECT: P R O J E C T;

REGEX: R E G E X;

ROLE: R O L E;

SECONDARY: S E C O N D A R Y;

SELECT: S E L E C T;

SES: S E S;

SEX: S E X;

SHAPE: S H A P E;

SESSION: S E S S I O N;

SPEAKER: S P E A K E R;

STRESS: S T R E S S;

SYLLABLE: S Y L L A B L E;

THAN: T H A N;

THEN: T H E N;

TONE: T O N E;

UNSTRESSED: U N S T R E S S E D;

WHERE: W H E R E;

WORD: W O R D;

// word fragments
// allows for case-insensitive token parsing
fragment A: [Aa];
fragment B: [Bb];
fragment C: [Cc];
fragment D: [Dd];
fragment E: [Ee];
fragment F: [Ff];
fragment G: [Gg];
fragment H: [Hh];
fragment I: [Ii];
fragment J: [Jj];
fragment K: [Kk];
fragment L: [Ll];
fragment M: [Mm];
fragment N: [Nn];
fragment O: [Oo];
fragment P: [Pp];
fragment Q: [Qq];
fragment R: [Rr];
fragment S: [Ss];
fragment T: [Tt];
fragment U: [Uu];
fragment V: [Vv];
fragment W: [Ww];
fragment X: [Xx];
fragment Y: [Yy];
fragment Z: [Zz];

/*
 * An SQL-like query langauge for Phon
 */
grammar PQL;

start
	:   query EOF
	;

query
	:   find_query
	|   select_query
	;

find_query
	:   FIND expr IN select_query
	;

select_query
	:   SELECT tier_list search_by? (FROM query_context)? filter_blocks includes
	;

search_by
	:   BY (GROUP|WORD) (THEN BY SYLLABLE)?
	;

expr
	:   QUOTED_STRING                   # PlainTextExpr
	|   PHONEX SLASHED_STRING           # PhonexExpr
	|   REGEX SLASHED_STRING            # RegexExpr
	|   WORD SHAPE QUOTED_STRING        # WordShapeExpr
	|   CV PATTERN QUOTED_STRING        # CVPatternExpr
	;

tier_list
	:   QUOTED_STRING (COMMA QUOTED_STRING)*    # TierList
	|   STAR                                    # AllTiers
	;

query_context
	:   session_list
	|   record_list
	;

session_list
	:   session_name (COMMA session_name)   # SessionList
	|   STAR DOT STAR                       # AllProjectSessions
	;

session_name
	:   QUOTED_STRING (HASH record_list HASH)?
	;

record_list
	:   STAR                                        # AllRecords
	|   record_or_range (COMMA record_or_range)*    # RecordList
	;

record_or_range
	:   INT                 # RecordNumber
	|   INT RANGE_OP INT    # RecordRange
	;

filter_blocks
	:   filter_block*
	;

filter_block
	:   where_speaker
	|   where_group
	|   where_word
	|   where_syllable
	|   where_session
	|   where_project
	;

containsOrEquals
	:   CONTAINS
	|   EQUALS
	;

where_project
	:   WHERE PROJECT OPEN_BRACE where_project_stmt CLOSE_BRACE
	;

where_project_stmt
	:   NAME NOT? containsOrEquals expr
	;

where_session
	:   WHERE SESSION OPEN_BRACE where_session_stmt CLOSE_BRACE
	;

where_session_stmt
	:   NAME NOT? containsOrEquals expr
	|   DATE NOT? (AFTER|BEFORE) date
	|   where_session_stmt (AND|OR) where_session_stmt
	;

where_speaker
	:   WHERE SPEAKER OPEN_BRACE where_speaker_stmt CLOSE_BRACE
	;

where_group
	:   WHERE GROUP OPEN_BRACE where_group_stmt CLOSE_BRACE
	;

where_word
	:   WHERE WORD OPEN_BRACE where_word_stmt CLOSE_BRACE
	;

where_syllable
	:   WHERE SYLLABLE OPEN_BRACE where_syllable_stmt CLOSE_BRACE
	;

includes
	:   include_stmt*
	;

include_stmt
	:   INCLUDE ALIGNED PHONES      # IncludeAlignedPhones
	|   INCLUDE EXCLUDED RECORDS    # IncludeExcludedRecords
	|   INCLUDE tier_list           # IncludeTiers
	|   INCLUDE function            # IncludeFunction
	;

function
	:   ppc
	|   pmlu
	|   dist
	;

ppc
	:   PPC empty_param_list
	;

pmlu
	:   PMLU OPEN_PAREN number CLOSE_PAREN
	;

dist
	:   DIST empty_param_list
	;

empty_param_list
	:   OPEN_PAREN CLOSE_PAREN
	;

number
	:   DIGIT+ (PERIOD DIGIT+)
	;

// TOKENS
COMMA: ',';

SHAPE: S H A P E;

PATTERN: P A T T E R N;

CV: C V;

PHONEX: P H O N E X;

REGEX: R E G E X;

FIND: F I N D;

IN: I N;

WHERE: W H E R E;

IS: I S;

SELECT: S E L E C T;

FROM: F R O M;

GROUP: G R O U P;

WORD: W O R D;

SYLLABLE: S Y L L A B L E;

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
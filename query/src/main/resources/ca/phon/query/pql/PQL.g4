/*
 * A query langauge for Phon projects/sessions
 *
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
	:   plain_text_expr
	|   ipa_expr
	;

plain_text_expr
	:   QUOTED_STRING                   # PlainTextExpr
	|   REGEX SLASHED_STRING            # RegexExpr
	;

ipa_expr
	:   PHONEX SLASHED_STRING                       # PhonexExpr
	|   (WORD SHAPE|STRESS PATTERN) QUOTED_STRING   # WordShapeExpr
	|   CV PATTERN QUOTED_STRING                    # CVPatternExpr
	;

tier_list
	:   tier_name (COMMA tier_name)*    # TierList
	|   STAR                            # AllTiers
	;

tier_name
	:   ORTHOGRAPHY
	|   IPATARGET
	|   IPAACTUAL
	|   ALIGNMENT
	|   NOTES
	|   QUOTED_STRING
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
	:   integer                     # RecordNumber
	|   integer RANGE_OP integer    # RecordRange
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

contains_or_equals
	:   CONTAINS
	|   EQUALS
	;

where_project
	:   WHERE PROJECT OPEN_BRACE where_project_stmt CLOSE_BRACE
	;

where_project_stmt
	:   where_project_or_stmt
	;

where_project_or_stmt
	:   where_project_and_stmt (OR where_project_and_stmt)*
	;

where_project_and_stmt
	:   where_project_unary_stmt (AND where_project_unary_stmt)*
	;

where_project_unary_stmt
	:   NAME NOT? contains_or_equals plain_text_expr
	|   OPEN_PAREN where_project_stmt CLOSE_PAREN
	;

where_session
	:   WHERE SESSION OPEN_BRACE where_session_stmt CLOSE_BRACE
	;

where_session_stmt
	:   where_session_or_stmt
	;

where_session_or_stmt
	:   where_session_and_stmt (OR where_session_and_stmt)*
	;

where_session_and_stmt
	:   where_session_unary_stmt (AND where_session_unary_stmt)*
	;

where_session_unary_stmt
	:   NAME NOT? contains_or_equals plain_text_expr
	|   DATE NOT? (AFTER|BEFORE|EQUALS) date_expr
	|   OPEN_PAREN where_session_stmt CLOSE_PAREN
	;

where_speaker
	:   WHERE SPEAKER OPEN_BRACE where_speaker_stmt CLOSE_BRACE
	;

where_speaker_stmt
	:   where_speaker_or_stmt
	;

where_speaker_or_stmt
	:   where_speaker_and_stmt (OR where_speaker_and_stmt)*
	;

where_speaker_and_stmt
	:   where_speaker_unary_stmt (AND where_speaker_unary_stmt)*
	;

where_speaker_unary_stmt
	:   ID NOT? contains_or_equals plain_text_expr
	|   ROLE NOT? IS QUOTED_STRING
	|   NAME NOT? contains_or_equals plain_text_expr
	|   AGE NOT? (EQUALS|GREATER THAN|LESS THAN) period_expr
	|   BIRTHDAY NOT? (EQUALS|BEFORE|AFTER) date_expr
	|   SEX NOT? IS (MALE|FEMALE)
	|   LANGUAGE NOT? contains_or_equals plain_text_expr
	|   EDUCATION NOT? contains_or_equals plain_text_expr
	|   SES NOT? contains_or_equals plain_text_expr
	|   OPEN_PAREN where_speaker_stmt CLOSE_PAREN
	;

where_group
	:   WHERE GROUP OPEN_BRACE where_group_stmt CLOSE_BRACE
	;

where_group_stmt
	:   where_group_or_stmt
	;

where_group_or_stmt
	:   where_group_and_stmt (OR where_group_and_stmt)*
	;

where_group_and_stmt
	:   where_group_unary_stmt (AND where_group_unary_stmt)*
	;

where_group_unary_stmt
	:   tier_name? contains_or_equals expr
	|   tier_name? WORD COUNT NOT? (EQUALS|GREATER THAN|LESS THAN) integer
	|   SEGMENT LENGTH NOT? (EQUALS|GREATER THAN|LESS THAN) (number | period_expr)
	|   SEGMENT START NOT? (EQUALS|GREATER THAN|LESS THAN) (number | period_expr)
	|   SEGMENT END NOT? (EQUALS|GREATER THAN|LESS THAN) (number | period_expr)
	|   OPEN_PAREN where_group_stmt CLOSE_PAREN
	;

where_word
	:   WHERE WORD OPEN_BRACE where_word_stmt CLOSE_BRACE
	;

where_word_stmt
	:   where_word_or_stmt
	;

where_word_or_stmt
	:   where_word_and_stmt (OR where_word_and_stmt)*
	;

where_word_and_stmt
	:   where_word_unary_stmt (AND where_word_unary_stmt)*
	;

where_word_unary_stmt
	:   tier_name? contains_or_equals expr
	|   tier_name? SYLLABLE COUNT NOT? (EQUALS|GREATER THAN|LESS THAN) number
	|   tier_name? POSITION NOT? EQUALS (INITIAL|MEDIAL|FINAL|tier_name)
	|   OPEN_PAREN where_word_stmt CLOSE_PAREN
	;

where_syllable
	:   WHERE SYLLABLE OPEN_BRACE where_syllable_stmt CLOSE_BRACE
	;

where_syllable_stmt
	:   where_syllable_or_stmt
	;

where_syllable_or_stmt
	:   where_syllable_and_stmt (OR where_syllable_and_stmt)*
	;

where_syllable_and_stmt
	:   where_syllable_unary_stmt (AND where_syllable_unary_stmt)*
	;

where_syllable_unary_stmt
	:   tier_name? contains_or_equals expr
	|   tier_name? STRESS NOT? EQUALS (PRIMARY|SECONDARY|UNSTRESSED|ALIGNED)
	|   tier_name? TONE NUMBER NOT? EQUALS (number | ALIGNED)
	|   tier_name? POSITION NOT? EQUALS (INITIAL|MEDIAL|FINAL|ALIGNED)
	|   OPEN_PAREN where_syllable_stmt CLOSE_PAREN
	;

period_expr
	:   QUOTED_STRING
	;

date_expr
	:   QUOTED_STRING
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
	:   PPC OPEN_PAREN CLOSE_PAREN
	;

pmlu
	:   PMLU OPEN_PAREN number CLOSE_PAREN
	;

dist
	:   DIST OPEN_PAREN CLOSE_PAREN
	;

param_list
	:   OPEN_PAREN param_value (COMMA param_value)* CLOSE_PAREN
	;

param_value
	:   number
	|   QUOTED_STRING
	;

integer
	:   DIGIT+
	;

number
	:   DIGIT+ (PERIOD DIGIT+)
	;

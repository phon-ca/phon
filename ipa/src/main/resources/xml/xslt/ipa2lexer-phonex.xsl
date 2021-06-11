<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
				xmlns:fn="http://www.w3.org/2005/xpath-functions"
				xmlns:ipa="https://phon.ca/ns/ipa" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:fs="https://phon.ca/ns/features"
				exclude-result-prefixes="#all">

	<xsl:output encoding="UTF-8" method="text"/>

	<xsl:variable name="root" select="fn:root()"/>

	<xsl:template match="/">/*
* Copyright (C) Gregory Hedlund &amp; Yvan Rose - All rights reserved
* Unauthorized copying of this file, via any medium is strictly prohibited
* Proprietary and confidential
*/
lexer grammar phonexipa;

<xsl:for-each select="fn:distinct-values(ipa:ipa/ipa:char/ipa:token)">
<xsl:variable name="tokenType" select="."/>
<xsl:choose>
<xsl:when test="$tokenType = 'BACKSLASH'">
BACKSLASH
	: '\\'
;

FORWARDSLASH
	:	'/'
	;

</xsl:when>
<xsl:when test="$tokenType = 'SPACE'"></xsl:when>
<xsl:when test="$tokenType = 'MINOR_GROUP'">
PIPE
	:	'|'
	;
</xsl:when>
<xsl:when test="$tokenType = 'INTRA_WORD_PAUSE'">
CARET
	:	'^'
	;

</xsl:when>
<xsl:when test="$tokenType = 'PLUS'">
fragment
ZERO_OR_ONE
	:	'?'
	;

fragment
ZERO_OR_MORE
	:	'*'
	;

fragment
ONE_OR_MORE
	:	'+'
	;

SINGLE_QUANTIFIER
	:	ZERO_OR_ONE
	|	ZERO_OR_MORE
	|	ONE_OR_MORE
	;
</xsl:when>
<xsl:otherwise>
<xsl:choose>
<xsl:when test="$tokenType = 'DIGIT'">
NUMBER
	: DIGIT+
;

fragment
</xsl:when>
<xsl:when test="$tokenType = 'ALIGNMENT'">
fragment
</xsl:when>
<xsl:when test="$tokenType = 'COVER_SYMBOL'">
fragment
</xsl:when>
<xsl:when test="$tokenType = 'CONSONANT'">
fragment
</xsl:when>
<xsl:when test="$tokenType = 'GLIDE'">
fragment
</xsl:when>
<xsl:when test="$tokenType = 'VOWEL'">
fragment
</xsl:when>
</xsl:choose>
<xsl:value-of select="$tokenType"/> <xsl:apply-templates select="$root/ipa:ipa/ipa:char[ipa:token=$tokenType]"/>
;
	</xsl:otherwise>
</xsl:choose>
</xsl:for-each>

ESCAPED_PHONE_CLASS
	:	BACKSLASH ('c'|'v'|'g'|'p'|'P'|'w'|'W'|'s')
	;

ESCAPED_PUNCT
	:	BACKSLASH ('.'|'*'|CARET)
	;

ESCAPED_BOUNDARY
	:	BACKSLASH ('b'|'S')
	;

UNDERSCORE
	: 	'_'
	;

EXC
	:	'!'
	;

AMP
	:	'&amp;'
	;

MINUS
	: '-'
	;

SYLLABLE_CHAR
	: '\u03C3'
	;

SYLLABLE_BOUNDS_TO
	:	'..'
	;

NON_CAPTURING_GROUP
	:   '?='
	;

LOOK_BEHIND_GROUP
	:   '?&lt;'
	;

LOOK_AHEAD_GROUP
	:   '?&gt;'
	;

OPEN_BRACKET
	:	'['
	;

CLOSE_BRACKET
	:	']'
	;

BOUND_START
	:	'&lt;'
	;

BOUND_END
	:	'&gt;'
	;

DOLLAR_SIGN
	:	'$'
	;

EQUAL_SIGN
	:	'='
	;

COMMA
	:	','
	;

LETTER
	:	COVER_SYMBOL
	|	CONSONANT
	|	GLIDE
	|	VOWEL
	|	ALIGNMENT
	;

fragment
HEX_LETTER
	:   'a'..'f'
	|   'A'..'F'
	|   '0'..'9'
	;

HEX_CHAR
	:   BACKSLASH 'u' HEX_LETTER HEX_LETTER HEX_LETTER HEX_LETTER
	;

WS
	:	[ \t\r\n]+
		-> skip
	;

COMMENT
	:	'/*' .*? '*/'
		-> channel(2)
	;

EOL_COMMENT
	:	'//' .*? '\n'
		-> channel(2)
	;

fragment
ESC_SEQ
	:   BACKSLASH ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
	;

QUOTED_STRING
	:  '"' ( ESC_SEQ | HEX_CHAR | ~('"') )*? '"'
	;

SINGLE_QUOTED_STRING
	:	'\'' ( ESC_SEQ | HEX_CHAR | ~('\'') )*? '\''
	;

	</xsl:template>

	<xsl:template match="ipa:char">
	<xsl:choose>
		<xsl:when test="position() = 1">
	: '<xsl:value-of select="@value"/>'
		</xsl:when>
		<xsl:otherwise>
	| '<xsl:value-of select="@value"/>'
		</xsl:otherwise>
	</xsl:choose>
	</xsl:template>

</xsl:stylesheet>

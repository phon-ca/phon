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
* Copyright (C) 2012-2021 Gregory Hedlund &amp; Yvan Rose
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*    http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
lexer grammar ipa;

SC_TYPE
: COLON [lLoOnNdDcCrReEuU]
;

GROUP_NAME
: OPEN_BRACE [a-zA-Z][a-zA-Z0-9_]* CLOSE_BRACE
;

WS
: [ \t\r\n]+
;

<xsl:for-each select="fn:distinct-values(ipa:ipa/ipa:char/ipa:token)">
<xsl:variable name="tokenType" select="."/>
<xsl:choose>
	<xsl:when test="$tokenType = 'BACKSLASH'">
BACKSLASH
	: '\\'
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
<xsl:when test="$tokenType = 'COLON'">
fragment
</xsl:when>
</xsl:choose>
<xsl:value-of select="$tokenType"/> <xsl:apply-templates select="$root/ipa:ipa/ipa:char[ipa:token=$tokenType]"/>
;
	</xsl:otherwise>
</xsl:choose>
</xsl:for-each>
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

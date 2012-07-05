<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:param name="grammar.name" required="yes"/>
    <xsl:param name="grammar.pkg" required="yes"/>
    
    <!-- 
        Convert an xml-schema into an ANTLR3 parser grammar.
    -->
    <xsl:output method="text" encoding="UTF-8" indent="no"/>
    
    <xsl:template match="/">
        <!-- Parser -->
grammar <xsl:value-of select="$grammar.name"/>;

options {
    tokenVocab="<xsl:value-of select="$grammar.name"/>";
    output=AST;
}

@header {
package <xsl:value-of select="$grammar.pkg"/>;
}

<xsl:apply-templates select="/xs:schema/xs:element"/>
    </xsl:template>
    
    <xsl:template match="xs:element">
        <xsl:param name="rule.name" select="upper-case(@name)"/>
        <xsl:param name="ele.type" select="@type"/>
<xsl:value-of select="$rule.name"/>    :<xsl:apply-templates select="xs:attribute"><xsl:with-param name="parent.name" select="$rule.name"/></xsl:apply-templates>;
        <xsl:apply-templates select="/xs:schema/xs:complexType[@name=$ele.type]/xs:attribute">
            <xsl:with-param name="parent.name" select="$rule.name"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="xs:attribute">
        <xsl:param name="attr.name" select="upper-case(@name)"/>
        <xsl:param name="parent.name" required="yes"/><xsl:value-of select="$parent.name"/>_<xsl:value-of select="$attr.name"/></xsl:template>
</xsl:stylesheet>